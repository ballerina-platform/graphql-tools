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
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.service.Constants;
import io.ballerina.graphql.generator.service.ServiceCombiner;
import io.ballerina.graphql.generator.service.ServiceFileCombiner;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    public void generate(GraphqlProject project) throws GenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (ServiceGenerationException | IOException | FormatterException e) {
            throw new GenerationException(e.getMessage(), project.getName());
        }
    }

    public List<SrcFilePojo> generateBalSources(GraphqlProject project)
            throws ServiceGenerationException, ServiceTypesGenerationException, IOException, FormatterException {
        String projectName = project.getName();
        String fileName = project.getFileName();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateServiceTypes(projectName, fileName, project.getOutputPath(), graphQLSchema, sourceFiles);
        generateServices(projectName, fileName, project.getOutputPath(), sourceFiles);
        return sourceFiles;
    }

    private void generateServices(String projectName, String fileName, String outputPath, List<SrcFilePojo> sourceFiles)
            throws IOException, FormatterException {
        this.serviceGenerator.setFileName(fileName);
        Path outputFilePath = Paths.get(outputPath, CodeGeneratorConstants.SERVICE_FILE_NAME);
        String availableOutputFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(outputFilePath));
        ModulePartNode availableOutputFileNode = NodeParser.parseModulePart(availableOutputFileContent);
        this.serviceGenerator.setMethodDeclarations(this.serviceMethodDeclarations);
        ModulePartNode newServiceFileContentNode = this.serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner =
                new ServiceFileCombiner(availableOutputFileNode, newServiceFileContentNode);
        String mergedServiceFileContent = serviceFileCombiner.generateMergedSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CodeGeneratorConstants.SERVICE_FILE_NAME,
                        mergedServiceFileContent));
    }

    private void generateServiceTypes(String projectName, String fileName, String outputPath,
                                      GraphQLSchema graphQLSchema,
                                      List<SrcFilePojo> sourceFiles)
            throws ServiceTypesGenerationException, IOException, FormatterException {
        this.serviceTypesGenerator.setFileName(fileName);
        // get already available content in output file
        Path outputFilePath = Paths.get(outputPath, CodeGeneratorConstants.TYPES_FILE_NAME);
        String availableOutputFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(outputFilePath));
        ModulePartNode availableOutputFileNode = NodeParser.parseModulePart(availableOutputFileContent);
        ModulePartNode newTypesFileContentNode = serviceTypesGenerator.generateContentNode(graphQLSchema);
        ServiceCombiner serviceCombiner =
                new ServiceCombiner(availableOutputFileNode, newTypesFileContentNode, graphQLSchema);
        String mergedTypesFileContent = serviceCombiner.generateMergedSrc();
        warnings.addAll(serviceCombiner.getBreakingChangeWarnings());
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
