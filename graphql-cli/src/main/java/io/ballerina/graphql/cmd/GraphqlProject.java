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

package io.ballerina.graphql.cmd;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.pojo.Extension;

import java.util.List;

/**
 * Represents a GraphQL project in GraphQL config file.
 */
public class GraphqlProject {

    private String name;
    private String schema; // Web URL or File URL
    private List<String> documents;
    private Extension extensions;
    private String outputPath;
    private GraphQLSchema graphQLSchema; // Populated while validating

    public GraphqlProject(String name, String schema, List<String> documents, Extension extensions, String outputPath) {
        this.name = name;
        this.schema = schema;
        this.documents = documents;
        this.extensions = extensions;
        this.outputPath = outputPath;
    }

    public GraphqlProject(String name, String schema, List<String> documents, Extension extensions) {
        this.name = name;
        this.schema = schema;
        this.documents = documents;
        this.extensions = extensions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }

    public Extension getExtensions() {
        return extensions;
    }

    public void setExtensions(Extension extensions) {
        this.extensions = extensions;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public void setGraphQLSchema(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }
}
