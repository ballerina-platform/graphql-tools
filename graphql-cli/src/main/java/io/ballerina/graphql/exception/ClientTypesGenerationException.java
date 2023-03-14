package io.ballerina.graphql.exception;

import io.ballerina.graphql.cmd.DiagnosticMessages;
import io.ballerina.graphql.cmd.GraphqlDiagnostic;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

public class ClientTypesGenerationException extends GenerationException {
    private String message;

    public ClientTypesGenerationException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public ClientTypesGenerationException(String message) {
        super(message);
        this.message = message;
    }

    public String getDiagnosticMessage() {
        GraphqlDiagnostic graphqlDiagnostic = Utils.constructGraphqlDiagnostic(
                DiagnosticMessages.GRAPHQL_CLI_111.getCode(),
                this.message, DiagnosticSeverity.ERROR, null);
        return graphqlDiagnostic.toString();
    }
}
