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

package io.ballerina.graphql.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.generator.GenerationContext;
import io.ballerina.graphql.cmd.generator.GenerationEngine;
import io.ballerina.graphql.cmd.generator.ObjectType;
import io.ballerina.graphql.cmd.generator.OperationMode;
import io.ballerina.graphql.cmd.generator.OverwritePrompt;
import io.ballerina.graphql.exception.ExitCodes;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ToolException;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import picocli.CommandLine;

import java.io.Console;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
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
        sortOptions = false,
        headerHeading = "NAME%n",
        header = GraphqlCmd.HEADER,
        synopsisHeading = "%nSYNOPSIS%n",
        customSynopsis = {
                "  bal graphql <service.bal>",
                "              [-o | --output <output>]",
                "              [--service <service-base-path>]",
                "              [--force]",
                "              [-n | --dry-run]",
                "",
                "  bal graphql <balGraphQL.toml>",
                "              [-o | --output <output>]",
                "              [--mode <client|service>]",
                "              [--module <module-name>]",
                "              [--object-type <service|record>]",
                "              [--service-name <service-name>]",
                "              [--merge]",
                "              [--license <license-file-path>]",
                "              [--force]",
                "              [-n | --dry-run]"},
        descriptionHeading = "%nDESCRIPTION%n",
        description = {GraphqlCmd.DESCRIPTION_SUMMARY, GraphqlCmd.DESCRIPTION_DETAILS},
        parameterListHeading = "%nARGUMENTS%n",
        optionListHeading = "%nOPTIONS%n",
        footerHeading = "%nEXAMPLES%n",
        footer = GraphqlCmd.EXAMPLES)
public class GraphqlCmd implements BLauncherCmd {
    private static final String CMD_NAME = "graphql";
    private static final String USAGE_SYNOPSIS = "Usage: bal graphql [OPTIONS] <input>";
    private static final String USAGE_HELP_POINTER = "Try 'bal graphql --help' for more information.";
    private static final String ERROR_MISSING_INPUT = "Missing required argument <input>.";
    private static final ExitHandler DEFAULT_EXIT_HANDLER = code -> Runtime.getRuntime().exit(code);
    private static final String ERROR_INVALID_CONFIG_FILE_NAME =
            "The GraphQL configuration file should be named \"" + BalGraphqlConfig.FILE_NAME + "\". Found \"%s\".";
    private static final String ERROR_INVALID_OBJECT_TYPE =
            "\"%s\" is not a supported value for --object-type. It should be either \"service\" or \"record\".";
    private static final String ERROR_OBJECT_TYPE_NOT_SUPPORTED =
            "The --object-type flag only applies to service generation. It cannot be used with \"%s\".";
    private static final String ERROR_SERVICE_NAME_NOT_SUPPORTED =
            "The --service-name flag only applies to service generation. It cannot be used with \"%s\".";
    private static final String ERROR_LICENSE_NOT_SUPPORTED =
            "The --license flag only applies to client and service generation. It cannot be used with \"%s\".";
    private static final String ERROR_LICENSE_EMPTY_PATH = "The --license flag needs the path of a license file.";
    private static final String ERROR_LICENSE_WITH_MERGE = "The --license flag cannot be used with --merge. "
            + "Run without --merge to regenerate the files with the license header.";
    private static final String ERROR_MODULE_NOT_SUPPORTED =
            "The --module flag only applies to client and service generation. It cannot be used with \"%s\".";
    private static final String ERROR_UNKNOWN_OPTION =
            "Unknown option \"%s\". Run \"bal graphql --help\" to see the available options.";
    private static final String ERROR_MULTIPLE_INPUTS = "Only one input file can be given. Found: %s.";
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
                    "    $ bal graphql service.bal -o ./output --service /graphql%n%n" +
                    "  Generate into the current Ballerina package from a balGraphQL.toml file (client if " +
                    "documents are configured, otherwise service):%n" +
                    "    $ bal graphql balGraphQL.toml%n%n" +
                    "  Generate a Ballerina GraphQL service, using record types for object types:%n" +
                    "    $ bal graphql balGraphQL.toml --mode service --object-type record%n%n" +
                    "  Generate a Ballerina GraphQL service named \"OrdersService\":%n" +
                    "    $ bal graphql balGraphQL.toml --mode service --service-name orders%n%n" +
                    "  Generate a Ballerina GraphQL client into the \"countries\" module of the package:%n" +
                    "    $ bal graphql balGraphQL.toml --mode client --module countries%n%n" +
                    "  Regenerate a Ballerina GraphQL service after the schema changed, keeping the resolvers " +
                    "already implemented:%n" +
                    "    $ bal graphql balGraphQL.toml --mode service --merge%n%n" +
                    "  Generate a Ballerina GraphQL service with a license header at the top of each file:%n" +
                    "    $ bal graphql balGraphQL.toml --mode service --license license.txt%n%n" +
                    "  Preview the files without writing them, then overwrite existing files:%n" +
                    "    $ bal graphql balGraphQL.toml --dry-run%n" +
                    "    $ bal graphql balGraphQL.toml --force";
    private static final String INPUT_DESCRIPTION =
            "Path to a balGraphQL.toml file (client or service generation) or a Ballerina GraphQL service " +
                    "file (schema generation).";
    private static final String OUTPUT_DESCRIPTION =
            "Directory to write the generated files to. Defaults to the current directory. For client and service "
                    + "generation, it must be the root of a Ballerina package.";
    private static final String SERVICE_DESCRIPTION =
            "Base path of the service to generate the schema for. Defaults to all GraphQL services in the file.";
    private static final String MODE_DESCRIPTION =
            "Operation mode for a balGraphQL.toml file: client or service. Inferred from the file when not " +
                    "given: client when documents are configured, otherwise service.";
    private static final String MODULE_DESCRIPTION =
            "Module of the package to write the generated client or service to. Defaults to the default module. "
                    + "The module is created if it does not exist.";
    private static final String OBJECT_TYPE_DESCRIPTION =
            "How GraphQL object types are generated in service generation: service (default) or record.";
    private static final String SERVICE_NAME_DESCRIPTION =
            "Base name for the generated service, e.g. orders gives OrdersService. Defaults to the schema file "
                    + "name, or for a url or introspection schema, the module or package name.";
    private static final String MERGE_DESCRIPTION =
            "Merge the generated service into the output files that already exist instead of overwriting them. "
                    + "Existing resolvers are kept, new ones are added, and the ones whose signature no longer matches "
                    + "the schema are reported. Records, enums and unions in the schema are regenerated, so edits to "
                    + "them are lost. Service generation only.";
    private static final String LICENSE_DESCRIPTION =
            "Path to a file whose content is added at the top of each generated Ballerina file, such as a copyright "
                    + "notice. It can only contain comment lines and blank lines. Client and service generation "
                    + "only, and not with --merge.";
    private static final String FORCE_DESCRIPTION =
            "Overwrite or merge output files that already exist without asking.";
    private static final String DRY_RUN_DESCRIPTION =
            "Show the files that would be created or overwritten, without writing them.";

    private final PrintStream helpStream;
    private final PrintStream outStream;
    private final Path executionPath;
    private final ExitHandler exitHandler;
    private final OverwritePrompt overwritePrompt;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Parameters(paramLabel = "[<input>]", hideParamSyntax = true, description = INPUT_DESCRIPTION)
    private List<String> inputs;

    @CommandLine.Unmatched
    private List<String> unknownOptions;

    private String inputPath;

    @CommandLine.Option(names = {"-o", "--output"}, paramLabel = "<output>", description = OUTPUT_DESCRIPTION)
    private String outputPath;

    @CommandLine.Option(names = "--service", paramLabel = "<service-base-path>", description = SERVICE_DESCRIPTION)
    private String serviceBasePath;

    @CommandLine.Option(names = "--mode", paramLabel = "<client|service>", description = MODE_DESCRIPTION)
    private String mode;

    @CommandLine.Option(names = "--module", paramLabel = "<module-name>", description = MODULE_DESCRIPTION)
    private String moduleName;

    @CommandLine.Option(names = "--object-type", paramLabel = "<service|record>",
            description = OBJECT_TYPE_DESCRIPTION)
    private String objectType;

    @CommandLine.Option(names = "--service-name", paramLabel = "<service-name>",
            description = SERVICE_NAME_DESCRIPTION)
    private String serviceName;

    @CommandLine.Option(names = "--merge", description = MERGE_DESCRIPTION)
    private boolean merge;

    @CommandLine.Option(names = "--license", paramLabel = "<license-file-path>", description = LICENSE_DESCRIPTION)
    private String licensePath;

    @CommandLine.Option(names = "--force", description = FORCE_DESCRIPTION)
    private boolean force;

    @CommandLine.Option(names = {"-n", "--dry-run"}, description = DRY_RUN_DESCRIPTION)
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
     * Constructor that initialize with the default values: the help goes to standard output, and everything else to
     * standard error.
     */
    public GraphqlCmd() {
        this(System.out, System.err, Paths.get(System.getProperty("user.dir")), DEFAULT_EXIT_HANDLER);
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
        this(outStream, outStream, executionDir, exitHandler);
    }

    /**
     * Constructor that writes the help to its own stream, separate from the other output.
     *
     * @param helpStream   stream the help is written to
     * @param outStream    stream errors, warnings and generation messages are written to
     * @param executionDir defines the directory location of execution of ballerina command
     * @param exitHandler  custom exit handler (for testing)
     */
    public GraphqlCmd(PrintStream helpStream, PrintStream outStream, Path executionDir, ExitHandler exitHandler) {
        this(helpStream, outStream, executionDir, exitHandler, consolePrompt(outStream));
    }

    /**
     * Constructor that takes the prompt used to ask before overwriting existing files.
     *
     * @param helpStream      stream the help is written to
     * @param outStream       stream errors, warnings and generation messages are written to
     * @param executionDir    defines the directory location of execution of ballerina command
     * @param exitHandler     custom exit handler (for testing)
     * @param overwritePrompt asks before existing files are overwritten (for testing)
     */
    public GraphqlCmd(PrintStream helpStream, PrintStream outStream, Path executionDir, ExitHandler exitHandler,
                      OverwritePrompt overwritePrompt) {
        this.helpStream = helpStream;
        this.outStream = outStream;
        this.executionPath = executionDir;
        this.exitHandler = exitHandler;
        this.overwritePrompt = overwritePrompt;
    }

    // Asks only when there is a terminal to answer from (FF-6). The question goes to the error stream (FF-5).
    private static OverwritePrompt consolePrompt(PrintStream outStream) {
        return question -> {
            Console console = System.console();
            if (console == null || !isTerminal(console)) {
                return Optional.empty();
            }
            outStream.print(question);
            outStream.flush();
            return Optional.of(Objects.requireNonNullElse(console.readLine(), ""));
        };
    }

    // From JDK 22, System.console() is not null even when input or output is redirected, so the console itself is
    // asked. JDK 21 has no isTerminal(), and there a console that is not null is already a terminal.
    private static boolean isTerminal(Console console) {
        try {
            return (boolean) Console.class.getMethod("isTerminal").invoke(console);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    private void exit(int code) {
        exitHandler.exit(code);
    }

    @Override
    public void execute() {
        try {
            if (helpFlag) {
                helpStream.print(usage());
                helpStream.flush();
                exit(ExitCodes.SUCCESS);
                return;
            }
            if (unknownOptions != null && !unknownOptions.isEmpty()) {
                throw usageError(String.format(ERROR_UNKNOWN_OPTION, unknownOptions.get(0)));
            }
            if (inputs == null || inputs.isEmpty() || inputs.get(0).isEmpty()) {
                throw usageError(ERROR_MISSING_INPUT);
            }
            validateInputs();
            inputPath = inputs.get(0);
            validateInputFlags();
            executeOperation();
        } catch (ToolException e) {
            outStream.println(e.getMessage());
            // Only a usage error points to the help (ERR-8); other errors print just the error (EXIT-2).
            if (e.exitCode() == ExitCodes.USAGE) {
                outStream.println(USAGE_SYNOPSIS);
                outStream.println(USAGE_HELP_POINTER);
            }
            exit(e.exitCode());
            return;
        } catch (GenerationException e) {
            outStream.println(e.getMessage());
            exit(ExitCodes.RUNTIME);
            return;
        }
        exit(ExitCodes.SUCCESS);
    }

    private void validateInputs() {
        if (inputs.size() > 1) {
            throw usageError(String.format(ERROR_MULTIPLE_INPUTS, String.join(", ", inputs)));
        }
    }

    private void validateInputFlags() {
        if (!validInputFileExtension(inputPath)) {
            throw usageError(String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, inputPath));
        }

        if (isConfigFileInput(inputPath) && !isBalGraphqlConfigFile(inputPath)) {
            throw usageError(String.format(ERROR_INVALID_CONFIG_FILE_NAME, inputPath));
        }

        if (!isModeCompatible()) {
            throw usageError(String.format(MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION, mode, inputPath));
        }

        if (objectType != null) {
            if (ObjectType.fromValue(objectType).isEmpty()) {
                throw usageError(String.format(ERROR_INVALID_OBJECT_TYPE, objectType));
            }
            if (!inputPath.endsWith(GRAPHQL_EXTENSION) && !isConfigFileInput(inputPath)) {
                throw usageError(String.format(ERROR_OBJECT_TYPE_NOT_SUPPORTED, inputPath));
            }
        }

        if (serviceName != null && !inputPath.endsWith(GRAPHQL_EXTENSION) && !isConfigFileInput(inputPath)) {
            throw usageError(String.format(ERROR_SERVICE_NAME_NOT_SUPPORTED, inputPath));
        }

        if (moduleName != null && OperationMode.fromInputPath(inputPath).orElse(null) == OperationMode.SCHEMA) {
            throw usageError(String.format(ERROR_MODULE_NOT_SUPPORTED, inputPath));
        }

        if (licensePath != null) {
            // An empty path would name the current directory, so it is reported as a missing value instead.
            if (licensePath.isBlank()) {
                throw usageError(ERROR_LICENSE_EMPTY_PATH);
            }
            // A schema is not Ballerina source, so it takes no license header (IO-4).
            if (OperationMode.fromInputPath(inputPath).orElse(null) == OperationMode.SCHEMA) {
                throw usageError(String.format(ERROR_LICENSE_NOT_SUPPORTED, inputPath));
            }
            // A merge keeps the header the files already have, so it cannot add or replace one.
            if (merge) {
                throw usageError(ERROR_LICENSE_WITH_MERGE);
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

    private boolean isModeCompatible() {
        if (mode == null) {
            return true;
        }
        Optional<OperationMode> modeFromFlag = OperationMode.fromModeFlag(mode);
        if (modeFromFlag.isEmpty()) {
            throw usageError(String.format(MESSAGE_FOR_INVALID_MODE, mode));
        }
        if (isConfigFileInput(inputPath)) {
            // A configuration file drives client or service generation only; either declared mode overrides what
            // the configuration contents would otherwise resolve to.
            return modeFromFlag.get() != OperationMode.SCHEMA;
        }
        return modeFromFlag.equals(OperationMode.fromInputPath(inputPath));
    }

    private void executeOperation() throws GenerationException {
        GenerationContext context = GenerationContext.builder(inputPath)
                .declaredOperationMode(OperationMode.fromModeFlag(mode).orElse(null))
                .targetOutputPath(getTargetOutputPath())
                .serviceBasePath(serviceBasePath)
                .declaredObjectType(ObjectType.fromValue(objectType).orElse(null))
                .moduleName(moduleName)
                .serviceName(serviceName)
                .force(force)
                .dryRun(dryRun)
                .merge(merge)
                .licensePath(licensePath)
                .executionPath(executionPath)
                .overwritePrompt(overwritePrompt)
                .outStream(outStream)
                .build();
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

    private static ToolException usageError(String message) {
        return new ToolException(ExitCodes.USAGE, Utils.constructGraphqlDiagnostic(
                DiagnosticMessages.GRAPHQL_CLI_100.getCode(), message, DiagnosticSeverity.ERROR, null).toString());
    }

    private String usage() {
        StringBuilder usage = new StringBuilder();
        printLongDesc(usage);
        return usage.toString();
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.getHelpSectionMap().put(CommandLine.Model.UsageMessageSpec.SECTION_KEY_DESCRIPTION,
                help -> help.description().indent(2));
        // ANSI is off because ANSI codes print as raw characters when the help goes to a file, pipe or test output.
        stringBuilder.append(commandLine.getUsageMessage(CommandLine.Help.Ansi.OFF));
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {
    }
}
