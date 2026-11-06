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

import io.ballerina.graphql.exception.ExitCodes;
import io.ballerina.graphql.exception.ToolException;
import io.ballerina.projects.util.ProjectConstants;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class OutputFileGuard {

    private static final String EXISTING_FILES_HEADER = "The following file(s) already exist.";
    private static final String EXISTING_FILE_FORMAT = "  %s";
    private static final String OVERWRITE_QUESTION = "Overwrite these file(s)? [y/N] ";
    private static final String ERROR_OVERWRITE_REFUSED = "No files were written.";
    private static final String ERROR_NOT_INTERACTIVE =
            "Cannot ask to overwrite these file(s) in a non-interactive run. Use --force to overwrite them.";
    private static final String ERROR_NOT_WRITABLE = "Cannot write to \"%s\". Check its permissions.";
    private static final String ERROR_NOT_A_DIRECTORY = "Cannot create \"%s\": \"%s\" is not a directory.";
    private static final Set<String> YES_ANSWERS = Set.of("y", "yes");

    private OutputFileGuard() {
    }

    /**
     * Checks that every file a run writes can be written (FF-4, phase 5).
     *
     * @param filePaths the files the run writes
     * @throws ToolException with exit code 1 if a destination is not writable
     */
    static void checkWritable(List<Path> filePaths) {
        filePaths.forEach(OutputFileGuard::checkWritable);
    }

    /**
     * Adds the module the run creates, if any, and each file to the dry-run report, as created or overwritten.
     *
     * @param filePaths the files the run writes
     * @param context   the context of the run
     */
    static void addToDryRunReport(List<Path> filePaths, GenerationContext context) {
        moduleToCreate(context).ifPresent(moduleRoot -> context.getDryRunReport().addModule(displayPath(moduleRoot)));
        filePaths.forEach(filePath -> context.getDryRunReport().add(Files.exists(filePath) ?
                DryRunReport.Action.OVERWRITE : DryRunReport.Action.CREATE, displayPath(filePath)));
    }

    /**
     * Asks the user before existing files are overwritten (FF-5, FF-6). It does not ask when no file exists yet or
     * with --force.
     *
     * @param filePaths the files the run writes
     * @param context   the context of the run
     * @throws ToolException with exit code 4 if the user refuses or the run cannot ask
     */
    static void confirmOverwrite(List<Path> filePaths, GenerationContext context) {
        List<Path> existingFiles = filePaths.stream().filter(Files::exists).toList();
        if (existingFiles.isEmpty() || context.isForce()) {
            return;
        }
        PrintStream outStream = context.getOutStream();
        outStream.println(EXISTING_FILES_HEADER);
        existingFiles.forEach(filePath -> outStream.println(String.format(EXISTING_FILE_FORMAT,
                displayPath(filePath))));
        Optional<String> answer = context.getOverwritePrompt().ask(OVERWRITE_QUESTION);
        if (answer.isEmpty()) {
            throw new ToolException(ExitCodes.OVERWRITE_REFUSED, ERROR_NOT_INTERACTIVE);
        }
        if (!YES_ANSWERS.contains(answer.get().trim().toLowerCase(Locale.ROOT))) {
            throw new ToolException(ExitCodes.OVERWRITE_REFUSED, ERROR_OVERWRITE_REFUSED);
        }
    }

    // A file that doesn't exist yet is created in its nearest existing directory, so that directory must be writable
    // (FF-4, phase 5).
    private static void checkWritable(Path filePath) {
        if (Files.exists(filePath)) {
            if (!Files.isWritable(filePath)) {
                throw new ToolException(ExitCodes.RUNTIME, String.format(ERROR_NOT_WRITABLE, displayPath(filePath)));
            }
            return;
        }
        Path parent = filePath.toAbsolutePath().getParent();
        while (parent != null && !Files.exists(parent)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            return;
        }
        if (!Files.isDirectory(parent)) {
            throw new ToolException(ExitCodes.RUNTIME,
                    String.format(ERROR_NOT_A_DIRECTORY, displayPath(filePath), displayPath(parent)));
        }
        if (!Files.isWritable(parent)) {
            throw new ToolException(ExitCodes.RUNTIME, String.format(ERROR_NOT_WRITABLE, displayPath(parent)));
        }
    }

    private static Optional<Path> moduleToCreate(GenerationContext context) {
        if (context.getModuleName() == null) {
            return Optional.empty();
        }
        Path moduleRoot = context.getTargetOutputPath().toAbsolutePath().normalize()
                .resolve(ProjectConstants.MODULES_ROOT).resolve(context.getModuleName());
        return Files.exists(moduleRoot) ? Optional.empty() : Optional.of(moduleRoot);
    }

    // A path can only be shown relative to the current directory when both are on the same drive (Windows).
    static Path displayPath(Path filePath) {
        Path currentDir = Path.of("").toAbsolutePath();
        Path absolutePath = filePath.toAbsolutePath();
        return currentDir.getRoot().equals(absolutePath.getRoot()) ?
                currentDir.relativize(absolutePath) : absolutePath;
    }
}

