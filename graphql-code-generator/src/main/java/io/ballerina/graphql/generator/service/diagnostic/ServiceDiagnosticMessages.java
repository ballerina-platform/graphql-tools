package io.ballerina.graphql.generator.service.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code ServiceDiagnosticMessages} enum class represents the error messages related to GraphQL service
 * generation.
 */
public enum ServiceDiagnosticMessages {
    GRAPHQL_SERVICE_GEN_100("GRAPHQL_SERVICE_GEN_100", "GraphQL service generation failed: %s",
            DiagnosticSeverity.ERROR),
    GRAPHQL_SERVICE_GEN_101("GRAPHQL_SERVICE_GEN_101", "GraphQL service file generation failed: %s",
            DiagnosticSeverity.ERROR),
    GRAPHQL_SERVICE_GEN_102("GRAPHQL_SERVICE_GEN_102", "GraphQL service types file generation failed: %s",
            DiagnosticSeverity.ERROR),;

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    ServiceDiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
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
