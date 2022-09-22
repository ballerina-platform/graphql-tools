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
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;

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

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                    Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent =
                        readContent(this.tmpDir.resolve("client.bal"));
                String generatedTypesContent =
                        readContent(this.tmpDir.resolve("types.bal"));

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

    @Test(description = "Test graphql command execution with invalid config file extension")
    public void testExecuteWithInvalidConfigFileExtension() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql.config.yam"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_EXTENSION));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
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
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs",
                "graphql-config-with-projects.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve(Paths.get("modules", "country",
                    "client.bal"))) && Files.exists(this.tmpDir.resolve(Paths.get("modules",
                    "country", "types.bal")))) {
                String generatedClientContent = readContent(this.tmpDir.resolve(Paths.get("modules",
                        "country", "client.bal")));
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

    @Test(description = "Test successful graphql command execution with schema URL in config file")
    public void testExecuteWithSchemaUrl() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs",
                "graphql-config-with-schema-url.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);

            if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                    Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent =
                        readContent(this.tmpDir.resolve("client.bal"));
                String generatedTypesContent =
                        readContent(this.tmpDir.resolve("types.bal"));

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
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs",
                "graphql-config-with-invalid-introspection-url.yaml"));
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
