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

package io.ballerina.graphql.generator;


import io.ballerina.graphql.generator.utils.NullLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * Exception type definition for Ballerina code generation related errors.
 */
public class GenerationException extends Exception {
    private String message;
    private String projectName;

    public GenerationException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public GenerationException(String message) {
        super(message);
        this.message = message;
    }

    public GenerationException(String message, String projectName) {
        super(message);
        this.message = message;
        this.projectName = projectName;
    }

    private static String generateDescription(DiagnosticMessages message, String... args) {
        return String.format(message.getDescription(), (Object[]) args);
    }

    public Diagnostic createDiagnostic(DiagnosticMessages diagnosticMessage, Location location, String... args) {
        DiagnosticInfo diagnosticInfo =
                new DiagnosticInfo(diagnosticMessage.getCode(), generateDescription(diagnosticMessage, args),
                        diagnosticMessage.getSeverity());
        if (location == null) {
            location = NullLocation.getInstance();
        }
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        return diagnostic;
    }

    public String getMessage() {
        if (this.projectName != null) {
            Location location = new Location() {
                @Override
                public LineRange lineRange() {
                    LinePosition from = LinePosition.from(0, 0);
                    return LineRange.from("(" + projectName + ":)", from, from);
                }

                @Override
                public TextRange textRange() {
                    return TextRange.from(0, 0);
                }
            };
            Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_106, location,
                    this.message + "\nPlease check project : " + projectName);
            return diagnostic.toString();
        } else {
            return this.message;
        }
    }
}
