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
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The Ballerina package that client or service generation writes to.
 *
 * @param packageRoot the root folder of the package
 * @param outputPath  the folder the generated files are written to: the package root, or a module folder in it
 * @param packageName the name set in {@code [package]} of {@code Ballerina.toml}, if any
 */
record PackageOutput(Path packageRoot, Path outputPath, Optional<String> packageName) {

    private static final String ERROR_NOT_A_PACKAGE = "\"%s\" is not a Ballerina package. "
            + "Run the command from the package root, or use -o to point to it.";
    private static final String ERROR_UNREADABLE_BALLERINA_TOML = "Could not read \"%s\".";
    private static final String ERROR_INVALID_BALLERINA_TOML = "\"%s\" is not valid TOML: %s";
    private static final String ERROR_INVALID_MODULE_NAME = "\"%s\" is not a valid module name. Use letters, digits "
            + "and underscores, start with a letter, and separate parts with single periods (e.g. books or books.v2).";
    private static final Pattern MODULE_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$");
    private static final String PACKAGE_KEY = "package";
    private static final String WORKSPACE_KEY = "workspace";
    private static final String NAME_KEY = "name";

    static PackageOutput resolve(Path outputPath, String moduleName) throws GenerationException {
        Path packageRoot = outputPath.toAbsolutePath().normalize();
        Path ballerinaToml = packageRoot.resolve(ProjectConstants.BALLERINA_TOML);
        if (!Files.isRegularFile(ballerinaToml)) {
            throw new GenerationException(String.format(ERROR_NOT_A_PACKAGE, packageRoot));
        }
        Map<String, Object> content = read(ballerinaToml);
        // The same rule as ProjectPaths.isBuildProjectRoot: a workspace root without a [package] is not a package.
        Object packageTable = content.get(PACKAGE_KEY);
        if (packageTable == null && content.containsKey(WORKSPACE_KEY)) {
            throw new GenerationException(String.format(ERROR_NOT_A_PACKAGE, packageRoot));
        }
        Optional<String> packageName = packageTable instanceof Map<?, ?> table
                && table.get(NAME_KEY) instanceof String name ? Optional.of(name) : Optional.empty();
        if (moduleName == null) {
            return new PackageOutput(packageRoot, packageRoot, packageName);
        }
        if (!MODULE_NAME.matcher(moduleName).matches()) {
            throw new GenerationException(String.format(ERROR_INVALID_MODULE_NAME, moduleName));
        }
        return new PackageOutput(packageRoot,
                packageRoot.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName), packageName);
    }

    /**
     * Returns the name Ballerina gives a package that sets no name, made from its folder name.
     */
    String directoryName() {
        return ProjectUtils.defaultName(packageRoot).value();
    }

    private static Map<String, Object> read(Path ballerinaToml) throws GenerationException {
        Toml toml;
        try {
            toml = Toml.read(ballerinaToml);
        } catch (IOException e) {
            throw new GenerationException(String.format(ERROR_UNREADABLE_BALLERINA_TOML, ballerinaToml));
        }
        Optional<Diagnostic> syntaxError = toml.diagnostics().stream()
                .filter(diagnostic -> diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR)
                .findFirst();
        if (syntaxError.isPresent()) {
            throw new GenerationException(String.format(ERROR_INVALID_BALLERINA_TOML, ballerinaToml,
                    syntaxError.get().message()));
        }
        return toml.toMap();
    }
}
