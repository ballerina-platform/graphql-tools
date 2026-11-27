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

package io.ballerina.graphql.schema;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Names the SDL schema file written for a GraphQL service.
 */
public final class SchemaFileName {

    /**
     * Separates a base path from the number added when two services have the same base path.
     */
    public static final String DUPLICATE_SEPARATOR = ".";

    private static final String PREFIX = "schema_";
    private static final String EXTENSION = ".graphql";
    private static final String PATH_SEPARATOR = "/";
    private static final String WORD_SEPARATOR = "_";
    private static final String NON_ALPHANUMERIC = "[^a-zA-Z0-9]";

    private SchemaFileName() {
    }

    /**
     * Builds the schema file name of a service.
     *
     * @param balFilePath the path of the Ballerina file that holds the service
     * @param serviceName the base path of the service, with a number added if another service has the same one
     * @return the schema file name
     */
    public static String of(String balFilePath, String serviceName) {
        String name;
        if (serviceName.isBlank()) {
            name = FilenameUtils.removeExtension(balFilePath);
        } else if (serviceName.startsWith(DUPLICATE_SEPARATOR)) {
            name = FilenameUtils.removeExtension(balFilePath) + serviceName;
        } else {
            String basePath = serviceName.startsWith(PATH_SEPARATOR) ? serviceName.substring(1) : serviceName;
            name = basePath.replaceAll(PATH_SEPARATOR, WORD_SEPARATOR);
        }
        return PREFIX + normalize(name) + EXTENSION;
    }

    private static String normalize(String name) {
        String[] words = name.split(NON_ALPHANUMERIC);
        if (words.length > 0) {
            return Arrays.stream(words)
                    .filter(word -> !word.isBlank())
                    .collect(Collectors.joining(WORD_SEPARATOR));
        }
        return name;
    }
}
