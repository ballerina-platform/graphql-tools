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
 *
 */
public class SdlSchemaGenTest extends GraphqlTest {

    @Test(description = "Test successful graphql command execution")
    public void testExecute() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service1.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_graphql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            if (Files.exists(this.tmpDir.resolve("schema_graphql.graphql"))) {
                String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_graphql.graphql"));
                Assert.assertEquals(expectedSchema, generatedSchema);
            } else {
                Assert.fail("Schema generation failed. : " + readOutput(true));
            }
        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test successful graphql command execution with service name")
    public void testExecuteWithMultipleServices() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service2.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString(), "-s", "/service/gql"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_gql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            if (Files.exists(this.tmpDir.resolve("schema_service_gql.graphql"))) {
                String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_gql.graphql"));
                Assert.assertEquals(expectedSchema, generatedSchema);
            } else {
                Assert.fail("Schema generation failed. : " + readOutput(true));
            }
        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution with invalid bal file content")
    public void testExecuteWithBalFileIncludeCompilationErrors() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/invalid", "service1.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("Given Ballerina file contains compilation error(s)."));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with multiple services")
    public void testExecuteWithInvalidServiceName() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/invalid", "service2.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString(), "-s", "/service/gql"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            String expectedMessage = "No Ballerina services found with name \"/service/gql\" to generate SDL schema. " +
                 "These services are available in ballerina file. [/graphql, /graphql/new, /gql/new]";
            Assert.assertTrue(output.contains(expectedMessage));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with invalid bal file path")
    public void testExecuteWithInvalidBalFilePath() {
        String[] args = {"-i", "target/service.bal", "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            String expectedMessage = "SDL schema generation failed: Provided Ballerina file path does not exist";
            Assert.assertTrue(output.contains(expectedMessage));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }
}
