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

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.model.FieldType;

import static io.ballerina.graphql.generator.graphql.Constants.BALLERINA_ANYDATA_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.BALLERINA_BOOLEAN_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.BALLERINA_FLOAT_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.BALLERINA_INT_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.BALLERINA_STRING_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.GRAPHQL_BOOLEAN_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.GRAPHQL_FLOAT_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.GRAPHQL_ID_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.GRAPHQL_INT_TYPE;
import static io.ballerina.graphql.generator.graphql.Constants.GRAPHQL_STRING_TYPE;

/**
 * Utility class for GraphQL schema (SDL) reader & GraphQL query reader.
 */
public class Utils {

    /**
     * Gets the Ballerina type name for a given GraphQL type name.
     *
     * @param graphQLSchema     the object instance of the GraphQL schema (SDL)
     * @param graphqlTypeName   the GraphQL scalar type name
     * @return                  the Ballerina type name for a given GraphQL scalar type name
     */
    public static String getBallerinaTypeName(GraphQLSchema graphQLSchema, String graphqlTypeName) {
        String ballerinaTypeName;
        if (isCustomScalarType(graphQLSchema, graphqlTypeName)) {
            ballerinaTypeName = BALLERINA_ANYDATA_TYPE;
        } else if (isEnumType(graphQLSchema, graphqlTypeName)) {
            ballerinaTypeName = BALLERINA_STRING_TYPE;
        } else {
            switch (graphqlTypeName) {
                case GRAPHQL_ID_TYPE:
                case GRAPHQL_STRING_TYPE:
                    ballerinaTypeName = BALLERINA_STRING_TYPE;
                    break;
                case GRAPHQL_INT_TYPE:
                    ballerinaTypeName = BALLERINA_INT_TYPE;
                    break;
                case GRAPHQL_FLOAT_TYPE:
                    ballerinaTypeName = BALLERINA_FLOAT_TYPE;
                    break;
                case GRAPHQL_BOOLEAN_TYPE:
                    ballerinaTypeName = BALLERINA_BOOLEAN_TYPE;
                    break;
                default:
                    ballerinaTypeName = graphqlTypeName;
            }
        }
        return ballerinaTypeName;
    }

    /**
     * Gets the representation of Ballerina field type for a given GraphQL field type.
     *
     * @param graphQLSchema     the object instance of the GraphQL schema (SDL)
     * @param type              the field type
     * @return                  the string representation of Ballerina type for a given GraphQL field type
     */
    public static FieldType getFieldType(GraphQLSchema graphQLSchema, Type<?> type) {
        FieldType fieldType = new FieldType();
        if (type instanceof TypeName) {
            fieldType.setName(getBallerinaTypeName(graphQLSchema, ((TypeName) type).getName()));
            fieldType.setTokens("?");
        }
        if (type instanceof NonNullType) {
            if (((NonNullType) type).getType() instanceof TypeName) {
                fieldType.setName(getBallerinaTypeName(graphQLSchema,
                        ((TypeName) ((NonNullType) type).getType()).getName()));
                fieldType.setTokens("");
            }
            if (((NonNullType) type).getType() instanceof ListType) {
                if (((ListType) ((NonNullType) type).getType()).getType() instanceof TypeName) {
                    fieldType.setName(getBallerinaTypeName(graphQLSchema,
                            ((TypeName) ((ListType) ((NonNullType) type).getType()).getType()).getName()));
                    fieldType.setTokens("?[]");
                }
                if (((ListType) ((NonNullType) type).getType()).getType() instanceof NonNullType) {
                    if (((NonNullType) ((ListType) ((NonNullType) type).getType()).getType())
                            .getType() instanceof TypeName) {
                        fieldType.setName(getBallerinaTypeName(graphQLSchema,
                                ((TypeName) ((NonNullType) ((ListType) ((NonNullType) type).getType()).getType())
                                        .getType()).getName()));
                        fieldType.setTokens("[]");
                    }
                }
            }
        }
        if (type instanceof ListType) {
            if (((ListType) type).getType() instanceof TypeName) {
                fieldType.setName(getBallerinaTypeName(graphQLSchema,
                        ((TypeName) ((ListType) type).getType()).getName()));
                fieldType.setTokens("?[]?");
            }
            if (((ListType) type).getType() instanceof NonNullType) {
                if (((NonNullType) ((ListType) type).getType()).getType() instanceof TypeName) {
                    fieldType.setName(getBallerinaTypeName(graphQLSchema,
                            ((TypeName) ((NonNullType) ((ListType) type).getType()).getType()).getName()));
                    fieldType.setTokens("[]?");
                }
            }
        }
        return fieldType;
    }

    /**
     * Checks whether a given GraphQL scalar type name is a primitive scalar type.
     *
     * @param graphqlTypeName   the GraphQL scalar type name
     * @return                  whether a given GraphQL scalar type name is a primitive scalar type
     */
    public static Boolean isPrimitiveScalarType(String graphqlTypeName) {
        boolean isPrimitiveScalarType;
        switch (graphqlTypeName) {
            case GRAPHQL_ID_TYPE:
            case GRAPHQL_STRING_TYPE:
            case GRAPHQL_INT_TYPE:
            case GRAPHQL_FLOAT_TYPE:
            case GRAPHQL_BOOLEAN_TYPE:
                isPrimitiveScalarType = true;
                break;
            default:
                isPrimitiveScalarType = false;
        }
        return isPrimitiveScalarType;
    }

    /**
     * Checks whether a given GraphQL type name is a custom scalar type.
     *
     * @param graphQLSchema     the object instance of the GraphQL schema (SDL)
     * @param graphqlTypeName   the GraphQL scalar type name
     * @return                  whether a given GraphQL scalar type name is a primitive scalar type
     */
    public static Boolean isCustomScalarType(GraphQLSchema graphQLSchema, String graphqlTypeName) {
        return SpecReader.getCustomScalarTypeNames(graphQLSchema).contains(graphqlTypeName);
    }

    /**
     * Checks whether a given GraphQL type name is an enum type.
     *
     * @param graphQLSchema     the object instance of the GraphQL schema (SDL)
     * @param graphqlTypeName   the GraphQL scalar type name
     * @return                  whether a given GraphQL scalar type name is a primitive scalar type
     */
    public static Boolean isEnumType(GraphQLSchema graphQLSchema, String graphqlTypeName) {
        return SpecReader.getEnumTypeNames(graphQLSchema).contains(graphqlTypeName);
    }
}
