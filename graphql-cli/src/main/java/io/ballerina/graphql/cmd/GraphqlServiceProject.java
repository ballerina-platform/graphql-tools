package io.ballerina.graphql.cmd;

/**
 * Represents a GraphQL project in GraphQL config file.
 */
public class GraphqlServiceProject extends GraphqlProject {
    public GraphqlServiceProject(String name, String schema, String outputPath) {
        super(name, schema, outputPath);
    }

    public GraphqlServiceProject(String name, String schema) {
        super(name, schema);
    }
}
