package io.ballerina.graphql.generator.service;

import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.utils.GenerationType;

/**
 * Represents a GraphQL project in GraphQL config file.
 */
public class GraphqlServiceProject extends GraphqlProject {
    private static GenerationType generationType = GenerationType.SERVICE;

    public GraphqlServiceProject(String name, String schema, String outputPath) {
        super(name, schema, outputPath);
    }

    public GraphqlServiceProject(String name, String schema) {
        super(name, schema);
    }

    public GenerationType getGenerationType() {
        return generationType;
    }
}
