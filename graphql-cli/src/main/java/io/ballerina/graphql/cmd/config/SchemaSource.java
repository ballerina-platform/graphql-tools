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

package io.ballerina.graphql.cmd.config;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents where the GraphQL schema is read from.
 */
public enum SchemaSource {

    FILE("file"),
    URL("url"),
    INTROSPECTION("introspection");

    private final String value;

    SchemaSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<SchemaSource> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(SchemaSource.values())
                .filter(source -> source.value.equals(value))
                .findFirst();
    }
}
