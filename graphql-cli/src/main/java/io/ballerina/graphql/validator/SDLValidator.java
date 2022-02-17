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

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.errors.SchemaProblem;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.exception.IntospectionException;
import io.ballerina.graphql.exception.SDLValidationException;
import io.ballerina.graphql.exception.ValidationException;

import java.io.IOException;

/**
 * This class is used to validate the GraphQL schema (SDL) file.
 */
public class SDLValidator {
    private static SDLValidator sdlValidator = null;

    public static SDLValidator getInstance() {
        if (sdlValidator == null) {
            sdlValidator = new SDLValidator();
        }
        return sdlValidator;
    }

    /**
     * Validates the GraphQL schema (SDL) of the given project.
     *
     * @param project                               the instance of the Graphql project
     * @throws ValidationException                  when a validation error occurs
     * @throws IOException                          If an I/O error occurs
     */
    public void validate(GraphqlProject project) throws ValidationException, IOException {
        String schema = project.getSchema();
        Extension extensions = project.getExtensions();

        try {
            GraphQLSchema graphQLSchema = Utils.getGraphQLSchemaDocument(schema, extensions);
            project.setGraphQLSchema(graphQLSchema);
        } catch (IntospectionException e) {
            throw new ValidationException(e.getMessage(), project.getName());
        } catch (SchemaProblem e) {
            throw new SDLValidationException("GraphQL SDL validation failed.", e.getErrors(), project.getName());
        }
    }
}
