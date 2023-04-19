package io.ballerina.graphql.generator;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;


/**
 * Constants used in the code generation.
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
    GRAPHQL_GEN_106("GRAPHQL_GEN_106", "Ballerina code generation related error occurred. %s",
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
