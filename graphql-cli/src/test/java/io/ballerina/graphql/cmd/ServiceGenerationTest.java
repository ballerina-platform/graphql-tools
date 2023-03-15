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
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * This class includes tests for Ballerina Graphql service generation.
 */
public class ServiceGenerationTest extends GraphqlTest {
    @Test(description = "Test graphql command execution with mode flag")
    public void testServiceGenerationWithModeFlag() {
        Path graphql = resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "Schema01Api.graphql"));
        String[] args = {"-i", graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);

        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "service01.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "types01Default.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("service.bal")) &&
                    Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent =
                        readContent(this.tmpDir.resolve("service.bal"));
                String generatedTypesContent =
                        readContent(this.tmpDir.resolve("types.bal"));

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
        Path graphql = resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "Schema06Api.graphql"));
        String[] args = {"-i", graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--use-records-for-objects"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);

        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "service06.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "types06RecordObjects.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("service.bal")) &&
                    Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent =
                        readContent(this.tmpDir.resolve("service.bal"));
                String generatedTypesContent =
                        readContent(this.tmpDir.resolve("types.bal"));

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
        Path graphqlSchema = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "invalid",
                "Schema01Api.graphql"));
        String[] args = {"-i", graphqlSchema.toString(), "-o", this.tmpDir.toString()};
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
        Path invalidPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "invalid",
                "Schema.graphql"));
        String[] args = {"-i", invalidPath.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains("Provided Schema file path does not exist"));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    // TODO: test with read not allowed schema file
    // TODO: test for compilation errors
    @Test(description = "Test compilation for simple schema")
    public void testCompilationForSimpleSchema() {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                "Schema01Api.graphql"));
        String packagePath = "project01";
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

    @Test(description = "Test compilation for schema with more types")
    public void testCompilationForSchemaWithMoreTypes() {
        String schemaFile = "Schema02Api.graphql";
        String packagePath = "project02";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFile));
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

    @Test(description = "Test compilation for schema with input types")
    public void testCompilationForSchemaWithInputTypes() {
        String schemaFile = "Schema03Api.graphql";
        String packagePath = "project03";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFile));
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

    @Test(description = "Test compilation for schema with mutation types")
    public void testCompilationForSchemaWithMutationTypes() {
        String schemaFile = "Schema04Api.graphql";
        String packagePath = "project04";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFile));
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

    @Test(description = "Test compilation for schema with subscription types")
    public void testCompilationForSchemaWithSubscriptionTypes() {
        String schemaFile = "Schema05Api.graphql";
        String packagePath = "project05";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFile));
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

    @Test(description = "Test compilation for schema with record objects")
    public void testCompilationForSchemaWithRecordObjects() {
        String schemaFile = "Schema06Api.graphql";
        String packagePath = "project06";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFile));
        Path projectDir = this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", packagePath));

        String[] args = {"-i", schemaPath.toString(), "-o", projectDir.toString(), "--mode", "service", "--use-records-for-objects"};
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

    @Test(description = "Test compilation for complete schema")
    public void testCompilationForCompleteSchema() {
        String schemaFile = "SchemaCompleteApi.graphql";
        String packagePath = "project";
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                schemaFile));
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
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        return diagnosticResult;
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment =
                EnvironmentBuilder.getBuilder().setBallerinaHome(TestUtils.TEST_DISTRIBUTION_PATH.resolve(Paths.get(TestUtils.DISTRIBUTION_FILE_NAME))
                        .toAbsolutePath()).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }
}
