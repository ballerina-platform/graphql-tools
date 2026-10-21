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
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_MODE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_MISSING_SCHEMA_FILE;
import static io.ballerina.graphql.common.TestUtils.createBallerinaPackage;

/**
 * This class is used to test the functionality of the GraphQL command.
 */
public class GraphqlCmdTest extends GraphqlTest {

    private static final String SUCCESS_MESSAGE =
            "The service generation process is complete. The following files were written.";
    private static final String CREATED_FILE_PREFIX = "-- ";
    private static final Log log = LogFactory.getLog(GraphqlCmdTest.class);

    @AfterMethod
    public void afterTestCase() {
        File directory = new File(this.tmpDir.toString());
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            Assert.assertNotNull(files);
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    @Test(description = "Test successful graphql command execution")
    public void testExecute() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql.config.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("client.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            String generatedClientContent = readContent(this.tmpDir.resolve("client.bal"));
            String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

            Path expectedClientFile = resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile = resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);
            Assert.assertEquals(generatedClientContent, expectedClientContent);
            Assert.assertEquals(generatedTypesContent, expectedTypesContent);
            Assert.assertEquals(exitCaptor.getExitCode(), 0, "Successful execution should exit with code 0");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with mode flag")
    public void testExecuteWithModeFlag() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            String generatedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

            Path expectedPackageRoot = resourceDir.resolve(Paths.get("serviceGen", "expectedServices"));
            Path expectedServiceFile = expectedPackageRoot.resolve(Paths.get("serviceForSchemaWithSingleObject.bal"));
            Path expectedTypesFile = expectedPackageRoot.resolve(Paths.get("typesWithSingleObjectDefault.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);
            Assert.assertEquals(generatedServiceContent, expectedServiceContent);
            Assert.assertEquals(generatedTypesContent, expectedTypesContent);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testExecutionWithoutModeFlagForGraphqlFileInput() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString()};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            String generatedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

            Path expectedPackageRoot = resourceDir.resolve(Paths.get("serviceGen", "expectedServices"));
            Path expectedServiceFile = expectedPackageRoot.resolve(Paths.get("serviceForSchemaWithSingleObject.bal"));
            Path expectedTypesFile = expectedPackageRoot.resolve(Paths.get("typesWithSingleObjectDefault.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);
            Assert.assertEquals(generatedServiceContent, expectedServiceContent);
            Assert.assertEquals(generatedTypesContent, expectedTypesContent);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with mode and the record object type")
    public void testExecutionWithModeAndRecordObjectType() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithObjectTakingInputArgumentApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--object-type", "record"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();

            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            String generatedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));

            Path expectedPackageRoot = resourceDir.resolve(Paths.get("serviceGen", "expectedServices"));
            Path expectedServiceFile = expectedPackageRoot.resolve(
                    Paths.get("serviceForSchemaWithObjectTakingInputArgument.bal"));
            Path expectedTypesFile = expectedPackageRoot.resolve(
                    Paths.get("typesWithObjectTakingInputArgumentRecordsAllowed.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);
            String expectedTypesContent = readContent(expectedTypesFile);
            Assert.assertEquals(generatedServiceContent, expectedServiceContent);
            Assert.assertEquals(generatedTypesContent, expectedTypesContent);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that service generation lists the files it created")
    public void testExecutePrintsCreatedFiles() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        Path outputPath = this.tmpDir.resolve("created-files-output");
        String[] args = {graphql.toString(), "-o", outputPath.toString(), "--mode", "service"};
        try {
            createBallerinaPackage(outputPath);
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(SUCCESS_MESSAGE), output);
            List<String> createdFiles = createdFiles(output);
            Assert.assertEquals(createdFiles.size(), 2, output);
            Assert.assertTrue(createdFiles.get(0).endsWith("types.bal"), output);
            Assert.assertTrue(createdFiles.get(1).endsWith("service.bal"), output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that a schema file name that cannot be made an identifier is reported clearly")
    public void testExecuteWithInvalidServiceName() {
        Path validSchema = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        try {
            // A file named ".graphql" has an empty name, which cannot be used as the service name.
            Path graphql = Files.createDirectories(this.tmpDir.resolve("nameless-schema")).resolve(".graphql");
            Files.copy(validSchema, graphql);
            String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("GraphQL service types file generation failed: "
                    + "\"\" cannot be used as a Ballerina identifier."), output);
            Assert.assertFalse(Files.exists(this.tmpDir.resolve("types.bal")), output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that an existing output file is skipped without the force flag")
    public void testExecuteSkipsExistingFileWithoutForce() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            Path typesFile = this.tmpDir.resolve("types.bal");
            Files.writeString(typesFile, "// existing");
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("the file already exists. Use -f or --force to overwrite it."));
            Assert.assertEquals(Files.readString(typesFile), "// existing");
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            List<String> createdFiles = createdFiles(output);
            Assert.assertEquals(createdFiles.size(), 1, output);
            Assert.assertTrue(createdFiles.get(0).endsWith("service.bal"), output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "forceFlags")
    public Object[][] forceFlags() {
        return new Object[][]{{"--force"}, {"-f"}};
    }

    @Test(description = "Test that an existing output file is overwritten with the force flag",
            dataProvider = "forceFlags")
    public void testExecuteOverwritesExistingFileWithForce(String forceFlag) {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service", forceFlag};
        try {
            Path typesFile = this.tmpDir.resolve("types.bal");
            Files.writeString(typesFile, "// existing");
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertFalse(output.contains("the file already exists"));
            Path expectedTypesFile = resourceDir.resolve(
                    Paths.get("serviceGen", "expectedServices", "typesWithSingleObjectDefault.bal"));
            Assert.assertEquals(readContent(typesFile), readContent(expectedTypesFile));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "dryRunFlags")
    public Object[][] dryRunFlags() {
        return new Object[][]{{"--dry-run"}, {"-d"}};
    }

    @Test(description = "Test that a dry run lists the files it would create without writing them",
            dataProvider = "dryRunFlags")
    public void testDryRunListsFilesWithoutWriting(String dryRunFlag) {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        Path outputPath = this.tmpDir.resolve("dry-run-output");
        String[] args = {graphql.toString(), "-o", outputPath.toString(), "--mode", "service", dryRunFlag};
        try {
            createBallerinaPackage(outputPath);
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("Dry run: no files were written."));
            Assert.assertTrue(output.contains("2 to create, 0 to overwrite, 0 to skip."));
            Assert.assertFalse(output.contains(SUCCESS_MESSAGE), output);
            Assert.assertFalse(Files.exists(outputPath.resolve("types.bal")));
            Assert.assertFalse(Files.exists(outputPath.resolve("service.bal")));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that a dry run reports an existing file as skipped and leaves it unchanged")
    public void testDryRunReportsExistingFileAsSkipped() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service", "--dry-run"};
        try {
            Path typesFile = this.tmpDir.resolve("types.bal");
            Files.writeString(typesFile, "// existing");
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("1 to create, 0 to overwrite, 1 to skip."));
            Assert.assertEquals(Files.readString(typesFile), "// existing");
            Assert.assertFalse(Files.exists(this.tmpDir.resolve("service.bal")));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that a dry run with the force flag reports an existing file as overwritten")
    public void testDryRunWithForceReportsOverwrite() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service", "--dry-run", "--force"};
        try {
            Path typesFile = this.tmpDir.resolve("types.bal");
            Files.writeString(typesFile, "// existing");
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("1 to create, 1 to overwrite, 0 to skip."));
            Assert.assertEquals(Files.readString(typesFile), "// existing");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that a dry run with an invalid schema shows the error and no report")
    public void testDryRunWithInvalidSchema() {
        Path graphql = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "invalid", "SchemaWithMissingCharApi.graphql"));
        String[] args = {graphql.toString(), "-o", this.tmpDir.toString(), "--mode", "service", "--dry-run"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("GraphQL SDL validation failed."));
            Assert.assertFalse(output.contains("Dry run:"));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that the removed -i flag is rejected")
    public void testExecuteWithRemovedInputFlag() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, "-i", "schema.graphql");
        Assert.assertTrue(output.contains(
                "Unknown option \"-i\". Run \"bal graphql --help\" to see the available options."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
    }

    @Test(description = "Test graphql command execution with more than one input argument")
    public void testExecuteWithMultipleInputArguments() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, "schema.graphql", "extra.graphql");
        Assert.assertTrue(output.contains("Only one input file can be given. Found: schema.graphql, extra.graphql."),
                output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
    }

    @Test(description = "Test graphql command execution with invalid mode argument")
    public void testExecuteWithInvalidModeArgument() {
        Path filePath = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String mode = "invalid-service";
        String[] args = {filePath.toString(), "--mode", mode, "--object-type", "record"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_FOR_INVALID_MODE, mode);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with invalid config file extension")
    public void testExecuteWithInvalidConfigFileExtension() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql.config.yam"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        String message = String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, graphqlConfigYaml);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "invalidFileNameExtension")
    public Object[] createInvalidFileNameExtensionData() {
        return new Object[]{"graphql.config.yam", "service.bl", "schema.grq", "schema.py"};
    }

    @Test(description = "Test graphql command execution with invalid file extensions",
            dataProvider = "invalidFileNameExtension")
    public void testExecuteWithInvalidFileExtensions(String invalidFileNameExtension) {
        Path filePath = resourceDir.resolve(Paths.get("specs", invalidFileNameExtension));
        String[] args = {filePath.toString(), "-o", this.tmpDir.toString()};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "mismatchModeAndFile")
    public Object[][] createMismatchModeAndFileData() {
        return new Object[][]{{"service", "graphql.config.yaml"}, {"client", "service.bal"}};
    }

    @Test(description = "Test that schema is no longer accepted as a mode")
    public void testExecuteWithSchemaMode() {
        Path filePath = resourceDir.resolve(Paths.get("specs", "service.bal"));
        String[] args = {filePath.toString(), "-o", this.tmpDir.toString(), "--mode", "schema"};
        try {
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, new ExitCodeCaptor());
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(String.format(MESSAGE_FOR_INVALID_MODE, "schema")));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(
            description = "Test graphql command execution with mismatch mode and file extension",
            dataProvider = "mismatchModeAndFile"
    )
    public void testExecuteWithMismatchModeAndFileExtension(String mode, String fileName) {
        Path filePath = resourceDir.resolve(Paths.get("specs", fileName));
        String[] args = {filePath.toString(), "-o", this.tmpDir.toString(), "--mode", mode};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(Constants.MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION, mode, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "objectTypeWithNonServiceInput")
    public Object[][] createObjectTypeWithNonServiceInputData() {
        return new Object[][]{{"graphql.config.yaml"}, {"service.bal"}};
    }

    @Test(
            description = "Test graphql command execution with an object type for an input that is not a service",
            dataProvider = "objectTypeWithNonServiceInput"
    )
    public void testExecuteWithObjectTypeForNonServiceInput(String fileName) {
        Path filePath = resourceDir.resolve(Paths.get("specs", fileName));
        String[] args = new String[]{filePath.toString(), "-o", this.tmpDir.toString(), "--object-type", "record"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("only applies to service generation"), "Unexpected output: " + output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with an unsupported object type value")
    public void testExecuteWithInvalidObjectType() {
        Path filePath = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] args = {filePath.toString(), "-o", this.tmpDir.toString(), "--object-type", "banana"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("\"banana\" is not a supported value for --object-type"),
                    "Unexpected output: " + output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that the removed -r flag is rejected")
    public void testExecuteWithRemovedRecordsFlag() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, "schema.graphql", "-r");
        Assert.assertTrue(output.contains(
                "Unknown option \"-r\". Run \"bal graphql --help\" to see the available options."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
    }

    @Test(description = "Test graphql command execution with invalid schema file path")
    public void testExecuteWithInvalidSchemaFilePath() {
        Path filePath = resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", "schema.graphql"));
        String[] args = {filePath.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            String message = String.format(MESSAGE_MISSING_SCHEMA_FILE, filePath);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(message));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with empty config file")
    public void testExecuteWithEmptyConfigFile() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "empty.graphql.config.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(MESSAGE_FOR_EMPTY_CONFIGURATION_FILE));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with invalid config file content")
    public void testExecuteWithInvalidConfigFileContent() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "invalid.graphql.config.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test successful graphql command execution with projects in config file")
    public void testExecuteWithProjects() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql-config-with-projects.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();

            Path countryModulePath = this.tmpDir.resolve("modules").resolve("country");
            Assert.assertTrue(Files.exists(countryModulePath));
            Assert.assertTrue(Files.isDirectory(countryModulePath));
            Assert.assertTrue(Files.exists(countryModulePath.resolve("client.bal")));
            String generatedClientContent = readContent(countryModulePath.resolve("client.bal"));
            Assert.assertTrue(Files.exists(countryModulePath.resolve("types.bal")));
            String generatedTypesContent = readContent(countryModulePath.resolve("types.bal"));

            Path expectedClientFile = resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile = resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);
            Assert.assertEquals(generatedClientContent, expectedClientContent);
            Assert.assertEquals(generatedTypesContent, expectedTypesContent);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test successful graphql command execution with schema URL in config file", enabled = false)
    public void testExecuteWithSchemaUrl() {
        Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql-config-with-schema-url.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            Path expectedClientFile = resourceDir.resolve(Paths.get("expectedGenCode", "client.bal"));
            Path expectedTypesFile = resourceDir.resolve(Paths.get("expectedGenCode", "types.bal"));
            String expectedClientContent = readContent(expectedClientFile);
            String expectedTypesContent = readContent(expectedTypesFile);
            if (Files.exists(this.tmpDir.resolve("client.bal")) && Files.exists(this.tmpDir.resolve("types.bal"))) {
                String generatedClientContent = readContent(this.tmpDir.resolve("client.bal"));
                String generatedTypesContent = readContent(this.tmpDir.resolve("types.bal"));
                Assert.assertEquals(generatedClientContent, expectedClientContent);
                Assert.assertEquals(generatedTypesContent, expectedTypesContent);
            } else {
                Assert.fail("Code generation failed. : " + readOutput(true));
            }
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test successful graphql command execution with invalid introspection URL in config file")
    public void testExecuteWithInvalidIntrospectionUrl() {
        Path graphqlConfigYaml =
                resourceDir.resolve(Paths.get("specs", "graphql-config-with-invalid-introspection-url.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("Failed to retrieve SDL."));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test error message of unsupported operations in schema")
    public void testExecuteWithUnsupportedOperations1() {
        Path graphqlConfigYaml =
                resourceDir.resolve(Paths.get("specs", "graphql-schema-with-unsupported-operations.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(
                    "The provided schema includes operations that are not supported by the client generation."));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test error message of unsupported operations in schema")
    public void testExecuteWithUnsupportedOperations2() {
        Path graphqlConfigYaml =
                resourceDir.resolve(Paths.get("specs", "graphql-schema-with-subscription.yaml"));
        String[] args = {graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains(
                    "The provided schema includes operations that are not supported by the client generation."));
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command with no args")
    public void testExecuteWithNoArgs() {
        String[] args = {};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("SYNOPSIS"));
            Assert.assertTrue(output.contains("--force"));
            Assert.assertTrue(output.contains("--dry-run"));
            Assert.assertTrue(output.contains("--object-type"));
            Assert.assertFalse(output.contains("--input"));
            Assert.assertEquals(exitCaptor.getExitCode(), 2, "No arguments should exit with code 2");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command with help flag")
    public void testExecuteWithHelpFlag() {
        String[] args = {"-h"};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        try {
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("SYNOPSIS"));
            Assert.assertTrue(output.contains("-f, --force"), output);
            Assert.assertTrue(output.contains("-d, --dry-run"), output);
            Assert.assertTrue(output.contains("--module"), output);
            Assert.assertTrue(output.contains("--object-type"));
            Assert.assertFalse(output.contains("--input"));
            Assert.assertEquals(exitCaptor.getExitCode(), 0, "Help flag should exit with code 0");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that service generation outside a Ballerina package is rejected")
    public void testServiceGenerationOutsidePackage() throws IOException {
        Path outputPath = Files.createTempDirectory("no-package");
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            String output = run(exitCaptor, singleObjectSchema(), "-o", outputPath.toString(), "--mode", "service");
            Assert.assertTrue(output.contains(notAPackage(outputPath)), output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
            Assert.assertFalse(Files.exists(outputPath.resolve("types.bal")), output);
        } finally {
            FileUtils.deleteDirectory(outputPath.toFile());
        }
    }

    @Test(description = "Test that client generation outside a Ballerina package is rejected")
    public void testClientGenerationOutsidePackage() throws IOException {
        Path outputPath = Files.createTempDirectory("no-package");
        try {
            Path graphqlConfigYaml = resourceDir.resolve(Paths.get("specs", "graphql.config.yaml"));
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            String output = run(exitCaptor, graphqlConfigYaml.toString(), "-o", outputPath.toString());
            Assert.assertTrue(output.contains(notAPackage(outputPath)), output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
            Assert.assertFalse(Files.exists(outputPath.resolve("client.bal")), output);
        } finally {
            FileUtils.deleteDirectory(outputPath.toFile());
        }
    }

    @Test(description = "Test that a folder inside a Ballerina package is rejected as the output")
    public void testGenerationInPackageSubfolder() throws IOException {
        Path subfolder = Files.createDirectories(this.tmpDir.resolve("subfolder"));
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            String output = run(exitCaptor, singleObjectSchema(), "-o", subfolder.toString(), "--mode", "service");
            Assert.assertTrue(output.contains(notAPackage(subfolder)), output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
            Assert.assertFalse(Files.exists(subfolder.resolve("types.bal")), output);
        } finally {
            FileUtils.deleteDirectory(subfolder.toFile());
        }
    }

    @Test(description = "Test that a missing output folder outside a Ballerina package is not created")
    public void testGenerationToMissingFolderOutsidePackage() throws IOException {
        Path parent = Files.createTempDirectory("no-package");
        Path outputPath = parent.resolve("output");
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            String output = run(exitCaptor, singleObjectSchema(), "-o", outputPath.toString(), "--mode", "service");
            Assert.assertTrue(output.contains(notAPackage(outputPath)), output);
            Assert.assertFalse(Files.exists(outputPath), output);
        } finally {
            FileUtils.deleteDirectory(parent.toFile());
        }
    }

    @Test(description = "Test that the module flag writes the generated files into that module")
    public void testGenerationIntoModule() throws IOException {
        Path modulePath = this.tmpDir.resolve(Paths.get("modules", "books"));
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            String output = run(exitCaptor, singleObjectSchema(), "-o", this.tmpDir.toString(), "--mode", "service",
                    "--module", "books");
            Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
            Assert.assertTrue(Files.exists(modulePath.resolve("types.bal")), output);
            Assert.assertTrue(Files.exists(modulePath.resolve("service.bal")), output);
            Assert.assertFalse(Files.exists(this.tmpDir.resolve("types.bal")), output);
        } finally {
            FileUtils.deleteDirectory(this.tmpDir.resolve("modules").toFile());
        }
    }

    @Test(description = "Test that an invalid module name is rejected")
    public void testGenerationWithInvalidModuleName() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, singleObjectSchema(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--module", "my-module");
        Assert.assertTrue(output.contains("\"my-module\" is not a valid module name."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertFalse(Files.exists(this.tmpDir.resolve("modules")), output);
    }

    @Test(description = "Test that an unknown option gives a clear error")
    public void testUnknownOption() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, singleObjectSchema(), "-o", this.tmpDir.toString(), "-m", "service");
        Assert.assertTrue(output.contains(
                "Unknown option \"-m\". Run \"bal graphql --help\" to see the available options."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertFalse(Files.exists(this.tmpDir.resolve("types.bal")), output);
    }

    @Test(description = "Test that the module flag is rejected for schema generation")
    public void testModuleWithSchemaGeneration() throws IOException {
        String service = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, service, "-o", this.tmpDir.toString(), "--module", "books");
        Assert.assertTrue(output.contains("The --module flag only applies to client and service generation. "
                + "It cannot be used with \"" + service + "\"."), output);
        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
    }

    private String run(ExitCodeCaptor exitCaptor, String... args) throws IOException {
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        return readOutput(true);
    }

    private String singleObjectSchema() {
        return resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql")).toString();
    }

    private static String notAPackage(Path outputPath) {
        return "\"" + outputPath.toAbsolutePath().normalize() + "\" is not a Ballerina package. "
                + "Run the command from the package root, or use -o to point to it.";
    }

    private static List<String> createdFiles(String output) {
        return output.lines().filter(line -> line.startsWith(CREATED_FILE_PREFIX)).toList();
    }
}
