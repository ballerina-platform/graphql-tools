/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the functionality of the GraphQL command.
 */
public class SdlSchemaGenerationTest extends GraphqlTest {

    @Test(description = "Test successful GraphQL command execution")
    public void testSdlGeneration() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service1.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_graphql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_graphql.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_graphql.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution")
    public void testSdlGenerationWithProject() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid/project_1", "main.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_graphql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_project.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_project.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with service name")
    public void testSdlGenerationWithServiceBasePath() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service2.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString(), "-s", "/service/gql"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_gql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_gql.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_gql.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services")
    public void testSdlGenerationWithMultipleServices1() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service3.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service3.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service3.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service3.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service3_1.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service3_1.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service3_1.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service3_2.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service3_2.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service3_2.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services")
    public void testSdlGenerationWithMultipleServices2() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service4.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service4.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service4.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service4.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service4_1.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service4_1.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service4_1.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service4_2.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service4_2.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service4_2.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services in a bal project")
    public void testSdlGenerationWithMultipleServicesInProject() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid/project_2", "main.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_main.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service4.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_main.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_main_1.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service4_1.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_main_1.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_main_2.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service4_2.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_main_2.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with module-level variable service declaration")
    public void testExecuteWithModuleLevelVariableDeclaration() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service6.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service6.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service6.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service6.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple types services")
    public void testSdlGenerationForGraphqlServiceWithHttService() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service7.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_query.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_query.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_query.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with services include custom scalars")
    public void testSdlGenerationWithCustomScalars() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service8.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString(), "-s", "/gql"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_gql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_gql.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_gql.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services in same listener")
    public void testSdlGenerationWithMultipleServicesInSameListener() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/valid", "service9.bal"));
        String[] args = {"-i", graphqlService.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_person.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_person.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_person.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_inputs.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_inputs.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_inputs.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service9.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service9.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service9.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test GraphQL command execution with service includes compilation errors")
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

    @Test(description = "Test GraphQL command execution with invalid service base path")
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

    @Test(description = "Test GraphQL command execution with invalid input file path")
    public void testExecuteWithInvalidBalFilePath() {
        String[] args = {"-i", "/service.bal", "-o", this.tmpDir.toString()};
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

    @Test(description = "Test GraphQL command execution with readonly output path")
    public void testExecuteWithReadonlyOutputPath() {
        Path graphqlService = resourceDir.resolve(Paths.get("graphqlServices/invalid", "service2.bal"));
        try {
            Path outPath = Paths.get(tmpDir.toString(), "new");
            Files.createDirectories(outPath);
            File file = new File(outPath.toString());
            boolean setReadonly = file.setReadOnly();
            if (setReadonly) {
                // The `setReadOnly()` method doesn't work as expected in 'windows' environments. Hence,
                // added an 'if' block to skip this test in 'windows' until find a workaround
                String[] args = {"-i", graphqlService.toString(), "-o", outPath.toString()};
                GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
                new CommandLine(graphqlCmd).parseArgs(args);
                graphqlCmd.execute();
                String output = readOutput(true);
                String expectedMessage = "SDL schema generation failed: " + outPath +
                        "/schema_graphql_new.graphql (Permission denied)";
                file.setWritable(true);
                Assert.assertTrue(output.contains(expectedMessage));
            } else {
                Assert.assertTrue(true);
            }
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test GraphQL command execution with bal file without read permission")
    public void testSdlGenerationWithBalFileWithoutReadPermission() {
        Path graphqlService = Paths.get(tmpDir.toString(), "service4.bal");
        try {
            Files.createFile(graphqlService);
            File file = new File(graphqlService.toString());
            boolean setWritable = file.setReadable(false);
            if (setWritable) {
                // The `setReadable()` method doesn't work as expected in 'windows' environments. Hence,
                // added an 'if' block to skip this test in `windows` until find a workaround
                String[] args = {"-i", file.getPath(), "-o", this.tmpDir.toString()};
                GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
                new CommandLine(graphqlCmd).parseArgs(args);
                graphqlCmd.execute();
                String output = readOutput(true);
                String expectedMessage = "SDL schema generation failed: " +
                        "Cannot read provided Ballerina file (Permission denied)";
                file.setReadable(true);
                Assert.assertTrue(output.contains(expectedMessage));
            } else {
                Assert.assertTrue(true);
            }
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }
}
