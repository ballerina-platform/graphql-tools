package io.ballerina.graphql.cmd;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Enum for service generation related diagnostic messages.
 */
public enum ServiceDiagnosticMessages {
    SERVICE_GEN_100("SERVICE_GEN_100", "Given GraphQL schema contains compilation error(s)", DiagnosticSeverity.ERROR),
    SERVICE_GEN_101("SERVICE_GEN_101", "Service generation failed: %s", DiagnosticSeverity.ERROR),
//    SERVICE_GEN_101("SERVICE_GEN_101", "Given GraphQL file is not allowed to be read.", DiagnosticSeverity.ERROR),
    ;

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
