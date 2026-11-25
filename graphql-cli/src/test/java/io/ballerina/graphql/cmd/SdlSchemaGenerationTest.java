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
import java.util.stream.Stream;

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
                {"project_1", "schema_graphql.graphql", "schema_project.graphql"},
                {"service_2.bal", "schema_service_gql.graphql", "schema_service_gql.graphql"},
                {"service_3.bal", "schema_service_3.graphql", "schema_service_3.graphql"},
                {"service_3.bal", "schema_service_3_1.graphql", "schema_service_3_1.graphql"},
                {"service_3.bal", "schema_service_3_2.graphql", "schema_service_3_2.graphql"},
                {"service_4.bal", "schema_service_4.graphql", "schema_service_4.graphql"},
                {"service_4.bal", "schema_service_4_1.graphql", "schema_service_4_1.graphql"},
                {"service_4.bal", "schema_service_4_2.graphql", "schema_service_4_2.graphql"},
                {"project_2", "schema_service_4.graphql", "schema_main.graphql"},
                {"project_2", "schema_service_4_1.graphql", "schema_main_1.graphql"},
                {"project_2", "schema_service_4_2.graphql", "schema_main_2.graphql"},
                {"project_3", "schema_books.graphql", "schema_books.graphql"},
                {"project_3/modules/orders", "schema_orders.graphql", "schema_orders.graphql"},
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
        try {
            // Each run writes to its own directory, so a schema written by another run cannot make the check pass.
            Path outputPath = Files.createTempDirectory(this.tmpDir, "sdl");
            String[] args = {servicePath, "-o", outputPath.toString(), "--force"};
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            Path expectedSchemaFile = resourceDir.resolve(Paths.get("expectedSchemas", expSchema));
            String expectedSchema = readContentWithFormat(expectedSchemaFile);
            Assert.assertTrue(Files.exists(outputPath.resolve(genSchema)), fileNames(outputPath).toString());
            String generatedSchema = readContentWithFormat(outputPath.resolve(genSchema));
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

    @DataProvider(name = "packageInputs")
    public Object[][] packageInputs() {
        return new Object[][]{
                {"project_3", "schema_books.graphql", "schema_orders.graphql"},
                {"project_3/modules/orders", "schema_orders.graphql", "schema_books.graphql"}
        };
    }

    @Test(description = "Test that a package directory gives only its default module and a module directory only "
            + "that module", dataProvider = "packageInputs")
    public void testPackageInputTakesOnlyOneModule(String input, String writtenSchema, String skippedSchema)
            throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "package-input");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService(input), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(Files.exists(outputPath.resolve(writtenSchema)), output);
        Assert.assertFalse(Files.exists(outputPath.resolve(skippedSchema)), output);
    }

    @DataProvider(name = "filesInPackages")
    public Object[][] filesInPackages() {
        return new Object[][]{
                {"project_1/main.bal", "project_1"},
                {"project_2/main.bal", "project_2"},
                {"project_3/modules/orders/orders.bal", "project_3"}
        };
    }

    @Test(description = "Test that a Ballerina file inside a package is rejected", dataProvider = "filesInPackages")
    public void testBalFileInPackageRejected(String serviceFile, String packageDirectory) throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "file-in-package");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = validService(serviceFile);
        Path packageRoot = Path.of(validService(packageDirectory)).toRealPath();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is part of the "
                + "package at \"%s\". Pass the package directory or a module directory instead.", input, packageRoot)),
                output);
        Assert.assertNotEquals(exitCaptor.getExitCode(), 0, output);
        try (Stream<Path> files = Files.list(outputPath)) {
            Assert.assertEquals(files.count(), 0, output);
        }
    }

    @DataProvider(name = "directoriesThatAreNotPackages")
    public Object[][] directoriesThatAreNotPackages() {
        return new Object[][]{{""}, {"project_3/modules"}};
    }

    @Test(description = "Test that a directory that is not a Ballerina package or module is rejected",
            dataProvider = "directoriesThatAreNotPackages")
    public void testDirectoryThatIsNotPackageRejected(String directory) throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "not-package");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = validService(directory);

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is not a Ballerina "
                + "package or module directory.", input)), output);
        Assert.assertNotEquals(exitCaptor.getExitCode(), 0, output);
    }

    @DataProvider(name = "directoryNamesWithFormatCharacters")
    public Object[][] directoryNamesWithFormatCharacters() {
        return new Object[][]{{"a{b}"}, {"o'brien"}, {"{0}"}};
    }

    @Test(description = "Test that braces and quotes in the input path are printed as they are",
            dataProvider = "directoryNamesWithFormatCharacters")
    public void testInputPathWithFormatCharacters(String directoryName) throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "format-characters");
        Path input = Files.createDirectories(outputPath.resolve(directoryName));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is not a Ballerina "
                + "package or module directory.", input)), output);
        Assert.assertNotEquals(exitCaptor.getExitCode(), 0, output);
    }

    @Test(description = "Test that the services in every file of the default module are generated, and that a root "
            + "service is named after its own file")
    public void testPackageInputReadsEveryFile() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "every-file");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService("project_3"), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath),
                List.of("schema_books.graphql", "schema_main.graphql", "schema_reviews_1.graphql"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_main.graphql")).contains("greeting"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_reviews_1.graphql")).contains("stars"), output);
    }

    @Test(description = "Test that the service flag picks one service out of a package")
    public void testServiceFlagWithPackageInput() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "package-service");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService("project_3"), "-o", outputPath.toString(), "--service", "/books");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql"), output);
    }

    @Test(description = "Test that a relative path works for a package and is shown as given in an error")
    public void testRelativeInputPath() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "relative");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String packagePath = displayPath(Path.of(validService("project_1"))).toString();
        String filePath = displayPath(Path.of(validService("project_1/main.bal"))).toString();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                packagePath, "-o", outputPath.toString());
        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_project.graphql"), output);

        ExitCodeCaptor fileExitCaptor = new ExitCodeCaptor();
        String fileOutput = run(fileExitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                filePath, "-o", outputPath.toString());
        Assert.assertTrue(fileOutput.contains(String.format("\"%s\" is part of the package at \"%s\".",
                filePath, Path.of(validService("project_1")).toRealPath())), fileOutput);
        Assert.assertNotEquals(fileExitCaptor.getExitCode(), 0, fileOutput);
    }

    @Test(description = "Test that a package with compilation errors is reported")
    public void testPackageWithCompilationErrors() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "package-errors");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = resourceDir.resolve(Paths.get("graphqlServices", "invalid", "project_1")).toString();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains("Given Ballerina file contains compilation error(s)."), output);
        Assert.assertNotEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that a package without GraphQL services writes nothing")
    public void testPackageWithoutServices() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "package-no-services");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService("project_4"), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.contains("Given Ballerina file does not contain any GraphQL services"), output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that a client or service only flag is a usage error with a package directory")
    public void testClientAndServiceFlagsRejectedWithPackage() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "package-flags");
        String input = validService("project_3");

        ExitCodeCaptor modeExitCaptor = new ExitCodeCaptor();
        String modeOutput = run(modeExitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                input, "-o", outputPath.toString(), "--mode", "client");
        Assert.assertEquals(modeExitCaptor.getExitCode(), 2, modeOutput);
        Assert.assertTrue(modeOutput.contains(String.format("\"client\" mode cannot be used with \"%s\".", input)),
                modeOutput);

        ExitCodeCaptor licenseExitCaptor = new ExitCodeCaptor();
        String licenseOutput = run(licenseExitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                input, "-o", outputPath.toString(), "--license", "license.txt");
        Assert.assertEquals(licenseExitCaptor.getExitCode(), 2, licenseOutput);
        Assert.assertTrue(licenseOutput.contains(String.format("The --license flag only applies to client and "
                + "service generation. It cannot be used with \"%s\".", input)), licenseOutput);
        Assert.assertEquals(fileNames(outputPath), List.of(), licenseOutput);
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

    private static List<String> fileNames(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(file -> file.getFileName().toString()).sorted().toList();
        }
    }

    private static Path displayPath(Path filePath) {
        return Path.of("").toAbsolutePath().relativize(filePath.toAbsolutePath());
    }
}
