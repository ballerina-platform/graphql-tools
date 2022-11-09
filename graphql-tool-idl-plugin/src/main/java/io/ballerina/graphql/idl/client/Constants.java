/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.idl.client;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Storing constant for client IDL generator.
 *
 * @since 0.3.0
 */
public class Constants {
    /**
     * Enum class for contain diagnostic messages.
     */
    public enum DiagnosticMessages {
        ERROR_WHILE_GENERATING_CLIENT("GraphQL_IDL_CLIENT_100",
                "unexpected error occurred while generating the client", DiagnosticSeverity.ERROR),
        ERROR_WHILE_VALIDATING("GraphQL_IDL_CLIENT_101",
                "unexpected error occurred while validating the configurations", DiagnosticSeverity.ERROR),
        ERROR_MULTIPLE_PROJECT_AVAILABILITY("GraphQL_IDL_CLIENT_102",
                "multiple projects are found while generating the client", DiagnosticSeverity.ERROR);
        private final String code;
        private final String description;
        private final DiagnosticSeverity severity;

        DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
            this.code = code;
            this.description = description;
            this.severity = severity;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public DiagnosticSeverity getSeverity() {
            return severity;
        }
    }
}
