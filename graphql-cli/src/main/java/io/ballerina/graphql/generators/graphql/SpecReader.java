/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generators.graphql;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.TypeName;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

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
    public static Map<String, String> getInputTypeFieldsMap(GraphQLSchema graphQLSchema, String inputObjectTypeName) {
        Map<String, String> inputTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(inputObjectTypeName) instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType inputObjectType =
                    ((GraphQLInputObjectType) graphQLSchema.getType(inputObjectTypeName));
            if (inputObjectType != null) {
                for (GraphQLInputObjectField field : inputObjectType.getFields()) {
                    inputTypeFieldsMap.put(field.getName(), getInputFieldType(field));
                }
            }
        } else {
            return null;
        }
        return inputTypeFieldsMap;
    }

    /**
     * Gets the input field type as a string representation of Ballerina type.
     *
     * @param field         the instance of the `GraphQLInputObjectField`
     * @return              the input field type as a string representation of Ballerina type
     */
    private static String getInputFieldType(GraphQLInputObjectField field) {
        StringBuilder fieldTypeAsString = new StringBuilder();
        if (field.getDefinition().getType() instanceof TypeName) {
            fieldTypeAsString.append(((TypeName) field.getDefinition().getType()).getName()).append("?");
        }
        if (field.getDefinition().getType() instanceof NonNullType) {
            if (((NonNullType) field.getDefinition().getType()).getType() instanceof TypeName) {
                fieldTypeAsString.append(((TypeName) ((NonNullType) field.getDefinition().getType())
                        .getType()).getName());
            }
            if (((NonNullType) field.getDefinition().getType()).getType() instanceof ListType) {
                if (((ListType) ((NonNullType) field.getDefinition().getType())
                        .getType()).getType() instanceof TypeName) {
                    fieldTypeAsString.append(((TypeName) ((ListType) ((NonNullType) field.getDefinition().getType())
                            .getType()).getType()).getName()).append("?[]");
                }
                if (((ListType) ((NonNullType) field.getDefinition().getType())
                        .getType()).getType() instanceof NonNullType) {
                    if (((NonNullType) ((ListType) ((NonNullType) field.getDefinition().getType())
                            .getType()).getType()).getType() instanceof TypeName) {
                        fieldTypeAsString.append(((TypeName) ((NonNullType) ((ListType)
                                ((NonNullType) field.getDefinition().getType())
                                .getType()).getType()).getType()).getName()).append("[]");
                    }
                }

            }

        }
        if (field.getDefinition().getType() instanceof ListType) {
            if (((ListType) field.getDefinition().getType()).getType() instanceof TypeName) {
                fieldTypeAsString.append(((TypeName) ((ListType) field.getDefinition().getType())
                        .getType()).getName()).append("?[]?");
            }
            if (((ListType) field.getDefinition().getType()).getType() instanceof NonNullType) {
                if (((NonNullType) ((ListType) field.getDefinition().getType())
                        .getType()).getType() instanceof TypeName) {
                    fieldTypeAsString.append(((TypeName) ((NonNullType) ((ListType) field.getDefinition().getType())
                            .getType()).getType()).getName()).append("[]?");
                }

            }

        }
        return fieldTypeAsString.toString();
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
    public static Map<String, String> getObjectTypeFieldsMap(GraphQLSchema graphQLSchema, String objectTypeName) {
        Map<String, String> inputTypeFieldsMap = new HashMap<>();
        if (graphQLSchema.getType(objectTypeName) instanceof GraphQLObjectType) {
            GraphQLObjectType objectType =
                    ((GraphQLObjectType) graphQLSchema.getType(objectTypeName));
            if (objectType != null) {
                for (GraphQLFieldDefinition field : objectType.getFields()) {
                    inputTypeFieldsMap.put(field.getName(), getObjectFieldType(field));
                }
            }
        } else {
            return null;
        }
        return inputTypeFieldsMap;
    }

    /**
     * Gets the object field type as a string representation of Ballerina type.
     *
     * @param field         the instance of the `GraphQLFieldDefinition`
     * @return              the object field type as a string representation of Ballerina type
     */
    private static String getObjectFieldType(GraphQLFieldDefinition field) {
        StringBuilder fieldTypeAsString = new StringBuilder();
        if (field.getDefinition().getType() instanceof TypeName) {
            fieldTypeAsString.append(((TypeName) field.getDefinition().getType()).getName()).append("?");
        }
        if (field.getDefinition().getType() instanceof NonNullType) {
            if (((NonNullType) field.getDefinition().getType()).getType() instanceof TypeName) {
                fieldTypeAsString.append(((TypeName) ((NonNullType) field.getDefinition().getType())
                        .getType()).getName());
            }
            if (((NonNullType) field.getDefinition().getType()).getType() instanceof ListType) {
                if (((ListType) ((NonNullType) field.getDefinition().getType())
                        .getType()).getType() instanceof TypeName) {
                    fieldTypeAsString.append(((TypeName) ((ListType) ((NonNullType) field.getDefinition().getType())
                            .getType()).getType()).getName()).append("?[]");
                }
                if (((ListType) ((NonNullType) field.getDefinition().getType())
                        .getType()).getType() instanceof NonNullType) {
                    if (((NonNullType) ((ListType) ((NonNullType) field.getDefinition().getType())
                            .getType()).getType()).getType() instanceof TypeName) {
                        fieldTypeAsString.append(((TypeName) ((NonNullType) ((ListType)
                                ((NonNullType) field.getDefinition().getType())
                                        .getType()).getType()).getType()).getName()).append("[]");
                    }
                }

            }

        }
        if (field.getDefinition().getType() instanceof ListType) {
            if (((ListType) field.getDefinition().getType()).getType() instanceof TypeName) {
                fieldTypeAsString.append(((TypeName) ((ListType) field.getDefinition().getType())
                        .getType()).getName()).append("?[]?");
            }
            if (((ListType) field.getDefinition().getType()).getType() instanceof NonNullType) {
                if (((NonNullType) ((ListType) field.getDefinition().getType())
                        .getType()).getType() instanceof TypeName) {
                    fieldTypeAsString.append(((TypeName) ((NonNullType) ((ListType) field.getDefinition().getType())
                            .getType()).getType()).getName()).append("[]?");
                }

            }

        }
        return fieldTypeAsString.toString();
    }
}
