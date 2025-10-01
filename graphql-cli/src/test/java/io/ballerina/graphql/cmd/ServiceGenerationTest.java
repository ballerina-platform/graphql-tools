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
import org.testng.annotations.AfterMethod;
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

    @AfterMethod
    public void afterTestCase() {
        File directory = new File(this.tmpDir.toString());
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isFile() && !file.getName().endsWith("Ballerina.toml")) {
                    file.delete();
                }
            }
        }
    }

    @Test(description = "Test graphql command execution for service generation with invalid schema")
    public void testExecuteWithInvalidSchemaForServiceGen() {
        Path graphqlSchema = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "invalid", "SchemaWithMissingCharApi.graphql"));
        String[] args = {"-i", graphqlSchema.toString(), "-o", this.tmpDir.toString(), "-m", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String output;
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains("GraphQL SDL validation failed."));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(description = "Test graphql command execution with invalid schema input file path")
    public void testExecuteWithSchemaInvalidFilePath() {
        Path invalidPath =
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "invalid", "Schema.graphql"));
        String[] args = {"-i", invalidPath.toString(), "-o", this.tmpDir.toString(), "-m", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_MISSING_SCHEMA_FILE, invalidPath);
            String output;
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(groups = {"invalid_permission"}, description = "Test GraphQL command execution with readonly output path")
    public void testExecuteWithReadOnlyOutputPath() {
        Path graphqlSchema = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        Path outputPath = Paths.get(tmpDir.toString(), "new");
        String[] args = {"-i", graphqlSchema.toString(), "-o", outputPath.toString(), "-m", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format("%s/types.bal (Permission denied)", outputPath);
            Files.createDirectories(outputPath);
            File file = new File(outputPath.toString());
            file.setReadOnly();
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(
            groups = {"invalid_permission"},
            description = "Test GraphQL command execution with schema file without read permission"
    )
    public void testExecuteWithSchemaFileWithoutReadPermission() {
        Path graphqlSchema = Paths.get(tmpDir.toString(), "schema.graphql");
        String[] args = {"-i", graphqlSchema.toString(), "-o", tmpDir.toString(), "-m", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_CAN_NOT_READ_SCHEMA_FILE, graphqlSchema);
            Files.createFile(graphqlSchema);
            File file = new File(graphqlSchema.toString());
            file.setReadable(false);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.toString());
        }
    }

    @DataProvider(name = "schemaFileNames")
    public Object[] getSchemaFileNames() {
        return new Object[]{"SchemaWithSingleObjectApi.graphql", "SchemaWithMultipleObjectsApi.graphql",
                "SchemaWithInputsApi.graphql", "SchemaWithMutationApi.graphql", "SchemaWithSubscriptionApi.graphql",
                "SchemaWithObjectTakingInputArgumentApi.graphql", "SchemaWithEnumApi.graphql",
                "SchemaWithUnionApi.graphql", "SchemaWithInterfaceApi.graphql",
                "SchemaWithMultipleInterfacesApi.graphql", "SchemaWithInterfacesImplementingInterfacesApi.graphql",
                "SchemaWithMultiDimensionalListsApi.graphql", "SchemaWithDefaultParameters01Api.graphql",
                "SchemaWithDefaultParameters02Api.graphql", "SchemaWithDefaultParameters03Api.graphql",
                "SchemaWithDefaultParameters04Api.graphql", "SchemaDocsWithQueryResolversApi.graphql",
                "SchemaDocsWithMutationAndSubscriptionResolversApi.graphql",
                "SchemaDocsWithResolverMultipleLinesApi.graphql", "SchemaDocsWithResolverArgumentsApi.graphql",
                "SchemaDocsWithMultipleLinesApi.graphql", "SchemaDocsWithObjectsApi.graphql",
                "SchemaDocsWithUnionApi.graphql", "SchemaDocsWithEnumApi.graphql", "SchemaDocsWithInputsApi.graphql",
                "SchemaDocsWithInterfacesApi.graphql", "SchemaDocsWithDeprecated01Api.graphql",
                "SchemaCompleteApi.graphql"};
    }

    @Test(
            description = "Test compilation for all schemas without use-records-for-objects flag",
            dataProvider = "schemaFileNames"
    )
    public void testCompilationForAllSchemas(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));
        String[] args = {"-i", schemaPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
            Assert.assertTrue(hasOnlyFuncMustReturnResultErrors(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(
            description = "Test compilation for all schemas with use-records-for-objects flag",
            dataProvider = "schemaFileNames"
    )
    public void testCompilationForAllSchemasWithUseRecordsForObjects(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));
        String[] args = {"-i", schemaPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--use-records-for-objects"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
            Assert.assertTrue(hasOnlyFuncMustReturnResultErrors(diagnosticResult.errors()));
        } catch (BLauncherException e) {
            Assert.fail(e.toString());
        }
    }
}
