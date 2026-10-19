/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

/**
 * Tests resolving the output folder of client and service generation inside a Ballerina package.
 */
public class PackageOutputTest {

    private static final String BALLERINA_TOML = """
            [package]
            org = "test"
            name = "books"
            version = "0.1.0"
            """;
    private static final String NO_MODULE = null;

    private Path tmpDir;

    @BeforeMethod
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("package-output");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

    @Test(description = "Test that the package root is the output folder when no module is given")
    public void testPackageRoot() throws IOException, GenerationException {
        Path packageRoot = createPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, NO_MODULE), packageRoot.toAbsolutePath().normalize());
    }

    @Test(description = "Test that a module resolves to the modules folder of the package")
    public void testModule() throws IOException, GenerationException {
        Path packageRoot = createPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, "books"),
                packageRoot.toAbsolutePath().normalize().resolve("modules").resolve("books"));
    }

    @Test(description = "Test that a folder inside a package is rejected")
    public void testSubfolderRejected() throws IOException {
        Path subfolder = Files.createDirectory(createPackage(tmpDir).resolve("sub"));

        GenerationException error = Assert.expectThrows(GenerationException.class,
                () -> PackageOutput.resolve(subfolder, NO_MODULE));
        Assert.assertEquals(error.getMessage(), "\"" + subfolder.toAbsolutePath().normalize()
                + "\" is not a Ballerina package. Run the command from the package root, or use -o to point to it.");
    }

    @Test(description = "Test that a folder outside any package is rejected")
    public void testOutsidePackageRejected() {
        GenerationException error = Assert.expectThrows(GenerationException.class,
                () -> PackageOutput.resolve(tmpDir, NO_MODULE));
        Assert.assertEquals(error.getMessage(), "\"" + tmpDir.toAbsolutePath().normalize()
                + "\" is not a Ballerina package. Run the command from the package root, or use -o to point to it.");
    }

    @Test(description = "Test that an invalid module name is rejected")
    public void testInvalidModuleNameRejected() throws IOException {
        Path packageRoot = createPackage(tmpDir);

        GenerationException error = Assert.expectThrows(GenerationException.class,
                () -> PackageOutput.resolve(packageRoot, "my-module"));
        Assert.assertEquals(error.getMessage(), "\"my-module\" is not a valid module name.");
    }

    private static Path createPackage(Path directory) throws IOException {
        Files.writeString(directory.resolve("Ballerina.toml"), BALLERINA_TOML);
        return directory;
    }
}
