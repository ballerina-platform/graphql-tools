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

import io.ballerina.graphql.generator.common.model.FieldModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes closed Ballerina record types.
 */
public final class RecordEmitter {

    private static final String TEMPLATE = "templates/common/record.bal";
    private static final String DOCS = "docs";
    private static final String NAME = "name";
    private static final String FIELDS = "fields";
    private static final String NO_INDENT = "";
    private static final String NOT_DEPRECATED = null;
    private static final String MEMBER_INDENT = "    ";
    private static final String FIELD_FORMAT = "%s%s %s;";
    private static final String FIELD_WITH_DEFAULT_FORMAT = "%s%s %s = %s;";
    private static final String NEW_LINE = "\n";

    private RecordEmitter() {
    }

    /**
     * Writes a record type.
     *
     * @param name        the record name
     * @param fields      the record fields
     * @param description the GraphQL description, or null
     * @return the record type definition
     */
    public static String emit(String name, List<FieldModel> fields, String description) {
        return TemplateLoader.fill(TEMPLATE, Map.of(
                DOCS, DocEmitter.emit(description, NOT_DEPRECATED, NO_INDENT),
                NAME, name,
                FIELDS, fields.stream().map(RecordEmitter::field).collect(Collectors.joining(NEW_LINE))));
    }

    private static String field(FieldModel field) {
        String docs = DocEmitter.emit(field.description(), field.deprecationReason(), MEMBER_INDENT);
        String type = TypeEmitter.emitAnnotated(field.type());
        return docs + (field.defaultValue() == null
                ? String.format(FIELD_FORMAT, MEMBER_INDENT, type, field.name())
                : String.format(FIELD_WITH_DEFAULT_FORMAT, MEMBER_INDENT, type, field.name(), field.defaultValue()));
    }
}
