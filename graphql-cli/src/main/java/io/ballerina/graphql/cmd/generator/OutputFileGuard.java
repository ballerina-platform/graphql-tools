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

import java.nio.file.Files;
import java.nio.file.Path;

final class OutputFileGuard {

    private static final String WARNING_FILE_EXISTS =
            "WARNING: Skipped \"%s\": the file already exists. Use --force to overwrite it.";

    private OutputFileGuard() {
    }

    static boolean canWrite(Path filePath, GenerationContext context) {
        Path displayPath = displayPath(filePath);
        boolean exists = Files.exists(filePath);
        if (context.isDryRun()) {
            DryRunReport.Action action = !exists ? DryRunReport.Action.CREATE
                    : context.isForce() ? DryRunReport.Action.OVERWRITE : DryRunReport.Action.SKIP;
            context.getDryRunReport().add(action, displayPath);
            return false;
        }
        if (!exists || context.isForce()) {
            return true;
        }
        context.getOutStream().println(String.format(WARNING_FILE_EXISTS, displayPath));
        return false;
    }

    // A path can only be shown relative to the current directory when both are on the same drive (Windows).
    private static Path displayPath(Path filePath) {
        Path currentDir = Path.of("").toAbsolutePath();
        Path absolutePath = filePath.toAbsolutePath();
        return currentDir.getRoot().equals(absolutePath.getRoot()) ?
                currentDir.relativize(absolutePath) : absolutePath;
    }
}
