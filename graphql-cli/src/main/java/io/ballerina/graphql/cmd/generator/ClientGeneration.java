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

import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.exception.ClientCodeGenerationException;
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

    private final GenerationContext context;
    private final ClientCodeGenerator clientCodeGenerator;
    private List<GraphqlClientProject> projects;
    private final Map<GraphqlClientProject, List<SrcFilePojo>> generatedSources = new LinkedHashMap<>();

    public ClientGeneration(GenerationContext context) {
        this.context = context;
        this.clientCodeGenerator = new ClientCodeGenerator();
    }

    @Override
    public void validate() throws GenerationException {
        try {
            Config config = readConfig(context.getInputPath());
            ConfigValidator.getInstance().validate(config);
            this.projects = populateProjects(config);
            for (GraphqlClientProject project : this.projects) {
                Utils.validateGraphqlProject(project);
                QueryValidator.getInstance().validate(project);
            }
        } catch (ParseException | IOException | ValidationException e) {
            throw new GenerationException(e);
        }
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
