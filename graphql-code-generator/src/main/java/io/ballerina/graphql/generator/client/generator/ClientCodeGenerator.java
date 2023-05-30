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

package io.ballerina.graphql.generator.client.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.exception.ClientGenerationException;
import io.ballerina.graphql.generator.client.exception.ClientTypesGenerationException;
import io.ballerina.graphql.generator.client.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.generator.client.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.client.generator.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generator.client.generator.ballerina.ClientGenerator;
import io.ballerina.graphql.generator.client.generator.ballerina.ClientTypesGenerator;
import io.ballerina.graphql.generator.client.generator.ballerina.ConfigTypesGenerator;
import io.ballerina.graphql.generator.client.generator.ballerina.UtilsGenerator;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import io.ballerina.graphql.generator.utils.model.AuthConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Ballerina client code.
 */
public class ClientCodeGenerator extends CodeGenerator {

//    public void generate(GraphqlProject project) throws GenerationException {
//        String outputPath = project.getOutputPath();
//        try {
//            List<SrcFilePojo> genSources = generateBalSources(project, GeneratorContext.CLI);
//            writeGeneratedSources(genSources, Path.of(outputPath));
//        } catch (ClientGenerationException | UtilsGenerationException | TypesGenerationException | IOException e) {
//            throw new GenerationException(e.getMessage(), project.getName());
//        }
//    }

    @Override
    public List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
            throws ServiceGenerationException, ClientGenerationException, UtilsGenerationException,
            ClientTypesGenerationException, ConfigTypesGenerationException {
        String projectName = project.getName();
        Extension extensions = ((GraphqlClientProject) project).getExtensions();
        List<String> documents = ((GraphqlClientProject) project).getDocuments();
        GraphQLSchema schema = project.getGraphqlSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateClients(projectName, documents, schema, authConfig, sourceFiles, generatorContext);
        generateUtils(projectName, authConfig, sourceFiles);
        generateClientTypes(projectName, documents, schema, sourceFiles);
        generateConfigTypes(projectName, authConfig, sourceFiles);

        return sourceFiles;
    }

    /**
     * Generates the Ballerina clients source codes for a given GraphQL project.
     *
     * @param projectName      the name of the GraphQL project
     * @param documents        the list of documents of a given GraphQL project
     * @param schema           the object instance of the GraphQL schema (SDL)
     * @param authConfig       the object instance representing authentication config information
     * @param sourceFiles      the list of generated Ballerina source file pojo
     * @param generatorContext the context which triggered the source generation
     * @throws ClientGenerationException when a client code generation error occurs
     * @throws IOException               If an I/O error occurs
     */
    private void generateClients(String projectName, List<String> documents, GraphQLSchema schema,
                                 AuthConfig authConfig, List<SrcFilePojo> sourceFiles,
                                 GeneratorContext generatorContext) throws ClientGenerationException {
        String clientSrc = ClientGenerator.getInstance().generateSrc(documents, schema, authConfig, generatorContext);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CodeGeneratorConstants.CLIENT_FILE_NAME,
                        clientSrc));
    }

    /**
     * Generates the Ballerina Client types source codes for a given GraphQL project.
     *
     * @param projectName the name of the GraphQL project
     * @param documents   the list of documents of a given GraphQL project
     * @param schema      the GraphQL schema (SDL) of a given GraphQL project
     * @param sourceFiles the list of generated Ballerina source file pojo
     * @throws ClientTypesGenerationException when a types code generation error occurs
     */
    private void generateClientTypes(String projectName, List<String> documents, GraphQLSchema schema,
                                     List<SrcFilePojo> sourceFiles) throws ClientTypesGenerationException {
        String typesFileContent = "";
        typesFileContent = ClientTypesGenerator.getInstance().generateSrc(schema, documents);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, CodeGeneratorConstants.TYPES_FILE_NAME,
                        typesFileContent));
    }

    /**
     * Generates the Ballerina utils source codes for a given GraphQL project.
     *
     * @param projectName the name of the GraphQL project
     * @param authConfig  the object instance representing authentication config information
     * @param sourceFiles the list of generated Ballerina source file pojo
     * @throws UtilsGenerationException when an utils code generation error occurs
     */
    public void generateUtils(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
            throws UtilsGenerationException {
        String utilSrc = UtilsGenerator.getInstance().generateSrc(authConfig);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.UTIL_SRC, projectName, CodeGeneratorConstants.UTILS_FILE_NAME,
                        utilSrc));
    }

    /**
     * Generates the Ballerina config types source codes for a given GraphQL project.
     *
     * @param projectName the name of the GraphQL project
     * @param authConfig  the object instance representing authentication config information
     * @param sourceFiles the list of generated Ballerina source file pojo
     * @throws ConfigTypesGenerationException when a config types code generation error occurs
     */
    private void generateConfigTypes(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
            throws ConfigTypesGenerationException {
        String configTypesSrc = ConfigTypesGenerator.getInstance().generateSrc(authConfig);
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.CONFIG_SRC, projectName,
                CodeGeneratorConstants.CONFIG_TYPES_FILE_NAME, configTypesSrc));
    }
}
