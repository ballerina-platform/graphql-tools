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

package io.ballerina.graphql.generator.common.emitter;

import io.ballerina.graphql.generator.common.model.BalType;
import io.ballerina.graphql.generator.common.model.ListType;
import io.ballerina.graphql.generator.common.model.NamedType;

import java.util.List;

/**
 * Writes Ballerina type descriptors.
 */
public final class TypeEmitter {

    private static final String ARRAY_SUFFIX = "[]";
    private static final String OPTIONAL_SUFFIX = "?";
    private static final String UNION_SEPARATOR = "|";
    private static final String ID_ANNOTATION = "@graphql:ID ";
    private TypeEmitter() {
    }

    /**
     * Writes a type, e.g. {@code Book?[]?} for a nullable list of nullable books.
     *
     * @param type the type
     * @return the Ballerina type descriptor
     */
    public static String emit(BalType type) {
        String descriptor = type instanceof ListType list
                ? emit(list.element()) + ARRAY_SUFFIX
                : ((NamedType) type).name();
        return type.nullable() ? descriptor + OPTIONAL_SUFFIX : descriptor;
    }

    /**
     * Writes a type with the {@code @graphql:ID} annotation in front when it represents the GraphQL {@code ID}
     * scalar, e.g. {@code @graphql:ID string[]}.
     *
     * @param type the type
     * @return the Ballerina type descriptor, annotated if needed
     */
    public static String emitAnnotated(BalType type) {
        return isId(type) ? ID_ANNOTATION + emit(type) : emit(type);
    }

    private static boolean isId(BalType type) {
        return type instanceof ListType list ? isId(list.element()) : ((NamedType) type).id();
    }

    /**
     * Writes a union of named types, e.g. {@code Book|Magazine}.
     *
     * @param members the member type names
     * @return the Ballerina union type descriptor
     */
    public static String emitUnion(List<String> members) {
        return String.join(UNION_SEPARATOR, members);
    }
}
