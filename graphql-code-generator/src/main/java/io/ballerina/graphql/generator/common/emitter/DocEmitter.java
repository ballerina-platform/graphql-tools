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

import java.util.Map;

/**
 * Writes Ballerina documentation lines from GraphQL descriptions and deprecation reasons.
 */
public final class DocEmitter {

    private static final String DOC_PREFIX = "# ";
    private static final String EMPTY_DOC_LINE = "#";
    private static final String PARAMETER_DOC_FORMAT = "# + %s - %s";
    private static final String DEPRECATED_HEADING = "# # Deprecated";
    private static final String DEPRECATED_ANNOTATION = "@deprecated";
    private static final String NEW_LINE = "\n";
    private static final String SPACE = " ";

    private DocEmitter() {
    }

    /**
     * Writes the documentation for a type, field or enum value.
     *
     * @param description       the GraphQL description, or null
     * @param deprecationReason the deprecation reason, or null if not deprecated
     * @param indent            the indentation of each line
     * @return the documentation lines, each ending with a new line; empty if there is nothing to document
     */
    public static String emit(String description, String deprecationReason, String indent) {
        return emit(description, Map.of(), deprecationReason, indent);
    }

    /**
     * Writes the documentation for a function, including its parameter descriptions.
     *
     * @param description           the GraphQL description, or null
     * @param parameterDescriptions the description of each parameter, in parameter order; null values are skipped
     * @param deprecationReason     the deprecation reason, or null if not deprecated
     * @param indent                the indentation of each line
     * @return the documentation lines, each ending with a new line; empty if there is nothing to document
     */
    public static String emit(String description, Map<String, String> parameterDescriptions,
                              String deprecationReason, String indent) {
        StringBuilder docs = new StringBuilder();
        if (description != null) {
            appendLines(docs, description, indent);
        }
        parameterDescriptions.forEach((name, parameterDescription) -> {
            if (parameterDescription != null) {
                docs.append(indent).append(String.format(PARAMETER_DOC_FORMAT, name,
                        parameterDescription.replace(NEW_LINE, SPACE))).append(NEW_LINE);
            }
        });
        if (deprecationReason != null) {
            docs.append(indent).append(DEPRECATED_HEADING).append(NEW_LINE);
            appendLines(docs, deprecationReason, indent);
            docs.append(indent).append(DEPRECATED_ANNOTATION).append(NEW_LINE);
        }
        return docs.toString();
    }

    private static void appendLines(StringBuilder docs, String text, String indent) {
        for (String line : text.strip().split(NEW_LINE)) {
            String content = line.stripTrailing();
            docs.append(indent).append(content.isEmpty() ? EMPTY_DOC_LINE : DOC_PREFIX + content).append(NEW_LINE);
        }
    }
}
