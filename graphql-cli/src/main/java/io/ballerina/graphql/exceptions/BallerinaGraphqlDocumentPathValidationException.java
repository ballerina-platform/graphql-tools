package io.ballerina.graphql.exceptions;

/**
 * Exception type definition for GraphQL document path validation related errors.
 */
public class BallerinaGraphqlDocumentPathValidationException extends Exception {
    public BallerinaGraphqlDocumentPathValidationException(String message, Throwable e) {
        super(message, e);
    }

    public BallerinaGraphqlDocumentPathValidationException(String message) {
        super(message);
    }
}
