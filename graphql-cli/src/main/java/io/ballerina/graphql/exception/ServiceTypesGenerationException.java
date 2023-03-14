package io.ballerina.graphql.exception;

import io.ballerina.graphql.cmd.DiagnosticMessages;
import io.ballerina.graphql.cmd.GraphqlDiagnostic;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

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
        GraphqlDiagnostic graphqlDiagnostic = Utils.constructGraphqlDiagnostic(
                DiagnosticMessages.GRAPHQL_CLI_113.getCode(),
                this.getMessage(), DiagnosticSeverity.ERROR, null);
        return graphqlDiagnostic.toString();
    }
}
