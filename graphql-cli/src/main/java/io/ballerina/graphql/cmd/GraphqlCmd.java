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

package io.ballerina.graphql.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.graphql.cmd.generator.GenerationContext;
import io.ballerina.graphql.cmd.generator.GenerationEngine;
import io.ballerina.graphql.cmd.generator.OperationMode;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.GenerationException;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.ballerina.graphql.cmd.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_MODE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION;

/**
 * Main class to implement "graphql" command for Ballerina.
 * Commands for Client, Service and SDL Schema file generation.
 */
@CommandLine.Command(name = "graphql",
        description = "Generates Ballerina clients for GraphQL queries with GraphQL SDL, Ballerina services for " +
                "GraphQL schema and SDL schema for the given Ballerina GraphQL service.")
public class GraphqlCmd implements BLauncherCmd {
    private static final int EXIT_CODE_0 = 0;
    private static final int EXIT_CODE_1 = 1;
    private static final int EXIT_CODE_2 = 2;
    private static final String CMD_NAME = "graphql";
    private static final ExitHandler DEFAULT_EXIT_HANDLER = code -> Runtime.getRuntime().exit(code);

    private final PrintStream outStream;
    private final Path executionPath;
    private final ExitHandler exitHandler;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"},
            description = "File path to the GraphQL configuration file, GraphQL schema file or Ballerina service file.")
    private String inputPath;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Directory to store the generated Ballerina clients, Ballerina services or SDL schema file." +
                    " If this is not provided, the generated files will be stored in the current execution directory.")
    private String outputPath;

    @CommandLine.Option(names = {"-s", "--service"},
            description = "Base path of the service that the SDL schema is needed to be generated. " +
                    "If this is not provided, generate the SDL schema for each GraphQL service in the source file.")
    private String serviceBasePath;

    @CommandLine.Option(names = {"-m", "--mode"},
            description = "Ballerina operation mode. It can be client, service or schema.")
    private String mode;

    @CommandLine.Option(names = {"-r", "--use-records-for-objects"},
            description = "Inform the generator to generate records types where ever possible")
    private boolean useRecordsForObjectsFlag;

    /**
     * Functional interface for handling exit behavior.
     * Public to allow test access from other packages.
     */
    @FunctionalInterface
    public interface ExitHandler {
        void exit(int code);
    }

    /**
     * Constructor that initialize with the default values.
     */
    public GraphqlCmd() {
        this(System.err, Paths.get(System.getProperty("user.dir")));
    }

    /**
     * Constructor override, which takes output stream and execution dir as inputs.
     * Uses default exit handler that calls Runtime.getRuntime().exit().
     *
     * @param outStream    output stream from ballerina
     * @param executionDir defines the directory location of  execution of ballerina command
     */
    public GraphqlCmd(PrintStream outStream, Path executionDir) {
        this(outStream, executionDir, DEFAULT_EXIT_HANDLER);
    }

    /**
     * Constructor for testing with custom exit handler.
     * This is public to allow tests in other packages to use it.
     *
     * @param outStream    output stream from ballerina
     * @param executionDir defines the directory location of  execution of ballerina command
     * @param exitHandler  custom exit handler (for testing)
     */
    public GraphqlCmd(PrintStream outStream, Path executionDir, ExitHandler exitHandler) {
        this.outStream = outStream;
        this.executionPath = executionDir;
        this.exitHandler = exitHandler;
    }

    private void exit(int code) {
        exitHandler.exit(code);
    }

    @Override
    public void execute() {
        try {
            if (helpFlag) {
                printLongDesc(new StringBuilder());
                outStream.flush();
                exit(EXIT_CODE_0);
                return;
            }
            if (inputPath == null || inputPath.isEmpty()) {
                printLongDesc(new StringBuilder());
                outStream.flush();
                exit(EXIT_CODE_2);
                return;
            }
            validateInputFlags();
            executeOperation();
        } catch (CmdException | GenerationException e) {
            outStream.println(e.getMessage());
            exit(EXIT_CODE_1);
            return;
        }
        exit(EXIT_CODE_0);
    }

    /**
     * Validates the input flags in the GraphQL command line tool.
     *
     * @throws CmdException when a graphql command related error occurs
     */
    private void validateInputFlags() throws CmdException {
        if (!validInputFileExtension(inputPath)) {
            throw new CmdException(String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, inputPath));
        }

        if (!isModeCompatible()) {
            throw new CmdException(String.format(MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION, mode, inputPath));
        }

        if (useRecordsForObjectsFlag && !(inputPath.endsWith(GRAPHQL_EXTENSION))) {
            throw new CmdException(String.format(Constants.MESSAGE_FOR_USE_RECORDS_FOR_OBJECTS_FLAG_MISUSE, mode));
        }
    }

    private boolean validInputFileExtension(String filePath) {
        return OperationMode.isKnownExtension(filePath);
    }

    private boolean isModeCompatible() throws CmdException {
        if (mode == null) {
            return true;
        }
        Optional<OperationMode> modeFromFlag = OperationMode.fromModeFlag(mode);
        if (modeFromFlag.isEmpty()) {
            throw new CmdException(String.format(MESSAGE_FOR_INVALID_MODE, mode));
        }
        return modeFromFlag.equals(OperationMode.fromInputPath(inputPath));
    }

    /**
     * Execute the correct operation according to the given inputs.
     *
     * @throws CmdException        when a graphql command related error occurs
     * @throws GenerationException when a graphql generation related error occurs
     */
    private void executeOperation() throws CmdException, GenerationException {
        OperationMode operationMode = OperationMode.fromInputPath(inputPath).orElseThrow(
                () -> new CmdException(String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, inputPath)));
        GenerationContext context = new GenerationContext(operationMode, inputPath, getTargetOutputPath(),
                serviceBasePath, useRecordsForObjectsFlag, outStream);
        GenerationEngine.run(context);
    }

    /**
     * Gets the target output path for the code generation.
     *
     * @return the target output path for the code generation
     */
    private Path getTargetOutputPath() {
        Path targetOutputPath = executionPath;
        if (this.outputPath != null) {
            if (Paths.get(outputPath).isAbsolute()) {
                targetOutputPath = Paths.get(outputPath);
            } else {
                targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath);
            }
        }
        return targetOutputPath;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        Class<GraphqlCmd> cmdClass = GraphqlCmd.class;
        ClassLoader classLoader = cmdClass.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("ballerina-graphql.help");
        try (InputStreamReader inputStreamREader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(inputStreamREader)) {
            String content = br.readLine();
            outStream.append(content);
            while ((content = br.readLine()) != null) {
                outStream.append('\n').append(content);
            }
            outStream.append('\n');
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {
    }
}
