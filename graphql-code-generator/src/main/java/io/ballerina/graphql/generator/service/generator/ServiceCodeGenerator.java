package io.ballerina.graphql.generator.service.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.client.exception.ClientGenerationException;
import io.ballerina.graphql.generator.client.exception.ClientTypesGenerationException;
import io.ballerina.graphql.generator.client.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.generator.client.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Ballerina service code.
 */
public class ServiceCodeGenerator extends CodeGenerator {
    private ServiceGenerator serviceGenerator;
    private ServiceTypesGenerator serviceTypesGenerator;

    public ServiceCodeGenerator() {
        this.serviceGenerator = new ServiceGenerator();
        this.serviceTypesGenerator = new ServiceTypesGenerator();
    }

    @Override
    public void generate(GraphqlProject project) throws GenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (ServiceGenerationException | IOException e) {
            throw new GenerationException(e.getMessage(), project.getName());
        }
    }

    @Override
    public List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
            throws ServiceGenerationException, ClientGenerationException, UtilsGenerationException, IOException,
            ConfigTypesGenerationException, ClientTypesGenerationException, ServiceTypesGenerationException {
        return null;
    }

    public List<SrcFilePojo> generateBalSources(GraphqlProject project)
            throws ServiceGenerationException, ServiceTypesGenerationException {
        String projectName = project.getName();
        String fileName = project.getFileName();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateServices(projectName, fileName, graphQLSchema, sourceFiles);
        generateServiceTypes(projectName, fileName, graphQLSchema, sourceFiles);
        return sourceFiles;
    }

    private void generateServices(String projectName, String fileName, GraphQLSchema graphQLSchema,
                                  List<SrcFilePojo> sourceFiles)
            throws ServiceGenerationException {
        String serviceSrc = this.serviceGenerator.generateSrc(fileName, graphQLSchema);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, CodeGeneratorConstants.SERVICE_FILE_NAME,
                        serviceSrc));
    }

    private void generateServiceTypes(String projectName, String fileName, GraphQLSchema graphQLSchema,
                                      List<SrcFilePojo> sourceFiles) throws ServiceTypesGenerationException {
        this.serviceTypesGenerator.setFileName(fileName);
        String typesFileContent = this.serviceTypesGenerator.generateSrc(graphQLSchema);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, CodeGeneratorConstants.TYPES_FILE_NAME,
                        typesFileContent));
    }

    public ServiceTypesGenerator getServiceTypesGenerator() {
        return serviceTypesGenerator;
    }

    public void enableRecordForced() {
        this.serviceTypesGenerator.setRecordForced(true);
    }
}
