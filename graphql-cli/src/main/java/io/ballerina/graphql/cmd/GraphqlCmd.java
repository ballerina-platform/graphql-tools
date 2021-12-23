/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.graphql.cmd.mappers.Extension;
import io.ballerina.graphql.cmd.mappers.GraphqlConfig;
import io.ballerina.graphql.cmd.mappers.Project;
import io.ballerina.graphql.exceptions.BallerinaGraphqlDocumentPathValidationException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlIntospectionException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlQueryValidationException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlSDLValidationException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlSchemaPathValidationException;
import io.ballerina.graphql.generators.CodeGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.formatter.core.FormatterException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_YAML;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_YAML;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_GRAPHQL_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;
import static io.ballerina.graphql.cmd.Constants.URL_RECOGNIZER;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.cmd.Utils.isValidURL;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Main class to implement "graphql" command for Ballerina.
 * Commands for Client generation from GraphQL queries & GraphQL SDL.
 */
@CommandLine.Command(
        name = "graphql",
        description = "Generates Ballerina clients from GraphQL queries and GraphQL SDL."
)
public class GraphqlCmd implements BLauncherCmd {
    private static final Log log = LogFactory.getLog(GraphqlCmd.class);
    private static final String CMD_NAME = "graphql";
    private PrintStream outStream;
    private boolean exitWhenFinish;
    private Path executionPath = Paths.get(System.getProperty("user.dir"));

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "File path to the GraphQL configuration file.")
    private boolean inputPath;

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

        // Check if CLI help flag argument is present
        if (helpFlag) {
            // TODO: Send a PR with cli-help/ballerina-graphql.help file to
            //  https://github.com/ballerina-platform/ballerina-lang/tree/master/cli/ballerina-cli/src/main/resources/
//            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
//            outStream.println(commandUsageInfo);
            return;
        }

        // Check if CLI input path argument is present
        if (inputPath) {
            // Check if GraphQL configuration file is provided
            if (argList == null) {
                outStream.println(MESSAGE_FOR_MISSING_INPUT_ARGUMENT);
                exitError(this.exitWhenFinish);
                return;
            }

            try {
                String filePath = argList.get(0);
                GraphqlConfig graphqlConfig = getGraphQLConfig(filePath);
                if (graphqlConfig != null) {
                    generateCode(graphqlConfig);
                }
            } catch (YAMLException e) {
                outStream.println(MESSAGE_FOR_INVALID_CONFIGURATION_YAML);
                exitError(this.exitWhenFinish);
            } catch (BallerinaGraphqlSDLValidationException | BallerinaGraphqlQueryValidationException |
                    BallerinaGraphqlException | IOException e) {
                outStream.println(e.getMessage());
                exitError(this.exitWhenFinish);
            } catch (Exception e) {
                outStream.println(e);
                exitError(this.exitWhenFinish);
            }
        } else {
            // TODO: Send a PR with cli-help/ballerina-graphql.help file to
            //  https://github.com/ballerina-platform/ballerina-lang/tree/master/cli/ballerina-cli/src/main/resources/
//            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
//            outStream.println(commandUsageInfo);
//            exitError(this.exitWhenFinish);
            return;
        }

        // Successfully exit if no error occurs
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Constructs an instance of the `GraphqlConfig` reading the given GraphQL config file.
     *
     * @param filePath         the path of the GraphQl config file
     * @return                 the instance of the Graphql config file
     */
    private GraphqlConfig getGraphQLConfig(String filePath)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException, IOException {
        if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION)) {
            InputStream inputStream = new FileInputStream(new File(filePath));
            Constructor constructor = Utils.getProcessedConstructor();
            Yaml yaml = new Yaml(constructor);
            GraphqlConfig graphqlConfig = yaml.load(inputStream);
            if (graphqlConfig == null) {
                outStream.println(MESSAGE_FOR_EMPTY_CONFIGURATION_YAML);
                exitError(this.exitWhenFinish);
                return null;
            }
            validateGraphQLConfig(graphqlConfig);
            return graphqlConfig;
        } else {
            outStream.println(MESSAGE_FOR_MISSING_GRAPHQL_CONFIGURATION_FILE);
            exitError(this.exitWhenFinish);
            return null;
        }
    }

    /**
     * Validates the given GraphQL config file.
     *
     * @param graphqlConfig         the instance of the Graphql config file
     */
    private void validateGraphQLConfig(GraphqlConfig graphqlConfig)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException, IOException {
        validateAllProjectsConfiguration(graphqlConfig);
        validateAllProjectsContent(graphqlConfig);
    }

    /**
     * Validates the configuration of all the projects in the given GraphQL config file.
     *
     * @param graphqlConfig         the instance of the Graphql config file
     */
    private void validateAllProjectsConfiguration(GraphqlConfig graphqlConfig) throws IOException {
        String schema = graphqlConfig.getSchema();
        List<String> documents = graphqlConfig.getDocuments();
        Map<String, Project> projects = graphqlConfig.getProjects();

        validateProjectConfiguration(schema, documents);

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                validateProjectConfiguration(projects.get(projectName));
            }
        }
    }

    /**
     * Validates the configuration of the root project in the GraphQL config file.
     *
     * @param schema         the schema value of the Graphql config file
     * @param documents      the documents value of the Graphql config file
     */
    private void validateProjectConfiguration(String schema, List<String> documents) throws IOException {
        validateExistenceOfSchemaAndDocuments(schema, documents);
        validateSchemaAndDocumentsConfiguration(schema, documents);
    }

    /**
     * Validates the configuration of a project in the GraphQL config file.
     *
     * @param project         a project of the Graphql config file
     */
    private void validateProjectConfiguration(Project project) throws IOException {
        validateExistenceOfProject(project);
        validateExistenceOfSchemaAndDocuments(project.getSchema(), project.getDocuments());
        validateSchemaAndDocumentsConfiguration(project.getSchema(), project.getDocuments());
    }

    /**
     * Validates the existence of schema & document sections of the GraphQL config file.
     *
     * @param schema         the schema section of the Graphql config file
     * @param documents      the documents' section of the Graphql config file
     */
    private void validateExistenceOfSchemaAndDocuments(String schema, List<String> documents) {
        if ((schema == null && documents != null) || (schema != null && documents == null)) {
            outStream.println(MESSAGE_FOR_INVALID_CONFIGURATION_YAML);
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Validates the configuration of the schema & documents in the GraphQL config file.
     *
     * @param schema         the schema value of the Graphql config file
     * @param documents      the documents value of the Graphql config file
     */
    private void validateSchemaAndDocumentsConfiguration(String schema, List<String> documents) throws IOException {
        if (schema != null && documents != null && schema.startsWith(URL_RECOGNIZER)) {
            validateSchemaUrl(schema);
        }

        if (schema != null && documents != null && !schema.startsWith(URL_RECOGNIZER)) {
            File schemaFile = new File(schema);
            Path schemaPath = Paths.get(schemaFile.getCanonicalPath());
            try {
                Utils.validateSchemaPath(schemaPath);
            } catch (BallerinaGraphqlSchemaPathValidationException e) {
                outStream.println(e.getMessage());
                exitError(this.exitWhenFinish);
            }

            for (String document : documents) {
                File documentFile = new File(document);
                Path documentPath = Paths.get(documentFile.getCanonicalPath());
                try {
                    Utils.validateDocumentPath(documentPath);
                } catch (BallerinaGraphqlDocumentPathValidationException e) {
                    outStream.println(e.getMessage());
                    exitError(this.exitWhenFinish);
                }
            }
        }
    }

    /**
     * Validates the existence of projects section in the GraphQL config file.
     *
     * @param project         a project of the Graphql config file
     */
    private void validateExistenceOfProject(Project project) {
        if (project == null) {
            outStream.println(MESSAGE_FOR_INVALID_CONFIGURATION_YAML);
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Validates the SDL & queries of all the projects in the given GraphQL config file.
     *
     * @param graphqlConfig         the instance of the Graphql config file
     */
    private void validateAllProjectsContent(GraphqlConfig graphqlConfig)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException {
        String schema = graphqlConfig.getSchema();
        List<String> documents = graphqlConfig.getDocuments();
        Extension extensions = graphqlConfig.getExtensions();
        Map<String, Project> projects = graphqlConfig.getProjects();

        validateProjectContent(schema, documents, extensions);

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                Extension projectExtensions = projects.get(projectName).getExtensions();
                validateProjectContent(
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments(),
                        projectExtensions);
            }
        }
    }

    /**
     * Validates the SDL & queries of a given project in the given GraphQL config file.
     *
     * @param schema         the schema value of the Graphql config file
     * @param documents      the documents value of the Graphql config file
     * @param extensions     the extensions value of the Graphql config file
     */
    private void validateProjectContent(String schema, List<String> documents, Extension extensions)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException  {
        if (schema != null && documents != null) {
            for (String document : documents) {
                validate(schema, document, extensions);
            }
        }
    }

    /**
     * Validates the given SDL and GraphQL queries.
     *
     * @param schema         the schema
     * @param document       the document
     */
    private void validate(String schema, String document, Extension extensions)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException {
        try {
            GraphQLSchema graphQLSchema = Utils.getGraphQLSchemaDocument(schema, extensions);

            Document parsedDocument = Utils.getGraphQLQueriesDocument(document);

            Validator validator = new Validator();
            List<ValidationError> validationErrorList = validator.validateDocument(graphQLSchema, parsedDocument);
            if (validationErrorList.size() > 0) {
                throw new BallerinaGraphqlQueryValidationException("Graph Query validation exception",
                        validationErrorList);
            }
        } catch (BallerinaGraphqlIntospectionException | BallerinaGraphqlSchemaPathValidationException |
                BallerinaGraphqlDocumentPathValidationException e) {
            outStream.println(e.getMessage());
            exitError(this.exitWhenFinish);
        } catch (SchemaProblem e) {
            throw new BallerinaGraphqlSDLValidationException("GraphQL SDL validation exception", e.getErrors());
        } catch (BallerinaGraphqlQueryValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new BallerinaGraphqlException("Exception ", e);
        }
    }

    /**
     * Validates the schema URL.
     *
     * @param schema         the path to the schema
     */
    private void validateSchemaUrl(String schema) {
        if (!isValidURL(schema)) {
            outStream.println("Invalid URL " + schema);
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Generates the code for the projects given in the GraphQL config file.
     *
     * @param graphqlConfig         the instance of the Graphql config file
     */
    private void generateCode(GraphqlConfig graphqlConfig) throws IOException {
        try {
            CodeGenerator codeGenerator = new CodeGenerator();
            String schema = graphqlConfig.getSchema();
            List<String> documents = graphqlConfig.getDocuments();
            Extension extensions = graphqlConfig.getExtensions();
            Map<String, Project> projects = graphqlConfig.getProjects();

            codeGenerator.generateProjectCode(schema, documents, extensions, getTargetOutputPath().toString(),
                    ROOT_PROJECT_NAME);
            if (projects != null) {
                for (String projectName : projects.keySet()) {
                    Extension projectExtensions = projects.get(projectName).getExtensions();
                    codeGenerator.generateProjectCode(
                            projects.get(projectName).getSchema(),
                            projects.get(projectName).getDocuments(),
                            projectExtensions,
                            getTargetOutputPath().toString(),
                            projectName);
                }
            }
        } catch (BallerinaGraphqlIntospectionException | BallerinaGraphqlSchemaPathValidationException |
                BallerinaGraphqlDocumentPathValidationException | FormatterException e) {
            outStream.println(e.getMessage());
            exitError(this.exitWhenFinish);
        }
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
