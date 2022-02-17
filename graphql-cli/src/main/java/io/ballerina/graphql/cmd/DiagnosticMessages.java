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

package io.ballerina.graphql.cmd;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code DiagnosticMessages} enum class represents the error messages related to {@code graphql} command.
 */
public enum DiagnosticMessages {
    GRAPHQL_CLI_100("GRAPHQL_CLI_100", "GraphQL command execution related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_101("GRAPHQL_CLI_101", "GraphQL config file parsing related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_102("GRAPHQL_CLI_102", "GraphQL validation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_103("GRAPHQL_CLI_103", "GraphQL schema file path validation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_104("GRAPHQL_CLI_104", "GraphQL schema web URL validation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_105("GRAPHQL_CLI_105", "GraphQL document path validation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_106("GRAPHQL_CLI_106", "GraphQL schema (SDL) file validation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_107("GRAPHQL_CLI_107", "GraphQL query validation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_108("GRAPHQL_CLI_108", "Ballerina code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_109("GRAPHQL_CLI_109", "Ballerina client code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_110("GRAPHQL_CLI_110", "Ballerina utils code generation related error occurred.",
            DiagnosticSeverity.ERROR),
    GRAPHQL_CLI_111("GRAPHQL_CLI_111", "Ballerina types code generation related error occurred.",
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
