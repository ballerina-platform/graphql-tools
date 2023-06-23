package io.ballerina.graphql.generator.service.exception;

/**
 * Exception type definition for Ballerina service types file combination related errors.
 */
public class ServiceTypesFileCombinerException extends Exception {
    public ServiceTypesFileCombinerException(String errMessage) {
        super(errMessage);
    }
}
