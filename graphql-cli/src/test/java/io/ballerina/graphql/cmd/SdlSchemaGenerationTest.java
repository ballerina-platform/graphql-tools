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

import io.ballerina.graphql.common.GraphqlTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.graphql.common.TestUtils.WHITESPACE_REGEX;

/**
 * This class is used to test the functionality of the GraphQL command.
 */
public class SdlSchemaGenerationTest extends GraphqlTest {

    @Test(description = "Test successful GraphQL command execution")
    public void testSdlGeneration() {
        String[] args = {"-i", "valid/service_1.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_graphql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_graphql.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_graphql.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution")
    public void testSdlGenerationWithProject() {
        String[] args = {"-i", "valid/project_1/main.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_graphql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_project.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_project.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with service name")
    public void testSdlGenerationWithServiceBasePath() {
        String[] args = {"-i", "valid/service_2.bal", "-o", this.tmpDir.toString(), "-s", "/service/gql"};
        try {
            executeCommand(args);
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_gql.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_gql.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_gql.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services")
    public void testSdlGenerationWithMultipleServices1() {
        String[] args = {"-i", "valid/service_3.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_3.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_3.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_3.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_3_1.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_3_1.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_3_1.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_3_2.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_3_2.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_3_2.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services")
    public void testSdlGenerationWithMultipleServices2() {
        String[] args = {"-i", "valid/service_4.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_4.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_4.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_4.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_4_1.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_4_1.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_4_1.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_4_2.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_4_2.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_4_2.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services in a bal project")
    public void testSdlGenerationWithMultipleServicesInProject() {
        String[] args = {"-i", "valid/project_2/main.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_main.graphql")));
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_4.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_main.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_main_1.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_4_1.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_main_1.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_main_2.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_4_2.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_main_2.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with module-level variable service declaration")
    public void testExecuteWithModuleLevelVariableDeclaration() {
        String[] args = {"-i", "valid/service_6.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_6.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_6.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_6.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple types services")
    public void testSdlGenerationForGraphqlServiceWithHttService() {
        String[] args = {"-i", "valid/service_7.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_query.graphql"));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_query.graphql")));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_query.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test successful GraphQL command execution with multiple services in same listener")
    public void testSdlGenerationWithMultipleServicesInSameListener() {
        String[] args = {"-i", "valid/service_9.bal", "-o", this.tmpDir.toString()};
        try {
            executeCommand(args);
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

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("schema_service_9.graphql")));
            expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", "schema_service_9.graphql"));
            expectedSchema = readContentWithFormat(expectedSchemaFile);
            generatedSchema = readContentWithFormat(this.tmpDir.resolve("schema_service_9.graphql"));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test GraphQL command execution with service includes compilation errors")
    public void testExecuteWithBalFileIncludeCompilationErrors() {
        String[] args = {"-i", "invalid/service_1.bal", "-o", this.tmpDir.toString()};
        try {
            InputStream output = executeCommandWithErrors(args);
            String message = "ERROR [:(-1:-1,-1:-1)] Given Ballerina file contains compilation error(s).";
            BufferedReader br = new BufferedReader(new InputStreamReader(output));
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            // Replace following as Windows environment requirement
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_REGEX, "");
            message = (message.trim()).replaceAll(WHITESPACE_REGEX, "");
            Assert.assertTrue(generatedLog.contains(message));
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test GraphQL command execution with invalid service base path")
    public void testExecuteWithInvalidServiceName() {
        String[] args = {"-i", "invalid/service_2.bal", "-o", this.tmpDir.toString(), "-s", "/service/gql"};
        try {
            InputStream output = executeCommandWithErrors(args);
            String message = "ERROR [:(-1:-1,-1:-1)] No Ballerina services found with name \"/service/gql\" to " +
            "generate SDL schema. These services are available in ballerina file. [/graphql, /graphql/new, /gql/new]";
            BufferedReader br = new BufferedReader(new InputStreamReader(output));
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            // Replace following as Windows environment requirement
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_REGEX, "");
            message = (message.trim()).replaceAll(WHITESPACE_REGEX, "");
            Assert.assertTrue(generatedLog.contains(message));
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test GraphQL command execution with invalid input file path")
    public void testExecuteWithInvalidBalFilePath() {
        String[] args = {"-i", "/service.bal", "-o", this.tmpDir.toString()};
        try {
            InputStream output = executeCommandWithErrors(args);
            String message =
                    "ERROR [:(-1:-1,-1:-1)] SDL schema generation failed: Provided Ballerina file path does not exist";
            BufferedReader br = new BufferedReader(new InputStreamReader(output));
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            // Replace following as Windows environment requirement
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_REGEX, "");
            message = (message.trim()).replaceAll(WHITESPACE_REGEX, "");
            Assert.assertTrue(generatedLog.contains(message));
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(
        groups = {"invalid_permission"},
        description = "Test GraphQL command execution with readonly output path"
    )
    public void testExecuteWithReadonlyOutputPath() {
        try {
            Path outPath = Paths.get(tmpDir.toString(), "new");
            Files.createDirectories(outPath);
            File file = new File(outPath.toString());
            file.setReadOnly();
            String[] args = {"-i", "invalid/service_2.bal", "-o", outPath.toString()};
            InputStream output = executeCommandWithErrors(args);
            String message = "ERROR [:(-1:-1,-1:-1)] SDL schema generation failed: " + outPath +
                    "/schema_graphql_new.graphql (Permission denied)";
            BufferedReader br = new BufferedReader(new InputStreamReader(output));
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            Assert.assertEquals(generatedLog, message);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(
        groups = {"invalid_permission"},
        description = "Test GraphQL command execution with bal file without read permission"
    )
    public void testSdlGenerationWithBalFileWithoutReadPermission() {
        Path graphqlService = Paths.get(tmpDir.toString(), "service.bal");
        try {
            Files.createFile(graphqlService);
            File file = new File(graphqlService.toString());
            file.setReadable(false);
            String[] args = {"-i", "service.bal", "-o", tmpDir.toString()};
            InputStream output = executeCommandWithErrors(tmpDir, args);
            String message = "ERROR [:(-1:-1,-1:-1)] SDL schema generation failed: " +
                    "Cannot read provided Ballerina file (Permission denied)";
            BufferedReader br = new BufferedReader(new InputStreamReader(output));
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            Assert.assertEquals(generatedLog, message);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.toString());
        }
    }
}
