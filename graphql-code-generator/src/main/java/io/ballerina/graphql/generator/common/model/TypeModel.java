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

package io.ballerina.graphql.generator.common.model;

/**
 * A Ballerina type, modelled like a GraphQL type: a named type, or a list that wraps another type in
 * {@code ofType}. For example, {@code [Book!]} is a nullable list of a non-null named type {@code Book}.
 *
 * @param kind     whether the type is a named type or a list
 * @param name     the type name for a named type, e.g. {@code Book}; null for a list
 * @param ofType   the item type for a list; null for a named type
 * @param nullable whether the type is optional
 * @param id       whether a named type represents the GraphQL {@code ID} scalar
 */
public record TypeModel(Kind kind, String name, TypeModel ofType, boolean nullable, boolean id) {

    private static final String NO_NAME = null;
    private static final TypeModel NO_OF_TYPE = null;
    private static final boolean NOT_ID = false;

    /**
     * Creates a named type.
     *
     * @param name     the type name
     * @param nullable whether the type is optional
     * @param id       whether the type represents the GraphQL {@code ID} scalar
     * @return the named type
     */
    public static TypeModel named(String name, boolean nullable, boolean id) {
        return new TypeModel(Kind.NAMED, name, NO_OF_TYPE, nullable, id);
    }

    /**
     * Creates a named type that does not represent the GraphQL {@code ID} scalar.
     *
     * @param name     the type name
     * @param nullable whether the type is optional
     * @return the named type
     */
    public static TypeModel named(String name, boolean nullable) {
        return named(name, nullable, NOT_ID);
    }

    /**
     * Creates a list type.
     *
     * @param ofType   the item type
     * @param nullable whether the list itself is optional
     * @return the list type
     */
    public static TypeModel list(TypeModel ofType, boolean nullable) {
        return new TypeModel(Kind.LIST, NO_NAME, ofType, nullable, NOT_ID);
    }

    /**
     * The kinds of type.
     */
    public enum Kind {
        NAMED,
        LIST
    }
}
