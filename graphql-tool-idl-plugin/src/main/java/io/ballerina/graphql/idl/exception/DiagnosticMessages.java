package io.ballerina.graphql.idl.exception;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * enum for diagnostic messages of IDL client generation.
 */
public enum DiagnosticMessages {
    GRAPHQL_IDL_CLIENT_100("GRAPHQL_IDL_CLIENT_100", "GraphQL IDL support related error occurred.",
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
