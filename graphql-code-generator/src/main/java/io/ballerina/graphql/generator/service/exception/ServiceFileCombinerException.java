package io.ballerina.graphql.generator.service.exception;

import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception type definition for Ballerina service file combination related errors.
 */
public class ServiceFileCombinerException extends GenerationException {
    public ServiceFileCombinerException(String errMessage) {
        super(errMessage);
    }

    public String getMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_107, this.getLocation(),
                this.getErrMessage());
        return diagnostic.toString();
    }
}
