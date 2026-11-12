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

import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.generator.OverwritePrompt;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.projects.DiagnosticResult;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.graphql.common.TestUtils.createBallerinaPackage;
import static io.ballerina.graphql.common.TestUtils.getDiagnosticResult;

/**
 * Tests adding a license header to the generated files with the --license flag through the command.
 */
public class GraphqlLicenseCmdTest extends GraphqlTest {

    private static final String SERVICE_CONFIG = """
            [schema]
            source = "file"
            path = "schema.graphql"
            """;
    private static final String CLIENT_CONFIG = """
            documents = ["query.graphql"]

            [schema]
            source = "file"
            path = "schema.graphql"
            """;
    private static final String SCHEMA = """
            type Query {
              book(id: String!): Book
              books: [Book!]!
            }

            type Book {
              title: String!
            }
            """;
    private static final String QUERY = """
            query getBooks {
              books {
                title
              }
            }
            """;
    private static final String LICENSE = """
            // Copyright (c) 2026 My Company. All Rights Reserved.
            //
            // Licensed under the Apache License, Version 2.0.
            """;
    private static final String HEADER = LICENSE + "\n";
    private static final String LICENSE_FILE = "license.txt";
    private static final String MERGE_HINT = "Use --merge to keep the code in these file(s).";
    private static final String ERROR_CANNOT_READ =
            "Cannot read the license file \"%s\". Check that it exists and is readable.";

    private Path packageRoot;

    @BeforeMethod
    public void createLicensePackage() throws IOException {
        this.packageRoot = createBallerinaPackage(Files.createTempDirectory(this.tmpDir, "license"));
        Files.writeString(this.packageRoot.resolve("schema.graphql"), SCHEMA);
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), SERVICE_CONFIG);
    }

    @AfterMethod
    public void deleteLicensePackage() throws IOException {
        FileUtils.deleteDirectory(this.packageRoot.toFile());
    }

    @Test(description = "Test that the license is added at the top of each generated service file and compiles")
    public void testServiceWithLicense() throws IOException {
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), HEADER);
        assertHeader(this.packageRoot.resolve("types.bal"), HEADER);
        assertCompiles();
    }

    @Test(description = "Test that the license is added at the top of each generated client file")
    public void testClientWithLicense() throws IOException {
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), CLIENT_CONFIG);
        Files.writeString(this.packageRoot.resolve("query.graphql"), QUERY);
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = runWithMode(exitCaptor, "client", "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<Path> balFiles = balFiles(this.packageRoot);
        Assert.assertTrue(balFiles.contains(this.packageRoot.resolve("client.bal")), balFiles.toString());
        for (Path balFile : balFiles) {
            assertHeader(balFile, HEADER);
        }
    }

    @Test(description = "Test that the license is added to the files generated into a module")
    public void testModuleWithLicense() throws IOException {
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--module", "books", "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Path moduleRoot = this.packageRoot.resolve(Paths.get("modules", "books"));
        assertHeader(moduleRoot.resolve("service.bal"), HEADER);
        assertHeader(moduleRoot.resolve("types.bal"), HEADER);
        assertCompiles();
    }

    @Test(description = "Test that a license file that does not exist fails before anything is written")
    public void testMissingLicenseFile() throws IOException {
        Path license = this.packageRoot.resolve("lisence.txt");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains(String.format(ERROR_CANNOT_READ, license)), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that a directory given as the license file fails before anything is written")
    public void testLicenseDirectory() throws IOException {
        Path license = Files.createDirectory(this.packageRoot.resolve("license"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains(String.format(ERROR_CANNOT_READ, license)), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that a license line that is not a comment fails and names the line, so the "
            + "generated files always compile")
    public void testLicenseWithNonCommentLine() throws IOException {
        Path license = writeLicense("// Copyright (c) 2026 My Company.\n\nAll rights reserved.\n");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains("The license file \"" + license + "\" can only contain comment lines and "
                + "blank lines. Line 3 is not a comment: \"All rights reserved.\""), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that block and documentation comments are rejected, since they are not valid at the "
            + "top of a Ballerina file")
    public void testLicenseWithOtherCommentStyles() throws IOException {
        for (String line : List.of("/* Copyright 2026 */", "# Copyright 2026")) {
            Path license = writeLicense(line + "\n");
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

            String output = run(exitCaptor, "--license", license.toString());

            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
            Assert.assertTrue(output.contains("Line 1 is not a comment: \"" + line + "\""), output);
            Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
        }
    }

    @Test(description = "Test that a line indented or filled with a space Ballerina does not accept, such as an em "
            + "space from a word processor, is reported instead of breaking the build")
    public void testLicenseWithUnicodeSpace() throws IOException {
        for (String line : List.of("\u2003// Copyright 2026", "\u2003")) {
            Path license = writeLicense("// My Company\n" + line + "\n");
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

            String output = run(exitCaptor, "--license", license.toString());

            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
            // The em space is shown as its code, so the line does not look blank in the message.
            Assert.assertTrue(output.contains("The license file \"" + license + "\" can only contain comment "
                    + "lines and blank lines. Line 2 is not a comment: \"" + line.replace("\u2003", "\\u2003")
                    + "\"."), output);
            Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
        }
    }

    @Test(description = "Test that tabs and spaces around a comment are accepted")
    public void testLicenseWithTabsAndSpaces() throws IOException {
        String content = "// My Company\n\t// Copyright 2026  \n \t \n";
        Path license = writeLicense(content);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), "// My Company\n\t// Copyright 2026\n\n");
        assertCompiles();
    }

    @Test(description = "Test that a form feed, which Ballerina accepts as whitespace, is allowed before a comment "
            + "and on a line of its own, such as a page break in a GNU-style notice")
    public void testLicenseWithFormFeed() throws IOException {
        String content = "// Copyright 2026 My Company.\n\f\n\f// Licensed under the GNU GPL.\n";
        Path license = writeLicense(content);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), content + "\n");
        assertCompiles();
    }

    @Test(description = "Test that a character at the end of the last line that is not Ballerina whitespace, such "
            + "as an em space inside the comment, is kept as written")
    public void testLicenseKeepsTrailingNonBallerinaWhitespace() throws IOException {
        Path license = writeLicense("// Copyright 2026 My Company.\u2003\n\n");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), "// Copyright 2026 My Company.\u2003\n\n");
    }

    @Test(description = "Test that a path the file system cannot name fails cleanly instead of crashing")
    public void testInvalidLicensePath() throws IOException {
        String license = "lic\u0000ense.txt";
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license);

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains(String.format(ERROR_CANNOT_READ, license)), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that an empty license path is a usage error, not the current directory")
    public void testEmptyLicensePath() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", "");

        assertUsageError(output, exitCaptor);
        Assert.assertTrue(output.contains("The --license flag needs the path of a license file."), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that the license can also be given in the --license=<path> form")
    public void testLicenseWithEqualsForm() throws IOException {
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license=" + license);

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), HEADER);
    }

    @Test(description = "Test that a license with a character in a non-UTF-8 encoding, such as a copyright sign saved "
            + "as Windows-1252, fails with a clear message")
    public void testLicenseInOtherEncoding() throws IOException {
        Path license = this.packageRoot.resolve(LICENSE_FILE);
        Files.writeString(license, "// Copyright © 2026 My Company.\n", Charset.forName("windows-1252"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains("The license file \"" + license + "\" is not a UTF-8 text file."), output);
    }

    @Test(description = "Test that a license file that is not UTF-8 text fails before anything is written")
    public void testLicenseNotText() throws IOException {
        Path license = this.packageRoot.resolve(LICENSE_FILE);
        Files.write(license, new byte[]{(byte) 0xC3, (byte) 0x28, (byte) 0xFF});
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains("The license file \"" + license + "\" is not a UTF-8 text file."), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that an empty license file or one with only blank lines adds no header")
    public void testEmptyLicense() throws IOException {
        for (String content : List.of("", "\n\n   \n")) {
            Path license = writeLicense(content);
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

            String output = run(exitCaptor, "--force", "--license", license.toString());

            Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
            Assert.assertTrue(Files.readString(this.packageRoot.resolve("service.bal")).startsWith("import "),
                    Files.readString(this.packageRoot.resolve("service.bal")));
        }
    }

    @Test(description = "Test that Windows line endings, a byte order mark and trailing blank lines are normalized, "
            + "so exactly one blank line follows the license")
    public void testLicenseNormalized() throws IOException {
        Path license = this.packageRoot.resolve(LICENSE_FILE);
        Files.writeString(license, "\uFEFF// Copyright (c) 2026 My Company.\r\n//\r\n// All rights reserved."
                + "\r\n\r\n\r\n", StandardCharsets.UTF_8);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"),
                "// Copyright (c) 2026 My Company.\n//\n// All rights reserved.\n\n");
    }

    @Test(description = "Test that a license without a final line break still gets one blank line after it")
    public void testLicenseWithoutFinalLineBreak() throws IOException {
        Path license = writeLicense("// Copyright (c) 2026 My Company.");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), "// Copyright (c) 2026 My Company.\n\n");
    }

    @Test(description = "Test that indented comments and blank lines inside the license are kept as written")
    public void testLicenseKeptVerbatim() throws IOException {
        String content = "// Copyright (c) 2026 My Company.\n\n    // Licensed under the Apache License.\n";
        Path license = writeLicense(content);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), content + "\n");
        assertCompiles();
    }

    @Test(description = "Test that regenerating with the same license gives byte-identical files")
    public void testLicenseIsDeterministic() throws IOException {
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor firstCaptor = new ExitCodeCaptor();
        String firstOutput = run(firstCaptor, "--license", license.toString());
        Assert.assertEquals(firstCaptor.getExitCode(), 0, firstOutput);
        String service = Files.readString(this.packageRoot.resolve("service.bal"));
        String types = Files.readString(this.packageRoot.resolve("types.bal"));
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--force", "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(Files.readString(this.packageRoot.resolve("service.bal")), service);
        Assert.assertEquals(Files.readString(this.packageRoot.resolve("types.bal")), types);
    }

    @Test(description = "Test that overwriting existing files adds the license to them")
    public void testOverwriteAddsLicense() throws IOException {
        ExitCodeCaptor generateCaptor = new ExitCodeCaptor();
        String generateOutput = run(generateCaptor);
        Assert.assertEquals(generateCaptor.getExitCode(), 0, generateOutput);
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--force", "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), HEADER);
    }

    @Test(description = "Test that overwriting with a license does not suggest --merge, since the two cannot be "
            + "used together")
    public void testOverwriteWithLicenseDoesNotSuggestMerge() throws IOException {
        ExitCodeCaptor generateCaptor = new ExitCodeCaptor();
        String generateOutput = run(generateCaptor);
        Assert.assertEquals(generateCaptor.getExitCode(), 0, generateOutput);
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor dryRunCaptor = new ExitCodeCaptor();

        String dryRunOutput = run(dryRunCaptor, "--dry-run", "--license", license.toString());

        Assert.assertEquals(dryRunCaptor.getExitCode(), 0, dryRunOutput);
        Assert.assertFalse(dryRunOutput.contains(MERGE_HINT), dryRunOutput);

        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = runWithPrompt(exitCaptor, answering("y", questions), "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(questions, List.of("Overwrite these file(s)? [y/N] "), output);
        Assert.assertFalse(output.contains(MERGE_HINT), output);
        assertHeader(this.packageRoot.resolve("service.bal"), HEADER);
    }

    @Test(description = "Test that --license with --merge is a usage error, so a merge never changes a header")
    public void testLicenseWithMerge() throws IOException {
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--merge", "--license", license.toString());

        assertUsageError(output, exitCaptor);
        Assert.assertTrue(output.contains("The --license flag cannot be used with --merge. Run without --merge to "
                + "regenerate the files with the license header."), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that --license with schema generation is a usage error")
    public void testLicenseWithSchemaGeneration() throws IOException {
        Path license = writeLicense(LICENSE);
        Path service = Files.writeString(this.tmpDir.resolve("license_service.bal"), "");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, printStream, this.packageRoot, exitCaptor, failIfAsked());
        new CommandLine(graphqlCmd).parseArgs(service.toString(), "--license", license.toString());

        graphqlCmd.execute();

        String output = readOutput(true);
        assertUsageError(output, exitCaptor);
        Assert.assertTrue(output.contains("The --license flag only applies to client and service generation. "
                + "It cannot be used with \"" + service + "\"."), output);
    }

    @Test(description = "Test that a dry run checks the license file and reports a bad one without writing")
    public void testDryRunWithBadLicense() throws IOException {
        Path license = writeLicense("Copyright 2026 My Company.\n");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--dry-run", "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains("Line 1 is not a comment: \"Copyright 2026 My Company.\""), output);
        Assert.assertFalse(output.contains("Dry run:"), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that a dry run with a valid license writes nothing")
    public void testDryRunWithLicense() throws IOException {
        Path license = writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, "--dry-run", "--license", license.toString());

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.contains("Dry run: no files were written."), output);
        Assert.assertEquals(balFiles(this.packageRoot), List.of(), output);
    }

    @Test(description = "Test that a relative license path is resolved from the directory the command runs in, as "
            + "-o is, and not from the directory of the Java process")
    public void testRelativeLicensePath() throws IOException {
        writeLicense(LICENSE);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        // The command runs in the package root here, which is not the directory of the test process.
        String output = run(exitCaptor, "--license", LICENSE_FILE);

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        assertHeader(this.packageRoot.resolve("service.bal"), HEADER);
    }

    @Test(description = "Test that the help lists the --license flag")
    public void testHelpListsLicense() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, printStream, this.packageRoot, exitCaptor, failIfAsked());
        new CommandLine(graphqlCmd).parseArgs("--help");

        graphqlCmd.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("[--license <license-file-path>]"), output);
        Assert.assertTrue(output.contains("--license=<license-file-path>"), output);
    }

    private Path writeLicense(String content) throws IOException {
        return Files.writeString(this.packageRoot.resolve(LICENSE_FILE), content);
    }

    private String run(ExitCodeCaptor exitCaptor, String... flags) throws IOException {
        return runWithMode(exitCaptor, "service", failIfAsked(), flags);
    }

    private String runWithMode(ExitCodeCaptor exitCaptor, String mode, String... flags) throws IOException {
        return runWithMode(exitCaptor, mode, failIfAsked(), flags);
    }

    private String runWithPrompt(ExitCodeCaptor exitCaptor, OverwritePrompt prompt, String... flags)
            throws IOException {
        return runWithMode(exitCaptor, "service", prompt, flags);
    }

    // Runs generation from the package's configuration file in the given mode with the given extra flags.
    private String runWithMode(ExitCodeCaptor exitCaptor, String mode, OverwritePrompt prompt, String... flags)
            throws IOException {
        List<String> args = new ArrayList<>(List.of(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME).toString(),
                "-o", this.packageRoot.toString(), "--mode", mode));
        args.addAll(List.of(flags));
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, printStream, this.packageRoot, exitCaptor, prompt);
        new CommandLine(graphqlCmd).parseArgs(args.toArray(String[]::new));
        graphqlCmd.execute();
        return readOutput(true);
    }

    private static OverwritePrompt answering(String answer, List<String> questions) {
        return question -> {
            questions.add(question);
            return Optional.of(answer);
        };
    }

    private static OverwritePrompt failIfAsked() {
        return question -> {
            throw new AssertionError("Unexpected question: " + question);
        };
    }

    // The generated file starts with exactly the header, followed by one blank line and then the generated code.
    private static void assertHeader(Path file, String header) throws IOException {
        String content = Files.readString(file);
        Assert.assertTrue(content.startsWith(header), content);
        Assert.assertFalse(content.substring(header.length()).startsWith("\n"), content);
    }

    private static List<Path> balFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(file -> file.toString().endsWith(".bal")).sorted().toList();
        }
    }

    private void assertCompiles() {
        DiagnosticResult diagnosticResult = getDiagnosticResult(this.packageRoot);
        Assert.assertTrue(diagnosticResult.errors().isEmpty(),
                "Unexpected compilation errors: " + diagnosticResult.errors());
    }

    private static void assertUsageError(String output, ExitCodeCaptor exitCaptor) {
        Assert.assertEquals(exitCaptor.getExitCode(), 2, output);
        Assert.assertTrue(output.contains("Usage: bal graphql [OPTIONS] <input>" + System.lineSeparator()
                + "Try 'bal graphql --help' for more information."), output);
    }
}
