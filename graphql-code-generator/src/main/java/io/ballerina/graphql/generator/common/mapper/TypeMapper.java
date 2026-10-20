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
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.graphql.generator.common.model.TypeModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps GraphQL types to Ballerina types.
 */
public final class TypeMapper {

    private static final boolean NULLABLE = true;
    private static final boolean NON_NULL = false;
    // A leading digit, or any character that is not a letter, digit or underscore.
    private static final Pattern INVALID_IDENTIFIER_CHARACTER = Pattern.compile("^\\p{Nd}|[^\\p{L}\\p{Nd}_]");
    private static final int FIRST_CHARACTER = 0;
    private static final String UNICODE_ESCAPE_FORMAT = "\\u{%X}";
    private static final String ERROR_UNSUPPORTED_SCALAR = "Custom scalar \"%s\" is not supported.";
    private static final String ERROR_INVALID_NAME = "\"%s\" cannot be used as a Ballerina identifier.";

    private TypeMapper() {
    }

    /**
     * Maps a GraphQL type to a Ballerina type, e.g. {@code [Book!]} to {@code Book[]?}.
     *
     * @param type the GraphQL type
     * @return the Ballerina type
     * @throws IllegalArgumentException if the type is a custom scalar
     */
    public static TypeModel map(GraphQLType type) {
        return map(type, NULLABLE);
    }

    /**
     * Makes a name a valid Ballerina identifier. A keyword is quoted, e.g. {@code type} becomes {@code 'type}, and a
     * character that cannot appear in an identifier is escaped, e.g. {@code my-schema} becomes {@code my\-schema}.
     *
     * @param name a GraphQL or schema file name
     * @return the name as a valid Ballerina identifier
     * @throws IllegalArgumentException if the name cannot be made a valid identifier
     */
    public static String escapeName(String name) {
        if (SyntaxInfo.isKeyword(name)) {
            return SyntaxKind.SINGLE_QUOTE_TOKEN.stringValue() + name;
        }
        if (SyntaxInfo.isIdentifier(name)) {
            return name;
        }
        String escaped = INVALID_IDENTIFIER_CHARACTER.matcher(name)
                .replaceAll(match -> Matcher.quoteReplacement(escapeCharacter(match.group())));
        if (!SyntaxInfo.isIdentifier(escaped)) {
            throw new IllegalArgumentException(String.format(ERROR_INVALID_NAME, name));
        }
        return escaped;
    }

    private static String escapeCharacter(String character) {
        int codePoint = character.codePointAt(FIRST_CHARACTER);
        return Character.isWhitespace(codePoint) ? String.format(UNICODE_ESCAPE_FORMAT, codePoint)
                : SyntaxKind.BACK_SLASH_TOKEN.stringValue() + character;
    }

    private static TypeModel map(GraphQLType type, boolean nullable) {
        if (type instanceof GraphQLNonNull nonNull) {
            return map(nonNull.getWrappedType(), NON_NULL);
        }
        if (type instanceof GraphQLList list) {
            return TypeModel.list(map(list.getWrappedType(), NULLABLE), nullable);
        }
        GraphQLNamedType namedType = (GraphQLNamedType) type;
        return TypeModel.named(ballerinaName(namedType), nullable, isId(namedType));
    }

    private static boolean isId(GraphQLNamedType type) {
        return type instanceof GraphQLScalarType
                && Scalar.fromGraphqlName(type.getName()).filter(Scalar.ID::equals).isPresent();
    }

    private static String ballerinaName(GraphQLNamedType type) {
        if (type instanceof GraphQLScalarType) {
            return Scalar.fromGraphqlName(type.getName())
                    .map(Scalar::getBallerinaType)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(ERROR_UNSUPPORTED_SCALAR, type.getName())));
        }
        return escapeName(type.getName());
    }
}
