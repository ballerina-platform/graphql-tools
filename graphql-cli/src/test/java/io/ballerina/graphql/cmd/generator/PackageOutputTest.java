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

import io.ballerina.graphql.exception.ExitCodes;
import io.ballerina.graphql.exception.ToolException;
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
import java.util.Optional;

import static io.ballerina.graphql.common.TestUtils.createBallerinaPackage;

/**
 * Tests resolving the output directory of client and service generation inside a Ballerina package.
 */
public class PackageOutputTest {


    private Path tmpDir;

    @BeforeMethod
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("package-output");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

    @Test(description = "Test that the package root is the output directory when no module is given")
    public void testPackageRoot() throws IOException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(resolve(packageRoot), packageRoot.toAbsolutePath().normalize());
    }

    @Test(description = "Test that a module resolves to the modules directory of the package")
    public void testModule() throws IOException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, "books").outputPath(),
                packageRoot.toAbsolutePath().normalize().resolve("modules").resolve("books"));
    }

    @Test(description = "Test that a directory inside a package is rejected")
    public void testSubdirectoryRejected() throws IOException {
        Path subdirectory = Files.createDirectory(createBallerinaPackage(tmpDir).resolve("sub"));

        ToolException error = Assert.expectThrows(ToolException.class,
                () -> resolve(subdirectory));
        Assert.assertEquals(error.exitCode(), ExitCodes.INVALID_TARGET);
        Assert.assertEquals(error.getMessage(), "\"" + subdirectory.toAbsolutePath().normalize()
                + "\" is not a Ballerina package. Run the command from the package root, or use -o to point to it.");
    }

    @Test(description = "Test that a directory outside any package is rejected")
    public void testOutsidePackageRejected() {
        ToolException error = Assert.expectThrows(ToolException.class,
                () -> resolve(tmpDir));
        Assert.assertEquals(error.exitCode(), ExitCodes.INVALID_TARGET);
        Assert.assertEquals(error.getMessage(), "\"" + tmpDir.toAbsolutePath().normalize()
                + "\" is not a Ballerina package. Run the command from the package root, or use -o to point to it.");
    }

    @Test(description = "Test that a nested module name resolves to its own directory")
    public void testNestedModule() throws IOException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, "books.v2").outputPath(),
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
            ToolException error = Assert.expectThrows(ToolException.class,
                    () -> resolve(tmpDir));
            Assert.assertEquals(error.exitCode(), ExitCodes.INVALID_TARGET);
            Assert.assertEquals(error.getMessage(), "Could not read \""
                    + tmpDir.toAbsolutePath().normalize().resolve("Ballerina.toml") + "\".");
        } finally {
            file.setReadable(true);
        }
    }

    @Test(description = "Test that an invalid module name is rejected", dataProvider = "invalidModuleNames")
    public void testInvalidModuleNameRejected(String moduleName) throws IOException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        ToolException error = Assert.expectThrows(ToolException.class,
                () -> PackageOutput.resolve(packageRoot, moduleName));
        Assert.assertEquals(error.exitCode(), ExitCodes.USAGE);
        Assert.assertEquals(error.getMessage(), "\"" + moduleName + "\" is not a valid module name. Use letters, "
                + "digits and underscores, start with a letter, and separate parts with single periods "
                + "(e.g. books or books.v2).");
    }

    @Test(description = "Test that an invalid module name is rejected before the package is checked")
    public void testInvalidModuleNameOutsidePackage() {
        ToolException error = Assert.expectThrows(ToolException.class, () -> PackageOutput.resolve(tmpDir, "2x"));
        Assert.assertEquals(error.exitCode(), ExitCodes.USAGE);
        Assert.assertTrue(error.getMessage().startsWith("\"2x\" is not a valid module name."), error.getMessage());
    }

    @Test(description = "Test that a Ballerina.toml with a syntax error is rejected instead of being read in part")
    public void testInvalidBallerinaTomlRejected() throws IOException {
        Path ballerinaToml = Files.writeString(tmpDir.resolve("Ballerina.toml"), "[package\nname = \"shop\"\n");

        ToolException error = Assert.expectThrows(ToolException.class, () -> resolve(tmpDir));
        Assert.assertEquals(error.exitCode(), ExitCodes.INVALID_TARGET);
        Assert.assertTrue(error.getMessage().startsWith("\"" + ballerinaToml.toAbsolutePath().normalize()
                + "\" is not valid TOML: "), error.getMessage());
    }

    @Test(description = "Test that a workspace root without a package is rejected")
    public void testWorkspaceRootRejected() throws IOException {
        Files.writeString(tmpDir.resolve("Ballerina.toml"), "[workspace]\npackages = [\"shop\"]\n");

        ToolException error = Assert.expectThrows(ToolException.class, () -> resolve(tmpDir));
        Assert.assertEquals(error.exitCode(), ExitCodes.INVALID_TARGET);
        Assert.assertEquals(error.getMessage(), "\"" + tmpDir.toAbsolutePath().normalize()
                + "\" is not a Ballerina package. Run the command from the package root, or use -o to point to it.");
    }

    @Test(description = "Test that a Ballerina.toml with both a package and a workspace is a package")
    public void testPackageWithWorkspace() throws IOException {
        Files.writeString(tmpDir.resolve("Ballerina.toml"),
                "[package]\norg = \"test\"\nname = \"shop\"\n\n[workspace]\npackages = [\"shop\"]\n");

        PackageOutput packageOutput = PackageOutput.resolve(tmpDir, null);
        Assert.assertEquals(packageOutput.outputPath(), tmpDir.toAbsolutePath().normalize());
        Assert.assertEquals(packageOutput.packageName(), Optional.of("shop"));
    }

    @Test(description = "Test that the package name is read from Ballerina.toml")
    public void testPackageName() throws IOException {
        Path packageRoot = createBallerinaPackage(tmpDir);

        Assert.assertEquals(PackageOutput.resolve(packageRoot, null).packageName(), Optional.of("graphql_test"));
    }

    @Test(description = "Test that a package with no name falls back to a name made from its directory")
    public void testPackageWithoutName() throws IOException {
        Path packageRoot = Files.createDirectory(tmpDir.resolve("my-shop"));
        Files.writeString(packageRoot.resolve("Ballerina.toml"), "[package]\norg = \"test\"\n");

        PackageOutput packageOutput = PackageOutput.resolve(packageRoot, null);
        Assert.assertEquals(packageOutput.packageName(), Optional.empty());
        Assert.assertEquals(packageOutput.directoryName(), "my_shop");
    }

    private static Path resolve(Path outputPath) {
        return PackageOutput.resolve(outputPath, null).outputPath();
    }
}
