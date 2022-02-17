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
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import io.ballerina.graphql.validator.SDLValidator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Main class to implement "graphql" command for Ballerina.
 * Commands for Client generation from GraphQL queries & GraphQL SDL.
 */
@CommandLine.Command(
        name = "graphql",
        description = "Generates Ballerina clients from GraphQL queries and GraphQL SDL."
)
public class GraphqlCmd implements BLauncherCmd {
    private static final String CMD_NAME = "graphql";
    private PrintStream outStream;
    private boolean exitWhenFinish;
    private Path executionPath = Paths.get(System.getProperty("user.dir"));

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "File path to the GraphQL configuration file.")
    private boolean inputPathFlag;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Directory to store the generated Ballerina clients. " +
                    "If this is not provided, the generated files will be stored in the current execution directory.")
    private String outputPath;

    @CommandLine.Parameters
    private List<String> argList;

    /**
     * Constructor that initialize with the default values.
     */
    public GraphqlCmd() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    /**
     * Constructor override, which takes output stream and execution dir as inputs.
     *
     * @param outStream      output stream from ballerina
     * @param executionDir   defines the directory location of  execution of ballerina command
     */
    public GraphqlCmd(PrintStream outStream, Path executionDir) {
        new GraphqlCmd(outStream, executionDir, true);
    }

    /**
     * Constructor override, which takes output stream and execution dir and exits when finish as inputs.
     *
     * @param outStream         output stream from ballerina
     * @param executionDir      defines the directory location of  execution of ballerina command
     * @param exitWhenFinish    exit when finish the execution
     */
    public GraphqlCmd(PrintStream outStream, Path executionDir, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.executionPath = executionDir;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        try {
            validateInputFlags();
            Config config = readConfig();
            ConfigValidator.getInstance().validate(config);
            List<GraphqlProject> projects = populateProjects(config);
            for (GraphqlProject project : projects) {
                SDLValidator.getInstance().validate(project);
                QueryValidator.getInstance().validate(project);
            }
            for (GraphqlProject project : projects) {
                CodeGenerator.getInstance().generate(project);
            }
        } catch (CmdException | ParseException | ValidationException | GenerationException | IOException e) {
            outStream.println(e.getMessage());
            exitError(this.exitWhenFinish);
        } catch (Exception e) {
            outStream.println(e);
            exitError(this.exitWhenFinish);
        }

        // Successfully exit if no error occurs
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Validates the input flags in the GraphQL command line tool.
     *
     * @throws CmdException               when a graphql command related error occurs
     */
    private void validateInputFlags() throws CmdException {
        // Check if CLI help flag argument is present
        if (helpFlag) {
            // TODO: Send a PR with cli-help/ballerina-graphql.help file to
            //  https://github.com/ballerina-platform/ballerina-lang/tree/master/cli/ballerina-cli/src/main/resources/
//            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
//            outStream.println(commandUsageInfo);
            Runtime.getRuntime().exit(0);
            return;
        }

        // Check if CLI input path flag argument is present
        if (inputPathFlag) {
            // Check if GraphQL configuration file is provided
            if (argList == null) {
                throw new CmdException(MESSAGE_FOR_MISSING_INPUT_ARGUMENT);
            }
        } else {
            // TODO: Send a PR with cli-help/ballerina-graphql.help file to
            //  https://github.com/ballerina-platform/ballerina-lang/tree/master/cli/ballerina-cli/src/main/resources/
//            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
//            outStream.println(commandUsageInfo);
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Constructs an instance of the `Config` reading the given GraphQL config file.
     *
     * @return                              the instance of the Graphql config file
     * @throws FileNotFoundException        when the GraphQL config file doesn't exist
     * @throws ParseException               when a parsing related error occurs
     * @throws CmdException                 when a graphql command related error occurs
     */
    private Config readConfig() throws FileNotFoundException, ParseException, CmdException {
        try {
            String filePath = argList.get(0);
            if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION)) {
                InputStream inputStream = new FileInputStream(new File(filePath));
                Constructor constructor = Utils.getProcessedConstructor();
                Yaml yaml = new Yaml(constructor);
                Config config = yaml.load(inputStream);
                if (config == null) {
                    throw new ParseException(MESSAGE_FOR_EMPTY_CONFIGURATION_FILE);
                }
                return config;
            } else {
                throw new CmdException(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_EXTENSION);
            }
        } catch (YAMLException e) {
            throw new ParseException(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT + e.getMessage());
        }
    }

    /**
     * Populate the projects with information given in the GraphQL config file.
     *
     * @param config         the instance of the Graphql config file
     * @return               the list of instances of the GraphQL projects
     */
    private List<GraphqlProject> populateProjects(Config config) {
        List<GraphqlProject> graphqlProjects = new ArrayList<>();
        String schema = config.getSchema();
        List<String> documents = config.getDocuments();
        Extension extensions = config.getExtensions();
        Map<String, Project> projects = config.getProjects();

        if (schema != null || documents != null || extensions != null) {
            graphqlProjects.add(new GraphqlProject(ROOT_PROJECT_NAME, schema, documents, extensions,
                    getTargetOutputPath().toString()));
        }

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                graphqlProjects.add(new GraphqlProject(projectName,
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments(),
                        projects.get(projectName).getExtensions(),
                        getTargetOutputPath().toString()));
            }
        }
        return graphqlProjects;
    }

    /**
     * Gets the target output path for the code generation.
     *
     * @return      the target output path for the code generation
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
    public void printLongDesc(StringBuilder stringBuilder) {}

    @Override
    public void printUsage(StringBuilder stringBuilder) {}

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {}

    /**
     * Exit with error code 1.
     *
     * @param exit Whether to exit or not.
     */
    private static void exitError(boolean exit) {
        if (exit) {
            Runtime.getRuntime().exit(1);
        }
    }
}
