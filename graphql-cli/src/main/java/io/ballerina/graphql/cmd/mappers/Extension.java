package io.ballerina.graphql.cmd.mappers;

import java.util.Map;

/**
 * POJO class representing contents of GraphQL config file.
 */
public class Extension {
    Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
