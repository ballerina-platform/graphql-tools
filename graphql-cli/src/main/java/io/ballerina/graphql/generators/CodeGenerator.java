/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.graphql.generators;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.mappers.Extension;
import io.ballerina.graphql.exceptions.BallerinaGraphqlDocumentPathValidationException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlIntospectionException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlSchemaPathValidationException;
import io.ballerina.graphql.generators.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generators.ballerina.ClientGenerator;
import io.ballerina.graphql.generators.ballerina.UtilsGenerator;
import io.ballerina.graphql.generators.model.GenSrcFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generators.CodeGeneratorConstants.UTILS_FILE_NAME;

/**
 * This class implements the GraphQL client code generator tool.
 */
public class CodeGenerator implements ICodeGenerator {
    private static final Log log = LogFactory.getLog(CodeGenerator.class);

    @Override
    public void generateProjectCode(String schema, List<String> documents, Extension extensions,
                                    String outputPath, String projectName)
            throws IOException, FormatterException, BallerinaGraphqlIntospectionException,
            BallerinaGraphqlSchemaPathValidationException, BallerinaGraphqlDocumentPathValidationException {
        if (schema != null && documents != null) {
            List<GenSrcFile> genFiles = generateBalSource(schema, documents, extensions, projectName);
            writeGeneratedSources(genFiles, Path.of(outputPath));
        }
    }

    /**
     * Generates the Ballerina source code for a given GraphQL schema and project documents.
     *
     * @param schema            the schema value of the Graphql config file
     * @param documents         the documents' values of the Graphql config file
     * @param extensions        the extensions value of the Graphql config file
     * @param projectName       the project name
     * @return                  the list of generated Ballerina source files
     */
    private List<GenSrcFile> generateBalSource(String schema, List<String> documents, Extension extensions,
                                              String projectName)
            throws IOException, FormatterException, BallerinaGraphqlIntospectionException,
            BallerinaGraphqlSchemaPathValidationException, BallerinaGraphqlDocumentPathValidationException {
        GraphQLSchema schemaDocument = Utils.getGraphQLSchemaDocument(schema, extensions);

        AuthConfigGenerator ballerinaAuthConfigGenerator = new AuthConfigGenerator();
        ballerinaAuthConfigGenerator.setAuthConfigTypes(extensions);
        ballerinaAuthConfigGenerator.setApiHeaders(extensions);
        ballerinaAuthConfigGenerator.setApiKeysConfigRecordFields(extensions);

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        for (String document : documents) {
            File documentFile = new File(document);
            ClientGenerator ballerinaClientGenerator = new ClientGenerator(ballerinaAuthConfigGenerator);

            Document queriesDocument = Utils.getGraphQLQueriesDocument(document);
            String queriesDocumentName = CodeGeneratorUtils.getDocumentName(documentFile);
            String clientFileContent = Formatter.format(ballerinaClientGenerator.
                    generateSyntaxTree(queriesDocument, queriesDocumentName)).toString();

            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, projectName,
                    CodeGeneratorUtils.getClientFileName(documentFile), clientFileContent));
        }
        if (ballerinaAuthConfigGenerator.isApiKeysConfig()) {
            UtilsGenerator ballerinaUtilsGenerator = new UtilsGenerator();
            String utilsFileContent = Formatter.format(
                    ballerinaUtilsGenerator.generateSyntaxTree()).toString();

            sourceFiles.add(new GenSrcFile(
                    GenSrcFile.GenFileType.GEN_SRC, projectName, UTILS_FILE_NAME, utilsFileContent));
        }
        return sourceFiles;
    }

    /**
     * Writes the generated Ballerina source code to the given output path.
     *
     * @param outputPath        the target output path for the code generation
     */
    private void writeGeneratedSources(List<GenSrcFile> sources, Path outputPath) throws IOException {
        if (!sources.isEmpty()) {
            for (GenSrcFile file : sources) {
                Path filePath;
                if (file.getType().isOverwritable()) {
                    filePath = CodeGeneratorUtils.getAbsoluteFilePath(file, outputPath);
                    String fileContent = file.getContent();
                    CodeGeneratorUtils.writeFile(filePath, fileContent);
                }
            }
        }
    }
}
