package io.ballerina.graphql.generator.service.exception;

import io.ballerina.graphql.generator.DiagnosticMessages;
import io.ballerina.graphql.generator.GenerationException;
import io.ballerina.tools.diagnostics.Diagnostic;

/**
 * Exception type definition for Ballerina service code generation related errors.
 */
public class ServiceGenerationException extends GenerationException {
    public ServiceGenerationException(String message, Throwable e) {
        super(message, e);
    }

    public ServiceGenerationException(String message) {
        super(message);
    }

    public String getDiagnosticMessage() {
        Diagnostic diagnostic = createDiagnostic(DiagnosticMessages.GRAPHQL_GEN_100, null, this.getMessage());
        return diagnostic.toString();
    }

    // following schema pattern. change to extends to Exception.
//    private final Diagnostic diagnostic;
//
//    public ServiceGenerationException(ServiceDiagnosticMessages diagnosticMessage, Location location) {
//        super(diagnosticMessage.getDescription());
//        this.diagnostic = createDiagnostic(diagnosticMessage, location);
//    }
//
//    public ServiceGenerationException(ServiceDiagnosticMessages diagnosticMessage, Location location, String... args)
//    {
//        super(generateDescription(diagnosticMessage, args));
//        this.diagnostic = createDiagnostic(diagnosticMessage, location, args);
//    }
//    public String getMessage() {
//        return this.diagnostic.toString();
//    }
//
//    private static String generateDescription(ServiceDiagnosticMessages message, String... args) {
//        return String.format(message.getDescription(), (Object[]) args);
//    }
//    private static Diagnostic createDiagnostic(ServiceDiagnosticMessages diagnosticMessage, Location location, String
//    ... args) {
//        DiagnosticInfo diagnosticInfo =
//                new DiagnosticInfo(diagnosticMessage.getCode(), generateDescription(diagnosticMessage, args),
//                        diagnosticMessage.getSeverity());
//        if (location == null) {
//            location = new Utils.NullLocation();
//        }
//        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
//        return diagnostic;
//    }
}
