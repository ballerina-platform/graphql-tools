package io.ballerina.graphql.cmd.mappers;

import java.util.Map;

/**
 * POJO class representing default values of a project in GraphQL config file.
 */
public class Default {
    Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
