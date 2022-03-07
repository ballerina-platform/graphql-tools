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

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.exception.QueryValidationException;
import io.ballerina.graphql.exception.ValidationException;

import java.io.IOException;
import java.util.List;

/**
 * This class is used to validate the GraphQL query files.
 */
public class QueryValidator {
    private static QueryValidator queryValidator = null;

    public static QueryValidator getInstance() {
        if (queryValidator == null) {
            queryValidator = new QueryValidator();
        }
        return queryValidator;
    }

    /**
     * Validates the GraphQL query files (documents) of the given project.
     *
     * @param project                               the instance of the Graphql project
     * @throws ValidationException                  when a validation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    public void validate(GraphqlProject project) throws ValidationException, IOException {
        List<String> documents = project.getDocuments();
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String projectName = project.getName();

        for (String document : documents) {
            try {
                validateDocument(graphQLSchema, document, projectName);
            } catch (QueryValidationException e) {
                throw new ValidationException(e.getMessage());
            }
        }
    }

    /**
     * Validates a GraphQL query file (document) with the given GraphQL schema (SDL).
     *
     * @param graphQLSchema                         the GraphQL schema instance
     * @param document                              the GraphQL query document value
     * @param projectName                           the name of the project
     * @throws QueryValidationException             If a GraphQL queries related error occurs
     * @throws IOException                          If an I/O error occurs
     */
    private void validateDocument(GraphQLSchema graphQLSchema, String document, String projectName)
            throws QueryValidationException, IOException {
        Document parsedDocument = Utils.getGraphQLQueryDocument(document);

        Validator validator = new Validator();
        List<ValidationError> validationErrors = validator.validateDocument(graphQLSchema, parsedDocument);
        if (validationErrors.size() > 0) {
            throw new QueryValidationException("Graph query validation failed.", validationErrors, projectName);
        }
    }
}
