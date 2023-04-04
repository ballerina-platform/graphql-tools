package io.ballerina.graphql.generator.client;

import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.utils.GenerationType;

import java.util.List;

/**
 * Represents a GraphQL project in GraphQL config file.
 */
public class GraphqlClientProject extends GraphqlProject {
    private List<String> documents;
    private static GenerationType generationType = GenerationType.CLIENT;
    private Extension extensions;

    public GraphqlClientProject(String name, String schema, List<String> documents, Extension extensions,
                                String outputPath) {
        super(name, schema, outputPath);
        this.documents = documents;
        this.extensions = extensions;
    }

    public GraphqlClientProject(String name, String schema, List<String> documents, Extension extensions) {
        super(name, schema);
        this.documents = documents;
        this.extensions = extensions;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public Extension getExtensions() {
        return extensions;
    }

    public GenerationType getGenerationType() {
        return generationType;
    }

}
