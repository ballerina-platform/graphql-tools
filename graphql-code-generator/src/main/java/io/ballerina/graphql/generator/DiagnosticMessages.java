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

package io.ballerina.graphql.generator;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code DiagnosticMessages} enum class represents the error messages related to service generation.
 */
public enum DiagnosticMessages {
    GRAPHQL_GEN_100("GRAPHQL_GEN_100", "Ballerina service code generation related error occurred. %s",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_101("GRAPHQL_GEN_101", "Ballerina service types code generation related error occurred. %s",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_102("GRAPHQL_GEN_102", "Ballerina client code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_103("GRAPHQL_GEN_103", "Ballerina utils code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_104("GRAPHQL_GEN_104", "Ballerina client types code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_105("GRAPHQL_GEN_105", "Ballerina config types code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_106("GRAPHQL_GEN_106", "Ballerina code generation failed. %s",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_107("GRAPHQL_GEN_107", "Ballerina service file combiner related error occurred. %s",
            DiagnosticSeverity.ERROR),
    GRAPHQL_GEN_108("GRAPHQL_GEN_108", "Ballerina service types file combiner related error occurred. %s",
            DiagnosticSeverity.ERROR);
    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }
}
