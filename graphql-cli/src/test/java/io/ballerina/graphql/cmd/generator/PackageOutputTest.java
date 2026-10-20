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
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.ballerina.graphql.common.TestUtils.createBallerinaPackage;

/**
 * Tests resolving the output folder of client and service generation inside a Ballerina package.
 */
public class PackageOutputTest {

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
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, NO_MODULE), packageRoot.toAbsolutePath().normalize());
    }

    @Test(description = "Test that a module resolves to the modules folder of the package")
    public void testModule() throws IOException, GenerationException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, "books"),
                packageRoot.toAbsolutePath().normalize().resolve("modules").resolve("books"));
    }

    @Test(description = "Test that a folder inside a package is rejected")
    public void testSubfolderRejected() throws IOException {
        Path subfolder = Files.createDirectory(createBallerinaPackage(tmpDir).resolve("sub"));

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

    @Test(description = "Test that a nested module name resolves to its own folder")
    public void testNestedModule() throws IOException, GenerationException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, "books.v2"),
                packageRoot.toAbsolutePath().normalize().resolve("modules").resolve("books.v2"));
    }

    @DataProvider(name = "invalidModuleNames")
    public Object[][] invalidModuleNames() {
        return new Object[][]{{"my-module"}, {""}, {".x"}, {"x."}, {"a..b"}, {"2books"}, {"_books"}};
    }

    @Test(description = "Test that an unreadable Ballerina.toml gives a clear error")
    public void testUnreadableBallerinaToml() throws IOException {
        Path ballerinaToml = createBallerinaPackage(tmpDir).resolve("Ballerina.toml");
        File file = ballerinaToml.toFile();
        if (!file.setReadable(false) || file.canRead()) {
            throw new SkipException("Cannot make Ballerina.toml unreadable on this system");
        }
        try {
            GenerationException error = Assert.expectThrows(GenerationException.class,
                    () -> PackageOutput.resolve(tmpDir, NO_MODULE));
            Assert.assertEquals(error.getMessage(), "Could not read \""
                    + tmpDir.toAbsolutePath().normalize().resolve("Ballerina.toml") + "\".");
        } finally {
            file.setReadable(true);
        }
    }

    @Test(description = "Test that an invalid module name is rejected", dataProvider = "invalidModuleNames")
    public void testInvalidModuleNameRejected(String moduleName) throws IOException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        GenerationException error = Assert.expectThrows(GenerationException.class,
                () -> PackageOutput.resolve(packageRoot, moduleName));
        Assert.assertEquals(error.getMessage(), "\"" + moduleName + "\" is not a valid module name. Use letters, "
                + "digits and underscores, start with a letter, and separate parts with single periods "
                + "(e.g. books or books.v2).");
    }
}
