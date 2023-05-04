/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
