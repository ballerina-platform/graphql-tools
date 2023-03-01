package io.ballerina.graphql.cmd;

import io.ballerina.graphql.cmd.pojo.Extension;

import java.util.List;

/**
 * Represents a GraphQL project in GraphQL config file.
 */
public class GraphqlClientProject extends GraphqlProject {
    private List<String> documents;

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


}