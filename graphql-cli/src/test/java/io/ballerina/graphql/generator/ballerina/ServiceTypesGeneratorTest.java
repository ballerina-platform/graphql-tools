package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlServiceProject;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.model.SrcFilePojo;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.ballerina.graphql.common.TestUtils.getDiagnosticResult;
import static io.ballerina.graphql.common.TestUtils.writeSources;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;

/**
 * Test class for ServiceTypesGenerator.
 * Test the successful generation of service types
 */
public class ServiceTypesGeneratorTest extends GraphqlTest {
    private Path typesCheckProjectDir =
            this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "typesCheckProject"));

    // TODO: add compilation check
    @Test(description = "Test for simple schema, method - default")
    public void testGenerateSrc()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema01Api";
        String expectedFile = "types01Default.bal";

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
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    private void writeContentTo(String content, Path projectDir) throws IOException {
        SrcFilePojo srcFile = new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, "root", TYPES_FILE_NAME,
                content);
        List<SrcFilePojo> srcFiles = new ArrayList<>();
        srcFiles.add(srcFile);
        writeSources(srcFiles, projectDir);
    }

    private boolean hasOnlyResourceFuncMustReturnResultErrors(Collection<Diagnostic> errors) {
        for (Diagnostic error : errors) {
            if (!error.message().contains("this resource_func must return a result")) {
                return false;
            }
        }
        return true;
    }

    @Test(description = "Test for schema with more types, method - default")
    public void testGenerateSrcForMoreTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema02Api";
        String expectedFile = "types02Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with mutation types, method - default")
    public void testGenerateSrcForSchemaWithMutationTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema04Api";
        String expectedFile = "types04Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with subscription types, method - default")
    public void testGenerateSrcForSchemaWithSubscriptionTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema05Api";
        String expectedFile = "types05Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with input types, method - default")
    public void testGenerateSrcForSchemaWithInputTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema03Api";
        String expectedFile = "types03Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema, method - record forced")
    public void testGenerateSrcForRecordForced()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema06Api";
        String expectedFile = "types06RecordObjects.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setRecordForced(true);
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with enum, method - default")
    public void testGenerateSrcForEnum()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema07Api";
        String expectedFile = "types07Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with union, method - default")
    public void testGenerateSrcForUnion()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema08Api";
        String expectedFile = "types08Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with interface, method - default")
    public void testGenerateSrcForInterface()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema09Api";
        String expectedFile = "types09Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for simple schema with multiple interfaces, method - default")
    public void testGenerateSrcForMultipleInterface()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema10Api";
        String expectedFile = "types10Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with interfaces implementing interfaces, method - default")
    public void testGenerateSrcForInterfacesImplementingInterfaces()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema11Api";
        String expectedFile = "types11Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema multi-dimensional lists, method - default")
    public void testGenerateSrcForSchemaWithMultiDimensionalLists()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema12Api";
        String expectedFile = "types12Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @DataProvider(name = "schemasWithDefaultParameterValuesAndExpectedFiles")
    public Object[][] createSchemasWithDefaultParameterValuesAndExpectedFilesData() {
        return new Object[][]{{"Schema13Api", "types13Default.bal"}, {"Schema14Api", "types14Default.bal"}, {
                "Schema15Api", "types15Default.bal"}, {"Schema16Api", "types16Default.bal"}};
    }

    @Test(description = "Test for schema with default parameter values, method - default", dataProvider =
            "schemasWithDefaultParameterValuesAndExpectedFiles")
    public void testGenerateSrcForSchemaWithDefaultParameterValues(String fileName, String expectedFile)
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with types with optional fields, method - record forced", enabled = false)
    public void testGenerateSrcForSchemaWithTypesWithOptionalFields()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema17Api";
        String expectedFile = "types17RecordObjects.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(fileName);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(graphQLSchema);
        writeContentTo(generatedServiceTypesContent, typesCheckProjectDir);
        DiagnosticResult diagnosticResult = getDiagnosticResult(typesCheckProjectDir);
        Assert.assertTrue(hasOnlyResourceFuncMustReturnResultErrors(diagnosticResult.errors()));

        String generatedServiceTypesContentTrimmed =
                serviceTypesGenerator.generateSrc(graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContentTrimmed);
    }

    @Test(description = "Test for schema with docs in arguments in resolver functions, method - default")
    public void testGenerateSrcForSchemaWithDocsInArgumentsInResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs04Api";
        String expectedFile = "typesDocs04Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with multiple line docs in resolver function arguments, method - default")
    public void testGenerateSrcForSchemaWithMultipleLineDocsInResolverFunctionArguments()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs05Api";
        String expectedFile = "typesDocs05Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with docs in output types, method - default")
    public void testGenerateSrcForSchemaWithDocsInOutputTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs06Api";
        String expectedFile = "typesDocs06Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with docs in output types, method - with records")
    public void testGenerateSrcForSchemaWithDocsInOutputTypesWithRecords()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs06Api";
        String expectedFile = "typesDocs06RecordsAllowed.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setRecordForced(true);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with docs in output types, method - default")
    public void testGenerateSrcForSchemaWithDocsInUnionTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs07Api";
        String expectedFile = "typesDocs07Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with docs in Enum types")
    public void testGenerateSrcForSchemaWithDocsInEnumTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs08Api";
        String expectedFile = "typesDocs08Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with docs in input types")
    public void testGenerateSrcForSchemaWithDocsInInputTypes()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs09Api";
        String expectedFile = "typesDocs09Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with docs in interfaces")
    public void testGenerateSrcForSchemaWithDocsInInterfaces()
            throws ValidationException, IOException, ServiceTypesGenerationException {
        String fileName = "SchemaDocs10Api";
        String expectedFile = "typesDocs10Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @DataProvider(name = "schemaFileNamesWithDeprecationAndExpectedFiles")
    public Object[][] getSchemaFileNamesWithDeprecationAndExpectedFiles() {
        return new Object[][]{
                {"SchemaDocs11Api", "typesDocs11Default.bal"},
                {"SchemaDocs12Api", "typesDocs12Default.bal"}
        };
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
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    // TODO: Test for schema with deprecated fields in enums
    // TODO: Test for schema with deprecated fields in interfaces
    // TODO: Test for schema with deprecated fields in records
}
