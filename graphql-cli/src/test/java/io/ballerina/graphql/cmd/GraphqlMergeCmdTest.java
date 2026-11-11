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
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.graphql.common.TestUtils.createBallerinaPackage;
import static io.ballerina.graphql.common.TestUtils.getDiagnosticResult;

/**
 * Tests regenerating a service with the --merge flag through the command.
 */
public class GraphqlMergeCmdTest extends GraphqlTest {

    private static final String CONFIG = """
            [schema]
            source = "file"
            path = "schema.graphql"
            """;
    private static final String DATALOADER_CONFIG = CONFIG + """

            [[dataloaders]]
            field = "Query.book"
            loader = "bookLoader"
            """;
    private static final String SCHEMA = """
            type Query {
              book(id: String!): Book
            }

            type Book {
              title: String!
            }
            """;
    // The argument of book changes, and books and Book.pages are new.
    private static final String CHANGED_SCHEMA = """
            type Query {
              book(id: ID!): Book
              books: [Book!]!
            }

            type Book {
              title: String!
              pages: Int
            }
            """;
    private static final String NOT_IMPLEMENTED = "return error(\"Not implemented\");";
    private static final String MERGE_QUESTION = "Merge these file(s)? [y/N] ";
    private static final String MERGE_HINT = "Use --merge to keep the code in these file(s).";

    private Path packageRoot;
    private Path serviceFile;
    private Path typesFile;

    @BeforeMethod
    public void createMergePackage() throws IOException {
        this.packageRoot = createBallerinaPackage(Files.createTempDirectory(this.tmpDir, "merge"));
        this.serviceFile = this.packageRoot.resolve("service.bal");
        this.typesFile = this.packageRoot.resolve("types.bal");
    }

    @AfterMethod
    public void deleteMergePackage() throws IOException {
        FileUtils.deleteDirectory(this.packageRoot.toFile());
    }

    @Test(description = "Test that the developer's code is kept when the schema did not change")
    public void testMergeKeepsDeveloperCode() throws IOException {
        generate(SCHEMA);
        replace(this.serviceFile, "import ballerina/graphql;", "import ballerina/graphql;\nimport ballerina/log;");
        replace(this.serviceFile, "configurable int port = 9090;", "configurable int port = 7000;");
        replace(this.serviceFile, NOT_IMPLEMENTED, "logRequest(id);\n        return new Book();");
        append(this.serviceFile, "\nfunction logRequest(string id) {\n    log:printInfo(id);\n}\n");
        replace(this.typesFile, "public distinct service class Book {",
                "public distinct service class Book {\n    private final string name = \"Dune\";\n");
        replace(this.typesFile, NOT_IMPLEMENTED, "return self.name;");
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", questions), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertEquals(questions, List.of(MERGE_QUESTION), output);
        Assert.assertTrue(output.lines().toList().contains("Resolvers: 0 replaced, 0 added, 2 kept."), output);
        String service = Files.readString(this.serviceFile);
        Assert.assertTrue(service.contains("import ballerina/log;"), service);
        Assert.assertTrue(service.contains("configurable int port = 7000;"), service);
        Assert.assertTrue(service.contains("logRequest(id);"), service);
        Assert.assertTrue(service.contains("function logRequest(string id) {"), service);
        String types = Files.readString(this.typesFile);
        Assert.assertTrue(types.contains("private final string name = \"Dune\";"), types);
        Assert.assertTrue(types.contains("return self.name;"), types);
        assertCompiles();
    }

    @Test(description = "Test that changed resolvers are replaced, new ones added and unchanged ones kept")
    public void testMergeFollowsSchemaChanges() throws IOException {
        generate(SCHEMA);
        replace(this.serviceFile, NOT_IMPLEMENTED, "// developer book\n        return new Book();");
        replace(this.typesFile, NOT_IMPLEMENTED, "return \"Dune\";");
        writeSchema(CHANGED_SCHEMA);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("Resolvers: 1 replaced, 2 added, 1 kept."), output);
        Assert.assertTrue(lines.contains("  replaced  get book"), output);
        Assert.assertTrue(lines.contains("  added     get books"), output);
        Assert.assertTrue(lines.contains("  added     Book.get pages"), output);
        Assert.assertFalse(lines.stream().anyMatch(line -> line.contains("Book.get title")), output);
        String service = Files.readString(this.serviceFile);
        Assert.assertTrue(service.contains(
                "resource function get book(@graphql:ID string id) returns Book?|error {"), service);
        Assert.assertFalse(service.contains("// developer book"), service);
        Assert.assertTrue(service.contains("resource function get books() returns Book[]|error {"), service);
        String types = Files.readString(this.typesFile);
        Assert.assertTrue(types.contains("return \"Dune\";"), types);
        Assert.assertTrue(types.contains("resource function get pages() returns int?|error {"), types);
        Assert.assertTrue(types.contains("resource function get books() returns Book[]|error;"), types);
        assertCompiles();
    }

    @Test(description = "Test that a DataLoader added to the configuration is wired into the existing service")
    public void testMergeAddsDataLoader() throws IOException {
        generate(SCHEMA);
        append(this.serviceFile, "\nfunction audit() {\n}\n");
        replace(this.typesFile, NOT_IMPLEMENTED, "return \"Dune\";");
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), DATALOADER_CONFIG);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.lines().toList().contains("Resolvers: 1 replaced, 0 added, 1 kept."), output);
        String service = Files.readString(this.serviceFile);
        Assert.assertTrue(service.contains("import ballerina/graphql.dataloader;"), service);
        Assert.assertTrue(service.contains("import ballerina/http;"), service);
        Assert.assertTrue(service.contains(
                "context.registerDataLoader(\"bookLoader\", new dataloader:DefaultDataLoader(bookLoader));"), service);
        Assert.assertTrue(service.contains("@graphql:ServiceConfig {contextInit: initContext}"), service);
        Assert.assertTrue(service.contains("function preBook(graphql:Context context, string id) {"), service);
        Assert.assertTrue(service.contains(
                "resource function get book(graphql:Context context, string id) returns Book?|error {"), service);
        Assert.assertTrue(service.contains(
                "isolated function bookLoader(readonly & anydata[] keys) returns anydata[]|error {"), service);
        Assert.assertTrue(service.contains("function audit() {"), service);
        Assert.assertTrue(Files.readString(this.typesFile).contains("return \"Dune\";"));
        assertCompiles();
    }

    @Test(description = "Test that a DataLoader the developer implemented is kept and not registered twice")
    public void testMergeKeepsDataLoaderCode() throws IOException {
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), DATALOADER_CONFIG);
        writeSchema(SCHEMA);
        ExitCodeCaptor generateCaptor = new ExitCodeCaptor();
        String generateOutput = run(generateCaptor, failIfAsked());
        Assert.assertEquals(generateCaptor.getExitCode(), 0, generateOutput);
        replace(this.serviceFile, "isolated function bookLoader(readonly & anydata[] keys) returns anydata[]|error {\n"
                + "    " + NOT_IMPLEMENTED, "isolated function bookLoader(readonly & anydata[] keys) "
                + "returns anydata[]|error {\n    return keys;");
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.lines().toList().contains("Resolvers: 0 replaced, 0 added, 2 kept."), output);
        String service = Files.readString(this.serviceFile);
        Assert.assertTrue(service.contains("return keys;"), service);
        Assert.assertEquals(service.split("registerDataLoader\\(\"bookLoader\"").length, 2, service);
        assertCompiles();
    }

    @Test(description = "Test that a DataLoader removed from the configuration is not removed from the service")
    public void testMergeKeepsRemovedDataLoader() throws IOException {
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), DATALOADER_CONFIG);
        writeSchema(SCHEMA);
        ExitCodeCaptor generateCaptor = new ExitCodeCaptor();
        String generateOutput = run(generateCaptor, failIfAsked());
        Assert.assertEquals(generateCaptor.getExitCode(), 0, generateOutput);
        replace(this.serviceFile, "isolated function bookLoader(readonly & anydata[] keys) returns anydata[]|error {\n"
                + "    " + NOT_IMPLEMENTED, "isolated function bookLoader(readonly & anydata[] keys) "
                + "returns anydata[]|error {\n    return keys;");
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), CONFIG);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("Resolvers: 1 replaced, 0 added, 1 kept."), output);
        Assert.assertTrue(lines.contains("  replaced  get book"), output);
        String service = Files.readString(this.serviceFile);
        Assert.assertTrue(service.contains("resource function get book(string id) returns Book?|error {"), service);
        Assert.assertTrue(service.contains(
                "context.registerDataLoader(\"bookLoader\", new dataloader:DefaultDataLoader(bookLoader));"), service);
        Assert.assertTrue(service.contains("@graphql:ServiceConfig {contextInit: initContext}"), service);
        Assert.assertTrue(service.contains("function preBook(graphql:Context context, string id) {"), service);
        Assert.assertTrue(service.contains("return keys;"), service);
        assertCompiles();
    }

    @Test(description = "Test that a resolver no longer in the schema fails the merge before asking")
    public void testMergeWithResolverNotInSchema() throws IOException {
        generate(CHANGED_SCHEMA);
        writeSchema(CHANGED_SCHEMA.replace("  pages: Int\n", ""));
        Map<Path, String> before = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains(
                "Resolvers not in the schema: \"Book.get pages\". Update or delete them."), output);
        Assert.assertFalse(output.contains("already exist"), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that a service class no longer in the schema fails the merge before asking")
    public void testMergeWithServiceClassNotInSchema() throws IOException {
        generate("""
                type Query {
                  book: Book
                  author: Author
                }

                type Book {
                  title: String!
                }

                type Author {
                  name: String!
                }
                """);
        writeSchema("""
                type Query {
                  book: Book
                }

                type Book {
                  title: String!
                }
                """);
        Map<Path, String> before = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains(
                "Service classes not in the schema: \"Author\". Update or delete them."), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that an existing file with a syntax error fails the merge before asking")
    public void testMergeWithSyntaxError() throws IOException {
        generate(SCHEMA);
        replace(this.serviceFile, NOT_IMPLEMENTED, "return error(\"Not implemented\")");
        Map<Path, String> before = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains("The existing service has a syntax error at line"), output);
        Assert.assertTrue(output.contains("Fix it before merging."), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that nothing is merged when the user refuses")
    public void testMergeRefused() throws IOException {
        generate(SCHEMA);
        writeSchema(CHANGED_SCHEMA);
        Map<Path, String> before = existingFiles();
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("n", questions), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 4, output);
        Assert.assertEquals(questions, List.of(MERGE_QUESTION), output);
        Assert.assertTrue(output.contains("No files were written."), output);
        Assert.assertFalse(output.contains(MERGE_HINT), output);
        Assert.assertFalse(output.contains("Resolvers:"), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that a non-interactive merge without --force points to --force")
    public void testMergeNotInteractive() throws IOException {
        generate(SCHEMA);
        writeSchema(CHANGED_SCHEMA);
        Map<Path, String> before = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, question -> Optional.empty(), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 4, output);
        Assert.assertTrue(output.contains(
                "Cannot ask to merge these file(s) in a non-interactive run. Use --force to merge them."), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that --force merges without asking")
    public void testMergeWithForce() throws IOException {
        generate(SCHEMA);
        replace(this.typesFile, NOT_IMPLEMENTED, "return \"Dune\";");
        writeSchema(CHANGED_SCHEMA);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge", "--force");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertFalse(output.contains("already exist"), output);
        Assert.assertTrue(output.lines().toList().contains("Resolvers: 1 replaced, 2 added, 1 kept."), output);
        Assert.assertTrue(Files.readString(this.typesFile).contains("return \"Dune\";"));
    }

    @Test(description = "Test that a dry run lists the files to merge and what the merge would do, without writing")
    public void testMergeDryRun() throws IOException {
        generate(SCHEMA);
        writeSchema(CHANGED_SCHEMA);
        Map<Path, String> before = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge", "--dry-run");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("Dry run: no files were written."), output);
        Assert.assertTrue(lines.contains("  merge      " + displayPath(this.typesFile)), output);
        Assert.assertTrue(lines.contains("  merge      " + displayPath(this.serviceFile)), output);
        Assert.assertTrue(lines.contains("0 to create, 2 to merge."), output);
        Assert.assertTrue(lines.contains("Resolvers: 1 replaced, 2 added, 1 kept."), output);
        Assert.assertTrue(lines.contains("  replaced  get book"), output);
        Assert.assertFalse(output.contains(MERGE_HINT), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that only the existing file is merged and the missing one is created")
    public void testMergeWithOneExistingFile() throws IOException {
        generate(SCHEMA);
        replace(this.serviceFile, NOT_IMPLEMENTED, "// developer book\n        return new Book();");
        Files.delete(this.typesFile);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("  " + displayPath(this.serviceFile)), output);
        Assert.assertFalse(lines.contains("  " + displayPath(this.typesFile)), output);
        Assert.assertTrue(lines.contains("Resolvers: 0 replaced, 0 added, 1 kept."), output);
        Assert.assertTrue(Files.readString(this.serviceFile).contains("// developer book"));
        Assert.assertTrue(Files.readString(this.typesFile).contains("public distinct service class Book {"));
        assertCompiles();
    }

    @Test(description = "Test that --merge without existing files generates them without asking or a summary")
    public void testMergeWithoutExistingFiles() throws IOException {
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), CONFIG);
        writeSchema(SCHEMA);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertFalse(output.contains("Resolvers:"), output);
        Assert.assertTrue(Files.exists(this.serviceFile), output);
        Assert.assertTrue(Files.exists(this.typesFile), output);
    }

    @Test(description = "Test that overwriting a service points to --merge, also in a non-interactive run")
    public void testOverwriteSuggestsMerge() throws IOException {
        generate(SCHEMA);
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("n", questions));

        Assert.assertEquals(exitCaptor.getExitCode(), 4, output);
        Assert.assertEquals(questions, List.of("Overwrite these file(s)? [y/N] "), output);
        Assert.assertTrue(output.lines().toList().contains(MERGE_HINT), output);

        exitCaptor = new ExitCodeCaptor();
        output = run(exitCaptor, question -> Optional.empty());

        Assert.assertEquals(exitCaptor.getExitCode(), 4, output);
        Assert.assertTrue(output.lines().toList().contains(MERGE_HINT), output);
        Assert.assertTrue(output.contains("Use --force to overwrite them."), output);
    }

    @Test(description = "Test that a dry run that would overwrite a service points to --merge")
    public void testDryRunSuggestsMerge() throws IOException {
        generate(SCHEMA);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--dry-run");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("  overwrite  " + displayPath(this.serviceFile)), output);
        Assert.assertTrue(lines.contains("0 to create, 2 to overwrite."), output);
        Assert.assertTrue(lines.contains(MERGE_HINT), output);
    }

    @Test(description = "Test that --merge is rejected for client generation")
    public void testMergeWithClientGeneration() throws IOException {
        Path config = Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), CONFIG);
        writeSchema(SCHEMA);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), config.toString(), "-o", this.packageRoot.toString(),
                "--mode", "client", "--merge");

        Assert.assertTrue(output.contains("The --merge flag only applies to service generation. It cannot be used "
                + "with \"" + config + "\"."), output);
        assertUsageError(output, exitCaptor);
    }

    @Test(description = "Test that --merge is rejected for schema generation")
    public void testMergeWithSchemaGeneration() throws IOException {
        String service = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), service, "-o", this.packageRoot.toString(), "--merge");

        Assert.assertTrue(output.contains("The --merge flag only applies to service generation. It cannot be used "
                + "with \"" + service + "\"."), output);
        assertUsageError(output, exitCaptor);
    }

    @Test(description = "Test that the help lists the --merge flag")
    public void testHelpListsMerge() throws IOException {
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--help");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.contains("[--merge]"), output);
        Assert.assertTrue(output.contains("--merge             Merge the generated service"), output);
        Assert.assertTrue(output.contains("$ bal graphql balGraphQL.toml --mode service --merge"), output);
    }

    @Test(description = "Test that merging again without a schema change leaves the merged files as they are")
    public void testMergeTwiceGivesSameResult() throws IOException {
        generate(SCHEMA);
        replace(this.serviceFile, NOT_IMPLEMENTED, "// developer book\n        return new Book();");
        replace(this.typesFile, NOT_IMPLEMENTED, "return \"Dune\";");
        writeSchema(CHANGED_SCHEMA);
        ExitCodeCaptor firstCaptor = new ExitCodeCaptor();
        String firstOutput = run(firstCaptor, failIfAsked(), "--merge", "--force");
        Assert.assertEquals(firstCaptor.getExitCode(), 0, firstOutput);
        Map<Path, String> merged = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge", "--force");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        Assert.assertTrue(output.lines().toList().contains("Resolvers: 0 replaced, 0 added, 4 kept."), output);
        Assert.assertEquals(existingFiles(), merged, output);
    }

    @Test(description = "Test that a type the schema no longer has is removed and reported, also in a dry run")
    public void testMergeRemovesTypesNotInSchema() throws IOException {
        generate("""
                type Query {
                  book(id: String!, genre: Genre): Book
                }

                enum Genre {
                  FICTION
                }

                type Book {
                  title: String!
                }
                """);
        replace(this.typesFile, NOT_IMPLEMENTED, "return \"Dune\";");
        writeSchema(SCHEMA);
        ExitCodeCaptor dryRunCaptor = new ExitCodeCaptor();

        String dryRunOutput = run(dryRunCaptor, failIfAsked(), "--merge", "--dry-run");

        Assert.assertEquals(dryRunCaptor.getExitCode(), 0, dryRunOutput);
        Assert.assertTrue(dryRunOutput.lines().toList().contains("Removed types not in the schema: Genre."),
                dryRunOutput);
        Assert.assertTrue(Files.readString(this.typesFile).contains("enum Genre"));

        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("Resolvers: 1 replaced, 0 added, 1 kept."), output);
        Assert.assertTrue(lines.contains("Removed types not in the schema: Genre."), output);
        String types = Files.readString(this.typesFile);
        Assert.assertFalse(types.contains("Genre"), types);
        Assert.assertTrue(types.contains("return \"Dune\";"), types);
        assertCompiles();
    }

    @Test(description = "Test that the files of a module are merged")
    public void testMergeIntoModule() throws IOException {
        generate(SCHEMA, "--module", "books");
        Path moduleRoot = this.packageRoot.resolve(Paths.get("modules", "books"));
        Path moduleService = moduleRoot.resolve("service.bal");
        Path moduleTypes = moduleRoot.resolve("types.bal");
        replace(moduleService, NOT_IMPLEMENTED, "// developer book\n        return new Book();");
        replace(moduleTypes, NOT_IMPLEMENTED, "return \"Dune\";");
        writeSchema(CHANGED_SCHEMA.replace("  book(id: ID!): Book\n", "  book(id: String!): Book\n"));
        List<String> questions = new ArrayList<>();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", questions), "--module", "books", "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("  " + displayPath(moduleService)), output);
        Assert.assertTrue(lines.contains("  " + displayPath(moduleTypes)), output);
        Assert.assertTrue(lines.contains("Resolvers: 0 replaced, 2 added, 2 kept."), output);
        Assert.assertTrue(Files.readString(moduleService).contains("// developer book"));
        Assert.assertTrue(Files.readString(moduleService).contains("resource function get books()"));
        Assert.assertTrue(Files.readString(moduleTypes).contains("return \"Dune\";"));
        Assert.assertFalse(Files.exists(this.serviceFile), output);
        assertCompiles();
    }

    @Test(description = "Test that remote resolvers are merged like resource resolvers")
    public void testMergeRemoteResolvers() throws IOException {
        generate(SCHEMA + """

                type Mutation {
                  addBook(title: String!): Book
                }
                """);
        replace(this.serviceFile, "resource function get book(string id) returns Book?|error {",
                "resource function get book(string id) returns Book?|error {\n        // developer book");
        replace(this.serviceFile, "remote function addBook(string title) returns Book?|error {",
                "remote function addBook(string title) returns Book?|error {\n        // developer addBook");
        writeSchema(SCHEMA + """

                type Mutation {
                  addBook(title: String!, pages: Int): Book
                  removeBook(id: String!): Boolean
                }
                """);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, answering("y", new ArrayList<>()), "--merge");

        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
        List<String> lines = output.lines().toList();
        Assert.assertTrue(lines.contains("Resolvers: 1 replaced, 1 added, 2 kept."), output);
        Assert.assertTrue(lines.contains("  replaced  remote addBook"), output);
        Assert.assertTrue(lines.contains("  added     remote removeBook"), output);
        String service = Files.readString(this.serviceFile);
        Assert.assertTrue(service.contains("// developer book"), service);
        Assert.assertFalse(service.contains("// developer addBook"), service);
        Assert.assertTrue(service.contains("remote function addBook(string title, int? pages) returns Book?|error {"),
                service);
        Assert.assertTrue(service.contains("remote function removeBook(string id) returns boolean?|error {"),
                service);
        assertCompiles();
    }

    @Test(description = "Test that a dry run reports a merge error instead of the planned files")
    public void testMergeDryRunWithResolverNotInSchema() throws IOException {
        generate(CHANGED_SCHEMA);
        writeSchema(CHANGED_SCHEMA.replace("  pages: Int\n", ""));
        Map<Path, String> before = existingFiles();
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

        String output = run(exitCaptor, failIfAsked(), "--merge", "--dry-run");

        Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
        Assert.assertTrue(output.contains(
                "Resolvers not in the schema: \"Book.get pages\". Update or delete them."), output);
        Assert.assertFalse(output.contains("Dry run:"), output);
        Assert.assertEquals(existingFiles(), before, output);
    }

    @Test(description = "Test that an existing file that cannot be read fails the merge before asking")
    public void testMergeWithUnreadableFile() throws IOException {
        generate(SCHEMA);
        File service = this.serviceFile.toFile();
        try {
            if (!service.setReadable(false) || Files.isReadable(this.serviceFile)) {
                throw new SkipException("Cannot make a file unreadable on this system");
            }
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();

            String output = run(exitCaptor, failIfAsked(), "--merge");

            Assert.assertEquals(exitCaptor.getExitCode(), 1, output);
            Assert.assertTrue(output.contains("Cannot read \"" + displayPath(this.serviceFile)
                    + "\" to merge it. Check its permissions."), output);
            Assert.assertFalse(output.contains("already exist"), output);
        } finally {
            service.setReadable(true);
        }
    }

    // Writes the configuration and the schema, and generates the service the developer then works on.
    private void generate(String schema, String... flags) throws IOException {
        Files.writeString(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME), CONFIG);
        writeSchema(schema);
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        String output = run(exitCaptor, failIfAsked(), flags);
        Assert.assertEquals(exitCaptor.getExitCode(), 0, output);
    }

    private void writeSchema(String schema) throws IOException {
        Files.writeString(this.packageRoot.resolve("schema.graphql"), schema);
    }

    // Runs service generation from the package's configuration file with the given extra flags.
    private String run(ExitCodeCaptor exitCaptor, OverwritePrompt prompt, String... flags) throws IOException {
        List<String> args = new ArrayList<>();
        if (flags.length == 0 || flags[0].startsWith("-") && !flags[0].equals("--help")) {
            args.addAll(List.of(this.packageRoot.resolve(BalGraphqlConfig.FILE_NAME).toString(),
                    "-o", this.packageRoot.toString(), "--mode", "service"));
        }
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

    // A developer's edit to a generated file. The text to replace must be there, so a change in the generated code
    // fails here instead of making the test pass for the wrong reason.
    private static void replace(Path file, String target, String replacement) throws IOException {
        String content = Files.readString(file);
        Assert.assertTrue(content.contains(target), "\"" + target + "\" not found in " + file + ":\n" + content);
        Files.writeString(file, content.replace(target, replacement));
    }

    private static void append(Path file, String text) throws IOException {
        Files.writeString(file, Files.readString(file) + text);
    }

    private Map<Path, String> existingFiles() throws IOException {
        return Map.of(this.serviceFile, Files.readString(this.serviceFile),
                this.typesFile, Files.readString(this.typesFile));
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

    private static Path displayPath(Path filePath) {
        return Path.of("").toAbsolutePath().relativize(filePath.toAbsolutePath());
    }
}
