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

package io.ballerina.graphql.generator.graphql;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.model.FieldType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the GraphQL Schema (SDL) reader.
 */
public class SpecReader {

    /**
     * Get the input object type names from the GraphQL schema.
     *
     * @param graphQLSchema         the instance of the Graphql schema file
     * @return                      the list of the input object type names
     */
    public static List<String> getInputObjectTypeNames(GraphQLSchema graphQLSchema) {
        List<String> inputObjectTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLInputObjectType) {
                inputObjectTypeNames.add(graphQLNamedType.getName());
            }
        }
        return inputObjectTypeNames;
    }

    /**
     * Get the input object type fields map based on the input object type name from the GraphQL schema.
     *
     * @param graphQLSchema         the instance of the Graphql schema file
     * @param inputObjectTypeName   the input object type name
     * @return                      the input object type fields map
     */
    public static Map<String, FieldType> getInputTypeFieldsMap(GraphQLSchema graphQLSchema,
                                                               String inputObjectTypeName) {
        Map<String, FieldType> inputTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(inputObjectTypeName) instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType inputObjectType =
                    ((GraphQLInputObjectType) graphQLSchema.getType(inputObjectTypeName));
            if (inputObjectType != null) {
                for (GraphQLInputObjectField field : inputObjectType.getFields()) {
                    inputTypeFieldsMap.put(CodeGeneratorUtils.escapeIdentifier(field.getName()),
                            Utils.getFieldType(graphQLSchema, field.getDefinition().getType()));
                }
            }
        }
        return inputTypeFieldsMap;
    }

    /**
     * Get the object type names from the GraphQL schema.
     *
     * @param graphQLSchema         the instance of the Graphql schema file
     * @return                      the list of the object type names
     */
    public static List<String> getObjectTypeNames(GraphQLSchema graphQLSchema) {
        List<String> objectTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLObjectType && !graphQLNamedType.getName().startsWith("__")) {
                objectTypeNames.add(graphQLNamedType.getName());
            }
        }
        return objectTypeNames;
    }

    /**
     * Get the object type fields map based on the input object type name from the GraphQL schema.
     *
     * @param graphQLSchema         the instance of the Graphql schema file
     * @param objectTypeName        the object type name
     * @return                      the object type fields map
     */
    public static Map<String, FieldType> getObjectTypeFieldsMap(GraphQLSchema graphQLSchema, String objectTypeName) {
        Map<String, FieldType> objectTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(objectTypeName) instanceof GraphQLObjectType) {
            GraphQLObjectType objectType =
                    ((GraphQLObjectType) graphQLSchema.getType(objectTypeName));
            if (objectType != null) {
                for (GraphQLFieldDefinition field : objectType.getFields()) {
                    objectTypeFieldsMap.put(CodeGeneratorUtils.escapeIdentifier(field.getName()),
                            Utils.getFieldType(graphQLSchema, field.getDefinition().getType()));
                }
            }
        }
        return objectTypeFieldsMap;
    }

    /**
     * Get the custom scalar type names from the GraphQL schema.
     *
     * @param graphQLSchema         the instance of the Graphql schema file
     * @return                      the list of the custom scalar type names
     */
    public static List<String> getCustomScalarTypeNames(GraphQLSchema graphQLSchema) {
        List<String> scalarTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLScalarType && !graphQLNamedType.getName().startsWith("__")
                    && !Utils.isPrimitiveScalarType(graphQLNamedType.getName())) {
                scalarTypeNames.add(graphQLNamedType.getName());
            }
        }
        return scalarTypeNames;
    }

    /**
     * Get the enum type names from the GraphQL schema.
     *
     * @param graphQLSchema         the instance of the Graphql schema file
     * @return                      the list of the enum type names
     */
    public static List<String> getEnumTypeNames(GraphQLSchema graphQLSchema) {
        List<String> enumTypeNames = new ArrayList<>();
        for (GraphQLNamedType graphQLNamedType : graphQLSchema.getAllTypesAsList()) {
            if (graphQLNamedType instanceof GraphQLEnumType && !graphQLNamedType.getName().startsWith("__")) {
                enumTypeNames.add(graphQLNamedType.getName());
            }
        }
        return enumTypeNames;
    }
}
