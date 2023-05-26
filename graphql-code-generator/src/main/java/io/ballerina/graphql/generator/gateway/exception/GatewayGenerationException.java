/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.generator.gateway.exception;

import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception for errors thrown by util functions.
 * */
public class GatewayGenerationException extends GenerationException {
    public GatewayGenerationException(String errMessage) {
        super(errMessage);
    }

    public GatewayGenerationException(String errMessage, String projectName) {
        super(errMessage, projectName);
    }

    @Override
    public String getMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_107, this.getLocation(),
                this.getErrMessage());
        return diagnostic.toString();
    }
}
