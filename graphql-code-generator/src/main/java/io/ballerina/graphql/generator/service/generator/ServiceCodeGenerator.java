/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.generator.service.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.service.Constants;
import io.ballerina.graphql.generator.service.combiner.ServiceFileCombiner;
import io.ballerina.graphql.generator.service.combiner.ServiceTypesFileCombiner;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceFileCombinerException;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesFileCombinerException;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.service.Constants.MESSAGE_FOR_COMBINE_INTO_EMPTY_SERVICE_FILE;
import static io.ballerina.graphql.generator.service.Constants.MESSAGE_FOR_COMBINE_INTO_EMPTY_SERVICE_TYPES_FILE;

/**
 * Generates Ballerina code for service files.
 */
public class ServiceCodeGenerator extends CodeGenerator {
    private ServiceGenerator serviceGenerator;
    private ServiceTypesGenerator serviceTypesGenerator;
    private List<MethodDeclarationNode> serviceMethodDeclarations;
    private List<String> warnings;

    public ServiceCodeGenerator() {
        this.serviceGenerator = new ServiceGenerator();
        this.serviceTypesGenerator = new ServiceTypesGenerator();
        warnings = new ArrayList<>();
    }

    @Override
    public void generate(GraphqlProject project) throws ServiceGenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (IOException | FormatterException | ServiceTypesFileCombinerException |
                ServiceFileCombinerException e) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    e.getMessage());
        }
    }

    public List<SrcFilePojo> generateBalSources(GraphqlProject project) throws ServiceGenerationException, IOException,
            FormatterException, ServiceTypesFileCombinerException, ServiceFileCombinerException {
        String projectName = project.getName();
        String fileName = project.getFileName();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateServiceTypes(projectName, fileName, project.getOutputPath(), graphQLSchema, sourceFiles);
        generateServices(projectName, fileName, project.getOutputPath(), sourceFiles);
        return sourceFiles;
    }

    private void generateServices(String projectName, String fileName, String outputPath, List<SrcFilePojo> sourceFiles)
            throws IOException, FormatterException, ServiceGenerationException, ServiceFileCombinerException {
        this.serviceGenerator.setFileName(fileName);
        this.serviceGenerator.setMethodDeclarations(this.serviceMethodDeclarations);
        ModulePartNode newServiceFileContentNode = this.serviceGenerator.generateContentNode();
        String mergedServiceFileContent = "";
        Path outputFilePath = Paths.get(outputPath, CodeGeneratorConstants.SERVICE_FILE_NAME);
        File outputFile = new File(outputFilePath.toString());
        if (outputFile.exists()) {
            if (outputFile.length() == 0) {
                throw new ServiceFileCombinerException(String.format(MESSAGE_FOR_COMBINE_INTO_EMPTY_SERVICE_FILE,
                        CodeGeneratorConstants.SERVICE_FILE_NAME, outputPath));
            } else {
                String availableOutputFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(outputFilePath));
                ModulePartNode availableOutputFileNode = NodeParser.parseModulePart(availableOutputFileContent);
                ServiceFileCombiner serviceFileCombiner =
                        new ServiceFileCombiner(availableOutputFileNode, newServiceFileContentNode);
                mergedServiceFileContent = serviceFileCombiner.generateMergedSrc();
            }
        } else {
            SyntaxTree newServiceFileSyntaxTree = serviceGenerator.generateSyntaxTree(newServiceFileContentNode);
            mergedServiceFileContent = serviceGenerator.generateSrc(newServiceFileSyntaxTree);
        }
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CodeGeneratorConstants.SERVICE_FILE_NAME,
                        mergedServiceFileContent));
    }

    private void generateServiceTypes(String projectName, String fileName, String outputPath,
                                      GraphQLSchema graphQLSchema, List<SrcFilePojo> sourceFiles)
            throws ServiceGenerationException, IOException, FormatterException, ServiceTypesFileCombinerException {
        this.serviceTypesGenerator.setFileName(fileName);
        ModulePartNode newTypesFileContentNode = serviceTypesGenerator.generateContentNode(graphQLSchema);
        String mergedTypesFileContent = "";
        Path outputFilePath = Paths.get(outputPath, CodeGeneratorConstants.TYPES_FILE_NAME);
        File outputFile = new File(outputFilePath.toString());
        if (outputFile.exists()) {
            if (outputFile.length() == 0) {
                throw new ServiceTypesFileCombinerException(String.format(
                        MESSAGE_FOR_COMBINE_INTO_EMPTY_SERVICE_TYPES_FILE,
                        CodeGeneratorConstants.TYPES_FILE_NAME, outputPath));
            } else {
                String availableOutputFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(outputFilePath));
                ModulePartNode availableOutputFileNode = NodeParser.parseModulePart(availableOutputFileContent);
                ServiceTypesFileCombiner serviceTypesFileCombiner =
                        new ServiceTypesFileCombiner(availableOutputFileNode, newTypesFileContentNode, graphQLSchema);
                mergedTypesFileContent = serviceTypesFileCombiner.generateMergedSrc();
                warnings.addAll(serviceTypesFileCombiner.getBreakingChangeWarnings());
            }
        } else {
            SyntaxTree mergedTypesSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newTypesFileContentNode);
            mergedTypesFileContent = serviceTypesGenerator.generateSrc(mergedTypesSyntaxTree);
        }

        setServiceMethodDeclarations(this.serviceTypesGenerator.getServiceMethodDeclarations());
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, CodeGeneratorConstants.TYPES_FILE_NAME,
                        mergedTypesFileContent));
    }

    public void enableToUseRecords() {
        this.serviceTypesGenerator.setUseRecordsForObjects(true);
    }

    public void setServiceMethodDeclarations(List<MethodDeclarationNode> serviceMethodDeclarations) {
        this.serviceMethodDeclarations = serviceMethodDeclarations;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
