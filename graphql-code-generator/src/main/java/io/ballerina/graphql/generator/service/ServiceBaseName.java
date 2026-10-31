/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.generator.service;

/**
 * The name a generated service is named after, and where it came from.
 *
 * @param name   the base name, e.g. {@code book-api}, which gives {@code BookApiService}
 * @param source where the base name came from
 */
public record ServiceBaseName(String name, Source source) {

    /**
     * Where a service base name came from, with the advice shown when no valid service name can be made from it.
     */
    public enum Source {
        GIVEN("given name", "Choose another name."),
        SCHEMA_FILE("schema file", "Rename the schema file, or set a name with --service-name."),
        MODULE("module", "Rename the module, or set a name with --service-name."),
        PACKAGE("package", "Rename the package in Ballerina.toml, or set a name with --service-name."),
        DIRECTORY("directory", "Rename the directory, or set a name with --service-name.");

        private final String label;
        private final String advice;

        Source(String label, String advice) {
            this.label = label;
            this.advice = advice;
        }

        public String label() {
            return label;
        }

        public String advice() {
            return advice;
        }
    }
}
