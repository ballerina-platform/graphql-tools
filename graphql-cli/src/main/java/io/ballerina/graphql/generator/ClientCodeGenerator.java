package io.ballerina.graphql.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlClientProject;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.ClientTypesGenerationException;
import io.ballerina.graphql.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.exception.ServiceGenerationException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generator.ballerina.ClientGenerator;
import io.ballerina.graphql.generator.ballerina.ClientTypesGenerator;
import io.ballerina.graphql.generator.ballerina.ConfigTypesGenerator;
import io.ballerina.graphql.generator.ballerina.UtilsGenerator;
import io.ballerina.graphql.generator.model.AuthConfig;
import io.ballerina.graphql.generator.model.SrcFilePojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CONFIG_TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.UTILS_FILE_NAME;

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
            ClientTypesGenerationException,
            ConfigTypesGenerationException {
        String projectName = project.getName();
        Extension extensions = ((GraphqlClientProject) project).getExtensions();
        List<String> documents = ((GraphqlClientProject) project).getDocuments();
        GraphQLSchema schema = project.getGraphQLSchema();

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
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CLIENT_FILE_NAME, clientSrc));
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
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, TYPES_FILE_NAME, typesFileContent));
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
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.UTIL_SRC, projectName, UTILS_FILE_NAME, utilSrc));
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
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.CONFIG_SRC, projectName, CONFIG_TYPES_FILE_NAME,
                configTypesSrc));
    }
}
