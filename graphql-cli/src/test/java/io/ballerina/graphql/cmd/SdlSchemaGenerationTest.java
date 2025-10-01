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
import org.testng.annotations.DataProvider;
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

    @DataProvider(name = "serviceFileNames")
    public Object[][] dataProviderSdlGeneration() {
        return new Object[][] {
                {"service_1.bal", "schema_graphql.graphql", "schema_graphql.graphql"},
                {"project_1/main.bal", "schema_graphql.graphql", "schema_graphql.graphql"},
                {"service_2.bal", "schema_service_gql.graphql", "schema_service_gql.graphql"},
                {"service_3.bal", "schema_service_3.graphql", "schema_service_3.graphql"},
                {"service_3.bal", "schema_service_3_1.graphql", "schema_service_3_1.graphql"},
                {"service_3.bal", "schema_service_3_2.graphql", "schema_service_3_2.graphql"},
                {"service_4.bal", "schema_service_4.graphql", "schema_service_4.graphql"},
                {"service_4.bal", "schema_service_4_1.graphql", "schema_service_4_1.graphql"},
                {"service_4.bal", "schema_service_4_2.graphql", "schema_service_4_2.graphql"},
                {"project_2/main.bal", "schema_service_4.graphql", "schema_main.graphql"},
                {"project_2/main_1.bal", "schema_service_4_1.graphql", "schema_main_1.graphql"},
                {"project_2/main_2.bal", "schema_service_4_2.graphql", "schema_main_2.graphql"},
                {"service_6.bal", "schema_service_6.graphql", "schema_service_6.graphql"},
                {"service_7.bal", "schema_query.graphql", "schema_query.graphql"},
                {"service_8.bal", "schema_gql.graphql", "schema_gql.graphql"},
                {"service_9.bal", "schema_person.graphql", "schema_person.graphql"},
                {"service_9.bal", "schema_inputs.graphql", "schema_inputs.graphql"},
                {"service_9.bal", "schema_service_9.graphql", "schema_service_9.graphql"},
                {"service_10.bal", "schema_graphql_docs.graphql", "schema_graphql_docs.graphql"},
                {"service_11.bal", "schema_product.graphql", "schema_product.graphql"}
        };
    }

    @Test(
            description = "Test successful GraphQL command execution",
            dataProvider = "serviceFileNames"
    )
    public void testSdlGeneration(String svcFile, String expSchema, String genSchema) {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", svcFile)).toString();
        String[] args = {"-i", servicePath, "-o", this.tmpDir.toString()};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", expSchema));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(this.tmpDir.resolve(genSchema)));
            String generatedSchema = readContentWithFormat(this.tmpDir.resolve(genSchema));
            Assert.assertEquals(expectedSchema, generatedSchema);
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    @DataProvider(name = "invalidServiceFileNames")
    public Object[][] dataProviderExecuteWithBalFileIncludeCompilationErrors() {
        return new Object[][]{
                {"service_1.bal", "ERROR [:(-1:-1,-1:-1)] Given Ballerina file contains compilation error(s)."},
                {"service.bal", "ERROR [:(-1:-1,-1:-1)] SDL schema generation failed: " +
                        "Provided Ballerina file path does not exist"}
        };
    }

    @Test(
            description = "Test GraphQL command execution with service includes compilation errors",
            dataProvider = "invalidServiceFileNames"
    )
    public void testExecuteWithBalFileIncludeCompilationErrors(String svcFile, String errMessage) {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "invalid", svcFile)).toString();
        String[] args = {"-i", servicePath, "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(errMessage));
        } catch (IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(description = "Test GraphQL command execution with invalid service base path")
    public void testExecuteWithInvalidServiceName() {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "invalid", "service_2.bal")).toString();
        String[] args = {"-i", servicePath, "-o", this.tmpDir.toString(), "-s", "/service/gql"};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        String message = "ERROR [:(-1:-1,-1:-1)] No Ballerina services found with name \"/service/gql\" to generate " +
                "SDL schema. These services are available in ballerina file. [/graphql, /graphql/new, /gql/new]";
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (IOException e) {
            output = e.toString();
            Assert.fail(output);
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
            Path servicePath = resourceDir.resolve(Paths.get("graphqlServices/invalid", "service_2.bal"));
            String[] args = {"-i", servicePath.toString(), "-o", outPath.toString()};
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = "ERROR [:(-1:-1,-1:-1)] SDL schema generation failed: " + outPath +
                    "/schema_graphql_new.graphql (Permission denied)";
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (IOException e) {
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
            String[] args = {"-i", graphqlService.toString(), "-o", tmpDir.toString()};
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = "ERROR [:(-1:-1,-1:-1)] SDL schema generation failed: " +
                    "Cannot read provided Ballerina file (Permission denied)";
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }
}
