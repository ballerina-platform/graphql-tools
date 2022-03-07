/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generator.ballerina.ClientGenerator;
import io.ballerina.graphql.generator.ballerina.TypesGenerator;
import io.ballerina.graphql.generator.ballerina.UtilsGenerator;
import io.ballerina.graphql.generator.model.AuthConfig;
import io.ballerina.graphql.generator.model.SrcFilePojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.UTILS_FILE_NAME;

/**
 * This class implements the GraphQL client code generator tool.
 */
public class CodeGenerator {
    private static CodeGenerator codeGenerator = null;

    public static CodeGenerator getInstance() {
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator();
        }
        return codeGenerator;
    }

    /**
     * Generates the code for a given GraphQL project.
     *
     * @param project                               the instance of the GraphQL project
     * @throws GenerationException                  when a code generation error occurs
     */
    public void generate(GraphqlProject project) throws GenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (ClientGenerationException | UtilsGenerationException | TypesGenerationException | IOException e) {
            throw new GenerationException(e.getMessage(), project.getName());
        }
    }

    /**
     * Generates the Ballerina source codes for a given GraphQL project.
     *
     * @param project                               the instance of the GraphQL project
     * @return                                      the list of generated Ballerina source file pojo
     * @throws ClientGenerationException            when a client code generation error occurs
     * @throws UtilsGenerationException             when an utils code generation error occurs
     * @throws TypesGenerationException             when a types code generation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    private List<SrcFilePojo> generateBalSources(GraphqlProject project)
            throws ClientGenerationException, UtilsGenerationException, TypesGenerationException, IOException {
        String projectName = project.getName();
        Extension extensions = project.getExtensions();
        List<String> documents = project.getDocuments();
        GraphQLSchema schema = project.getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateClients(projectName, documents, schema, authConfig, sourceFiles);
        generateUtils(projectName, authConfig, sourceFiles);
        generateTypes(projectName, documents, schema, sourceFiles);

        return sourceFiles;
    }

    /**
     * Generates the Ballerina clients source codes for a given GraphQL project.
     *
     * @param projectName                           the name of the GraphQL project
     * @param documents                             the list of documents of a given GraphQL project
     * @param schema                                the object instance of the GraphQL schema (SDL)
     * @param authConfig                            the object instance representing authentication config information
     * @param sourceFiles                           the list of generated Ballerina source file pojo
     * @throws ClientGenerationException            when a client code generation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    private void generateClients(String projectName, List<String> documents, GraphQLSchema schema,
                                 AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
            throws ClientGenerationException, IOException {
        for (String document : documents) {
            File documentFile = new File(document);
            Document queryDocument = Utils.getGraphQLQueryDocument(document);
            String queryDocumentName = CodeGeneratorUtils.getDocumentName(documentFile);

            String clientSrc = ClientGenerator.getInstance().
                    generateSrc(queryDocument, queryDocumentName, schema, authConfig);
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                    CodeGeneratorUtils.getClientFileName(documentFile), clientSrc));
        }
    }

    /**
     * Generates the Ballerina types source codes for a given GraphQL project.
     *
     * @param projectName                           the name of the GraphQL project
     * @param documents                             the list of documents of a given GraphQL project
     * @param schema                                the GraphQL schema (SDL) of a given GraphQL project
     * @param sourceFiles                           the list of generated Ballerina source file pojo
     * @throws TypesGenerationException             when a types code generation error occurs
     */
    private void generateTypes(String projectName, List<String> documents, GraphQLSchema schema,
                               List<SrcFilePojo> sourceFiles) throws TypesGenerationException {
        String typesFileContent = TypesGenerator.getInstance().generateSrc(schema, documents);
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                TYPES_FILE_NAME, typesFileContent));
    }

    /**
     * Generates the Ballerina utils source codes for a given GraphQL project.
     *
     * @param projectName                           the name of the GraphQL project
     * @param authConfig                            the object instance representing authentication config information
     * @param sourceFiles                           the list of generated Ballerina source file pojo
     * @throws UtilsGenerationException             when an utils code generation error occurs
     */
    private void generateUtils(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
            throws UtilsGenerationException {
        // Generate utils.bal if the auth configuration is API key config
        if (authConfig.isApiKeysConfig()) {
            String utilSrc = UtilsGenerator.getInstance().generateSrc();
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                    UTILS_FILE_NAME, utilSrc));
        }
    }

    /**
     * Writes the generated Ballerina source codes to the files in the specified {@code outputPath}.
     *
     * @param sources                               the list of generated Ballerina source file pojo
     * @param outputPath                            the target output path for the code generation
     * @throws IOException                          If an I/O error occurs
     */
    private void writeGeneratedSources(List<SrcFilePojo> sources, Path outputPath) throws IOException {
        if (!sources.isEmpty()) {
            for (SrcFilePojo file : sources) {
                if (file.getType().isOverwritable()) {
                    Path filePath = CodeGeneratorUtils.getAbsoluteFilePath(file, outputPath);
                    String fileContent = file.getContent();
                    CodeGeneratorUtils.writeFile(filePath, fileContent);
                }
            }
        }
    }
}
