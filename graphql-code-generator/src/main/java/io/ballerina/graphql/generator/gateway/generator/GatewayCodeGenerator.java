package io.ballerina.graphql.generator.gateway.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.client.exception.ClientGenerationException;
import io.ballerina.graphql.generator.client.exception.ClientTypesGenerationException;
import io.ballerina.graphql.generator.client.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.generator.client.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayQueryPlanGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayTypeGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import io.ballerina.graphql.generator.gateway.generator.common.Constants;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to generated source code for the gateway.
 */
public class GatewayCodeGenerator extends CodeGenerator {
    @Override
    public List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
            throws GatewayGenerationException, ServiceGenerationException, ClientGenerationException,
            UtilsGenerationException, IOException,
            ConfigTypesGenerationException, ClientTypesGenerationException, ServiceTypesGenerationException {
        return null;
    }

    @Override
    public void generate(GraphqlProject project) throws GenerationException {
        String outputPath = project.getOutputPath();
        try {
            copyTemplateFiles((GraphqlGatewayProject) project);
            Path outputDirectoryPath = Path.of(outputPath);
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, ((GraphqlGatewayProject) project).getTempDir());

            // Delete partial files
            try {
                deletePartialFiles(((GraphqlGatewayProject) project).getTempDir());
            } catch (IOException ignored) {

            }
            //Generating the executable
            CommonUtils.getCompiledBallerinaProject(((GraphqlGatewayProject) project).getTempDir(),
                    outputDirectoryPath, project.getFileName() + "-gateway");
        } catch (GatewayGenerationException | IOException | GatewayTypeGenerationException |
                 GatewayQueryPlanGenerationException | URISyntaxException e) {
            throw new GenerationException(e.getMessage(), e.getMessage());
        }
    }

    private List<SrcFilePojo> generateBalSources(GraphqlProject project)
            throws GatewayGenerationException, GatewayTypeGenerationException, GatewayQueryPlanGenerationException,
            IOException, URISyntaxException {
        String projectName = project.getName();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateTypes(projectName, graphQLSchema, sourceFiles);
        generateQueryPlan(projectName, graphQLSchema, sourceFiles);
        generateServiceFile(projectName, (GraphqlGatewayProject) project, sourceFiles);
        return sourceFiles;
    }

    private void generateTypes(String projectName, GraphQLSchema graphQLSchema,
                               List<SrcFilePojo> sourceFiles)
            throws GatewayTypeGenerationException {
        GatewayTypeGenerator gatewayTypeGenerator = new GatewayTypeGenerator(graphQLSchema);
        String typesFileContent = gatewayTypeGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, CodeGeneratorConstants.TYPES_FILE_NAME,
                        typesFileContent));
    }

    private void generateQueryPlan(String projectName, GraphQLSchema graphQLSchema,
                                   List<SrcFilePojo> sourceFiles)
            throws GatewayQueryPlanGenerationException, GatewayGenerationException {
        GatewayQueryPlanGenerator gatewayQueryPlanGenerator = new GatewayQueryPlanGenerator(graphQLSchema);
        String queryPlanFileContent = gatewayQueryPlanGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.UTIL_SRC, projectName,
                        CodeGeneratorConstants.QUERY_PLAN_FILE_NAME, queryPlanFileContent));
    }

    private void generateServiceFile(String projectName, GraphqlGatewayProject project,
                                     List<SrcFilePojo> sourceFiles)
            throws GatewayServiceGenerationException, IOException {
        GatewayServiceGenerator gatewayServiceGenerator = new GatewayServiceGenerator(project);
        String serviceFileContent = gatewayServiceGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                        CodeGeneratorConstants.SERVICE_FILE_NAME, serviceFileContent));
    }

    private static void copyTemplateFiles(GraphqlGatewayProject project) throws IOException, URISyntaxException,
            GatewayGenerationException {
        copyTemplateFiles(project.getTempDir());
    }

    public static void copyTemplateFiles(Path targetPath) throws GatewayGenerationException, IOException {
        ClassLoader classLoader = GatewayCodeGenerator.class.getClassLoader();
        for (String fileName : Constants.GATEWAY_PROJECT_TEMPLATE_FILES) {
            InputStream inputStream = classLoader.getResourceAsStream(
                    Constants.GATEWAY_PROJECT_TEMPLATE_DIRECTORY + "/" + fileName);

            checkInputStream(inputStream);
            String resource = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Path path = targetPath.resolve(fileName);
            try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
                writer.print(resource);
            } catch (IOException e) {
                throw new GatewayGenerationException("Error while copying the template files.");
            }
        }
    }

    private static void checkInputStream(InputStream inputStream) throws GatewayGenerationException {
        if (inputStream == null) {
            throw new GatewayGenerationException("Error while copying the template files.");
        }
    }

    private void deletePartialFiles(Path directoryPath) throws IOException {
        for (Path path : Files.walk(directoryPath)
                .filter(path -> path.toString().endsWith(".partial")).toArray(Path[]::new)) {
            Files.delete(path);
        }
    }

}
