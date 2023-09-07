/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.graphql.generator.client;

import io.ballerina.graphql.generator.client.exception.IntospectionException;
import io.ballerina.graphql.generator.client.pojo.Default;
import io.ballerina.graphql.generator.client.pojo.Endpoints;
import io.ballerina.graphql.generator.client.pojo.Extension;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.APPLICATION_JSON;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CONTENT_TYPE;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.DATA_FIELD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ERROR_FIELD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.INTROSPECTION_QUERY;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.QUERY_VAR_NAME;

/**
 * This class is used to introspect a GraphQL API.
 */
public class Introspector {
    private static Introspector introspector = null;

    public static Introspector getInstance() {
        if (introspector == null) {
            introspector = new Introspector();
        }
        return introspector;
    }

    /**
     * Returns the introspection results map for a given GraphQL schema URL.
     *
     * @param schema                                the GraphQL schema URL value of the Graphql config file
     * @param extensions                            the extensions value of the Graphql config file
     * @return                                      the introspection results map
     * @throws IntospectionException                If an error occurs during introspection of the GraphQL API
     */
    public Map<String, Object> getIntrospectionResult(String schema, Extension extensions)
            throws IntospectionException {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest;
            if (extensions != null) {
                httpRequest = createHttpRequest(schema, extensions);
            } else {
                httpRequest = createHttpRequest(schema);
            }
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject introspectionResult = new JSONObject(response.body());
                if (introspectionResult.has(ERROR_FIELD) || !introspectionResult.has(DATA_FIELD)) {
                    throw new IntospectionException("Failed to retrieve SDL. Please provide a valid GraphQL endpoint " +
                            "with relevant headers or a local SDL file path.");
                }
                return ((JSONObject) introspectionResult.get("data")).toMap();
            } else {
                throw new IntospectionException("Failed to retrieve SDL. Please provide a valid GraphQL endpoint " +
                        "with relevant headers or a local SDL file path.");
            }
        } catch (InterruptedException | IOException e) {
            throw new IntospectionException("Failed to retrieve SDL. Please provide a valid GraphQL " +
                    "endpoint with relevant headers or a local SDL file path." +
                    (e.getMessage() != null ? "\n" + e.getMessage() : ""));
        }
    }

    /**
     * Creates the HTTP request object with the GraphQL payload & headers attached to it.
     *
     * @param endpoint         the Graphql API endpoint
     * @return                 the HTTP request object
     */
    private HttpRequest createHttpRequest(String endpoint, Extension extensions) {
        Map<String, String> headers = null;
        Endpoints endpoints = extensions.getEndpoints();
        if (endpoints != null) {
            Default defaultName = endpoints.getDefaultName();
            if (defaultName != null) {
                headers = defaultName.getHeaders();
            }
        }
        String graphqlPayload = getRequestPayload();
        HttpRequest request;
        if (headers != null) {
            request = addHeaders(HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .headers(CONTENT_TYPE, APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(graphqlPayload, StandardCharsets.UTF_8)), headers)
                    .build();
        } else {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .headers(CONTENT_TYPE, APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(graphqlPayload, StandardCharsets.UTF_8))
                    .build();
        }
        return request;
    }

    /**
     * Creates the HTTP request object with the GraphQL payload attached to it.
     *
     * @param endpoint         the Graphql API endpoint
     * @return                 the HTTP request object
     */
    private HttpRequest createHttpRequest(String endpoint) {
        String graphqlPayload = getRequestPayload();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .headers(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(graphqlPayload, StandardCharsets.UTF_8))
                .build();
        return request;
    }

    /**
     * Gets the GraphQL request payload constructed using the introspection query.
     *
     * @return               the GraphQL request payload
     */
    private String getRequestPayload() {
        JSONObject graphqlJsonPayload = new JSONObject();
        graphqlJsonPayload.put(QUERY_VAR_NAME, INTROSPECTION_QUERY);
        return graphqlJsonPayload.toString();
    }

    /**
     * Attaches headers to the HTTP request object.
     *
     * @param builder         the builder of HTTP requests
     * @param headers         the headers map
     */
    private HttpRequest.Builder addHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            builder.header(e.getKey(), e.getValue());
        }
        return builder;
    }
}
