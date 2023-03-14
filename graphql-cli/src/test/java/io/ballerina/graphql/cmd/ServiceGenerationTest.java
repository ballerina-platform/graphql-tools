package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    // TODO: test with read not allowed schema file
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
}
