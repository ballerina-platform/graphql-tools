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

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Collects the planned action for each output file during a dry run and prints them with a summary.
public final class DryRunReport {

    private static final String HEADER = "Dry run: no files were written.";
    private static final String LINE_FORMAT = "  %-10s %s";
    private static final String SKIP_NOTE = "  (already exists, use --force to overwrite)";
    private static final String SUMMARY = "%d to create, %d to overwrite, %d to skip.";

    private final List<PlannedFile> files = new ArrayList<>();

    void add(Action action, Path path) {
        files.add(new PlannedFile(action, path));
    }

    public void print(PrintStream outStream) {
        outStream.println(HEADER);
        outStream.println();
        for (PlannedFile file : files) {
            String line = String.format(LINE_FORMAT, file.action().label(), file.path());
            outStream.println(file.action() == Action.SKIP ? line + SKIP_NOTE : line);
        }
        outStream.println();
        outStream.println(String.format(SUMMARY, count(Action.CREATE), count(Action.OVERWRITE), count(Action.SKIP)));
    }

    private long count(Action action) {
        return files.stream().filter(file -> file.action() == action).count();
    }

    enum Action {
        CREATE, OVERWRITE, SKIP;

        String label() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private record PlannedFile(Action action, Path path) {
    }
}
