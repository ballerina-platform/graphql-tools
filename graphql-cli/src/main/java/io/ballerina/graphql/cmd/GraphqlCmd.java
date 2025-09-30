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
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.exception.ClientCodeGenerationException;
import io.ballerina.graphql.generator.client.generator.ClientCodeGenerator;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceCodeGenerator;
import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaFileGenerationException;
import io.ballerina.graphql.schema.generator.SdlSchemaGenerator;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.BAL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_MODE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MODE_CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MODE_SCHEMA;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MODE_SERVICE;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;
import static io.ballerina.graphql.schema.Constants.MESSAGE_CANNOT_READ_BAL_FILE;
import static io.ballerina.graphql.schema.Constants.MESSAGE_MISSING_BAL_FILE;

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

    private ClientCodeGenerator clientCodeGenerator;
    private ServiceCodeGenerator serviceCodeGenerator;

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
        } catch (CmdException | ParseException | ValidationException | ClientCodeGenerationException | IOException |
                 SchemaFileGenerationException | ServiceGenerationException e) {
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
        return filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION) ||
                filePath.endsWith(BAL_EXTENSION) || filePath.endsWith(GRAPHQL_EXTENSION);
    }

    private boolean isModeCompatible() throws CmdException {
        if (mode != null) {
            if (MODE_CLIENT.equals(mode)) {
                return inputPath.endsWith(Constants.YAML_EXTENSION) || inputPath.endsWith(Constants.YML_EXTENSION);
            } else if (MODE_SCHEMA.equals(mode)) {
                return inputPath.endsWith(Constants.BAL_EXTENSION);
            } else if (MODE_SERVICE.equals(mode)) {
                return inputPath.endsWith(Constants.GRAPHQL_EXTENSION);
            } else {
                throw new CmdException(String.format(MESSAGE_FOR_INVALID_MODE, mode));
            }
        }
        return true;
    }

    /**
     * Execute the correct operation according to the given inputs.
     *
     * @throws CmdException                  when a graphql command related error occurs
     * @throws ParseException                when a parsing related error occurs
     * @throws IOException                   If an I/O error occurs
     * @throws ClientCodeGenerationException when a graphql client generation related error occurs
     * @throws ValidationException           when validation related error occurs
     * @throws SchemaFileGenerationException when a SDL schema generation related error occurs
     */
    private void executeOperation()
            throws CmdException, ParseException, IOException, ValidationException, ClientCodeGenerationException,
            SchemaFileGenerationException, ServiceGenerationException {
        if ((MODE_CLIENT.equals(mode) || mode == null) &&
                (inputPath.endsWith(YAML_EXTENSION) || inputPath.endsWith(YML_EXTENSION))) {
            setClientCodeGenerator(new ClientCodeGenerator());
            generateClient(inputPath);
        } else if ((MODE_SCHEMA.equals(mode) || mode == null) && (inputPath.endsWith(BAL_EXTENSION))) {
            generateSchema(inputPath);
        } else if ((MODE_SERVICE.equals(mode) || mode == null) && (inputPath.endsWith(
                io.ballerina.graphql.schema.Constants.GRAPHQL_EXTENSION))) {
            setServiceCodeGenerator(new ServiceCodeGenerator());
            generateService(inputPath);
        }
    }

    /**
     * Generate the client according to the given configurations.
     *
     * @throws ParseException                when a parsing related error occurs
     * @throws IOException                   If an I/O error occurs
     * @throws ValidationException           when validation related error occurs
     * @throws ClientCodeGenerationException when a code generation error occurs
     */
    private void generateClient(String filePath)
            throws ParseException, IOException, ValidationException, ClientCodeGenerationException {
        Config config = readConfig(filePath);
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = populateProjects(config);
        for (GraphqlClientProject project : projects) {
            Utils.validateGraphqlProject(project);
            QueryValidator.getInstance().validate(project);
        }
        for (GraphqlProject project : projects) {
            this.clientCodeGenerator.generate(project);
        }
    }

    private void generateService(String filePath)
            throws IOException, ValidationException, ServiceGenerationException {
        File graphqlFile = new File(filePath);
        if (!graphqlFile.exists()) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_MISSING_SCHEMA_FILE, filePath));
        }
        if (!graphqlFile.canRead()) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_CAN_NOT_READ_SCHEMA_FILE, filePath));
        }
        GraphqlServiceProject graphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, filePath, getTargetOutputPath().toString());
        Utils.validateGraphqlProject(graphqlProject);
        if (useRecordsForObjectsFlag) {
            this.serviceCodeGenerator.enableToUseRecords();
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

    public void setClientCodeGenerator(ClientCodeGenerator clientCodeGenerator) {
        this.clientCodeGenerator = clientCodeGenerator;
    }

    public void setServiceCodeGenerator(ServiceCodeGenerator serviceCodeGenerator) {
        this.serviceCodeGenerator = serviceCodeGenerator;
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
