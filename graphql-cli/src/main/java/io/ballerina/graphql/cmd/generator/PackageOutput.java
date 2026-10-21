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
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;

import java.nio.file.Path;
import java.util.regex.Pattern;

final class PackageOutput {

    private static final String ERROR_NOT_A_PACKAGE = "\"%s\" is not a Ballerina package. "
            + "Run the command from the package root, or use -o to point to it.";
    private static final String ERROR_UNREADABLE_BALLERINA_TOML = "Could not read \"%s\".";
    private static final String ERROR_INVALID_MODULE_NAME = "\"%s\" is not a valid module name. Use letters, digits "
            + "and underscores, start with a letter, and separate parts with single periods (e.g. books or books.v2).";
    private static final Pattern MODULE_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$");

    private PackageOutput() {
    }

    static Path resolve(Path outputPath, String moduleName) throws GenerationException {
        Path packageRoot = outputPath.toAbsolutePath().normalize();
        boolean isPackageRoot;
        try {
            isPackageRoot = ProjectPaths.isBuildProjectRoot(packageRoot);
        } catch (ProjectException e) {
            throw new GenerationException(String.format(ERROR_UNREADABLE_BALLERINA_TOML,
                    packageRoot.resolve(ProjectConstants.BALLERINA_TOML)));
        }
        if (!isPackageRoot) {
            throw new GenerationException(String.format(ERROR_NOT_A_PACKAGE, packageRoot));
        }
        if (moduleName == null) {
            return packageRoot;
        }
        if (!MODULE_NAME.matcher(moduleName).matches()) {
            throw new GenerationException(String.format(ERROR_INVALID_MODULE_NAME, moduleName));
        }
        return packageRoot.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
    }
}
