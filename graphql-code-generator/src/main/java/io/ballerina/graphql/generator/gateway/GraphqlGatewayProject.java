/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.generator.gateway;

import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.utils.GenerationType;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to represent GraphQL federation gateway generation project.
 */
public class GraphqlGatewayProject extends GraphqlProject {

    public static GenerationType generationType = GenerationType.GATEWAY;
    public static final Path GATEWAY_TEMPLATE_PATH =
            Paths.get("src", "main", "resources", "gateway");

    public GraphqlGatewayProject(String name, String schema, String outputPath) {
        super(name, schema, outputPath);
    }

    @Override
    public GenerationType getGenerationType() {
        return generationType;
    }
}