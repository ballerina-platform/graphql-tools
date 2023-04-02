package io.ballerina.graphql.generator.service.exception;


import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception type definition for Ballerina service types code generation related errors.
 */
public class ServiceTypesGenerationException extends GenerationException {
    public ServiceTypesGenerationException(String message, Throwable e) {
        super(message, e);
    }

    public ServiceTypesGenerationException(String message) {
        super(message);
    }

    public String getDiagnosticMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_101, null, this.getMessage());
        return diagnostic.toString();
    }


}
