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

package io.ballerina.graphql.utils;

import io.ballerina.graphql.schema.exception.SchemaFileGenerationException;
import io.ballerina.graphql.schema.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Unit tests for Utils.checkFileOverwriteConsent() method.
 * Tests the file overwrite prompt behavior and default settings.
 */
public class UtilsFileOverwriteTest {

    private Path tmpDir;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private InputStream originalSystemIn;

    @BeforeMethod
    public void setup() throws IOException {
        this.tmpDir = Files.createTempDirectory("utils-file-test-");
        this.outputStream = new ByteArrayOutputStream();
        this.printStream = new PrintStream(outputStream);
        this.originalSystemIn = System.in;
    }

    @AfterMethod
    public void cleanup() throws IOException {
        if (tmpDir != null && Files.exists(tmpDir)) {
            Files.walk(tmpDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (printStream != null) {
            printStream.close();
        }
        System.setIn(originalSystemIn);
    }

    @Test(description = "Test no prompt when no files exist")
    public void testNoPromptForNonExistentFiles() throws SchemaFileGenerationException {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");
        fileNames.add("schema_test2.graphql");

        // Should not throw exception and should not prompt
        Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);

        String output = outputStream.toString();
        Assert.assertFalse(output.contains("already exist"));
        Assert.assertFalse(output.contains("overwrite"));
    }

    @Test(description = "Test prompt appears when files exist")
    public void testPromptAppearsForExistingFiles() throws IOException {
        // Create existing file
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        // Simulate user input 'y'
        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
        } catch (SchemaFileGenerationException e) {
            Assert.fail("Should not throw exception when user confirms overwrite");
        }

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("already exist"));
        Assert.assertTrue(output.contains("schema_test.graphql"));
        Assert.assertTrue(output.contains("[Y/n]"), "Prompt should show Y as default");
    }

    @Test(description = "Test user declining with 'n' throws exception")
    public void testUserDecliningThrowsException() throws IOException {
        // Create existing file
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        // Simulate user input 'n'
        System.setIn(new ByteArrayInputStream("n\n".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
            Assert.fail("Should throw exception when user declines overwrite");
        } catch (SchemaFileGenerationException e) {
            // Expected
            String output = outputStream.toString();
            Assert.assertTrue(output.contains("already exist"));
        }
    }

    @Test(description = "Test empty input (Enter key) defaults to Yes")
    public void testEmptyInputDefaultsToYes() throws IOException {
        // Create existing file
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        // Simulate user pressing Enter (empty input)
        System.setIn(new ByteArrayInputStream("\n".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
            // Should not throw exception - empty input defaults to Yes
        } catch (SchemaFileGenerationException e) {
            Assert.fail("Empty input should default to Yes and not throw exception. Exception: " + e.getMessage());
        }

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("already exist"));
    }

    @Test(description = "Test uppercase 'Y' is accepted")
    public void testUppercaseYAccepted() throws IOException {
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        System.setIn(new ByteArrayInputStream("Y\n".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
        } catch (SchemaFileGenerationException e) {
            Assert.fail("Uppercase Y should be accepted");
        }
    }

    @Test(description = "Test lowercase 'n' declines")
    public void testLowercaseNDeclines() throws IOException {
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        System.setIn(new ByteArrayInputStream("n\n".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
            Assert.fail("Should throw exception when user says no");
        } catch (SchemaFileGenerationException e) {
            // Expected
        }
    }

    @Test(description = "Test uppercase 'N' declines")
    public void testUppercaseNDeclines() throws IOException {
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        System.setIn(new ByteArrayInputStream("N\n".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
            Assert.fail("Should throw exception when user says no");
        } catch (SchemaFileGenerationException e) {
            // Expected
        }
    }

    @Test(description = "Test multiple existing files shown in prompt")
    public void testMultipleFilesInPrompt() throws IOException {
        // Create multiple existing files
        Path file1 = tmpDir.resolve("schema_test1.graphql");
        Path file2 = tmpDir.resolve("schema_test2.graphql");
        Path file3 = tmpDir.resolve("schema_test3.graphql");
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createFile(file3);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test1.graphql");
        fileNames.add("schema_test2.graphql");
        fileNames.add("schema_test3.graphql");

        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);

        String output = outputStream.toString();
        Assert.assertTrue(output.contains("schema_test1.graphql"));
        Assert.assertTrue(output.contains("schema_test2.graphql"));
        Assert.assertTrue(output.contains("schema_test3.graphql"));
        Assert.assertTrue(output.contains("-- "), "Files should be listed with '--' prefix");
    }

    @Test(description = "Test mixed existing and non-existing files")
    public void testMixedExistingFiles() throws IOException {
        // Create only one of the files
        Path existingFile = tmpDir.resolve("schema_exists.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_exists.graphql");
        fileNames.add("schema_not_exists.graphql");

        System.setIn(new ByteArrayInputStream("y\n".getBytes()));

        Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);

        String output = outputStream.toString();
        // Should only mention the existing file
        Assert.assertTrue(output.contains("schema_exists.graphql"));
        // Should not mention non-existing file in the list
        // (though it might be in the file list being checked, only existing ones are
        // shown)
    }

    @Test(description = "Test non-interactive mode defaults to overwrite")
    public void testNonInteractiveModeDefaultsToOverwrite() throws IOException {
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        // Simulate non-interactive mode by providing no input stream
        // The Utils method should handle this gracefully and default to overwrite
        System.setIn(new ByteArrayInputStream("".getBytes()));

        try {
            Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
            // Should not throw exception in non-interactive mode
        } catch (SchemaFileGenerationException e) {
            // Check if it's the cancellation exception
            // In non-interactive mode, it should default to overwrite, not cancel
            Assert.fail("Non-interactive mode should default to overwrite, but got exception: " + e.getMessage());
        }

        String output = outputStream.toString();
        // Should show that it's proceeding or mention the files
        Assert.assertTrue(output.contains("already exist") || output.contains("proceeding"));
    }

    @Test(description = "Test arbitrary input defaults to Yes (overwrite)")
    public void testArbitraryInputDefaultsToYes() throws IOException {
        Path existingFile = tmpDir.resolve("schema_test.graphql");
        Files.createFile(existingFile);

        List<String> fileNames = new ArrayList<>();
        fileNames.add("schema_test.graphql");

        // Test with various inputs that are not 'n' or 'N'
        String[] inputs = { "yes", "YES", "anything", "123", " ", "y " };

        for (String input : inputs) {
            // Reset output stream
            outputStream.reset();

            System.setIn(new ByteArrayInputStream((input + "\n").getBytes()));

            try {
                Utils.checkFileOverwriteConsent(tmpDir, fileNames, printStream);
                // Should not throw exception - only 'n' or 'N' should cancel
            } catch (SchemaFileGenerationException e) {
                Assert.fail("Input '" + input + "' should default to Yes, but got exception: " + e.getMessage());
            }
        }
    }
}
