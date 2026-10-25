/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.ValidationException;
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
import static io.ballerina.graphql.common.TestUtils.createBallerinaPackage;
import static io.ballerina.graphql.common.TestUtils.getDiagnosticResult;
import static io.ballerina.graphql.common.TestUtils.getValidatedMockServiceProject;

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
        String[] args = {graphqlSchema.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
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
        String[] args = {invalidPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
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
        String[] args = {graphqlSchema.toString(), "-o", outputPath.toString(), "--mode", "service"};
        File file = new File(outputPath.toString());
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format("%s/types.bal (Permission denied)", outputPath);
            createBallerinaPackage(outputPath);
            file.setReadOnly();
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (IOException e) {
            Assert.fail(e.toString());
        } finally {
            // Restores write access so that the class cleanup can delete the package inside.
            file.setWritable(true);
        }
    }

    @Test(
            groups = {"invalid_permission"},
            description = "Test GraphQL command execution with schema file without read permission"
    )
    public void testExecuteWithSchemaFileWithoutReadPermission() {
        Path graphqlSchema = Paths.get(tmpDir.toString(), "schema.graphql");
        String[] args = {graphqlSchema.toString(), "-o", tmpDir.toString(), "--mode", "service"};
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
                "SchemaCompleteApi.graphql", "SchemaWith-HyphenApi.graphql"};
    }

    @Test(
            description = "Test compilation for all schemas with the default object type",
            dataProvider = "schemaFileNames"
    )
    public void testCompilationForAllSchemas(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));
        String[] args = {schemaPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
            Assert.assertTrue(diagnosticResult.errors().isEmpty(),
                    "Unexpected compilation errors: " + diagnosticResult.errors());
        } catch (BLauncherException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(
            description = "Test compilation for all schemas with the record object type",
            dataProvider = "schemaFileNames"
    )
    public void testCompilationForAllSchemasWithUseRecordsForObjects(String file) {
        Path schemaPath = this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", file));
        String[] args = {schemaPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--object-type", "record"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
            Assert.assertTrue(diagnosticResult.errors().isEmpty(),
                    "Unexpected compilation errors: " + diagnosticResult.errors());
        } catch (BLauncherException e) {
            Assert.fail(e.toString());
        }
    }

    @DataProvider(name = "objectTypes")
    public Object[] objectTypes() {
        return new Object[]{"service", "record"};
    }

    @Test(description = "Test that a service generated with configured ID types uses them and compiles",
            dataProvider = "objectTypes")
    public void testIdTypes(String objectType) throws IOException {
        Path configPath = this.resourceDir.resolve(Paths.get("serviceGen", "idTypes", "balGraphQL.toml"));
        String[] args = {configPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service", "--object-type",
                objectType};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, new ExitCodeCaptor());
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        String output = readOutput(true);

        String types = readContent(this.tmpDir.resolve("types.bal"));
        Assert.assertTrue(types.contains("resourcefunctiongetid()returns@graphql:IDint|error;"), types);
        Assert.assertTrue(types.contains("resourcefunctiongetfriendIds()returns@graphql:IDint[]|error"), types);
        Assert.assertTrue(types.contains("resourcefunctiongetaccountId()returns@graphql:IDdecimal?|error"), types);
        Assert.assertTrue(types.contains("remotefunctionupdateProfile(@graphql:IDintid,ProfileInputinput)"), types);
        Assert.assertTrue(types.contains("@graphql:IDstringid;@graphql:IDfloat?score;"), types);
        Assert.assertTrue(types.contains("importballerina/uuid;"), types);
        String service = readContent(this.tmpDir.resolve("service.bal"));
        Assert.assertTrue(service.contains("importballerina/uuid;"), service);
        Assert.assertTrue(service.contains("resourcefunctiongetnode(@graphql:IDuuid:Uuidid)"), service);
        DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
        Assert.assertTrue(diagnosticResult.errors().isEmpty(),
                "Unexpected compilation errors: " + diagnosticResult.errors() + System.lineSeparator() + output);
    }

    @Test(description = "Test that every invalid ID type mapping is reported and nothing is generated")
    public void testInvalidIdTypes() throws IOException {
        Path configPath = this.resourceDir.resolve(Paths.get("serviceGen", "idTypesInvalid", "balGraphQL.toml"));
        String[] args = {configPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        String output = readOutput(true);

        Assert.assertTrue(output.contains(
                "\"Profile.nope\" in [id-types] does not match any field or argument in the schema."), output);
        Assert.assertTrue(output.contains("\"Profile.name\" in [id-types] is not an ID field or argument."), output);
        Assert.assertTrue(output.contains("\"long\" is not a supported ID type for \"Account.id\". Use string, int, "
                + "float, decimal or uuid:Uuid."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertFalse(Files.exists(this.tmpDir.resolve("types.bal")), output);
    }

    @Test(description = "Test that a service generated with DataLoaders wires them and compiles")
    public void testDataLoaders() throws IOException {
        Path configPath = this.resourceDir.resolve(Paths.get("serviceGen", "dataLoaders", "balGraphQL.toml"));
        String[] args = {configPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, new ExitCodeCaptor());
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        String output = readOutput(true);

        String service = readContent(this.tmpDir.resolve("service.bal"));
        Assert.assertTrue(service.contains("importballerina/graphql.dataloader;importballerina/http;"), service);
        Assert.assertTrue(service.contains("context.registerDataLoader(\"profileLoader\",newdataloader:"
                + "DefaultDataLoader(profileLoader));context.registerDataLoader(\"authorLoader\",newdataloader:"
                + "DefaultDataLoader(authorLoader));"), service);
        Assert.assertTrue(service.contains("@graphql:ServiceConfig{contextInit:initContext}"), service);
        Assert.assertTrue(service.contains("functionpreProfile(graphql:Contextcontext,@graphql:IDstringid){"
                + "context.getDataLoader(\"profileLoader\").add(id);}"), service);
        Assert.assertTrue(service.contains("isolatedfunctionauthorLoader(readonly&anydata[]keys)"), service);
        String types = readContent(this.tmpDir.resolve("types.bal"));
        Assert.assertTrue(types.contains("functionpreAuthor(graphql:Contextcontext){}"), types);
        DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
        Assert.assertTrue(diagnosticResult.errors().isEmpty(),
                "Unexpected compilation errors: " + diagnosticResult.errors() + System.lineSeparator() + output);
    }

    @Test(description = "Test that a DataLoader field returning a record returns the loaded value and compiles")
    public void testDataLoadersWithRecords() throws IOException {
        Path configPath = this.resourceDir.resolve(Paths.get("serviceGen", "dataLoadersRecords", "balGraphQL.toml"));
        String[] args = {configPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service", "--object-type",
                "record"};
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, new ExitCodeCaptor());
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        String output = readOutput(true);

        String service = readContent(this.tmpDir.resolve("service.bal"));
        Assert.assertTrue(service.contains("resourcefunctiongetprofile(graphql:Contextcontext,@graphql:IDstringid)"
                + "returnsProfile?|error{returncontext.getDataLoader(\"profileLoader\").get(id);}"), service);
        DiagnosticResult diagnosticResult = getDiagnosticResult(this.tmpDir);
        Assert.assertTrue(diagnosticResult.errors().isEmpty(),
                "Unexpected compilation errors: " + diagnosticResult.errors() + System.lineSeparator() + output);
    }

    @Test(description = "Test that every invalid DataLoader mapping is reported and nothing is generated")
    public void testInvalidDataLoaders() throws IOException {
        Path configPath = this.resourceDir.resolve(Paths.get("serviceGen", "dataLoadersInvalid", "balGraphQL.toml"));
        String[] args = {configPath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        String output = readOutput(true);

        Assert.assertTrue(output.contains(
                "\"Query.nope\" in [dataloaders] does not match any object type field in the schema."), output);
        Assert.assertTrue(output.contains("\"Mutation.addBook\" in [dataloaders] cannot use a DataLoader. Only Query "
                + "fields and object type fields can."), output);
        Assert.assertTrue(output.contains(
                "\"type\" in [dataloaders] is not a valid Ballerina function name for \"Query.profile\"."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertFalse(Files.exists(this.tmpDir.resolve("types.bal")), output);
    }

    @DataProvider(name = "invalidSchemas")
    public Object[][] invalidSchemas() {
        return new Object[][]{
                {"InvalidSchemaWithUnionMemberTypeOfEnum", "The member types of a Union type must all be Object " +
                        "base types. member type Gender in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInterface", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfUnion", "The member types of a Union type must all be Object " +
                        "base types. member type Internal in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInputType", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union PersonInfo is invalid."}
        };
    }

    @Test(description = "Test that an invalid schema is rejected before generation", dataProvider = "invalidSchemas")
    public void testInvalidSchema(String schemaName, String expectedMessage) {
        Path schemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "invalid", schemaName + ".graphql"));
        ValidationException error = Assert.expectThrows(ValidationException.class,
                () -> getValidatedMockServiceProject(schemaPath.toString(), this.tmpDir));
        Assert.assertTrue(error.getMessage().contains(expectedMessage), error.getMessage());
    }
}
