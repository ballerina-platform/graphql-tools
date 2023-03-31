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
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.generator.ClientCodeGenerator;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceCodeGenerator;
import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaFileGenerationException;
import io.ballerina.graphql.schema.generator.SdlSchemaGenerator;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.BAL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_MODE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;
import static io.ballerina.graphql.cmd.Constants.SERVICE;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.SCHEMA;
import static io.ballerina.graphql.schema.Constants.MESSAGE_CANNOT_READ_BAL_FILE;
import static io.ballerina.graphql.schema.Constants.MESSAGE_MISSING_BAL_FILE;

/**
 * Main class to implement "graphql" command for Ballerina.
 * Commands for Client and SDL Schema file generation.
 */
@CommandLine.Command(name = "graphql",
        description = "Generates Ballerina clients for GraphQL queries with GraphQL SDL and " +
                "SDL schema for the given Ballerina GraphQL service.")
public class GraphqlCmd implements BLauncherCmd {
    private static final String CMD_NAME = "graphql";
    private static final List<String> VALID_MODES = Arrays.asList(CLIENT.toLowerCase(), SCHEMA, SERVICE);

    private PrintStream outStream;
    private boolean exitWhenFinish;
    private Path executionPath = Paths.get(System.getProperty("user.dir"));

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"},
            description = "File path to the GraphQL configuration file or Ballerina service file.")
    private boolean inputPathFlag;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Directory to store the generated Ballerina clients or SDL schema file. " +
                    "If this is not provided, the generated files will be stored in the current execution directory.")
    private String outputPath;

    @CommandLine.Option(names = {"-s", "--service"},
            description = "Base path of the service that the SDL schema is needed to be generated. " +
                    "If this is not provided, generate the SDL schema for each GraphQL service in the source file.")
    private String serviceBasePath;

    @CommandLine.Option(names = {"-m", "--mode"}, description = "Ballerina source [service]")
    private String mode;

    @CommandLine.Option(names = {"--use-records-for-objects"},
            description = "Force the generator to generate records" + " types where ever possible")
    private boolean useRecordsForObjectsFlag;

    @CommandLine.Parameters
    private List<String> argList;

    private ClientCodeGenerator clientCodeGenerator;
    private ServiceCodeGenerator serviceCodeGenerator;

    /**
     * Constructor that initialize with the default values.
     */
    public GraphqlCmd() {
        this(System.err, Paths.get(System.getProperty("user.dir")), true);
    }

    /**
     * Constructor override, which takes output stream and execution dir as inputs.
     *
     * @param outStream    output stream from ballerina
     * @param executionDir defines the directory location of  execution of ballerina command
     */
//    public GraphqlCmd(PrintStream outStream, Path executionDir) {
//        new GraphqlCmd(outStream, executionDir, true);
//    }

    /**
     * Constructor override, which takes output stream and execution dir and exits when finish as inputs.
     *
     * @param outStream      output stream from ballerina
     * @param executionDir   defines the directory location of  execution of ballerina command
     * @param exitWhenFinish exit when finish the execution
     */
    public GraphqlCmd(PrintStream outStream, Path executionDir, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.executionPath = executionDir;
        this.exitWhenFinish = exitWhenFinish;
        this.clientCodeGenerator = new ClientCodeGenerator();
        this.serviceCodeGenerator = new ServiceCodeGenerator();
    }

    @Override
    public void execute() {
        try {
            validateInputFlags();
            executeOperation();
        } catch (CmdException | ParseException | ValidationException | GenerationException | IOException |
                 SchemaFileGenerationException e) {
            outStream.println(e.getMessage());
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
     * @throws CmdException when a graphql command related error occurs
     */
    private void validateInputFlags() throws CmdException {
        // Check if CLI help flag argument is present
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            exitError(this.exitWhenFinish);
        }

        // Check if CLI input path flag argument is present
        if (inputPathFlag) {
            // Check if GraphQL configuration file is provided
            if (argList == null) {
                throw new CmdException(MESSAGE_FOR_MISSING_INPUT_ARGUMENT);
            }
        } else {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            exitError(this.exitWhenFinish);
        }

        String filePath = argList.get(0);
        if (!validInputFileExtension(filePath)) {
            throw new CmdException(MESSAGE_FOR_INVALID_FILE_EXTENSION);
        }

        if (!isModeAndFileCompatible()) {
            throw new CmdException(MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION);
        }

        if (useRecordsForObjectsFlag && !filePath.endsWith(Constants.GRAPHQL_EXTENSION)) {
            throw new CmdException(Constants.MESSAGE_FOR_USE_RECORDS_FOR_OBJECTS_FLAG_MISUSE);
        }
    }

    private boolean validInputFileExtension(String filePath) {
        return filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION) ||
                filePath.endsWith(BAL_EXTENSION) || filePath.endsWith(GRAPHQL_EXTENSION);
    }

    private boolean isModeAndFileCompatible() throws CmdException {
        String filePath = argList.get(0);
        if (mode != null) {
            // TODO: constant first
            if (mode.equals(CLIENT.toLowerCase())) {
                return filePath.endsWith(Constants.YAML_EXTENSION) || filePath.endsWith(Constants.YML_EXTENSION);
            } else if (mode.equals(SCHEMA)) {
                return filePath.endsWith(Constants.BAL_EXTENSION);
            } else if (mode.equals(SERVICE)) {
                return filePath.endsWith(Constants.GRAPHQL_EXTENSION);
            } else {
                throw new CmdException(mode.concat(MESSAGE_FOR_INVALID_MODE));
            }
        }
        return true;
    }

    /**
     * Execute the correct operation according to the given inputs.
     *
     * @throws CmdException                  when a graphql command related error occurs
     * @throws ParseException                when a parsing related error occurs
     *                                       if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION))
     *                                       {
     *                                       generateClient(filePath);
     *                                       } else if (filePath.endsWith(BAL_EXTENSION)) {
     *                                       generateSchema(filePath);
     *                                       } else if (filePath.endsWith(GRAPHQL_EXTENSION)) {
     *                                       generateService(filePath);
     *                                       } else {
     *                                       throw new CmdException(MESSAGE_FOR_INVALID_FILE_EXTENSION);
     *                                       }@throws IOException                   If an I/O error occurs
     * @throws GenerationException           when a graphql client generation related error occurs
     * @throws ValidationException           when validation related error occurs
     * @throws SchemaFileGenerationException when a SDL schema generation related error occurs
     */
    private void executeOperation()
            throws CmdException, ParseException, IOException, ValidationException, GenerationException,
            SchemaFileGenerationException {
        String filePath = argList.get(0);

        if (CLIENT.toLowerCase().equals(mode)) {
            if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION)) {
                generateClient(filePath);
            }
        } else if (SCHEMA.equals(mode)) {
            if (filePath.endsWith(BAL_EXTENSION)) {
                generateSchema(filePath);
            }
        } else if (SERVICE.equals(mode)) {
            if (filePath.endsWith(GRAPHQL_EXTENSION)) {
                generateService(filePath);
            }
        } else {
            if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION)) {
                generateClient(filePath);
            } else if (filePath.endsWith(BAL_EXTENSION)) {
                generateSchema(filePath);
            } else if (filePath.endsWith(GRAPHQL_EXTENSION)) {
                generateService(filePath);
            }
        }
    }

    /**
     * Generate the client according to the given configurations.
     *
     * @throws ParseException      when a parsing related error occurs
     * @throws IOException         If an I/O error occurs
     * @throws ValidationException when validation related error occurs
     * @throws GenerationException when a code generation error occurs
     */
    private void generateClient(String filePath)
            throws ParseException, IOException, ValidationException, GenerationException {
        Config config = readConfig(filePath);
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = populateProjects(config);
        for (GraphqlClientProject project : projects) {
            SDLValidator.getInstance().validate(project);
            QueryValidator.getInstance().validate(project);
        }
        for (GraphqlProject project : projects) {
            this.clientCodeGenerator.generate(project);
        }
    }

    private void generateService(String filePath) throws IOException, ValidationException, GenerationException {
        File graphqlFile = new File(filePath);
        if (!graphqlFile.exists()) {
            throw new ServiceGenerationException(Constants.MESSAGE_MISSING_SCHEMA_FILE);
        }
        if (!graphqlFile.canRead()) {
            throw new ServiceGenerationException(Constants.MESSAGE_CAN_NOT_READ_SCHEMA_FILE);
        }

        GraphqlServiceProject graphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, filePath, getTargetOutputPath().toString());

        SDLValidator.getInstance().validate(graphqlProject);
        if (useRecordsForObjectsFlag) {
            this.serviceCodeGenerator.enableRecordForced();
        }

        this.serviceCodeGenerator.generate(graphqlProject);

    }

    /**
     * Generate the SDL schema according to the given input file.
     *
     * @throws SchemaFileGenerationException when a SDL schema generation related error occurs
     */
    private void generateSchema(String fileName) throws SchemaFileGenerationException {
        final File balFile = new File(fileName);
        if (!balFile.exists()) {
            throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_103, null, MESSAGE_MISSING_BAL_FILE);
        }
        if (!balFile.canRead()) {
            throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_103, null,
                    MESSAGE_CANNOT_READ_BAL_FILE);
        }
        Path balFilePath = null;
        try {
            balFilePath = Paths.get(balFile.getCanonicalPath());
        } catch (IOException e) {
            throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_103, null, e.toString());
        }
        SdlSchemaGenerator.generate(balFilePath, getTargetOutputPath(), serviceBasePath, outStream);
    }

    /**
     * Constructs an instance of the `Config` reading the given GraphQL config file.
     *
     * @return the instance of the Graphql config file
     * @throws FileNotFoundException when the GraphQL config file doesn't exist
     * @throws ParseException        when a parsing related error occurs
     */
    private Config readConfig(String filePath) throws FileNotFoundException, ParseException {
        try {
            InputStream inputStream = new FileInputStream(new File(filePath));
            Constructor constructor = Utils.getProcessedConstructor();
            Yaml yaml = new Yaml(constructor);
            Config config = yaml.load(inputStream);
            if (config == null) {
                throw new ParseException(MESSAGE_FOR_EMPTY_CONFIGURATION_FILE);
            }
            return config;
        } catch (YAMLException e) {
            throw new ParseException(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT + e.getMessage());
        }
    }

    /**
     * Populate the projects with information given in the GraphQL config file.
     *
     * @param config the instance of the Graphql config file
     * @return the list of instances of the GraphQL projects
     */
    private List<GraphqlClientProject> populateProjects(Config config) {
        List<GraphqlClientProject> graphqlClientProjects = new ArrayList<>();
        String schema = config.getSchema();
        List<String> documents = config.getDocuments();
        Extension extensions = config.getExtensions();
        Map<String, Project> projects = config.getProjects();

        if (schema != null || documents != null || extensions != null) {
            graphqlClientProjects.add(new GraphqlClientProject(ROOT_PROJECT_NAME, schema, documents, extensions,
                    getTargetOutputPath().toString()));
        }

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                graphqlClientProjects.add(new GraphqlClientProject(projectName, projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments(), projects.get(projectName).getExtensions(),
                        getTargetOutputPath().toString()));
            }
        }
        return graphqlClientProjects;
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
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {
    }

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
