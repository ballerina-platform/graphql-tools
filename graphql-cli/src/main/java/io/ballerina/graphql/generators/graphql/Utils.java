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
import graphql.language.Type;
import graphql.language.TypeName;

import static io.ballerina.graphql.generators.graphql.Constants.GRAPHQL_BOOLEAN_TYPE;
import static io.ballerina.graphql.generators.graphql.Constants.GRAPHQL_FLOAT_TYPE;
import static io.ballerina.graphql.generators.graphql.Constants.GRAPHQL_ID_TYPE;
import static io.ballerina.graphql.generators.graphql.Constants.GRAPHQL_INT_TYPE;
import static io.ballerina.graphql.generators.graphql.Constants.GRAPHQL_STRING_TYPE;

/**
 * Utility class for GraphQL schema (SDL) reader & GraphQL query reader.
 */
public class Utils {
    public static String getBallerinaTypeName(String graphqlTypeName) {
        String ballerinaTypeName;
        switch (graphqlTypeName) {
            case GRAPHQL_ID_TYPE:
            case GRAPHQL_STRING_TYPE:
                ballerinaTypeName = "string";
                break;
            case GRAPHQL_INT_TYPE:
                ballerinaTypeName = "int";
                break;
            case GRAPHQL_FLOAT_TYPE:
                ballerinaTypeName = "float";
                break;
            case GRAPHQL_BOOLEAN_TYPE:
                ballerinaTypeName = "boolean";
                break;
            default:
                ballerinaTypeName = graphqlTypeName;
        }
        return ballerinaTypeName;
    }

    public static String getVariableType(Type<?> type) {
        StringBuilder fieldTypeAsString = new StringBuilder();
        if (type instanceof TypeName) {
            fieldTypeAsString.append(getBallerinaTypeName(((TypeName) type).getName())).append("?");

        }
        if (type instanceof NonNullType) {
            if (((NonNullType) type).getType() instanceof TypeName) {
                fieldTypeAsString.append(getBallerinaTypeName(((TypeName) ((NonNullType) type)
                        .getType()).getName()));
            }
            if (((NonNullType) type).getType() instanceof ListType) {
                if (((ListType) ((NonNullType) type)
                        .getType()).getType() instanceof TypeName) {
                    fieldTypeAsString.append(getBallerinaTypeName(((TypeName) ((ListType) ((NonNullType) type)
                            .getType()).getType()).getName())).append("?[]");
                }
                if (((ListType) ((NonNullType) type)
                        .getType()).getType() instanceof NonNullType) {
                    if (((NonNullType) ((ListType) ((NonNullType) type)
                            .getType()).getType()).getType() instanceof TypeName) {
                        fieldTypeAsString.append(getBallerinaTypeName(((TypeName) ((NonNullType) ((ListType)
                                ((NonNullType) type)
                                        .getType()).getType()).getType()).getName())).append("[]");
                    }
                }
            }
        }
        if (type instanceof ListType) {
            if (((ListType) type).getType() instanceof TypeName) {
                fieldTypeAsString.append(getBallerinaTypeName(((TypeName) ((ListType) type)
                        .getType()).getName())).append("?[]?");
            }
            if (((ListType) type).getType() instanceof NonNullType) {
                if (((NonNullType) ((ListType) type)
                        .getType()).getType() instanceof TypeName) {
                    fieldTypeAsString.append(getBallerinaTypeName(((TypeName) ((NonNullType) ((ListType) type)
                            .getType()).getType()).getName())).append("[]?");
                }
            }
        }
        return fieldTypeAsString.toString();
    }
}
