/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.graphql.generator.common.mapper;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import io.ballerina.graphql.generator.common.model.BalType;
import io.ballerina.graphql.generator.common.model.ListType;
import io.ballerina.graphql.generator.common.model.NamedType;

/**
 * Maps GraphQL types to Ballerina types.
 */
public final class TypeMapper {

    private static final String ERROR_UNSUPPORTED_SCALAR = "Custom scalar \"%s\" is not supported.";

    private TypeMapper() {
    }

    /**
     * Maps a GraphQL type to a Ballerina type, e.g. {@code [Book!]} to {@code Book[]?}.
     *
     * @param type the GraphQL type
     * @return the Ballerina type
     * @throws IllegalArgumentException if the type is a custom scalar
     */
    public static BalType map(GraphQLType type) {
        return map(type, true);
    }

    private static BalType map(GraphQLType type, boolean nullable) {
        if (type instanceof GraphQLNonNull nonNull) {
            return map(nonNull.getWrappedType(), false);
        }
        if (type instanceof GraphQLList list) {
            return new ListType(map(list.getWrappedType(), true), nullable);
        }
        return new NamedType(ballerinaName((GraphQLNamedType) type), nullable);
    }

    private static String ballerinaName(GraphQLNamedType type) {
        if (type instanceof GraphQLScalarType) {
            return Scalar.fromGraphqlName(type.getName())
                    .map(Scalar::getBallerinaType)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(ERROR_UNSUPPORTED_SCALAR, type.getName())));
        }
        return NameEscaper.escape(type.getName());
    }
}
