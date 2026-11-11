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

package io.ballerina.graphql.generator.utils;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Tests writing the files of a run together.
 */
public class CodeGeneratorUtilsTest {

    private Path outputPath;

    @BeforeMethod
    public void createOutputPath() throws IOException {
        this.outputPath = Files.createTempDirectory("write-files");
    }

    @AfterMethod
    public void deleteOutputPath() throws IOException {
        FileUtils.deleteDirectory(this.outputPath.toFile());
    }

    @Test(description = "Test that every file is written, an existing one is replaced and no staging is left behind")
    public void testWritesFiles() throws IOException {
        Path service = this.outputPath.resolve("service.bal");
        Path moduleTypes = this.outputPath.resolve("modules").resolve("books").resolve("types.bal");
        Files.writeString(service, "old service");
        Map<Path, String> files = new LinkedHashMap<>();
        files.put(service, "new service");
        files.put(moduleTypes, "new types");

        CodeGeneratorUtils.writeFiles(files, this.outputPath);

        Assert.assertEquals(Files.readString(service), "new service");
        Assert.assertEquals(Files.readString(moduleTypes), "new types");
        Assert.assertEquals(entries(), List.of("modules", "service.bal"));
    }

    @Test(description = "Test that two files of the same name in different modules are both written")
    public void testWritesFilesOfSameName() throws IOException {
        Path rootTypes = this.outputPath.resolve("types.bal");
        Path moduleTypes = this.outputPath.resolve("modules").resolve("books").resolve("types.bal");
        Map<Path, String> files = new LinkedHashMap<>();
        files.put(rootTypes, "root types");
        files.put(moduleTypes, "module types");

        CodeGeneratorUtils.writeFiles(files, this.outputPath);

        Assert.assertEquals(Files.readString(rootTypes), "root types");
        Assert.assertEquals(Files.readString(moduleTypes), "module types");
    }

    @Test(description = "Test that a failure while writing leaves the existing files as they were (FF-8)")
    public void testFailureLeavesExistingFiles() throws IOException {
        Path service = this.outputPath.resolve("service.bal");
        Files.writeString(service, "developer service");
        // The longest name the file system allows, so the staged copy of it, with its prefix, cannot be created.
        Path types = this.outputPath.resolve("t".repeat(251) + ".bal");
        Map<Path, String> files = new LinkedHashMap<>();
        files.put(service, "merged service");
        files.put(types, "merged types");

        try {
            CodeGeneratorUtils.writeFiles(files, this.outputPath);
            throw new SkipException("This file system allows the staged file name, so the write cannot be failed");
        } catch (IOException expected) {
            Assert.assertEquals(Files.readString(service), "developer service");
            Assert.assertFalse(Files.exists(types));
            Assert.assertEquals(entries(), List.of("service.bal"));
        }
    }

    @Test(description = "Test that nothing is created when there are no files to write")
    public void testWritesNoFiles() throws IOException {
        CodeGeneratorUtils.writeFiles(Map.of(), this.outputPath);

        Assert.assertEquals(entries(), List.of());
    }

    private List<String> entries() throws IOException {
        try (Stream<Path> entries = Files.list(this.outputPath)) {
            return entries.map(entry -> entry.getFileName().toString()).sorted().toList();
        }
    }
}
