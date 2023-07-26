/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.service.exception;

import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.utils.NullLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;

import static io.ballerina.tools.diagnostics.DiagnosticFactory.createDiagnostic;

/**
 * Exception type definition for Ballerina GraphQL service generation related errors.
 */
public class ServiceGenerationException extends Exception {
    private final Diagnostic diagnostic;

    public ServiceGenerationException(ServiceDiagnosticMessages diagnosticMessage, Location location,
                                      String... args) {
        super(diagnosticMessage.getDescription());
        this.diagnostic = createDiagnostic(generateDiagnosticInfo(diagnosticMessage, args),
                getLocation(location));
    }

    public String getMessage() {
        return this.diagnostic.toString();
    }

    private String generateDescription(ServiceDiagnosticMessages diagnosticMessage, String... args) {
        return String.format(diagnosticMessage.getDescription(), (Object[]) args);
    }

    private DiagnosticInfo generateDiagnosticInfo(ServiceDiagnosticMessages diagnosticMessage, String... args) {
        return new DiagnosticInfo(diagnosticMessage.getCode(), generateDescription(diagnosticMessage, args),
                diagnosticMessage.getSeverity());
    }

    private Location getLocation(Location location) {
        if (location == null) {
            return NullLocation.getInstance();
        }
        return location;
    }
}
