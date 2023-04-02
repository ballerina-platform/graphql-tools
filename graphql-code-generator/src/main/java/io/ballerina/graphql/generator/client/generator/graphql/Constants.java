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

package io.ballerina.graphql.generator.client.generator.graphql;

/**
 * This class represents GraphQL schema (SDL) reader & GraphQL query reader related constants.
 */
public class Constants {
    // The GraphQL scalar types
    public static final String GRAPHQL_ID_TYPE = "ID";
    public static final String GRAPHQL_STRING_TYPE = "String";
    public static final String GRAPHQL_INT_TYPE = "Int";
    public static final String GRAPHQL_FLOAT_TYPE = "Float";
    public static final String GRAPHQL_BOOLEAN_TYPE = "Boolean";

    // The GraphQL named types
    public static final String GRAPHQL_INPUT_OBJECT_TYPE = "GraphQLInputObjectType";
    public static final String GRAPHQL_OBJECT_TYPE = "GraphQLObjectType";
    public static final String GRAPHQL_INTERFACE_TYPE = "GraphQLInterfaceType";
    public static final String GRAPHQL_UNION_TYPE = "GraphQLUnionType";
    public static final String GRAPHQL_ENUM_TYPE = "GraphQLEnumType";
    public static final String GRAPHQL_SCALAR_TYPE = "GraphQLScalarType";
    public static final String GRAPHQL_TYPE_REFERENCE = "GraphQLTypeReference";

    // The Ballerina types
    public static final String BALLERINA_STRING_TYPE = "string";
    public static final String BALLERINA_INT_TYPE = "int";
    public static final String BALLERINA_FLOAT_TYPE = "float";
    public static final String BALLERINA_BOOLEAN_TYPE = "boolean";
    public static final String BALLERINA_ANYDATA_TYPE = "anydata";

    // GraphQL directives
    public static final String GRAPHQL_DEPRECATED_DIRECTIVE = "deprecated";
}
