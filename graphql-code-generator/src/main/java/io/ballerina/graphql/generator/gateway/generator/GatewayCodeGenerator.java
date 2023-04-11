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
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayQueryPlanGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayTypeGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayCodeGenerator.class);

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
            Path templateProjectPath = getProjectTemplatePath();
            Path outputDirectoryPath = Path.of(outputPath);
            copyTemplateProjectFiles(templateProjectPath, outputDirectoryPath);
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, outputDirectoryPath);

            //Generating the executable
            // TODO: Generating executable is not working due to the issue with graphql module.
//            BuildOptions buildOptions = BuildOptions.builder().build();
//            BuildProject buildProject = BuildProject.load(outputDirectoryPath, buildOptions);
//            PackageCompilation packageCompilation = buildProject.currentPackage().getCompilation();
//            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
//            if (jBallerinaBackend.diagnosticResult().hasErrors()) {
//                throw new GatewayGenerationException("Error while generating the executable.");
//            }
//            jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, outputDirectoryPath.resolve("gateway.jar"));

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
        generateServiceFile(projectName, graphQLSchema, sourceFiles);
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
            throws GatewayQueryPlanGenerationException {
        GatewayQueryPlanGenerator gatewayQueryPlanGenerator = new GatewayQueryPlanGenerator(graphQLSchema);
        String queryPlanFileContent = gatewayQueryPlanGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.UTIL_SRC, projectName,
                        CodeGeneratorConstants.QUERY_PLAN_FILE_NAME, queryPlanFileContent));
    }

    private void generateServiceFile(String projectName, GraphQLSchema graphQLSchema,
                                     List<SrcFilePojo> sourceFiles)
            throws GatewayServiceGenerationException, IOException, URISyntaxException {
        GatewayServiceGenerator gatewayServiceGenerator = new GatewayServiceGenerator(graphQLSchema);
        String serviceFileContent = gatewayServiceGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                        CodeGeneratorConstants.SERVICE_FILE_NAME, serviceFileContent));
    }

    private Path getProjectTemplatePath() throws IOException, URISyntaxException, GatewayGenerationException {
        Path tmpDir = Files.createTempDirectory(".gateway-project-tmp" + System.nanoTime());
        ClassLoader classLoader = getClass().getClassLoader();

        String[] fileNames = {
                "Ballerina.toml",
                "Dependencies.toml",
                "devcontainer.json",
                "records.bal",
                "resolver.bal",
                "utils.bal",
                "queryFieldClassifier.bal"
        };

        for (String fileName : fileNames) {
            InputStream inputStream =
                    classLoader.getResourceAsStream("gateway/" + fileName);
            if (inputStream != null) {
                String resource = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                Path path = tmpDir.resolve(fileName);
                try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
                    writer.print(resource);
                }
            }

        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tmpDir.toFile());
            } catch (IOException ex) {
                LOGGER.error("Unable to delete the temporary directory : " + tmpDir, ex);
            }
        }));

        return tmpDir;
    }

}
