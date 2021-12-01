package io.ballerina.graphql.cmd.mappers;

import java.util.Map;

/**
 * POJO class representing default values of a project in GraphQL config file.
 */
public class Default {
    private String url;
    private Map<String, String> headers;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
