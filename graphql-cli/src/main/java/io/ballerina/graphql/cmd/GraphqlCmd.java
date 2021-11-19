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
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import io.ballerina.cli.BLauncherCmd;
import io.ballerina.graphql.cmd.mappers.GraphqlConfig;
import io.ballerina.graphql.cmd.mappers.Project;
import io.ballerina.graphql.exceptions.BallerinaGraphqlException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlQueryValidationException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlSDLValidationException;
import io.ballerina.graphql.generators.CodeGenerator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_YAML;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_GRAPHQL_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;

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

        // Check if CLI input argument is present
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
     * Constructs `GraphqlConfig` reading the given GraphQL config file.
     *
     * @param filePath         the path of the GraphQl config file
     * @return                 the instance of the Graphql config file
     */
    private GraphqlConfig getGraphQLConfig(String filePath)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException, IOException {
        if (filePath.endsWith(YAML_EXTENSION)) {
            InputStream inputStream = new FileInputStream(new File(filePath));
            Yaml yaml = new Yaml(new Constructor(GraphqlConfig.class));
            GraphqlConfig graphqlConfig = yaml.load(inputStream);
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
        String schema = graphqlConfig.getSchema();
        List<String> documents = graphqlConfig.getDocuments();
        Map<String, Project> projects = graphqlConfig.getProjects();

        validateExistenceOfSchemaAndDocuments(schema, documents);

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                validateExistenceOfProject(projects.get(projectName), projectName);

                validateSchemaAndDocuments(projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments());
            }
        }
        validateSchemaAndDocuments(schema, documents);

        validateProjects(graphqlConfig);
    }

    /**
     * Validates the existence of schema & document sections of the GraphQL config file.
     *
     * @param schema         the schema section of the Graphql config file
     * @param documents      the documents section of the Graphql config file
     */
    private void validateExistenceOfSchemaAndDocuments(String schema, List<String> documents) {
        if ((schema == null && documents != null) || (schema != null && documents == null)) {
            outStream.println(MESSAGE_FOR_INVALID_CONFIGURATION_YAML);
            exitError(this.exitWhenFinish);
        }

        if (schema != null && schema.startsWith("http")) {
            outStream.println("The schema path cannot be a URL.");
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Validates the existence of schema & document sections of the GraphQL config file.
     *
     * @param schema         the schema section of the Graphql config file
     * @param documents      the documents section of the Graphql config file
     * @param projectName    the project name of a project under Graphql config file
     */
    private void validateExistenceOfSchemaAndDocuments(String schema, List<String> documents, String projectName) {
        if ((schema == null && documents != null) || (schema != null && documents == null)) {
            outStream.println(MESSAGE_FOR_INVALID_CONFIGURATION_YAML);
            exitError(this.exitWhenFinish);
        }

        if (schema != null && schema.startsWith("http")) {
            outStream.println("The schema path of project " + projectName + " cannot be a URL.");
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Validates the existence of projects in the GraphQL config file.
     *
     * @param project         a project of the Graphql config file
     * @param projectName     the project name of a project under Graphql config file
     */
    private void validateExistenceOfProject(Project project, String projectName) {
        if (project == null) {
            outStream.println(MESSAGE_FOR_INVALID_CONFIGURATION_YAML);
            exitError(this.exitWhenFinish);
        }
        validateExistenceOfSchemaAndDocuments(project.getSchema(),
                project.getDocuments(), projectName);
    }

    /**
     * Validates the schema & documents in the GraphQL config file.
     *
     * @param schema         the schema value of the Graphql config file
     * @param documents      the documents value of the Graphql config file
     */
    private void validateSchemaAndDocuments(String schema, List<String> documents) throws IOException {
        if (schema != null && documents != null) {
            File schemaFile = new File(schema);
            Path schemaPath = Paths.get(schemaFile.getCanonicalPath());
            validateSchemaPath(schemaPath);

            for (String document : documents) {
                File documentFile = new File(document);
                Path documentPath = Paths.get(documentFile.getCanonicalPath());
                validateDocumentPath(documentPath);
            }
        }
    }

    /**
     * Validates the SDL & queries of projects in the given GraphQL config file.
     *
     * @param graphqlConfig         the instance of the Graphql config file
     */
    private void validateProjects(GraphqlConfig graphqlConfig)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException, IOException {
        String schema = graphqlConfig.getSchema();
        List<String> documents = graphqlConfig.getDocuments();
        Map<String, Project> projects = graphqlConfig.getProjects();

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                validateProject(
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments());
            }
        }
        validateProject(schema, documents);
    }

    /**
     * Validates the SDL & queries of a given project in the given GraphQL config file.
     *
     * @param schema         the schema value of the Graphql config file
     * @param documents      the documents value of the Graphql config file
     */
    private void validateProject(String schema, List<String> documents)
            throws BallerinaGraphqlSDLValidationException, BallerinaGraphqlQueryValidationException,
            BallerinaGraphqlException, IOException  {
        if (schema != null && documents != null) {
            String schemaContent = extractSchemaContent(schema);
            for (String document : documents) {
                String documentContent = extractDocumentContent(document);
                validate(schemaContent, documentContent);
            }
        }
    }

    /**
     * Validates the given SDL and GraphQL queries.
     *
     * @param sdlInput         the schema content
     * @param queriesInput     the queries content
     */
    private void validate(String sdlInput, String queriesInput) throws BallerinaGraphqlSDLValidationException,
            BallerinaGraphqlQueryValidationException, BallerinaGraphqlException {
        try {
            SchemaParser schemaParser = new SchemaParser();
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            TypeDefinitionRegistry typeRegistry = schemaParser.parse(sdlInput);
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry,
                    RuntimeWiring.MOCKED_WIRING);

            Parser parser = new Parser();
            Document document = parser.parseDocument(queriesInput);

            Validator validator = new Validator();
            List<ValidationError> validationErrorList = validator.validateDocument(graphQLSchema, document);
            if (validationErrorList.size() > 0) {
                throw new BallerinaGraphqlQueryValidationException("Graph Query validation exception",
                        validationErrorList);
            }
        } catch (SchemaProblem e) {
            throw new BallerinaGraphqlSDLValidationException("GraphQL SDL validation exception", e.getErrors());
        } catch (BallerinaGraphqlQueryValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new BallerinaGraphqlException("Exception ", e);
        }
    }

    /**
     * Extracts the schema content.
     *
     * @param schema         the schema value of the Graphql config file
     * @return               the schema content
     */
    private String extractSchemaContent(String schema) throws IOException {
        File schemaFile = new File(schema);
        Path schemaPath = Paths.get(schemaFile.getCanonicalPath());
        validateSchemaPath(schemaPath);
        return Files.readString(schemaPath);
    }

    /**
     * Validates the schema path.
     *
     * @param schemaPath         the path to the schema
     */
    private void validateSchemaPath(Path schemaPath) {
        if (!Files.exists(schemaPath)) {
            outStream.println("Schema file " + schemaPath + " doesn't exist.");
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Extracts the document content.
     *
     * @param document         the document value of the Graphql config file
     * @return                 the document content
     */
    private String extractDocumentContent(String document) throws IOException {
        File documentFile = new File(document);
        Path documentPath = Paths.get(documentFile.getCanonicalPath());
        validateDocumentPath(documentPath);
        return Files.readString(documentPath);
    }

    /**
     * Validates the documents' path.
     *
     * @param documentPath         the path to the document
     */
    private void validateDocumentPath(Path documentPath) {
        if (!Files.exists(documentPath)) {
            outStream.println("Queries file " + documentPath + " doesn't exist.");
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Generates the code for the projects given in the GraphQL config file.
     *
     * @param graphqlConfig         the instance of the Graphql config file
     */
    private void generateCode(GraphqlConfig graphqlConfig) throws IOException {
        String schema = graphqlConfig.getSchema();
        List<String> documents = graphqlConfig.getDocuments();
        Map<String, Project> projects = graphqlConfig.getProjects();

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                generateProjectCode(
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments());
            }
        }
        generateProjectCode(schema, documents);
    }

    /**
     * Generates the code for each project.
     *
     * @param schema         the schema value of the Graphql config file
     * @param documents      the documents value of the Graphql config file
     */
    private void generateProjectCode(String schema, List<String> documents) throws IOException {
        CodeGenerator codeGenerator = new CodeGenerator();

        if (schema != null && documents != null) {
            String schemaContent = extractSchemaContent(schema);

            for (String document : documents) {
                String documentContent = extractDocumentContent(document);

                codeGenerator.generate(schemaContent, documentContent, getTargetOutputPath().toString());
            }
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
