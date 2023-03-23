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

/**
 * Represents a GraphQL project in GraphQL config file.
 */
public abstract class GraphqlProject {

    private String name;
    private String schema; // Web URL or File URL
    private String outputPath;
    private GraphQLSchema graphQLSchema; // Populated while validating

    public GraphqlProject(String name, String schema, String outputPath) {
        this.name = name;
        this.schema = schema;
        this.outputPath = outputPath;
    }

    public GraphqlProject(String name, String schema) {
        this.name = name;
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public String getFileName() {
        String[] splits = schema.split("\\\\|/");
        return splits[splits.length - 1].split("\\.")[0];
    }

    public void setGraphQLSchema(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public abstract GenerationType getGenerationType();

}
