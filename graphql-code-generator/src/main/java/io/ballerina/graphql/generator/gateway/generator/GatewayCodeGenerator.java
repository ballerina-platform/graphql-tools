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
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.IOException;
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
            Path templateProjectPath = GraphqlGatewayProject.GATEWAY_TEMPLATE_PATH;
            Path outputDirectoryPath = Path.of(outputPath);
            copyTemplateProjectFiles(templateProjectPath, outputDirectoryPath);
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, outputDirectoryPath);

        } catch (GatewayGenerationException | IOException | GatewayTypeGenerationException |
                 GatewayQueryPlanGenerationException e) {
            throw new GenerationException(e.getMessage(), project.getName());
        }
    }

    private List<SrcFilePojo> generateBalSources(GraphqlProject project)
            throws GatewayGenerationException, GatewayTypeGenerationException, GatewayQueryPlanGenerationException {
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
            throws GatewayServiceGenerationException {
        GatewayServiceGenerator gatewayServiceGenerator = new GatewayServiceGenerator(graphQLSchema);
        String serviceFileContent = gatewayServiceGenerator.generateSrc();
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                        CodeGeneratorConstants.SERVICE_FILE_NAME, serviceFileContent));
    }

}
