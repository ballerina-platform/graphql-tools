/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.idl.exception;

import io.ballerina.graphql.cmd.GraphqlDiagnostic;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.idl.client.Constants;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Exception type definition for multiple project detection in client generation.
 *
 * @since 0.3.0
 */
public class IDLMultipleProjectException extends Exception {
    private String message;

    public IDLMultipleProjectException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return getDiagnosticMessage();
    }

    public String getDiagnosticMessage() {
        GraphqlDiagnostic graphqlDiagnostic = Utils.constructGraphqlDiagnostic(
                Constants.DiagnosticMessages.ERROR_MULTIPLE_PROJECT_AVAILABILITY.getCode(),
                this.message, DiagnosticSeverity.ERROR, null);
        return graphqlDiagnostic.toString();
    }
}
