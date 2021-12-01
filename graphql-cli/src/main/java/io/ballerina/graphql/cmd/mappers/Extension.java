package io.ballerina.graphql.cmd.mappers;

/**
 * POJO class representing extensions of a project in GraphQL config file.
 */
public class Extension {
    private Endpoints endpoints;

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
    }
}
