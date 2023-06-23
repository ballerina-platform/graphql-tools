package io.ballerina.graphql.generator.service.exception;

/**
 * Exception type definition for Ballerina service file combination related errors.
 */
public class ServiceFileCombinerException extends Exception {
    public ServiceFileCombinerException(String errMessage) {
        super(errMessage);
    }
}
