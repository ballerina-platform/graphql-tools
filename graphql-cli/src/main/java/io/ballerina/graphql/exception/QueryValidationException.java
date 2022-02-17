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

package io.ballerina.graphql.exception;

import graphql.language.SourceLocation;
import graphql.validation.ValidationError;
import io.ballerina.graphql.cmd.DiagnosticMessages;
import io.ballerina.graphql.cmd.GraphqlDiagnostic;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception type definition for GraphQL query validation related errors.
 */
public class QueryValidationException extends ValidationException {

    private String message;
    List<ValidationError> errors;
    private String projectName;

    public QueryValidationException(String message, List<ValidationError> errors, String projectName) {
        super(message);
        this.message = message;
        this.errors = new ArrayList<>(errors);
        this.projectName = projectName;
    }

    public QueryValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.message = message;
        this.errors = new ArrayList<>(errors);;
    }

    public QueryValidationException(String message, Throwable e) {
        super(message, e);
    }

    public QueryValidationException(String message) {
        super(message);
    }

    public String getMessage() {
        List<String> messages = new ArrayList<>();
        for (ValidationError error : errors) {
            for (SourceLocation sourceLocation : error.getLocations()) {
                Location location = new Location() {
                    @Override
                    public LineRange lineRange() {
                        LinePosition startLine =
                                LinePosition.from(sourceLocation.getLine(), sourceLocation.getColumn());
                        LinePosition endLine =
                                LinePosition.from(sourceLocation.getLine(), sourceLocation.getColumn());
                        return LineRange.from((sourceLocation.getSourceName() != null
                                ? "(" + projectName + ":" + sourceLocation.getSourceName() + ")"
                                : "(" + projectName + ":)"), startLine, endLine);
                    }

                    @Override
                    public TextRange textRange() {
                        return TextRange.from(0, 0);
                    }
                };
                GraphqlDiagnostic graphqlDiagnostic = Utils.constructGraphqlDiagnostic(
                        DiagnosticMessages.GRAPHQL_CLI_107.getCode(),
                        error.getMessage(), DiagnosticSeverity.ERROR, location);
                messages.add(graphqlDiagnostic.toString());
            }
        }
        StringBuilder concatenatedMessage = new StringBuilder();
        for (String message : messages) {
            concatenatedMessage.append(message).append("\n");
        }
        concatenatedMessage.append("GraphQL Query validation errors in project : ").append(this.projectName);
        concatenatedMessage.append("\n").append(this.message);
        return concatenatedMessage.toString();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public String toString() {
        return "GraphqlQueryValidationErrors{" +
                "errors=" + errors +
                '}';
    }
}



