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

import io.ballerina.compiler.syntax.tree.SyntaxInfo;

/**
 * Makes GraphQL names usable as Ballerina identifiers.
 */
public final class NameEscaper {

    private static final String QUOTE = "'";

    private NameEscaper() {
    }

    /**
     * Quotes a name that is a Ballerina keyword, e.g. {@code type} becomes {@code 'type}.
     *
     * @param name a GraphQL name
     * @return the name, quoted if it is a keyword
     */
    public static String escape(String name) {
        return SyntaxInfo.isKeyword(name) ? QUOTE + name : name;
    }
}
