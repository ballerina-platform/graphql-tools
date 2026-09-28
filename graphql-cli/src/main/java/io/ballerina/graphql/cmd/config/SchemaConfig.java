/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.cmd.config;

import java.util.Map;

/**
 * Represents the schema section of the balGraphQL.toml configuration file.
 */
public class SchemaConfig {

    private final SchemaSource source;
    private final String path;
    private final String url;
    private final String endpoint;
    private final Map<String, String> headers;

    public SchemaConfig(SchemaSource source, String path, String url, String endpoint,
                        Map<String, String> headers) {
        this.source = source;
        this.path = path;
        this.url = url;
        this.endpoint = endpoint;
        this.headers = headers;
    }

    public SchemaSource getSource() {
        return source;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
