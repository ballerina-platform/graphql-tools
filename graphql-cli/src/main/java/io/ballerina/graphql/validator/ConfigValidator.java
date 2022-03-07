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

package io.ballerina.graphql.validator;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.DocumentPathValidationException;
import io.ballerina.graphql.exception.SchemaPathValidationException;
import io.ballerina.graphql.exception.SchemaUrlValidationException;
import io.ballerina.graphql.exception.ValidationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_PROJECT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_DOCUMENT_PATH;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_SCHEMA_PATH;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_SCHEMA_URL;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_MISSING_SCHEMA_OR_DOCUMENTS;
import static io.ballerina.graphql.cmd.Constants.URL_RECOGNIZER;
import static io.ballerina.graphql.cmd.Utils.isValidURL;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * This class is used to validate the GraphQL configuration file.
 */
public class ConfigValidator {
    private static ConfigValidator configValidator = null;
    private List<GraphqlProject> projects = new ArrayList<>();

    public static ConfigValidator getInstance() {
        if (configValidator == null) {
            configValidator = new ConfigValidator();
        }
        return configValidator;
    }

    /**
     * Validates the configuration of all the projects in the given GraphQL config file.
     *
     * @param config                                the instance of the Graphql config file
     * @throws ValidationException                  when a validation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    public void validate(Config config) throws ValidationException, IOException {
        populateProjects(config);
        for (GraphqlProject project : this.projects) {
            validateProject(project);
        }
    }

    /**
     * Populate the projects with information given in the GraphQL config file.
     *
     * @param config                                the instance of the Graphql config file
     * @throws ValidationException                  when a validation error occurs
     */
    private void populateProjects(Config config) throws ValidationException {
        String schema = config.getSchema();
        List<String> documents = config.getDocuments();
        Extension extensions = config.getExtensions();
        Map<String, Project> projects = config.getProjects();

        if (schema != null || documents != null || extensions != null) {
            this.projects.add(new GraphqlProject(ROOT_PROJECT_NAME, schema, documents, extensions));
        }

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                if (projects.get(projectName) == null) {
                    throw new ValidationException(MESSAGE_FOR_EMPTY_PROJECT, projectName);
                }
                this.projects.add(new GraphqlProject(projectName,
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments(),
                        projects.get(projectName).getExtensions()));
            }
        }
    }

    /**
     * Validates the configuration of a project in the GraphQL config file.
     *
     * @param project                               the instance of the Graphql project
     * @throws ValidationException                  when a validation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    private void validateProject(GraphqlProject project) throws ValidationException, IOException {
        String schema = project.getSchema();
        List<String> documents = project.getDocuments();

        if (!(schema != null && documents != null)) {
            throw new ValidationException(MESSAGE_FOR_MISSING_SCHEMA_OR_DOCUMENTS, project.getName());
        }

        try {
            validateSchema(schema);
            validateDocuments(documents);
        } catch (DocumentPathValidationException | SchemaPathValidationException | SchemaUrlValidationException e) {
            throw new ValidationException(e.getMessage(), project.getName());
        }
    }

    /**
     * Validates the schema value of the Graphql config file.
     *
     * @param schema                                the schema value of the Graphql config file
     * @throws SchemaUrlValidationException         when a schema web URL validation error occurs
     * @throws SchemaPathValidationException        when a schema path validation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    private void validateSchema(String schema)
            throws SchemaUrlValidationException, SchemaPathValidationException, IOException {
        if (schema.startsWith(URL_RECOGNIZER)) {
            validateSchemaUrl(schema);
        }

        if (!schema.startsWith(URL_RECOGNIZER)) {
            File schemaFile = new File(schema);
            Path schemaPath = Paths.get(schemaFile.getCanonicalPath());
            validateSchemaPath(schemaPath);
        }
    }

    /**
     * Validates the documents value of the Graphql config file.
     *
     * @param documents                                 the documents value of the Graphql config file
     * @throws DocumentPathValidationException          when a document path validation error occurs
     * @throws IOException                              If an I/O error occurs
     */
    private void validateDocuments(List<String> documents) throws DocumentPathValidationException, IOException {
        for (String document : documents) {
            File documentFile = new File(document);
            Path documentPath = Paths.get(documentFile.getCanonicalPath());
            validateDocumentPath(documentPath);
        }
    }

    /**
     * Validates the schema web URL.
     *
     * @param schema                                the schema value of the Graphql config file
     * @throws SchemaUrlValidationException         when a schema web URL validation error occurs
     */
    private void validateSchemaUrl(String schema) throws SchemaUrlValidationException {
        if (!isValidURL(schema)) {
            throw new SchemaUrlValidationException(MESSAGE_FOR_INVALID_SCHEMA_URL + schema);
        }
    }

    /**
     * Validates the schema file path.
     *
     * @param schemaPath                            the path to the schema
     * @throws SchemaPathValidationException        when a schema path validation error occurs
     */
    private static void validateSchemaPath(Path schemaPath) throws SchemaPathValidationException {
        if (!Files.exists(schemaPath)) {
            throw new SchemaPathValidationException(MESSAGE_FOR_INVALID_SCHEMA_PATH + schemaPath);
        }
    }

    /**
     * Validates the documents' file path.
     *
     * @param documentPath                          the path to the document
     * @throws DocumentPathValidationException      when a document path validation error occurs
     */
    private static void validateDocumentPath(Path documentPath) throws DocumentPathValidationException {
        if (!Files.exists(documentPath)) {
            throw new DocumentPathValidationException(MESSAGE_FOR_INVALID_DOCUMENT_PATH + documentPath);
        }
    }
}
