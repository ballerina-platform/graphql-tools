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

import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.EnumValueModel;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes Ballerina enums.
 */
public final class EnumEmitter {

    private static final String TEMPLATE = "templates/common/enum.bal";
    private static final String DOCS = "docs";
    private static final String NAME = "name";
    private static final String VALUES = "values";
    private static final String NOT_DEPRECATED = null;
    private static final String VALUE_SEPARATOR = "," + System.lineSeparator();

    private EnumEmitter() {
    }

    /**
     * Writes an enum.
     *
     * @param enumModel the enum
     * @return the enum definition
     */
    public static String emit(EnumModel enumModel) {
        return TemplateLoader.fill(TEMPLATE, Map.of(
                DOCS, DocEmitter.emit(enumModel.description(), NOT_DEPRECATED),
                NAME, enumModel.name(),
                VALUES, enumModel.values().stream().map(EnumEmitter::value)
                        .collect(Collectors.joining(VALUE_SEPARATOR))));
    }

    private static String value(EnumValueModel value) {
        return DocEmitter.emit(value.description(), value.deprecationReason()) + value.name();
    }
}
