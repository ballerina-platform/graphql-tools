package io.ballerina.graphql.exceptions;

/**
 * Exception type definition for GraphQL schema path validation related errors.
 */
public class BallerinaGraphqlSchemaPathValidationException extends Exception {
    public BallerinaGraphqlSchemaPathValidationException(String message, Throwable e) {
        super(message, e);
    }

    public BallerinaGraphqlSchemaPathValidationException(String message) {
        super(message);
    }
}
