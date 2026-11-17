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

import io.ballerina.graphql.cmd.generator.OverwritePrompt;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to test the functionality of the GraphQL command.
 */
public class SdlSchemaGenerationTest extends GraphqlTest {
    private static final String SERVICE_FILE = "service_1.bal";
    private static final String SCHEMA_FILE = "schema_graphql.graphql";
    private static final String EXISTING_CONTENT = "# existing";
    private static final String OVERWRITE_QUESTION = "Overwrite these file(s)? [y/N] ";

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
        String[] args = {servicePath, "-o", this.tmpDir.toString(), "--force"};
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
        String[] args = {servicePath, "-o", this.tmpDir.toString()};
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
        String[] args = {servicePath, "-o", this.tmpDir.toString(), "--service", "/service/gql"};
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
            String[] args = {servicePath.toString(), "-o", outPath.toString()};
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, this.tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format("Cannot write to \"%s\". Check its permissions.",
                    Path.of("").toAbsolutePath().relativize(outPath.toAbsolutePath()));
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message), output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
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
            String[] args = {graphqlService.toString(), "-o", tmpDir.toString()};
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

    @DataProvider(name = "dryRunFlags")
    public Object[][] dryRunFlags() {
        return new Object[][]{{"--dry-run"}, {"-n"}};
    }

    @Test(description = "Test that a dry run lists the schema file it would create without writing it",
            dataProvider = "dryRunFlags")
    public void testDryRunListsSchemaWithoutWriting(String dryRunFlag) throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "dry-run");
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(questions, "y"),
                validService(SERVICE_FILE), "-o", outputPath.toString(), dryRunFlag);

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.contains("Dry run: no files were written."), output);
        Assert.assertTrue(output.contains("  create     " + displayPath(outputPath.resolve(SCHEMA_FILE))), output);
        Assert.assertTrue(output.contains("1 to create, 0 to overwrite."), output);
        Assert.assertTrue(questions.isEmpty(), output);
        Assert.assertFalse(Files.exists(outputPath.resolve(SCHEMA_FILE)), output);
    }

    @Test(description = "Test that an existing schema file is overwritten when the user agrees")
    public void testExistingSchemaOverwrittenWhenAgreed() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "prompt-yes");
        Path schemaFile = Files.writeString(outputPath.resolve(SCHEMA_FILE), EXISTING_CONTENT);
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(questions, "y"),
                validService(SERVICE_FILE), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(questions, List.of(OVERWRITE_QUESTION), output);
        Assert.assertEquals(readContentWithFormat(schemaFile), expectedSchema(SCHEMA_FILE));
    }

    @Test(description = "Test that an existing schema file is kept when the user declines")
    public void testExistingSchemaKeptWhenDeclined() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "prompt-no");
        Path schemaFile = Files.writeString(outputPath.resolve(SCHEMA_FILE), EXISTING_CONTENT);
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(questions, "n"),
                validService(SERVICE_FILE), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 4, output);
        Assert.assertEquals(questions, List.of(OVERWRITE_QUESTION), output);
        Assert.assertTrue(output.contains("No files were written."), output);
        Assert.assertEquals(Files.readString(schemaFile), EXISTING_CONTENT);
    }

    @Test(description = "Test that an existing schema file is kept when the run cannot ask")
    public void testExistingSchemaKeptWhenNotInteractive() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "not-interactive");
        Path schemaFile = Files.writeString(outputPath.resolve(SCHEMA_FILE), EXISTING_CONTENT);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, question -> Optional.empty(),
                validService(SERVICE_FILE), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 4, output);
        Assert.assertTrue(output.contains("The following file(s) already exist." + System.lineSeparator()
                + "  " + displayPath(schemaFile)), output);
        Assert.assertTrue(output.contains("Cannot ask to overwrite these file(s) in a non-interactive run. "
                + "Use --force to overwrite them."), output);
        Assert.assertEquals(Files.readString(schemaFile), EXISTING_CONTENT);
    }

    @Test(description = "Test that an existing schema file is overwritten with the force flag without asking")
    public void testExistingSchemaOverwrittenWithForce() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "force");
        Path schemaFile = Files.writeString(outputPath.resolve(SCHEMA_FILE), EXISTING_CONTENT);
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(questions, "n"),
                validService(SERVICE_FILE), "-o", outputPath.toString(), "--force");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(questions.isEmpty(), output);
        Assert.assertEquals(readContentWithFormat(schemaFile), expectedSchema(SCHEMA_FILE));
    }

    private String run(ExitCodeCaptor exitCaptor, OverwritePrompt overwritePrompt, String... args)
            throws IOException {
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, printStream, this.tmpDir, exitCaptor, overwritePrompt);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        return readOutput(true);
    }

    private static OverwritePrompt recordingPrompt(List<String> questions, String answer) {
        return question -> {
            questions.add(question);
            return Optional.of(answer);
        };
    }

    private String validService(String serviceFile) {
        return resourceDir.resolve(Paths.get("graphqlServices", "valid", serviceFile)).toString();
    }

    private String expectedSchema(String schemaFile) throws IOException {
        return readContentWithFormat(resourceDir.resolve(Paths.get("expectedSchemas", schemaFile)));
    }

    private static Path displayPath(Path filePath) {
        return Path.of("").toAbsolutePath().relativize(filePath.toAbsolutePath());
    }
}
