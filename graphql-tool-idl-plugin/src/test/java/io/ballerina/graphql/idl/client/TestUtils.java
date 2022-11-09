/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.idl.client;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Integration tests for IDL support.
 *
 * @since 0.3.0
 */
public class TestUtils {
    public static final PrintStream OUT = System.out;
    public static final Path TARGET_DIR = Paths.get(System.getProperty("target.dir"));
    public static final Path RESOURCE = Paths.get(System.getProperty("user.dir")).resolve("build/resources/" +
            "test/graphql-client-projects");
    public static final Path TEST_DISTRIBUTION_PATH = TARGET_DIR.resolve("extracted-distribution");
    public static final String DISTRIBUTION_FILE_NAME = System.getProperty("ballerina.version");
    private static String balFile = "bal";

    public static File[] getMatchingFiles(String project) throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, RESOURCE.resolve(project));
        File dir = new File(new File(String.valueOf(RESOURCE.resolve(project).resolve("generated"))).toString());
        final String id = "graphql_client";
        File[] matchingFiles = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains(id);
            }
        });
        return matchingFiles;
    }

    /**
     * Execute Ballerina run command.
     */
    public static Process executeRun(String distributionName, Path sourceDirectory)
            throws IOException, InterruptedException {
        List<String> args = new LinkedList<>();
        args.add(0, "run");
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        process.waitFor();
        return process;
    }

    /**
     *  Get Process from given arguments.
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return process
     * @throws IOException          Error executing build command.
     */
    public static Process getProcessBuilderResults(String distributionName, Path sourceDirectory, List<String> args)
            throws IOException {

        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        args.add(0, TEST_DISTRIBUTION_PATH.resolve(distributionName).resolve("bin").resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(args, ' '));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(sourceDirectory.toFile());
        Process process = pb.start();
        return process;
    }

    /**
     * Log the output of an input stream.
     *
     * @param inputStream The stream.
     * @throws IOException Error reading the stream.
     */
    public static String logOutput(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
}
