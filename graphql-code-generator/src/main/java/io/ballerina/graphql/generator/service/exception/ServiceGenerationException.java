package io.ballerina.graphql.generator.service.exception;

import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception type definition for Ballerina service code generation related errors.
 */
public class ServiceGenerationException extends GenerationException {
    public ServiceGenerationException(String errMessage) {
        super(errMessage);
    }

    public ServiceGenerationException(String errMessage, String projectName) {
        super(errMessage, projectName);
    }

    @Override
    public String getMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_100, this.getLocation(),
                this.getErrMessage());
        return diagnostic.toString();
    }

}
