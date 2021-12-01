package io.ballerina.graphql.exceptions;

/**
 * Exception type definition for GraphQL introspection related errors.
 */
public class BallerinaGraphqlIntospectionException extends Exception {
    public BallerinaGraphqlIntospectionException(String message, Throwable e) {
        super(message, e);
    }

    public BallerinaGraphqlIntospectionException(String message) {
        super(message);
    }
}
