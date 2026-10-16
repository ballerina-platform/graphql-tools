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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Loads Ballerina code templates from the resources and fills their {@code {{placeholder}}} values.
 */
public final class TemplateLoader {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");
    private static final String RESOURCE_ROOT = "/";
    private static final String ERROR_MISSING_TEMPLATE = "Template \"%s\" was not found.";
    private static final String ERROR_MISSING_VALUE = "No value was given for \"%s\" in template \"%s\".";

    private TemplateLoader() {
    }

    /**
     * Loads a template and replaces each of its placeholders with the given value.
     *
     * @param templatePath the template path under the resources, e.g. {@code templates/common/record.bal}
     * @param values       the value for each placeholder name
     * @return the filled template
     * @throws IllegalStateException if the template is missing or a placeholder has no value
     */
    public static String fill(String templatePath, Map<String, String> values) {
        Matcher placeholders = PLACEHOLDER.matcher(load(templatePath));
        StringBuilder filled = new StringBuilder();
        while (placeholders.find()) {
            String value = values.get(placeholders.group(1));
            if (value == null) {
                throw new IllegalStateException(String.format(ERROR_MISSING_VALUE, placeholders.group(1),
                        templatePath));
            }
            placeholders.appendReplacement(filled, Matcher.quoteReplacement(value));
        }
        return placeholders.appendTail(filled).toString();
    }

    private static String load(String templatePath) {
        try (InputStream stream = TemplateLoader.class.getResourceAsStream(RESOURCE_ROOT + templatePath)) {
            if (stream == null) {
                throw new IllegalStateException(String.format(ERROR_MISSING_TEMPLATE, templatePath));
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).stripTrailing().lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
