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
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.SkipException;
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
                {"service.bal", "service.bal\" does not exist."}
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

    @Test(description = "Test that root services are listed as / when the given service is not found")
    public void testRootServicesListedWhenServiceNotFound() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "root-services");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_3"),
                "-o", outputPath.toString(), "--service", "/missing");

        Assert.assertTrue(output.contains("No Ballerina services found with name \"/missing\" to generate SDL "
                + "schema. These services are available in ballerina file. [/books, /, /]"), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
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
        Path packageRoot = Path.of(validService(packageDirectory)).toAbsolutePath().normalize();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is part of the "
                + "package at \"%s\". Pass the package directory or a module directory instead.", input, packageRoot)),
                output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
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
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
    }

    @DataProvider(name = "directoryNamesWithFormatCharacters")
    public Object[][] directoryNamesWithFormatCharacters() {
        return new Object[][]{{"a{b}"}, {"o'brien"}, {"{0}"}, {"{}"}, {"{id}{x}"}, {"it's {a}'s"}};
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
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
    }

    @Test(description = "Test that braces in the --service value are printed as they are")
    public void testServiceNameWithFormatCharacters() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "format-characters-service");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_3"),
                "-o", outputPath.toString(), "--service", "{}");

        Assert.assertTrue(output.contains("No Ballerina services found with name \"{}\" to generate SDL schema."),
                output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
    }

    @Test(description = "Test that a package in a workspace is generated with the types of the sibling it imports")
    public void testWorkspaceMemberGenerated() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "workspace-member");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("workspace_1/books"),
                "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_books.graphql")).contains("type Book {"), output);
    }

    @Test(description = "Test that an error in the test sources of a sibling package doesn't stop the generation")
    public void testSiblingTestSourcesIgnored() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "workspace-sibling-tests");
        Path workspace = workDir.resolve("workspace");
        FileUtils.copyDirectory(new File(validService("workspace_1")), workspace.toFile());
        Path siblingTests = Files.createDirectories(workspace.resolve(Paths.get("common", "tests")));
        Files.writeString(siblingTests.resolve("broken_test.bal"), "int broken = \"not an int\";\n");
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), workspace.resolve("books").toString(),
                "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql"), output);
    }

    @Test(description = "Test that a workspace root is rejected")
    public void testWorkspaceRootRejected() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "workspace-root");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = validService("workspace_1");

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is a Ballerina "
                + "workspace. Pass the directory of one of its packages instead.", input)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that a Ballerina file inside a package of a workspace is rejected with the package root")
    public void testBalFileInWorkspaceMemberRejected() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "workspace-file");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = validService("workspace_1/books/main.bal");

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("\"%s\" is part of the package at \"%s\".", input,
                Path.of(validService("workspace_1/books")).toAbsolutePath().normalize())), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
    }

    @Test(description = "Test that a directory whose Ballerina.toml has no [package] table is rejected")
    public void testBallerinaTomlWithoutPackageTableRejected() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "no-package-table");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = resourceDir.resolve(Paths.get("graphqlServices", "invalid", "no_package_table")).toString();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is not a Ballerina "
                + "package. Its Ballerina.toml has no [package] table.", input)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @DataProvider(name = "invalidInputs")
    public Object[][] invalidInputs() {
        return new Object[][]{
                {"missing.bal", false, "\"%s\" does not exist."},
                {"missing_directory", false, "\"%s\" does not exist."},
                {"service.bala", true, "File \"%s\" is not a supported input."},
                {"service.txt", true, "File \"%s\" is not a supported input."}
        };
    }

    @Test(description = "Test that an input that does not exist or is not a supported file is an invalid target",
            dataProvider = "invalidInputs")
    public void testInvalidInputIsInvalidTarget(String fileName, boolean create, String message) throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "invalid-input");
        Path input = workDir.resolve(fileName);
        if (create) {
            Files.writeString(input, "");
        }
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains(String.format(message, input)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
        Assert.assertFalse(output.contains("Usage: bal graphql"), output);
    }

    @DataProvider(name = "invalidBallerinaTomls")
    public Object[][] invalidBallerinaTomls() {
        return new Object[][]{
                {"[package\norg = \"test\"\nname = \"shop\"\nversion = \"0.1.0\"\n", "missing close bracket token"},
                {"[package]\norg = \"my-org!\"\nname = \"shop\"\nversion = \"abc\"\n",
                        "invalid 'org' under [package]: 'org' can only contain alphanumerics and underscores; "
                                + "invalid 'version' under [package]: 'version' should be compatible with semver"}
        };
    }

    @Test(description = "Test that the errors in an invalid Ballerina.toml are reported, instead of a missing "
            + "[package] table or missing lock file", dataProvider = "invalidBallerinaTomls")
    public void testInvalidBallerinaTomlReported(String ballerinaToml, String errors) throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "invalid-toml");
        Path packageRoot = Files.createDirectory(workDir.resolve("shop"));
        Files.writeString(packageRoot.resolve("Ballerina.toml"), ballerinaToml);
        Files.copy(Path.of(validService("project_1"), "main.bal"), packageRoot.resolve("main.bal"));
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), packageRoot.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is not valid: %s",
                packageRoot.resolve("Ballerina.toml"), errors)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that a package without a Dependencies.toml is rejected, and that none is created")
    public void testPackageWithoutLockedDependenciesRejected() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "unlocked");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        Path input = resourceDir.resolve(Paths.get("graphqlServices", "invalid", "unlocked_package"));

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: Dependencies are not locked "
                + "for the package \"unlocked_package\". Run 'bal build' to lock the dependencies, then re-run. "
                + "Try: bal build \"%s\"", input.toAbsolutePath().normalize())), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
        Assert.assertFalse(Files.exists(input.resolve("Dependencies.toml")), output);
    }

    @Test(description = "Test that the suggested bal build command quotes a package path that has spaces")
    public void testUnlockedPackagePathWithSpaces() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "unlocked-spaces");
        Path packageRoot = workDir.resolve("my shop");
        FileUtils.copyDirectory(resourceDir.resolve(Paths.get("graphqlServices", "invalid", "unlocked_package"))
                .toFile(), packageRoot.toFile());
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), packageRoot.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains(String.format("Try: bal build \"%s\"", packageRoot)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
    }

    @DataProvider(name = "unreadableFiles")
    public Object[][] unreadableFiles() {
        return new Object[][]{{"", "reviews.bal"}, {"modules/orders", "modules/orders/orders.bal"}};
    }

    @Test(description = "Test that a file the loader cannot read is named, instead of saying the directory is not a "
            + "package", dataProvider = "unreadableFiles")
    public void testUnreadableFileInPackageReported(String input, String unreadableFile) throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "unreadable-file");
        Path packageRoot = workDir.resolve("shop");
        FileUtils.copyDirectory(new File(validService("project_3")), packageRoot.toFile());
        File file = packageRoot.resolve(unreadableFile).toFile();
        if (!file.setReadable(false) || file.canRead()) {
            throw new SkipException("Files cannot be made unreadable here, e.g. when running as root");
        }
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        try {
            String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                    packageRoot.resolve(input).toString(), "-o", outputPath.toString());

            Assert.assertTrue(output.contains(String.format("SDL schema generation failed: Cannot read \"%s\".",
                    file.toPath())), output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        } finally {
            file.setReadable(true);
        }
    }

    @Test(description = "Test that the Dependencies.toml of the package is not changed")
    public void testDependenciesTomlNotChanged() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "locked");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        Path dependenciesToml = Path.of(validService("project_3"), "Dependencies.toml");
        byte[] before = Files.readAllBytes(dependenciesToml);

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_3"),
                "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(Files.readAllBytes(dependenciesToml), before, output);
    }

    @Test(description = "Test that a warning about the dependencies is passed on, and the schema is still generated")
    public void testDependencyWarningPassedOn() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "old-distribution");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        Path dependenciesToml = Path.of(validService("old_distribution"), "Dependencies.toml");
        byte[] before = Files.readAllBytes(dependenciesToml);

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("old_distribution"),
                "-o", outputPath.toString());

        // The current update number is left out, so the check still holds after the Ballerina version changes.
        Assert.assertTrue(output.contains("WARNING [old_distribution] Detected an attempt to compile this package "
                + "using Swan Lake Update "), output);
        Assert.assertTrue(output.contains("However, this package was built using Swan Lake Update 10."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql"), output);
        Assert.assertEquals(Files.readAllBytes(dependenciesToml), before, output);
    }

    @Test(description = "Test that a module directory that is a symbolic link is reported, since the compiler does not "
            + "read its files")
    public void testLinkedModuleDirectoryRejected() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "linked-module");
        Path packageRoot = workDir.resolve("shop");
        FileUtils.copyDirectory(new File(validService("project_3")), packageRoot.toFile());
        Path moduleDirectory = packageRoot.resolve(Paths.get("modules", "orders"));
        FileUtils.moveDirectory(moduleDirectory.toFile(), workDir.resolve("orders").toFile());
        createSymbolicLink(moduleDirectory, workDir.resolve("orders"));
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), moduleDirectory.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is a symbolic link, "
                + "and the Ballerina compiler does not read a module directory that is a symbolic link. Replace the "
                + "link with the module directory.", moduleDirectory)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that a module is found when the package is reached through a symbolic link")
    public void testModuleOfLinkedPackage() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "linked-package");
        Path linkedPackage = workDir.resolve("shop");
        createSymbolicLink(linkedPackage, Path.of(validService("project_3")).toAbsolutePath());
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                linkedPackage.resolve(Paths.get("modules", "orders")).toString(), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_orders.graphql"), output);
    }

    @Test(description = "Test that a package directory that is a symbolic link is generated from the real package")
    public void testLinkedPackageDirectory() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "linked-package-directory");
        Path linkedPackage = workDir.resolve("shop");
        createSymbolicLink(linkedPackage, Path.of(validService("project_3")).toAbsolutePath());
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), linkedPackage.toString(), "-o",
                outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql", "schema_main.graphql",
                "schema_reviews_1.graphql"), output);
    }

    @Test(description = "Test that \"..\" after a symbolic link goes up from the real directory of the link, so the "
            + "package the path points to is generated")
    public void testParentDirectoryAfterLink() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "parent-after-link");
        // link points to real/inner, so link/../proj is real/proj, while removing ".." as text would give proj.
        Files.createDirectories(workDir.resolve(Paths.get("real", "inner")));
        FileUtils.copyDirectory(new File(validService("project_3")), workDir.resolve(Paths.get("real", "proj"))
                .toFile());
        FileUtils.copyDirectory(new File(validService("project_1")), workDir.resolve("proj").toFile());
        createSymbolicLink(workDir.resolve("link"), workDir.resolve(Paths.get("real", "inner")));
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                workDir.resolve(Paths.get("link", "..", "proj")).toString(), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql", "schema_main.graphql",
                "schema_reviews_1.graphql"), output);
    }

    @Test(description = "Test that a package is generated without its test services, even when a test file has errors")
    public void testTestSourcesIgnored() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "with-tests");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_with_tests"),
                "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql"), output);
    }

    @Test(description = "Test that a test file is rejected, since test services are not supported")
    public void testTestFileRejected() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "test-file");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = validService("project_with_tests/tests/service_test.bal");

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains(String.format("SDL schema generation failed: \"%s\" is a test file. Schema "
                + "generation does not support test services.", input)), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 3, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that a standalone file that imports a package outside the distribution gets a clear "
            + "error, since it only sees the packages in the distribution")
    public void testStandaloneFileWithPackageOutsideDistribution() throws IOException {
        Path workDir = Files.createTempDirectory(this.tmpDir, "standalone-import");
        Path input = workDir.resolve("service.bal");
        Files.writeString(input, "import ballerina/graphql;\nimport ballerinax/kafka;\n\n"
                + "service /orders on new graphql:Listener(9090) {\n"
                + "    resource function get id() returns int => 1;\n}\n\n"
                + "kafka:ProducerConfiguration producerConfig = {};\n");
        Path outputPath = Files.createDirectory(workDir.resolve("out"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input.toString(), "-o",
                outputPath.toString());

        Assert.assertTrue(output.contains("SDL schema generation failed: A standalone Ballerina file can only import "
                + "the packages in the Ballerina distribution (cannot resolve module 'ballerinax/kafka'). Put the "
                + "service in a package to use other packages."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertEquals(fileNames(outputPath), List.of(), output);
    }

    @Test(description = "Test that services whose file names clash, ignoring case, each get their own schema file")
    public void testClashingFileNamesKeepEverySchema() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "name-clash");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_name_clash"),
                "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_booKs_1.graphql", "schema_books.graphql",
                "schema_main.graphql", "schema_main_1.graphql"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_main.graphql")).contains("rootField"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_main_1.graphql")).contains("mainField"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_books.graphql")).contains("lowerCaseField"),
                output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_booKs_1.graphql")).contains("mixedCaseField"),
                output);
    }

    @Test(description = "Test that a dry run counts each clashing schema file once")
    public void testClashingFileNamesInDryRun() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "name-clash-dry-run");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_name_clash"),
                "-o", outputPath.toString(), "--dry-run");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.contains("4 to create, 0 to overwrite."), output);
        Assert.assertTrue(output.contains("  create     " + displayPath(outputPath.resolve("schema_main_1.graphql"))),
                output);
    }

    @Test(description = "Test that --service names a service the same as a full run, so writing it with --force "
            + "doesn't overwrite the schema of another service")
    public void testServiceFlagKeepsFullRunFileName() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "name-clash-service");
        ExitCodeCaptor fullRunCaptor = new ExitCodeCaptor();
        String fullRunOutput = run(fullRunCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService("project_name_clash"), "-o", outputPath.toString());
        Assert.assertEquals(fullRunCaptor.getExitCode(), 0, fullRunOutput);

        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), validService("project_name_clash"),
                "-o", outputPath.toString(), "--service", "/main", "--force");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.contains("-- schema_main_1.graphql"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_main.graphql")).contains("rootField"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_main_1.graphql")).contains("mainField"), output);
    }

    @Test(description = "Test that the services of a file that imports ballerina/graphql with another prefix are "
            + "generated together with the services of the other files")
    public void testAliasedGraphqlImport() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "aliased-import");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService("project_aliased_import"), "-o", outputPath.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_books.graphql", "schema_main.graphql",
                "schema_orders.graphql"), output);
        Assert.assertTrue(Files.readString(outputPath.resolve("schema_main.graphql")).contains("stars"), output);
    }

    @Test(description = "Test that --service finds a service when another file imports ballerina/graphql with "
            + "another prefix")
    public void testServiceFlagWithAliasedGraphqlImport() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "aliased-import-service");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"),
                validService("project_aliased_import"), "-o", outputPath.toString(), "--service", "/orders");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(fileNames(outputPath), List.of("schema_orders.graphql"), output);
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
                filePath, Path.of(validService("project_1")).toAbsolutePath().normalize())), fileOutput);
        Assert.assertEquals(fileExitCaptor.getExitCode(), 3, fileOutput);
    }

    @Test(description = "Test that a package with compilation errors is reported")
    public void testPackageWithCompilationErrors() throws IOException {
        Path outputPath = Files.createTempDirectory(this.tmpDir, "package-errors");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String input = resourceDir.resolve(Paths.get("graphqlServices", "invalid", "project_1")).toString();

        String output = run(exitCaptor, recordingPrompt(new ArrayList<>(), "y"), input, "-o", outputPath.toString());

        Assert.assertTrue(output.contains("Given Ballerina file contains compilation error(s)."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
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

    // Skips the test where links cannot be created, e.g. on Windows without the permission for it.
    private static void createSymbolicLink(Path link, Path target) {
        try {
            Files.createSymbolicLink(link, target);
        } catch (IOException | UnsupportedOperationException e) {
            throw new SkipException("Symbolic links are not supported here: " + e.getMessage());
        }
    }

    private static Path displayPath(Path filePath) {
        return Path.of("").toAbsolutePath().relativize(filePath.toAbsolutePath());
    }
}
