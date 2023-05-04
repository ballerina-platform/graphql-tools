/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.projects.DiagnosticResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_CAN_NOT_READ_SCHEMA_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_MISSING_SCHEMA_FILE;
import static io.ballerina.graphql.common.TestUtils.getDiagnosticResult;
import static io.ballerina.graphql.common.TestUtils.hasOnlyFuncMustReturnResultErrors;

/**
 * This class includes tests for Ballerina Graphql service generation.
 */
public class ServiceGenerationTest extends GraphqlTest {
    private final Path balTomlPath =
            this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "Ballerina.toml"));

    @BeforeClass
    public void copyBalTomlFile() throws IOException {
        Files.copy(balTomlPath, this.tmpDir.resolve(balTomlPath.getFileName()));
    }

    @AfterClass
    public void removeBalTomlFile() throws IOException {
        Files.deleteIfExists(this.tmpDir.resolve(balTomlPath.getFileName()));
    }

    @Test(description = "Test graphql command execution for service generation with invalid schema")
    public void testExecuteWithInvalidSchemaForServiceGen() {
        Path graphqlSchema =
                this.resourceDir.resolve(
                        Paths.get("serviceGen", "graphqlSchemas", "invalid", "SchemaWithMissingCharApi.graphql"));
        String[] args = {"-i", graphqlSchema.toString(), "-o", this.tmpDir.toString(), "-m", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        String output;
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
        String output;
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(output);
        }
    }

    @Test(
            groups = {"invalid_permission"},
            description = "Test GraphQL command execution with readonly output path"
    )
    public void testExecuteWithReadOnlyOutputPath() {
        Path graphqlSchema =
                this.resourceDir.resolve(
                        Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithBasic01Api.graphql"));
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
        try {
            Path outputPath = Paths.get(tmpDir.toString(), "new");
            Files.createDirectories(outputPath);
            File file = new File(outputPath.toString());
            file.setReadOnly();
            String message = String.format("%s/types.bal (Permission denied)", outputPath);
            String[] args = {"-i", graphqlSchema.toString(), "-o", outputPath.toString(), "-m", "service"};
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(
            groups = {"invalid_permission"},
            description = "Test GraphQL command execution with schema file without read permission"
    )
    public void testExecuteWithSchemaFileWithoutReadPermission() {
        Path graphqlSchema = Paths.get(tmpDir.toString(), "schema.graphql");
        try {
            Files.createFile(graphqlSchema);
            File file = new File(graphqlSchema.toString());
            file.setReadable(false);
            String[] args = {"-i", graphqlSchema.toString(), "-o", tmpDir.toString(), "-m", "service"};
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, false);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_CAN_NOT_READ_SCHEMA_FILE, graphqlSchema);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @DataProvider(name = "schemaFiles")
    public Object[] createSchemaFilesData() {
        return new Object[]{"SchemaWithBasic01Api.graphql", "SchemaWithBasic02Api.graphql",
                "SchemaWithInputsApi.graphql", "SchemaWithMutationApi.graphql", "SchemaWithSubscriptionApi.graphql",
                "SchemaWithBasic03Api.graphql", "SchemaWithEnumApi.graphql", "SchemaWithUnionApi.graphql",
                "SchemaWithInterfaceApi.graphql", "SchemaWithMultipleInterfacesApi.graphql",
                "SchemaWithInterfacesImplementingInterfacesApi.graphql", "SchemaWithMultiDimensionalListsApi.graphql",
                "SchemaWithDefaultParameters01Api.graphql", "SchemaWithDefaultParameters02Api.graphql",
                "SchemaWithDefaultParameters03Api.graphql", "SchemaWithDefaultParameters04Api.graphql",
                "SchemaDocsWithQueryResolversApi.graphql",
                "SchemaDocsWithMutationAndSubscriptionResolversApi.graphql",
                "SchemaDocsWithResolverMultipleLinesApi.graphql", "SchemaDocsWithResolverArgumentsApi.graphql",
                "SchemaDocsWithMultipleLinesApi.graphql", "SchemaDocsWithOutputsApi.graphql",
                "SchemaDocsWithUnionApi.graphql", "SchemaDocsWithEnumApi.graphql", "SchemaDocsWithInputsApi.graphql",
                "SchemaDocsWithInterfacesApi.graphql", "SchemaDocsWithDeprecated01Api.graphql",
                "SchemaCompleteApi.graphql"};
    }

    @Test(description = "Test compilation for all schemas, method - default", dataProvider = "schemaFiles")
    public void testCompilationForAllSchemas(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));

        String[] args = {"-i", schemaPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
            Assert.assertTrue(hasOnlyFuncMustReturnResultErrors(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            String output = e.toString();
            Assert.fail(output);
        }

    }

    @Test(description = "Test compilation for all schemas, method - use records for objects", dataProvider =
            "schemaFiles")
    public void testCompilationForAllSchemasWithUseRecordsForObjects(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));

        String[] args = {"-i", schemaPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--use-records-for-objects"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);

        try {
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
            Assert.assertTrue(hasOnlyFuncMustReturnResultErrors(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            String output = e.toString();
            Assert.fail(output);
        }
    }
}
