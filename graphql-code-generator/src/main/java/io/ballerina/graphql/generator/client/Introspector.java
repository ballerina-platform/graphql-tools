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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
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
        return getIntrospectionResult(schema, extractHeaders(extensions));
    }

    /**
     * Returns the introspection results map for a given GraphQL endpoint using a plain headers map, for callers
     * that do not have the GraphQL config file's nested extensions/endpoints/default structure (e.g. the
     * balGraphQL.toml configuration, whose headers are a flat table).
     *
     * @param endpoint                               the GraphQL endpoint to introspect
     * @param headers                                the headers to send with the introspection request, or null
     * @return                                        the introspection results map
     * @throws IntospectionException                 If an error occurs during introspection of the GraphQL API
     */
    public Map<String, Object> getIntrospectionResult(String endpoint, Map<String, String> headers)
            throws IntospectionException {
        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
            HttpRequest httpRequest = createHttpRequest(endpoint, headers);
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject introspectionResult;
                try {
                    introspectionResult = new JSONObject(response.body());
                } catch (JSONException e) {
                    throw new IntospectionException("Failed to retrieve SDL. The endpoint did not return a JSON " +
                            "response. Please provide a valid GraphQL endpoint or a local SDL file path.");
                }
                Object data = introspectionResult.opt(DATA_FIELD);
                if (introspectionResult.has(ERROR_FIELD) || !(data instanceof JSONObject)) {
                    throw new IntospectionException("Failed to retrieve SDL. Please provide a valid GraphQL endpoint " +
                            "with relevant headers or a local SDL file path.");
                }
                return ((JSONObject) data).toMap();
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

    private Map<String, String> extractHeaders(Extension extensions) {
        if (extensions == null) {
            return null;
        }
        Endpoints endpoints = extensions.getEndpoints();
        if (endpoints == null) {
            return null;
        }
        Default defaultName = endpoints.getDefaultName();
        return defaultName == null ? null : defaultName.getHeaders();
    }

    private HttpRequest createHttpRequest(String endpoint, Map<String, String> headers) {
        String graphqlPayload = getRequestPayload();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .headers(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(graphqlPayload, StandardCharsets.UTF_8));
        if (headers != null) {
            addHeaders(builder, headers);
        }
        return builder.build();
    }

    private String getRequestPayload() {
        JSONObject graphqlJsonPayload = new JSONObject();
        graphqlJsonPayload.put(QUERY_VAR_NAME, INTROSPECTION_QUERY);
        return graphqlJsonPayload.toString();
    }

    private HttpRequest.Builder addHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            builder.header(e.getKey(), e.getValue());
        }
        return builder;
    }
}
