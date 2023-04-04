package io.ballerina.graphql.generator.service.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

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
    public List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
            throws ServiceGenerationException, ServiceTypesGenerationException {
        String projectName = project.getName();
        String fileName = project.getFileName();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateServices(projectName, fileName, graphQLSchema, sourceFiles, generatorContext);
        generateServiceTypes(projectName, fileName, graphQLSchema, sourceFiles);
        return sourceFiles;
    }

    private void generateServices(String projectName, String fileName, GraphQLSchema graphQLSchema,
                                  List<SrcFilePojo> sourceFiles, GeneratorContext generatorContext)
            throws ServiceGenerationException {
        String serviceSrc = this.serviceGenerator.generateSrc(fileName, graphQLSchema, generatorContext);
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
