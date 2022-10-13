package io.ballerina.graphql.idl.exception;

import io.ballerina.graphql.cmd.GraphqlDiagnostic;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Exception type definition for multiple project detection in client generation.
 */
public class IDLMultipleProjectException extends Exception {
    private String message;

    public IDLMultipleProjectException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return getDiagnosticMessage();
    }

    public String getDiagnosticMessage() {
        GraphqlDiagnostic graphqlDiagnostic = Utils.constructGraphqlDiagnostic(
                DiagnosticMessages.GRAPHQL_IDL_CLIENT_100.getCode(),
                this.message, DiagnosticSeverity.ERROR, null);
        return graphqlDiagnostic.toString();
    }
}
