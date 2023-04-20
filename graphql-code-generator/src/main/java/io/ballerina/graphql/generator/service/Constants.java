package io.ballerina.graphql.generator.service;

/**
 * This class represents GraphQL generation related constants.
 */
public class Constants {
    public static final String UNSUPPORTED_DEFAULT_ARGUMENT_VALUE =
            "Type \"%s\" is an unsupported default argument value type.";
    public static final String UNSUPPORTED_TYPE = "Type \"%s\" is not supported.";
    public static final String ONLY_SCALAR_TYPE_ALLOWED = "Should be a scalar type but found \"%s\".";
    public static final String NOT_ALLOWED_UNION_SUB_TYPE =
            "Union type can only have object types as members. But found: \"%s\" with different type.";
}
