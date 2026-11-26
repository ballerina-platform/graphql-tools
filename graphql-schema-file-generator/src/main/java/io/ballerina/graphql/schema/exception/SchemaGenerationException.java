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

import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.utils.NullLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;

import static io.ballerina.tools.diagnostics.DiagnosticFactory.createDiagnostic;

/**
 * Exception type definition for GraphQL SDL schema generation related errors.
 */
public class SchemaGenerationException extends Exception {
    private static final String MESSAGE_PATTERN = "{0}";

    private final Diagnostic diagnostic;
    private final SchemaDiagnosticMessages diagnosticMessage;

    public SchemaGenerationException(SchemaDiagnosticMessages diagnosticMessage, Location location,
                                     String... args) {
        super(generateDescription(diagnosticMessage, args));
        // The description is passed as the only argument of a {0} pattern, since createDiagnostic formats the
        // message with MessageFormat, which would otherwise read quotes and braces in it as format syntax.
        this.diagnostic = createDiagnostic(new DiagnosticInfo(diagnosticMessage.getCode(), MESSAGE_PATTERN,
                diagnosticMessage.getSeverity()), getLocation(location), generateDescription(diagnosticMessage, args));
        this.diagnosticMessage = diagnosticMessage;
    }

    public String getMessage() {
        return this.diagnostic.toString();
    }

    public SchemaDiagnosticMessages getDiagnosticMessage() {
        return this.diagnosticMessage;
    }

    private static String generateDescription(SchemaDiagnosticMessages diagnosticMessage, String... args) {
        return String.format(diagnosticMessage.getDescription(), (Object[]) args);
    }

    private static Location getLocation(Location location) {
        if (location == null) {
            return NullLocation.getInstance();
        }
        return location;
    }
}
