/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.common;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.graphql.common.TestUtils.DISTRIBUTION_FILE_NAME;
import static io.ballerina.graphql.common.TestUtils.executeGraphql;
import static io.ballerina.graphql.common.TestUtils.executeGraphqlWithErrors;

/**
 * Utility class for Graphql tests.
 */
public class GraphqlTest {
    protected Path tmpDir;
    private ByteArrayOutputStream console;
    protected PrintStream printStream;
    protected final Path resourceDir = Paths.get("src/test/resources/").toAbsolutePath();

    @BeforeClass
    public void setup() throws IOException {
        this.tmpDir = Files.createTempDirectory("graphql-cmd-test-out-" + System.nanoTime());
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @AfterClass
    public void cleanup() throws IOException {
        Files.walk(this.tmpDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });
        this.console.close();
        this.printStream.close();
    }

    @BeforeTest(description = "This will create a new ballerina project for testing below scenarios.")
    public void setupBallerinaProject() throws IOException {
        setup();
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }

    protected String readOutput(boolean status) throws IOException {
        String output = this.console.toString();
        this.console.close();
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
        if (!status) {
            PrintStream out = System.out;
            out.println(output);
        }
        return output;
    }

    protected String readContent(Path path) throws IOException {
        Stream<String> lines = Files.lines(path);
        String output = lines.collect(Collectors.joining(System.lineSeparator()));
        lines.close();
        return output.trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");
    }

    protected String readContentWithFormat(Path filePath) throws IOException {
        Stream<String> schemaLines = Files.lines(filePath);
        String schemaContent = schemaLines.collect(Collectors.joining(System.getProperty("line.separator")));
        schemaLines.close();
        return schemaContent;
    }

    protected void executeCommand(String[] args) throws IOException, InterruptedException {
        Path graphqlService = resourceDir.resolve("graphqlServices");
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "graphql");
        List<String> argList = Arrays.asList(args);
        buildArgs.addAll(argList);
        boolean successful = executeGraphql(DISTRIBUTION_FILE_NAME, graphqlService, buildArgs);
    }

    protected InputStream executeCommandWithErrors(String[] args) throws IOException, InterruptedException {
        Path graphqlService = resourceDir.resolve("graphqlServices");
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "graphql");
        List<String> argList = Arrays.asList(args);
        buildArgs.addAll(argList);
        return executeGraphqlWithErrors(DISTRIBUTION_FILE_NAME, graphqlService, buildArgs);
    }

    protected InputStream executeCommandWithErrors(Path resourcePath, String[] args)
            throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "graphql");
        List<String> argList = Arrays.asList(args);
        buildArgs.addAll(argList);
        return executeGraphqlWithErrors(DISTRIBUTION_FILE_NAME, resourcePath, buildArgs);
    }
}
