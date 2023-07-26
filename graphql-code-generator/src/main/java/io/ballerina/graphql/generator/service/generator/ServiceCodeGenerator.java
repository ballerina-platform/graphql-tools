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
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Ballerina code for service files.
 */
public class ServiceCodeGenerator extends CodeGenerator {
    private ServiceGenerator serviceGenerator;
    private ServiceTypesGenerator serviceTypesGenerator;
    private List<MethodDeclarationNode> serviceMethodDeclarations;

    public ServiceCodeGenerator() {
        this.serviceGenerator = new ServiceGenerator();
        this.serviceTypesGenerator = new ServiceTypesGenerator();
    }

    @Override
    public void generate(GraphqlProject project) throws ServiceGenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (IOException e) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    e.getMessage());
        }
    }

    public List<SrcFilePojo> generateBalSources(GraphqlProject project) throws ServiceGenerationException {
        String projectName = project.getName();
        String fileName = project.getFileName();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateServiceTypes(projectName, fileName, graphQLSchema, sourceFiles);
        generateServices(projectName, fileName, sourceFiles);
        return sourceFiles;
    }

    private void generateServices(String projectName, String fileName, List<SrcFilePojo> sourceFiles)
            throws ServiceGenerationException {
        this.serviceGenerator.setFileName(fileName);
        this.serviceGenerator.setMethodDeclarations(this.serviceMethodDeclarations);
        String serviceSrc = this.serviceGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CodeGeneratorConstants.SERVICE_FILE_NAME,
                        serviceSrc));
    }

    private void generateServiceTypes(String projectName, String fileName, GraphQLSchema graphQLSchema,
                                      List<SrcFilePojo> sourceFiles) throws ServiceGenerationException {
        this.serviceTypesGenerator.setFileName(fileName);
        String typesFileContent = this.serviceTypesGenerator.generateSrc(graphQLSchema);
        setServiceMethodDeclarations(this.serviceTypesGenerator.getServiceMethodDeclarations());
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, CodeGeneratorConstants.TYPES_FILE_NAME,
                        typesFileContent));
    }

    public void enableToUseRecords() {
        this.serviceTypesGenerator.setUseRecordsForObjects(true);
    }

    public void setServiceMethodDeclarations(List<MethodDeclarationNode> serviceMethodDeclarations) {
        this.serviceMethodDeclarations = serviceMethodDeclarations;
    }
}
