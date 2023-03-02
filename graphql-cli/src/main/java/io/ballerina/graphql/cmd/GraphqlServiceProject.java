package io.ballerina.graphql.cmd;

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

    // TODO: get type static method , use enum or const
    public GenerationType getGenerationType() {
        return generationType;
    };
}
