package io.ballerina.graphql.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.exception.ServiceGenerationException;
import io.ballerina.graphql.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.ballerina.ServiceGenerator;
import io.ballerina.graphql.generator.ballerina.ServiceTypesGenerator;
import io.ballerina.graphql.generator.model.SrcFilePojo;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.SERVICE_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;

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
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, SERVICE_FILE_NAME, serviceSrc));
    }

    private void generateServiceTypes(String projectName, String fileName, GraphQLSchema graphQLSchema,
                                      List<SrcFilePojo> sourceFiles) throws ServiceTypesGenerationException {
        this.serviceTypesGenerator.setFileName(fileName);
        String typesFileContent = this.serviceTypesGenerator.generateSrc(graphQLSchema);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, TYPES_FILE_NAME, typesFileContent));
    }

    public ServiceTypesGenerator getServiceTypesGenerator() {
        return serviceTypesGenerator;
    }

    public void enableRecordForced() {
        this.serviceTypesGenerator.setRecordForced(true);
    }
}
