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

package io.ballerina.graphql.generator.gateway.generator.common;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.ballerina.graphql.generator.gateway.exception.GatewayCommonException;
import io.ballerina.graphql.generator.utils.graphql.SpecReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common utility functions used inside the package.
 */
public class Utils {

    /**
     * Return the list of custom defined object type names in the GraphQL schema.
     *
     * @param graphQLSchema GraphQL schema
     * @return List of custom defined object type names
     * */
    public static List<String> getCustomDefinedObjectTypeNames(GraphQLSchema graphQLSchema) {
        return SpecReader.getObjectTypeNames(graphQLSchema).stream()
                .filter(name -> name != null && !name.isEmpty() && !name.equals("Query") && !name.equals("Mutation") &&
                        !name.equals("Subscription")).collect(Collectors.toList());
    }

    /**
     * Return the type name of the GraphQL type.
     *
     * @param queryType GraphQL type
     * @return Type name
     * @throws GatewayCommonException if the type is not supported
     * */
    public static String getTypeNameFromGraphQLType(GraphQLType queryType) throws GatewayCommonException {
        if (queryType instanceof GraphQLObjectType) {
            return ((GraphQLObjectType) queryType).getName();
        } else if (queryType instanceof GraphQLList) {
            return getTypeNameFromGraphQLType(((GraphQLList) queryType).getOriginalWrappedType()) + "[]";
        } else {
            throw new GatewayCommonException("Unsupported type: " + queryType);
        }
    }

    /**
     * Return map of join graphs in the GraphQL schema as Enum value as the key and a JoinGraph object as the value.
     *
     * @param graphQLSchema GraphQL schema
     * @return Map of join graphs
     * */
    public static Map<String, JoinGraph> getJoinGraphs(GraphQLSchema graphQLSchema) {
        Map<String, JoinGraph> joinGraphs = new HashMap<>();
        GraphQLType joinGraph = graphQLSchema.getType("join__Graph");
        if (joinGraph != null) {
            for (GraphQLEnumValueDefinition element: ((GraphQLEnumType) joinGraph).getValues()) {
                joinGraphs.put(element.getName(), new JoinGraph(element));
            }
        }
        return joinGraphs;
    }

}
