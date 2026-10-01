/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.cmd.generator;

import graphql.schema.idl.errors.SchemaProblem;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.config.SchemaConfig;
import io.ballerina.graphql.cmd.config.SchemaSource;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.SDLValidationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.exception.ClientCodeGenerationException;
import io.ballerina.graphql.generator.client.exception.IntospectionException;
import io.ballerina.graphql.generator.client.generator.ClientCodeGenerator;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Generates a Ballerina client for a given GraphQL configuration file.
 */
public class ClientGeneration implements Generator {

    private static final String ERROR_MISSING_DOCUMENTS =
            "The balGraphQL.toml file is missing the \"documents\" field, which is required for client generation.";

    private final GenerationContext context;
    private final BalGraphqlConfig balGraphqlConfig;
    private final ClientCodeGenerator clientCodeGenerator;
    private List<GraphqlClientProject> projects;
    private final Map<GraphqlClientProject, List<SrcFilePojo>> generatedSources = new LinkedHashMap<>();

    public ClientGeneration(GenerationContext context, BalGraphqlConfig balGraphqlConfig) {
        this.context = context;
        this.balGraphqlConfig = balGraphqlConfig;
        this.clientCodeGenerator = new ClientCodeGenerator();
    }

    @Override
    public void validate() throws GenerationException {
        this.projects = this.balGraphqlConfig == null ? populateProjectsFromYamlConfig()
                : populateProjectsFromBalGraphqlConfig();
        try {
            for (GraphqlClientProject project : this.projects) {
                // A "url" or "introspection" project already had its schema fetched and attached while it was
                // being populated, since that requires dispatching on the configured schema source rather than
                // Utils.validateGraphqlProject's URL-prefix-implies-introspection inference.
                if (project.getGraphQLSchema() == null) {
                    Utils.validateGraphqlProject(project);
                }
                QueryValidator.getInstance().validate(project);
            }
        } catch (IOException | ValidationException e) {
            throw new GenerationException(e);
        }
    }

    private List<GraphqlClientProject> populateProjectsFromYamlConfig() throws GenerationException {
        try {
            Config config = readConfig(context.getInputPath());
            ConfigValidator.getInstance().validate(config);
            return populateProjects(config);
        } catch (ParseException | IOException | ValidationException e) {
            throw new GenerationException(e);
        }
    }

    /**
     * Builds the project a client is generated for from a balGraphQL.toml configuration file. The schema and the
     * document locations are resolved against the configuration file directory, so that they are read relative to
     * the configuration file rather than the directory the command is run from.
     */
    private List<GraphqlClientProject> populateProjectsFromBalGraphqlConfig() throws GenerationException {
        if (!this.balGraphqlConfig.hasDocuments()) {
            throw new GenerationException(ERROR_MISSING_DOCUMENTS);
        }
        SchemaConfig schemaConfig = this.balGraphqlConfig.schema();
        Path configDirectory = Paths.get(context.getInputPath()).toAbsolutePath().getParent();
        List<String> documents = new ArrayList<>();
        for (String document : this.balGraphqlConfig.documents()) {
            documents.add(configDirectory.resolve(document).normalize().toString());
        }
        List<GraphqlClientProject> graphqlClientProjects = new ArrayList<>();
        if (schemaConfig.source() == SchemaSource.FILE) {
            String schema = configDirectory.resolve(schemaConfig.path()).normalize().toString();
            graphqlClientProjects.add(new GraphqlClientProject(ROOT_PROJECT_NAME, schema, documents, null,
                    context.getTargetOutputPath().toString()));
            return graphqlClientProjects;
        }
        graphqlClientProjects.add(populateRemoteSchemaProject(schemaConfig, documents));
        return graphqlClientProjects;
    }

    /**
     * Builds the client project for a "url" or "introspection" schema source: the schema is fetched or
     * introspected over the network and attached to the project directly, rather than being read from disk.
     */
    private GraphqlClientProject populateRemoteSchemaProject(SchemaConfig schemaConfig, List<String> documents)
            throws GenerationException {
        String schemaLocation = schemaConfig.source() == SchemaSource.URL
                ? schemaConfig.url() : schemaConfig.endpoint();
        GraphqlClientProject project = new GraphqlClientProject(ROOT_PROJECT_NAME, schemaLocation, documents, null,
                context.getTargetOutputPath().toString());
        try {
            project.setGraphQLSchema(Utils.resolveGraphQLSchema(schemaConfig));
        } catch (IntospectionException e) {
            throw new GenerationException(new ValidationException(e.getMessage(), project.getName()));
        } catch (SchemaProblem e) {
            throw new GenerationException(new SDLValidationException("GraphQL SDL validation failed.",
                    e.getErrors(), project.getName()));
        }
        return project;
    }

    @Override
    public void generate() throws GenerationException {
        for (GraphqlClientProject project : this.projects) {
            try {
                this.generatedSources.put(project,
                        this.clientCodeGenerator.generateBalSources(project, GeneratorContext.CLI));
            } catch (ClientCodeGenerationException e) {
                throw new GenerationException(e);
            } catch (NullPointerException e) {
                throw new GenerationException(new ClientCodeGenerationException(
                        "The provided schema includes operations that are not "
                                + "supported by the client generation.", project.getName()));
            }
        }
    }

    @Override
    public void write() throws GenerationException {
        for (Map.Entry<GraphqlClientProject, List<SrcFilePojo>> entry : this.generatedSources.entrySet()) {
            GraphqlClientProject project = entry.getKey();
            try {
                this.clientCodeGenerator.writeGeneratedSources(entry.getValue(),
                        Path.of(project.getOutputPath()));
            } catch (IOException e) {
                throw new GenerationException(new ClientCodeGenerationException(e.getMessage(), project.getName()));
            }
        }
    }

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

    private List<GraphqlClientProject> populateProjects(Config config) {
        List<GraphqlClientProject> graphqlClientProjects = new ArrayList<>();
        String schema = config.getSchema();
        List<String> documents = config.getDocuments();
        Extension extensions = config.getExtensions();
        Map<String, Project> configProjects = config.getProjects();
        String targetOutputPath = context.getTargetOutputPath().toString();

        if (schema != null || documents != null || extensions != null) {
            graphqlClientProjects.add(new GraphqlClientProject(ROOT_PROJECT_NAME, schema, documents, extensions,
                    targetOutputPath));
        }

        if (configProjects != null) {
            for (String projectName : configProjects.keySet()) {
                graphqlClientProjects.add(new GraphqlClientProject(projectName,
                        configProjects.get(projectName).getSchema(),
                        configProjects.get(projectName).getDocuments(),
                        configProjects.get(projectName).getExtensions(),
                        targetOutputPath));
            }
        }
        return graphqlClientProjects;
    }
}
