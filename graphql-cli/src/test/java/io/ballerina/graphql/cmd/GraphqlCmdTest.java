/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_GRAPHQL_FILE_WITH_NO_MODE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_MODE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_MISSING_SCHEMA_FILE;

/**
 * This class is used to test the functionality of the GraphQL command.
 */
public class GraphqlCmdTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(GraphqlCmdTest.class);

    @Test(description = "Test successful graphql command execution")
    public void testExecute() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql.config.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);

        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedClientFile = resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile = resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("client.bal")) && Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent = readContent(this.tmpDir.resolve("client.bal"));
                String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

                Assert.assertEquals(expectedClientContent, generatedClientContent);
                Assert.assertEquals(expectedTypesContent, generatedTypesContent);
            } else {
                Assert.fail("Code generation failed. : " + readOutput(true));
            }
        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution with mode flag")
    public void testExecuteWithModeFlag() {
        Path graphql =
                resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithBasic01Api.graphql"));
        String[] args = {"-i", graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "serviceForBasicSchema01.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "typesWithBasic01Default.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            String generatedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

            Assert.assertEquals(expectedServiceContent, generatedServiceContent);
            Assert.assertEquals(expectedTypesContent, generatedTypesContent);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with mode and use-records-for-objects flags")
    public void testExecutionWithModeAndUseRecordsForObjectsFlags() {
        Path graphql =
                resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithBasic03Api.graphql"));
        String[] args = {"-i", graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--use-records-for-objects"};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "serviceForBasicSchema03.bal"));
            Path expectedTypesFile = resourceDir.resolve(
                    Paths.get("serviceGen", "expectedServices", "typesWithBasic03RecordsAllowed.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            String generatedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

            Assert.assertEquals(expectedServiceContent, generatedServiceContent);
            Assert.assertEquals(expectedTypesContent, generatedTypesContent);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution without input file path argument")
    public void testExecuteWithoutInputFilePathArgument() {
        String[] args = {"-i"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(MESSAGE_FOR_MISSING_INPUT_ARGUMENT));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution with invalid mode argument")
    public void testExecuteWithInvalidModeArgument() {
        Path filePath =
                resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithBasic01Api.graphql"));
        String mode = "invalid-service";
        String[] args = {"-i", filePath.toString(), "--mode", mode, "--use-records-for-objects"};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_FOR_INVALID_MODE, mode);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with invalid config file extension")
    public void testExecuteWithInvalidConfigFileExtension() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql.config.yam"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        String message = String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, graphqlConfigYaml);
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @DataProvider(name = "invalidFileNameExtension")
    public Object[] createInvalidFileNameExtensionData() {
        return new Object[]{"graphql.config.yam", "service.bl", "schema.grq", "schema.py"};
    }

    @Test(description = "Test graphql command execution with invalid file extensions",
            dataProvider = "invalidFileNameExtension")
    public void testExecuteWithInvalidFileExtensions(String invalidFileNameExtension) {
        Path filePath = resourceDir.resolve(Paths.get("specs", invalidFileNameExtension));
        String[] args = {"-i", filePath.toString(), "-o", this.tmpDir.toString()};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with graphql file path without mode flag")
    public void testExecuteForGraphqlFileWithModeFlag() {
        Path filePath = resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                "SchemaWithBasic01Api.graphql"));
        String[] args = {"-i", filePath.toString(), "-o", this.tmpDir.toString()};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_FOR_GRAPHQL_FILE_WITH_NO_MODE, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @DataProvider(name = "mismatchModeAndFile")
    public Object[][] createMismatchModeAndFileData() {
        return new Object[][]{{"service", "graphql.config.yaml"}, {"client", "service.bal"},
                {"schema", "schema.graphql"}};
    }

    @Test(description = "Test graphql command execution with mismatch mode and file extension",
            dataProvider = "mismatchModeAndFile")
    public void testExecuteWithMismatchModeAndFileExtension(String mode, String fileName) {
        Path filePath = resourceDir.resolve(Paths.get("specs", fileName));
        String[] args = {"-i", filePath.toString(), "-o", this.tmpDir.toString(), "--mode", mode};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(Constants.MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION, mode, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @DataProvider(name = "useRecordsForObjectsFlagMisUse")
    public Object[][] createUseRecordsForObjectsFlagMisUseData() {
        return new Object[][]{{"client", "graphql.config.yaml"}, {"schema", "service.bal"}};
    }

    @Test(description = "Test graphql command execution with use-records-for-objects and incompatible mode",
            dataProvider = "useRecordsForObjectsFlagMisUse")
    public void testExecuteWithUseRecordsForObjectsFlagAndIncompatibleMode(String mode, String fileName) {
        Path filePath = resourceDir.resolve(Paths.get("specs", fileName));
        String[] args = new String[]{"-i", filePath.toString(), "-o", this.tmpDir.toString(), "--mode", mode,
                "--use-records-for-objects"};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(Constants.MESSAGE_FOR_USE_RECORDS_FOR_OBJECTS_FLAG_MISUSE, mode);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with invalid schema file path")
    public void testExecuteWithInvalidSchemaFilePath() {
        Path filePath = resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "schema.graphql"));
        String[] args = {"-i", filePath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_MISSING_SCHEMA_FILE, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with empty config file")
    public void testExecuteWithEmptyConfigFile() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "empty.graphql.config.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(MESSAGE_FOR_EMPTY_CONFIGURATION_FILE));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test graphql command execution with invalid config file content")
    public void testExecuteWithInvalidConfigFileContent() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "invalid.graphql.config.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test successful graphql command execution with projects in config file")
    public void testExecuteWithProjects() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql-config-with-projects.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedClientFile = resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile = resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve(Paths.get("modules", "country", "client.bal"))) &&
                    Files.exists(this.tmpDir.resolve(Paths.get("modules", "country", "types.bal")))) {
                String generatedClientContent =
                        readContent(this.tmpDir.resolve(Paths.get("modules", "country", "client.bal")));
                String generatedTypesContent =
                        readContent(this.tmpDir.resolve(Paths.get("modules", "country", "types.bal")));

                Assert.assertEquals(expectedClientContent, generatedClientContent);
                Assert.assertEquals(expectedTypesContent, generatedTypesContent);
            } else {
                Assert.fail("Code generation failed. : " + readOutput(true));
            }
        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test successful graphql command execution with schema URL in config file", enabled = false)
    public void testExecuteWithSchemaUrl() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql-config-with-schema-url.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedClientFile = resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile = resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("client.bal")) && Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent = readContent(this.tmpDir.resolve("client.bal"));
                String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

                Assert.assertEquals(expectedClientContent, generatedClientContent);
                Assert.assertEquals(expectedTypesContent, generatedTypesContent);
            } else {
                Assert.fail("Code generation failed. : " + readOutput(true));
            }
        } catch (BLauncherException | IOException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test successful graphql command execution with invalid introspection URL in config file")
    public void testExecuteWithInvalidIntrospectionUrl() {
        Path graphqlConfigYaml =
                resourceDir.resolve(Paths.get("specs", "graphql-config-with-invalid-introspection-url.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains("Failed to retrieve SDL."));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }
}
