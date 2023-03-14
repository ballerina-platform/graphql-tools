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

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.ClientTypesGenerationException;
import io.ballerina.graphql.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ServiceGenerationException;
import io.ballerina.graphql.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.ballerina.ServiceGenerator;
import io.ballerina.graphql.generator.ballerina.ServiceTypesGenerator;
import io.ballerina.graphql.generator.model.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * This class implements the GraphQL client code generator tool.
 */
public abstract class CodeGenerator {
    private static CodeGenerator codeGenerator = null;
    private ServiceGenerator serviceGenerator;
    private ServiceTypesGenerator serviceTypesGenerator;

    /**
     * Generates the code for a given GraphQL project.
     *
     * @param project the instance of the GraphQL project
     * @throws GenerationException when a code generation error occurs
     */
    public void generate(GraphqlProject project) throws GenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project, GeneratorContext.CLI);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (ServiceGenerationException | ClientGenerationException | UtilsGenerationException |
                 ClientTypesGenerationException | IOException e) {
            throw new GenerationException(e.getMessage(), project.getName());
        }
    }

//    /**
//     * Generates the Ballerina source codes for a given GraphQL project.
//     *
//     * @param project the instance of the GraphQL project
//     * @return the list of generated Ballerina source file pojo
//     * @throws ClientGenerationException when a client code generation error occurs
//     * @throws UtilsGenerationException  when an utils code generation error occurs
//     * @throws TypesGenerationException  when a types code generation error occurs
//     * @throws IOException               If an I/O error occurs
//     */
//    public List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
//            throws ClientGenerationException, UtilsGenerationException, TypesGenerationException, IOException,
//            ConfigTypesGenerationException {
//        if (project.getGenerationType() == GenerationType.CLIENT) {
//            String projectName = project.getName();
//            Extension extensions = ((GraphqlClientProject) project).getExtensions();
//            List<String> documents = ((GraphqlClientProject) project).getDocuments();
//            GraphQLSchema schema = project.getGraphQLSchema();
//
//            AuthConfig authConfig = new AuthConfig();
//            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
//            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);
//
//            List<SrcFilePojo> sourceFiles = new ArrayList<>();
//            generateClients(projectName, documents, schema, authConfig, sourceFiles, generatorContext);
////            generateUtils(projectName, authConfig, sourceFiles);
////            generateClientTypes(projectName, documents, schema, sourceFiles);
////            generateConfigTypes(projectName, authConfig, sourceFiles);
//
//            return sourceFiles;
//        } else if (project.getGenerationType() == GenerationType.SERVICE) {
//            String projectName = project.getName();
//            String fileName = project.getFileName();
//            GraphQLSchema graphQLSchema = project.getGraphQLSchema();
//
//            List<SrcFilePojo> sourceFiles = new ArrayList<>();
////            generateServices(projectName, fileName, graphQLSchema, sourceFiles, generatorContext);
////            generateServiceTypes(projectName, fileName, graphQLSchema, sourceFiles);
//            return sourceFiles;
//        } else {
//            throw new IOException("Invalid project type");
//        }
//    }

    public abstract List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
            throws ServiceGenerationException, ClientGenerationException, UtilsGenerationException,
            ClientTypesGenerationException, IOException,
            ConfigTypesGenerationException, ClientTypesGenerationException, ServiceTypesGenerationException;


//    private void generateServices(String projectName, String fileName, GraphQLSchema graphQLSchema,
//                                 List<SrcFilePojo> sourceFiles, GeneratorContext generatorContext)
//            throws ClientGenerationException {
//        String serviceSrc = this.serviceGenerator.generateSrc(fileName, graphQLSchema, generatorContext);
//        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, SERVICE_FILE_NAME, serviceSrc));
//    }

//    /**
//     * Generates the Ballerina Client types source codes for a given GraphQL project.
//     *
//     * @param projectName the name of the GraphQL project
//     * @param documents   the list of documents of a given GraphQL project
//     * @param schema      the GraphQL schema (SDL) of a given GraphQL project
//     * @param sourceFiles the list of generated Ballerina source file pojo
//     * @throws TypesGenerationException when a types code generation error occurs
//     */
//    private void generateClientTypes(String projectName, List<String> documents, GraphQLSchema schema,
//                                     List<SrcFilePojo> sourceFiles) throws TypesGenerationException {
//        String typesFileContent = "";
//        typesFileContent = ClientTypesGenerator.getInstance().generateSrc(schema, documents);
//        sourceFiles.add(
//                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, TYPES_FILE_NAME, typesFileContent));
//    }

//    private void generateServiceTypes(String projectName, String fileName, GraphQLSchema graphQLSchema,
//                                      List<SrcFilePojo> sourceFiles) throws TypesGenerationException {
//        String typesFileContent = "";
//        typesFileContent = this.serviceTypesGenerator.generateSrc(fileName, graphQLSchema);
//        sourceFiles.add(
//                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, TYPES_FILE_NAME, typesFileContent));
//    }

//    /**
//     * Generates the Ballerina clients source codes for a given GraphQL project.
//     *
//     * @param projectName      the name of the GraphQL project
//     * @param documents        the list of documents of a given GraphQL project
//     * @param schema           the object instance of the GraphQL schema (SDL)
//     * @param authConfig       the object instance representing authentication config information
//     * @param sourceFiles      the list of generated Ballerina source file pojo
//     * @param generatorContext the context which triggered the source generation
//     * @throws ClientGenerationException when a client code generation error occurs
//     * @throws IOException               If an I/O error occurs
//     */
//    private void generateClients(String projectName, List<String> documents, GraphQLSchema schema,
//    AuthConfig authConfig,
//                                List<SrcFilePojo> sourceFiles, GeneratorContext generatorContext)
//            throws ClientGenerationException {
//        String clientSrc = ClientGenerator.getInstance().generateSrc(documents, schema, authConfig, generatorContext);
//        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CLIENT_FILE_NAME, clientSrc));
//    }

//    /**
//     * Generates the Ballerina utils source codes for a given GraphQL project.
//     *
//     * @param projectName the name of the GraphQL project
//     * @param authConfig  the object instance representing authentication config information
//     * @param sourceFiles the list of generated Ballerina source file pojo
//     * @throws UtilsGenerationException when an utils code generation error occurs
//     */
//    public void generateUtils(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
//            throws UtilsGenerationException {
//        String utilSrc = UtilsGenerator.getInstance().generateSrc(authConfig);
//        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.UTIL_SRC, projectName, UTILS_FILE_NAME, utilSrc));
//    }

//    /**
//     * Generates the Ballerina config types source codes for a given GraphQL project.
//     *
//     * @param projectName the name of the GraphQL project
//     * @param authConfig  the object instance representing authentication config information
//     * @param sourceFiles the list of generated Ballerina source file pojo
//     * @throws ConfigTypesGenerationException when a config types code generation error occurs
//     */
//    private void generateConfigTypes(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
//            throws ConfigTypesGenerationException {
//        String configTypesSrc = ConfigTypesGenerator.getInstance().generateSrc(authConfig);
//        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.CONFIG_SRC, projectName, CONFIG_TYPES_FILE_NAME,
//                configTypesSrc));
//    }

    /**
     * Writes the generated Ballerina source codes to the files in the specified {@code outputPath}.
     *
     * @param sources    the list of generated Ballerina source file pojo
     * @param outputPath the target output path for the code generation
     * @throws IOException If an I/O error occurs
     */
    protected void writeGeneratedSources(List<SrcFilePojo> sources, Path outputPath) throws IOException {
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
