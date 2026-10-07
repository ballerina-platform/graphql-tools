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
import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.generator.GenerationContext;
import io.ballerina.graphql.cmd.generator.GenerationEngine;
import io.ballerina.graphql.cmd.generator.ObjectType;
import io.ballerina.graphql.cmd.generator.OperationMode;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.GenerationException;
import picocli.CommandLine;

import java.io.PrintStream;
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
        usageHelpWidth = 100,
        separator = " ",
        sortOptions = false,
        headerHeading = "NAME%n",
        header = GraphqlCmd.HEADER,
        synopsisHeading = "%nSYNOPSIS%n",
        customSynopsis = {
                "  bal graphql <service.bal>",
                "              [-o <output>]",
                "              [-s <service-base-path>]",
                "              [--force]",
                "              [--dry-run]",
                "",
                "  bal graphql <balGraphQL.toml>",
                "              [-o <output>]",
                "              [-m <client|service>]",
                "              [--object-type <service|record>]",
                "              [--force]",
                "              [--dry-run]"},
        descriptionHeading = "%nDESCRIPTION%n",
        description = {GraphqlCmd.DESCRIPTION_SUMMARY, GraphqlCmd.DESCRIPTION_DETAILS},
        parameterListHeading = "%nARGUMENTS%n",
        optionListHeading = "%nOPTIONS%n",
        footerHeading = "%nEXAMPLES%n",
        footer = GraphqlCmd.EXAMPLES)
public class GraphqlCmd implements BLauncherCmd {
    private static final int EXIT_CODE_0 = 0;
    private static final int EXIT_CODE_1 = 1;
    private static final int EXIT_CODE_2 = 2;
    private static final String CMD_NAME = "graphql";
    private static final ExitHandler DEFAULT_EXIT_HANDLER = code -> Runtime.getRuntime().exit(code);
    private static final String ERROR_INVALID_CONFIG_FILE_NAME =
            "The GraphQL configuration file should be named \"" + BalGraphqlConfig.FILE_NAME + "\". Found \"%s\".";
    private static final String ERROR_INVALID_OBJECT_TYPE =
            "\"%s\" is not a supported value for --object-type. It should be either \"service\" or \"record\".";
    private static final String ERROR_OBJECT_TYPE_NOT_SUPPORTED =
            "The --object-type flag only applies to service generation. It cannot be used with \"%s\".";
    // Not private: the @Command annotation on the class can only reference non-private constants.
    static final String HEADER =
            "  bal graphql - Generate Ballerina GraphQL services and clients, and GraphQL schemas";
    static final String DESCRIPTION_SUMMARY =
            "Generate Ballerina GraphQL services and clients, and GraphQL schemas.";
    static final String DESCRIPTION_DETAILS =
            "Services and clients are generated from a balGraphQL.toml file, and schemas are generated " +
                    "from a Ballerina GraphQL service.";
    static final String EXAMPLES =
            "  Generate a GraphQL schema for a Ballerina GraphQL service:%n" +
                    "    $ bal graphql service.bal -o ./output -s /graphql%n%n" +
                    "  Generate from a balGraphQL.toml file (client if documents are configured, " +
                    "otherwise service):%n" +
                    "    $ bal graphql balGraphQL.toml -o ./output%n%n" +
                    "  Generate a Ballerina GraphQL service, using record types for object types:%n" +
                    "    $ bal graphql balGraphQL.toml -m service -o ./output --object-type record%n%n" +
                    "  Preview the files without writing them, then overwrite existing files:%n" +
                    "    $ bal graphql balGraphQL.toml -o ./output --dry-run%n" +
                    "    $ bal graphql balGraphQL.toml -o ./output --force";
    private static final String INPUT_DESCRIPTION =
            "Path to a balGraphQL.toml file (client or service generation) or a Ballerina GraphQL service " +
                    "file (schema generation).";
    private static final String OUTPUT_DESCRIPTION =
            "Directory to write the generated files to. Defaults to the current directory.";
    private static final String SERVICE_DESCRIPTION =
            "Base path of the service to generate the schema for. Defaults to all GraphQL services in the file.";
    private static final String MODE_DESCRIPTION =
            "Operation mode for a balGraphQL.toml file: client or service. Inferred from the file when not " +
                    "given: client when documents are configured, otherwise service.";
    private static final String OBJECT_TYPE_DESCRIPTION =
            "How GraphQL object types are generated in service generation: service (default) or record.";
    private static final String FORCE_DESCRIPTION =
            "Overwrite output files that already exist. Without it, existing files are skipped with a warning.";
    private static final String DRY_RUN_DESCRIPTION =
            "Show the files that would be created, overwritten or skipped, without writing them.";

    private final PrintStream outStream;
    private final Path executionPath;
    private final ExitHandler exitHandler;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Parameters(arity = "0..1", paramLabel = "<input>", description = INPUT_DESCRIPTION)
    private String inputPath;

    @CommandLine.Option(names = {"-o", "--output"}, paramLabel = "<output>", description = OUTPUT_DESCRIPTION)
    private String outputPath;

    @CommandLine.Option(names = {"-s", "--service"}, paramLabel = "<service-base-path>",
            description = SERVICE_DESCRIPTION)
    private String serviceBasePath;

    @CommandLine.Option(names = {"-m", "--mode"}, paramLabel = "<client|service>", description = MODE_DESCRIPTION)
    private String mode;

    @CommandLine.Option(names = "--object-type", paramLabel = "<service|record>",
            description = OBJECT_TYPE_DESCRIPTION)
    private String objectType;

    @CommandLine.Option(names = "--force", description = FORCE_DESCRIPTION)
    private boolean force;

    @CommandLine.Option(names = "--dry-run", description = DRY_RUN_DESCRIPTION)
    private boolean dryRun;

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

    private void validateInputFlags() throws CmdException {
        if (!validInputFileExtension(inputPath)) {
            throw new CmdException(String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, inputPath));
        }

        if (isConfigFileInput(inputPath) && !isBalGraphqlConfigFile(inputPath)) {
            throw new CmdException(String.format(ERROR_INVALID_CONFIG_FILE_NAME, inputPath));
        }

        if (!isModeCompatible()) {
            throw new CmdException(String.format(MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION, mode, inputPath));
        }

        if (objectType != null) {
            if (ObjectType.fromValue(objectType).isEmpty()) {
                throw new CmdException(String.format(ERROR_INVALID_OBJECT_TYPE, objectType));
            }
            if (!inputPath.endsWith(GRAPHQL_EXTENSION) && !isConfigFileInput(inputPath)) {
                throw new CmdException(String.format(ERROR_OBJECT_TYPE_NOT_SUPPORTED, inputPath));
            }
        }
    }

    private boolean validInputFileExtension(String filePath) {
        return OperationMode.isKnownExtension(filePath);
    }

    private boolean isConfigFileInput(String filePath) {
        return filePath.endsWith(BalGraphqlConfig.FILE_EXTENSION);
    }

    private boolean isBalGraphqlConfigFile(String filePath) {
        Path fileName = Paths.get(filePath).getFileName();
        return fileName != null && BalGraphqlConfig.FILE_NAME.equals(fileName.toString());
    }

    private boolean isModeCompatible() throws CmdException {
        if (mode == null) {
            return true;
        }
        Optional<OperationMode> modeFromFlag = OperationMode.fromModeFlag(mode);
        if (modeFromFlag.isEmpty()) {
            throw new CmdException(String.format(MESSAGE_FOR_INVALID_MODE, mode));
        }
        if (isConfigFileInput(inputPath)) {
            // A configuration file drives client or service generation only; either declared mode overrides what
            // the configuration contents would otherwise resolve to.
            return modeFromFlag.get() != OperationMode.SCHEMA;
        }
        return modeFromFlag.equals(OperationMode.fromInputPath(inputPath));
    }

    private void executeOperation() throws GenerationException {
        GenerationContext context = new GenerationContext(inputPath,
                OperationMode.fromModeFlag(mode).orElse(null), getTargetOutputPath(), serviceBasePath,
                ObjectType.fromValue(objectType).orElse(null), force, dryRun, outStream);
        GenerationEngine.run(context);
        if (dryRun) {
            context.getDryRunReport().print(outStream);
        }
    }

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
        CommandLine commandLine = new CommandLine(this);
        commandLine.getHelpSectionMap().put(CommandLine.Model.UsageMessageSpec.SECTION_KEY_DESCRIPTION,
                help -> help.description().indent(2));
        // ANSI is off because ANSI codes print as raw characters when the help goes to a file, pipe or test output.
        commandLine.usage(outStream, CommandLine.Help.Ansi.OFF);
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {
    }
}
