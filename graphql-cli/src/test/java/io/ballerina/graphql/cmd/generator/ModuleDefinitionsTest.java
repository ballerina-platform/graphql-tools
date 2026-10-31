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

package io.ballerina.graphql.cmd.generator;

import io.ballerina.graphql.exception.GenerationException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Tests reading the top-level names a module already defines.
 */
public class ModuleDefinitionsTest {

    private static final Set<String> GENERATED_FILES = Set.of("types.bal", "service.bal");

    private Path tmpDir;

    @BeforeMethod
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("module-definitions");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

    @Test(description = "Test that every kind of top-level definition is read with the file that defines it")
    public void testReadDefinitions() throws IOException, GenerationException {
        Files.writeString(tmpDir.resolve("main.bal"), """
                import ballerina/http;

                type Book record {};
                public class Library {}
                function find() {}
                const LIMIT = 10;
                enum Color { RED, GREEN }
                listener http:Listener ep = new (9090);
                int count = 0;
                type 'Quoted record {};
                """);
        Files.writeString(tmpDir.resolve("orders.bal"), "type Order record {};\n");

        Assert.assertEquals(ModuleDefinitions.read(tmpDir, GENERATED_FILES), Map.ofEntries(
                Map.entry("Book", "main.bal"), Map.entry("Library", "main.bal"), Map.entry("find", "main.bal"),
                Map.entry("LIMIT", "main.bal"), Map.entry("Color", "main.bal"), Map.entry("RED", "main.bal"),
                Map.entry("GREEN", "main.bal"), Map.entry("ep", "main.bal"), Map.entry("count", "main.bal"),
                Map.entry("Quoted", "main.bal"), Map.entry("Order", "orders.bal")));
    }

    @Test(description = "Test that the generated files and files that are not Ballerina sources are not read")
    public void testSkippedFiles() throws IOException, GenerationException {
        Files.writeString(tmpDir.resolve("types.bal"), "type OrdersService service object {};\n");
        Files.writeString(tmpDir.resolve("service.bal"), "function main() {}\n");
        Files.writeString(tmpDir.resolve("notes.txt"), "type Notes record {};\n");
        Files.createDirectories(tmpDir.resolve("tests"));
        Files.writeString(tmpDir.resolve("tests").resolve("test.bal"), "function testOrders() {}\n");

        Assert.assertEquals(ModuleDefinitions.read(tmpDir, GENERATED_FILES), Map.of());
    }

    @Test(description = "Test that a module folder that does not exist yet has no definitions")
    public void testMissingModule() throws GenerationException {
        Assert.assertEquals(ModuleDefinitions.read(tmpDir.resolve("modules").resolve("orders"), GENERATED_FILES),
                Map.of());
    }
}
