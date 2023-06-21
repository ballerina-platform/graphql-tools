package io.ballerina.graphql.generator.service.exception;

import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception type definition for Ballerina service types file combination related errors.
 */
public class ServiceTypesFileCombinerException extends GenerationException {
    public ServiceTypesFileCombinerException(String errMessage) {
        super(errMessage);
    }

    public String getMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_108, this.getLocation(),
                this.getErrMessage());
        return diagnostic.toString();
    }
}
