/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.schema.exception;

import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.utils.Utils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;

/**
 * Exception type definition for GraphQL SDL schema generation related errors.
 */
public class SchemaGenerationException extends Exception {
    private final Diagnostic diagnostic;

    public SchemaGenerationException(DiagnosticMessages diagnosticMessage, Location location) {
        super(diagnosticMessage.getDescription());
        this.diagnostic = createDiagnostic(diagnosticMessage, location);
    }

    public SchemaGenerationException(DiagnosticMessages diagnosticMessage, Location location, String... args) {
        super(generateDescription(diagnosticMessage, args));
        this.diagnostic = createDiagnostic(diagnosticMessage, location, args);
    }

    public String getMessage() {
        return this.diagnostic.toString();
    }

    private static String generateDescription(DiagnosticMessages message, String... args) {
        return String.format(message.getDescription(), (Object[]) args);
    }

    private static Diagnostic createDiagnostic(DiagnosticMessages diagnosticMessage, Location location,
                                               String... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticMessage.getCode(),
                generateDescription(diagnosticMessage, args), diagnosticMessage.getSeverity());
        if (location == null) {
            location = new Utils.NullLocation();
        }
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        return diagnostic;
    }
}
