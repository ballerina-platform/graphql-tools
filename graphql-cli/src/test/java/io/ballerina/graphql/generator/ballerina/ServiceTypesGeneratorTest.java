package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import io.ballerina.projects.DiagnosticResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.common.TestUtils.getDiagnosticResult;
import static io.ballerina.graphql.common.TestUtils.hasOnlyResourceFuncMustReturnResultErrors;
import static io.ballerina.graphql.common.TestUtils.writeSources;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;

/**
 * Test class for ServiceTypesGenerator.
 * Test the successful generation of service types
 */
public class ServiceTypesGeneratorTest extends GraphqlTest {
    private Path typesCheckProjectDir =
            this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "typesCheckProject"));

    @Test(description = "Test for schema with basic, method - default")
    public void testGenerateSrc()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithBasic01Api";
        String expectedFile = "typesWithBasic01Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    private void writeContentTo(String content, Path projectDir) throws IOException {
        SrcFilePojo srcFile = new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, "root", TYPES_FILE_NAME, content);
        List<SrcFilePojo> srcFiles = new ArrayList<>();
        srcFiles.add(srcFile);
        writeSources(srcFiles, projectDir);
    }

    @Test(description = "Test for schema with more types, method - default")
    public void testGenerateSrcForMoreTypes()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithBasic02Api";
        String expectedFile = "typesWithBasic02Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with mutation types, method - default")
    public void testGenerateSrcForSchemaWithMutationTypes()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithMutationApi";
        String expectedFile = "typesWithMutationDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with subscription types, method - default")
    public void testGenerateSrcForSchemaWithSubscriptionTypes()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithSubscriptionApi";
        String expectedFile = "typesWithSubscriptionDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with lists, method - default")
    public void testGenerateSrcForSchemaWithLists()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithListsApi";
        String expectedFile = "typesWithListsDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with input types, method - default")
    public void testGenerateSrcForSchemaWithInputTypes()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithInputsApi";
        String expectedFile = "typesWithInputsDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema, method - use record objects")
    public void testGenerateSrcForRecordForced()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithBasic03Api";
        String expectedFile = "typesWithBasic03RecordsAllowed.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with enum, method - default")
    public void testGenerateSrcForEnum()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithEnumApi";
        String expectedFile = "typesWithEnumDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with union, method - default")
    public void testGenerateSrcForUnion()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithUnionApi";
        String expectedFile = "typesWithUnionDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with interface, method - default")
    public void testGenerateSrcForInterface()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithInterfaceApi";
        String expectedFile = "typesWithInterfaceDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with multiple interfaces, method - default")
    public void testGenerateSrcForMultipleInterface()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithMultipleInterfacesApi";
        String expectedFile = "typesWithMultipleInterfacesDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with interfaces implementing interfaces, method - default")
    public void testGenerateSrcForInterfacesImplementingInterfaces()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithInterfacesImplementingInterfacesApi";
        String expectedFile = "typesWithInterfacesImplementingInterfacesDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema multi-dimensional lists, method - default")
    public void testGenerateSrcForSchemaWithMultiDimensionalLists()
            throws IOException, ValidationException, ServiceTypesGenerationException {
        String fileName = "SchemaWithMultiDimensionalListsApi";
        String expectedFile = "typesWithMultiDimensionalListsDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @DataProvider(name = "schemasWithDefaultParameterValuesAndExpectedFiles")
    public Object[][] createSchemasWithDefaultParameterValuesAndExpectedFilesData() {
        return new Object[][]{{"SchemaWithDefaultParameters01Api", "typesWithDefaultParameters01Default.bal"}, {
                "SchemaWithDefaultParameters02Api",
                "typesWithDefaultParameters02Default.bal"},
                {"SchemaWithDefaultParameters03Api", "typesWithDefaultParameters03Default.bal"}, {
                "SchemaWithDefaultParameters04Api",
                "typesWithDefaultParameters04Default.bal"}};
    }

    @Test(description = "Test for schema with default parameter values, method - default",
            dataProvider = "schemasWithDefaultParameterValuesAndExpectedFiles")
    public void testGenerateSrcForSchemaWithDefaultParameterValues(String fileName, String expectedFile)
            throws IOException, ValidationException, ServiceTypesGenerationException {
        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in query resolvers, method - default")
    public void testGenerateSrcForSchemaWithDocsInQueryResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithQueryResolversApi";
        String expectedFile = "typesDocsWithQueryResolversDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }


    @Test(description = "Test for schema with docs in arguments in resolver functions, method - default")
    public void testGenerateSrcForSchemaWithDocsInArgumentsInResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithResolverArgumentsApi";
        String expectedFile = "typesDocsWithResolverArgumentsDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with multiple line docs in resolver function arguments, method - default")
    public void testGenerateSrcForSchemaWithMultipleLineDocsInResolverFunctionArguments()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithMultipleLinesApi";
        String expectedFile = "typesDocsWithMultipleLinesDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in output types, method - default")
    public void testGenerateSrcForSchemaWithDocsInOutputTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithOutputsApi";
        String expectedFile = "typesDocsWithOutputsDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in output types, method - use records objects")
    public void testGenerateSrcForSchemaWithDocsInOutputTypesWithRecords()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithOutputsApi";
        String expectedFile = "typesDocsWithOutputsRecordsAllowed.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in output types, method - default")
    public void testGenerateSrcForSchemaWithDocsInUnionTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithUnionApi";
        String expectedFile = "typesDocsWithUnionDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in Enum types")
    public void testGenerateSrcForSchemaWithDocsInEnumTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithEnumApi";
        String expectedFile = "typesDocsWithEnumDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in input types")
    public void testGenerateSrcForSchemaWithDocsInInputTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithInputsApi";
        String expectedFile = "typesDocsWithInputsDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in interfaces")
    public void testGenerateSrcForSchemaWithDocsInInterfaces()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithInterfacesApi";
        String expectedFile = "typesDocsWithInterfacesDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @DataProvider(name = "schemaFileNamesWithDeprecationAndExpectedFiles")
    public Object[][] getSchemaFileNamesWithDeprecationAndExpectedFiles() {
        return new Object[][]{{"SchemaDocsWithDeprecated01Api", "typesDocsWithDeprecated01Default.bal"},
                {"SchemaDocsWithDeprecated02Api", "typesDocsWithDeprecated02Default.bal"}, {
                "SchemaDocsWithDeprecated03Api", "typesDocsWithDeprecated03Default.bal"}};
    }

    @Test(description = "Test for schema with deprecated directive fields",
            dataProvider = "schemaFileNamesWithDeprecationAndExpectedFiles")
    public void testGenerateSrcForSchemaWithDeprecatedDirective(String fileName, String expectedFile)
            throws ValidationException, IOException, ServiceTypesGenerationException {
        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with deprecated directive fields - method records allowed")
    public void testGenerateSrcForSchemaWithDeprecatedAllowRecords()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocsWithDeprecated01Api";
        String expectedFile = "typesDocsWithDeprecated01RecordsAllowed.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with file upload fields")
    public void testGenerateSrcForSchemaWithFileUploadFields()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaWithFileUploadApi";
        String expectedFile = "typesWithFileUploadDefault.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));
        String generatedServiceTypesContentTrimmed =
                generatedServiceTypesContent.trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");
        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);
        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }
}
