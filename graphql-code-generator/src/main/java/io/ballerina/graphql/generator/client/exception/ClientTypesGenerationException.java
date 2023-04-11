package io.ballerina.graphql.generator.client.exception;


import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception type definition for Ballerina code generation related errors.
 */
public class ClientTypesGenerationException extends GenerationException {
    public ClientTypesGenerationException(String message, Throwable e) {
        super(message, e);
    }

    public ClientTypesGenerationException(String message) {
        super(message);
    }

    public String getDiagnosticMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_104, null, this.getMessage());
        return diagnostic.toString();
    }
}
