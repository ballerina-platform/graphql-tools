package io.ballerina.graphql.exception;

import io.ballerina.graphql.cmd.DiagnosticMessages;
import io.ballerina.graphql.cmd.GraphqlDiagnostic;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Exception type definition for Ballerina service code generation related errors.
 */
public class ServiceGenerationException extends GenerationException {
    private String message;

    public ServiceGenerationException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public ServiceGenerationException(String message) {
        super(message);
        this.message = message;
    }

    public String getDiagnosticMessage() {
        GraphqlDiagnostic graphqlDiagnostic =
                Utils.constructGraphqlDiagnostic(DiagnosticMessages.GRAPHQL_CLI_114.getCode(), this.message,
                        DiagnosticSeverity.ERROR, null);
        return graphqlDiagnostic.toString();
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
