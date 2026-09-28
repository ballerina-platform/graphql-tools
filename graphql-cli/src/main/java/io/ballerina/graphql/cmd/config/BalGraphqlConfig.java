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

import java.util.List;
import java.util.Map;

/**
 * Represents the content of a balGraphQL.toml configuration file.
 */
public class BalGraphqlConfig {

    public static final String FILE_NAME = "balGraphQL.toml";
    public static final String FILE_EXTENSION = ".toml";

    private final SchemaConfig schema;
    private final List<String> documents;
    private final Map<String, String> idTypes;
    private final Map<String, String> dataloaders;

    public BalGraphqlConfig(SchemaConfig schema, List<String> documents, Map<String, String> idTypes,
                            Map<String, String> dataloaders) {
        this.schema = schema;
        this.documents = documents;
        this.idTypes = idTypes;
        this.dataloaders = dataloaders;
    }

    public SchemaConfig getSchema() {
        return schema;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public Map<String, String> getIdTypes() {
        return idTypes;
    }

    public Map<String, String> getDataloaders() {
        return dataloaders;
    }

    public boolean hasDocuments() {
        return documents != null && !documents.isEmpty();
    }
}
