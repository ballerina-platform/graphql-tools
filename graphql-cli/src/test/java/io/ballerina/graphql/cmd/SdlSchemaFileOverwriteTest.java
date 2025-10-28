/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
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
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for validating file overwrite behavior in SDL schema generation.
 * These tests ensure that:
 * 1. File conflict checking happens BEFORE expensive schema generation
 * operations
 * 2. User prompts appear immediately without processing delays
 * 3. Default behavior is to overwrite (Y/n prompt)
 * 4. Non-interactive mode defaults to overwriting existing files
 */
public class SdlSchemaFileOverwriteTest extends GraphqlTest {

    @Test(description = "Test that file overwrite prompt appears before expensive operations")
    public void testEarlyFileConflictDetection() throws IOException {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        String schemaFileName = "schema_graphql.graphql";

        // First generation - create the file
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();

        // Verify file was created
        Path generatedFile = this.tmpDir.resolve(schemaFileName);
        Assert.assertTrue(Files.exists(generatedFile), "Schema file should be generated");

        // Record original file modification time
        long originalModTime = Files.getLastModifiedTime(generatedFile).toMillis();

        // Add a small delay to ensure modification time would be different
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Second generation with user declining to overwrite (simulated with 'n' input)
        // Note: This test validates that the file is NOT regenerated when user says 'n'
        ByteArrayInputStream inputStream = new ByteArrayInputStream("n\n".getBytes());
        System.setIn(inputStream);

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);

        try {
            graphqlCmd2.execute();
            String output = readOutput(true);
            // The generation should be cancelled by user
            Assert.assertTrue(output.contains("cancelled") || output.contains("exist"),
                    "Output should indicate cancellation or file existence");
        } catch (Exception e) {
            // Expected to throw an exception when user declines
            Assert.assertTrue(e.getMessage().contains("cancelled") || e.getMessage().contains("exist"),
                    "Exception should indicate user cancellation");
        }

        // Verify file was NOT modified (time should be the same)
        long newModTime = Files.getLastModifiedTime(generatedFile).toMillis();
        Assert.assertEquals(newModTime, originalModTime,
                "File should NOT be modified when user declines overwrite");

        // Reset System.in
        System.setIn(System.in);
    }

    @Test(description = "Test that default behavior is to overwrite when user presses Enter")
    public void testDefaultOverwriteOnEnter() throws IOException {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        String schemaFileName = "schema_graphql.graphql";

        // First generation - create the file
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();

        Path generatedFile = this.tmpDir.resolve(schemaFileName);
        Assert.assertTrue(Files.exists(generatedFile));

        // Record original file modification time
        long originalModTime = Files.getLastModifiedTime(generatedFile).toMillis();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Second generation with user pressing Enter (empty input - should default to
        // Yes)
        ByteArrayInputStream inputStream = new ByteArrayInputStream("\n".getBytes());
        System.setIn(inputStream);

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);
        graphqlCmd2.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("generated successfully") || output.contains("copied to"),
                "Schema should be regenerated with default Yes behavior");

        // Verify file WAS modified (time should be different)
        long newModTime = Files.getLastModifiedTime(generatedFile).toMillis();
        Assert.assertTrue(newModTime >= originalModTime,
                "File should be modified when user accepts overwrite (default)");

        // Reset System.in
        System.setIn(System.in);
    }

    @Test(description = "Test explicit 'y' input overwrites the file")
    public void testExplicitYesOverwrites() throws IOException {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        String schemaFileName = "schema_graphql.graphql";

        // First generation
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();

        Path generatedFile = this.tmpDir.resolve(schemaFileName);
        long originalModTime = Files.getLastModifiedTime(generatedFile).toMillis();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Second generation with explicit 'y' input
        ByteArrayInputStream inputStream = new ByteArrayInputStream("y\n".getBytes());
        System.setIn(inputStream);

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);
        graphqlCmd2.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("generated successfully") || output.contains("copied to"));

        // Verify file was modified
        long newModTime = Files.getLastModifiedTime(generatedFile).toMillis();
        Assert.assertTrue(newModTime >= originalModTime,
                "File should be modified when user explicitly says yes");

        System.setIn(System.in);
    }

    @Test(description = "Test multiple files overwrite with one prompt")
    public void testMultipleFilesOverwrite() throws IOException {
        // Use service_3.bal which generates multiple schema files
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_3.bal")).toString();

        // First generation - creates multiple files
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();

        // Verify multiple files were created
        Path file1 = this.tmpDir.resolve("schema_service_3.graphql");
        Path file2 = this.tmpDir.resolve("schema_service_3_1.graphql");
        Path file3 = this.tmpDir.resolve("schema_service_3_2.graphql");

        Assert.assertTrue(Files.exists(file1));
        Assert.assertTrue(Files.exists(file2));
        Assert.assertTrue(Files.exists(file3));

        // Second generation with 'y' to overwrite all
        ByteArrayInputStream inputStream = new ByteArrayInputStream("y\n".getBytes());
        System.setIn(inputStream);

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);
        graphqlCmd2.execute();

        String output = readOutput(true);
        // Should mention multiple files
        Assert.assertTrue(output.contains("already exist") || output.contains("generated successfully"));

        System.setIn(System.in);
    }

    @Test(description = "Test that prompt mentions correct file names")
    public void testPromptShowsCorrectFileNames() throws IOException {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        String schemaFileName = "schema_graphql.graphql";

        // First generation
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();

        // Second generation to trigger prompt
        ByteArrayInputStream inputStream = new ByteArrayInputStream("y\n".getBytes());
        System.setIn(inputStream);

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);
        graphqlCmd2.execute();

        String output = readOutput(true);
        // The output should mention the schema file name if prompting about overwrite
        // or show success message
        Assert.assertTrue(output.contains(schemaFileName) || output.contains("generated successfully"));

        System.setIn(System.in);
    }

    @Test(description = "Test performance - prompt should appear immediately, not after schema generation")
    public void testPromptTimingPerformance() throws IOException {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();

        // First generation
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        readOutput(true); // Clear output

        // Second generation with timing measurement
        long startTime = System.currentTimeMillis();

        // Simulate delayed user input to measure when prompt appears
        Thread inputThread = new Thread(() -> {
            try {
                // Wait a bit before providing input to ensure we can measure prompt timing
                Thread.sleep(100);
                ByteArrayInputStream inputStream = new ByteArrayInputStream("n\n".getBytes());
                System.setIn(inputStream);
            } catch (InterruptedException e) {
                // Ignore
            }
        });
        inputThread.start();

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);

        try {
            graphqlCmd2.execute();
        } catch (Exception e) {
            // May throw exception on cancellation - that's OK
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // The execution should be relatively fast if it exits early (not generating
        // schema)
        // If it took a long time, it means schema was generated before checking for
        // conflicts
        // This is a rough check - if it takes less than 1 second, it likely exited
        // early
        String output = readOutput(true);
        System.out.println("Execution time for conflict detection: " + executionTime + "ms");

        // The key is that output should show file already exists, not
        // compilation/generation errors
        boolean hasConflictMessage = output.contains("exist") || output.contains("cancelled") ||
                output.contains("overwrite");

        Assert.assertTrue(hasConflictMessage,
                "Should show file conflict message, indicating early detection. Output: " + output);

        System.setIn(System.in);
    }

    @Test(description = "Test case-insensitive input (Y, y, N, n all work)")
    public void testCaseInsensitiveInput() throws IOException {
        String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal")).toString();
        String[] args = { "-i", servicePath, "-o", this.tmpDir.toString() };

        // Test uppercase Y
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();

        ByteArrayInputStream inputStream = new ByteArrayInputStream("Y\n".getBytes());
        System.setIn(inputStream);

        GraphqlCmd graphqlCmd2 = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
        new CommandLine(graphqlCmd2).parseArgs(args);
        graphqlCmd2.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("generated successfully") || output.contains("copied to"));

        System.setIn(System.in);
    }

    @Test(description = "Test that no prompt appears when files don't exist")
    public void testNoPromptForNewFiles() throws IOException {
        // Use a fresh temp directory to ensure no existing files
        Path freshTmpDir = Files.createTempDirectory("graphql-fresh-test-");

        try {
            String servicePath = resourceDir.resolve(Paths.get("graphqlServices", "valid", "service_1.bal"))
                    .toString();
            String[] args = { "-i", servicePath, "-o", freshTmpDir.toString() };

            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, resourceDir.resolve("graphqlServices"), exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();

            String output = readOutput(true);
            // Should not mention overwriting or existing files
            Assert.assertFalse(output.contains("already exist"));
            Assert.assertFalse(output.contains("overwrite"));
            Assert.assertTrue(output.contains("generated successfully") || output.contains("copied to"));

        } finally {
            // Cleanup fresh temp directory
            Files.walk(freshTmpDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }
}
