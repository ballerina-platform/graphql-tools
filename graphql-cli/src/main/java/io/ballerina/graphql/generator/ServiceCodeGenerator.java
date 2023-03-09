package io.ballerina.graphql.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.ballerina.ServiceGenerator;
import io.ballerina.graphql.generator.ballerina.ServiceTypesGenerator;
import io.ballerina.graphql.generator.model.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.SERVICE_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;

public class ServiceCodeGenerator extends CodeGenerator {
    private ServiceGenerator serviceGenerator;
    private ServiceTypesGenerator serviceTypesGenerator;

    public ServiceCodeGenerator() {
        this.serviceGenerator = new ServiceGenerator();
        this.serviceTypesGenerator = new ServiceTypesGenerator();
    }

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
            throws ClientGenerationException, UtilsGenerationException, TypesGenerationException {
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
            throws ClientGenerationException {
        String serviceSrc = this.serviceGenerator.generateSrc(fileName, graphQLSchema, generatorContext);
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName, SERVICE_FILE_NAME, serviceSrc));
    }

    private void generateServiceTypes(String projectName, String fileName, GraphQLSchema graphQLSchema,
                                      List<SrcFilePojo> sourceFiles) throws TypesGenerationException {
        String typesFileContent = "";
        typesFileContent = this.serviceTypesGenerator.generateSrc(fileName, graphQLSchema);
        sourceFiles.add(
                new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, TYPES_FILE_NAME, typesFileContent));
    }
}
