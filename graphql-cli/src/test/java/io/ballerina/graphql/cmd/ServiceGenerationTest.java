package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_MISSING_SCHEMA_FILE;

/**
 * This class includes tests for Ballerina Graphql service generation.
 */
public class ServiceGenerationTest extends GraphqlTest {
    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(
                        TestUtils.TEST_DISTRIBUTION_PATH
                                .resolve(Paths.get(TestUtils.DISTRIBUTION_FILE_NAME)).toAbsolutePath())
                .build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    @Test(description = "Test graphql command execution with mode flag")
    public void testServiceGenerationWithModeFlag() {
        Path graphql =
                resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithBasic01Api.graphql"));
        String[] args = {"-i", graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);

        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "serviceForBasicSchema01.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "typesWithBasic01Default.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("service.bal")) && Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent = readContent(this.tmpDir.resolve("service.bal"));
                String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

                Assert.assertEquals(expectedServiceContent, generatedClientContent);
                Assert.assertEquals(expectedTypesContent, generatedTypesContent);
            } else {
                Assert.fail("Code generation failed. : " + readOutput(true));
            }

        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution with mode and use-records-for-objects flags")
    public void testServiceGenerationWithModeAndUseRecordsForObjectsFlags() {
        Path graphql =
                resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithBasic03Api.graphql"));
        String[] args = {"-i", graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--use-records-for-objects"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);

        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "serviceForBasicSchema03.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(
                            Paths.get("serviceGen", "expectedServices", "typesWithBasic03RecordsAllowed.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("service.bal")) && Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent = readContent(this.tmpDir.resolve("service.bal"));
                String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

                Assert.assertEquals(expectedServiceContent, generatedClientContent);
                Assert.assertEquals(expectedTypesContent, generatedTypesContent);
            } else {
                Assert.fail("Code generation failed. : " + readOutput(true));
            }

        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution for service generation with invalid schema")
    public void testExecuteWithInvalidSchemaForServiceGen() {
        Path graphqlSchema =
                this.resourceDir.resolve(
                        Paths.get("serviceGen", "graphqlSchemas", "invalid", "SchemaWithMissingCharApi.graphql"));
        String[] args = {"-i", graphqlSchema.toString(), "-o", this.tmpDir.toString(), "-m", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains("GraphQL SDL validation failed."));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution with invalid schema input file path")
    public void testExecuteWithSchemaInvalidFilePath() {
        Path invalidPath =
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "invalid", "Schema.graphql"));
        String[] args = {"-i", invalidPath.toString(), "-o", this.tmpDir.toString(), "-m", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String message = String.format(MESSAGE_MISSING_SCHEMA_FILE, invalidPath);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @DataProvider(name = "schemaFiles")
    public Object[] createSchemaFilesData() {
        return new Object[]{"SchemaWithBasic01Api.graphql", "SchemaWithBasic02Api.graphql",
                "SchemaWithInputsApi.graphql", "SchemaWithMutationApi.graphql",
                "SchemaWithSubscriptionApi.graphql", "SchemaWithBasic03Api.graphql", "SchemaWithEnumApi.graphql",
                "SchemaWithUnionApi.graphql",
                "SchemaWithInterfaceApi.graphql", "SchemaWithMultipleInterfacesApi.graphql",
                "SchemaWithInterfacesImplementingInterfacesApi.graphql", "SchemaWithMultiDimensionalListsApi.graphql",
                "SchemaWithDefaultParameters01Api.graphql", "SchemaWithDefaultParameters02Api.graphql",
                "SchemaWithDefaultParameters03Api.graphql", "SchemaWithDefaultParameters04Api.graphql",
                "Schema17Api.graphql"};
    }

    @Test(description = "Test compilation for all schemas, method - default", dataProvider = "schemaFiles")
    public void testCompilationForAllSchemas(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));
        String packagePath = "project";
        Path projectDir = this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", packagePath));

        String[] args = {"-i", schemaPath.toString(), "-o", projectDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, projectDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(packagePath);
            Assert.assertTrue(hasOnlyNoMethodImplErrorsOfServiceObj(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            String output = e.toString();
            Assert.fail(output);
        }

    }

    @DataProvider(name = "schemaFilesWithDoc")
    public Object[] createSchemaFilesWithDoc() {
        return new Object[]{
                "SchemaDocsWithQueryResolversApi.graphql",
                "SchemaDocsWithMutationAndSubscriptionResolversApi.graphql",
                "SchemaDocsWithResolverMultipleLinesApi.graphql",
                "SchemaDocsWithResolverArgumentsApi.graphql",
                "SchemaDocsWithMultipleLinesApi.graphql",
                "SchemaDocsWithOutputsApi.graphql",
                "SchemaDocsWithUnionApi.graphql",
                "SchemaDocsWithEnumApi.graphql",
                "SchemaDocsWithInputsApi.graphql",
                "SchemaDocsWithInterfacesApi.graphql",
                "SchemaDocsWithDeprecated01Api.graphql"
        };
    }

    @Test(description = "Test compilation for schemas with documentation", dataProvider = "schemaFilesWithDoc")
    public void testCompilationForSchemasWithDocumentation(String schemaFileWithDoc) {
        String packagePath = "project";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFileWithDoc));
        Path projectDir = this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", packagePath));

        String[] args = {"-i", schemaPath.toString(), "-o", projectDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, projectDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            DiagnosticResult diagnosticResult = getDiagnosticResult(packagePath);
            Assert.assertTrue(hasOnlyNoMethodImplErrorsOfServiceObj(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test compilation for complete schema", enabled = false)
    public void testCompilationForCompleteSchema() {
        String schemaFile = "SchemaCompleteApi.graphql";
        String packagePath = "project";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", schemaFile));
        Path projectDir = this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", packagePath));

        String[] args = {"-i", schemaPath.toString(), "-o", projectDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, projectDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            DiagnosticResult diagnosticResult = getDiagnosticResult(packagePath);
            Assert.assertTrue(hasOnlyNoMethodImplErrorsOfServiceObj(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    private boolean hasOnlyNoMethodImplErrorsOfServiceObj(Collection<Diagnostic> errors) {
        for (Diagnostic error : errors) {
            boolean containsNoImpl = error.message().contains("no implementation found for the method");
            boolean containsServiceObj = error.message().contains("of service declaration \'object");
            if (!containsNoImpl || !containsServiceObj) {
                return false;
            }
        }
        return true;
    }

    private DiagnosticResult getDiagnosticResult(String packagePath) {
        Path projectDirPath = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", packagePath));
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage().getCompilation().diagnosticResult();
    }
}
