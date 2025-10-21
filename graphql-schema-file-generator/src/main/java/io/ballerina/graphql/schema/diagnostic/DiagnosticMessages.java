/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.schema.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code DiagnosticMessages} enum class represents the error messages related to SDL schema generation.
 */
public enum DiagnosticMessages {
    SDL_SCHEMA_100("SDL_SCHEMA_100", "Given Ballerina file contains compilation error(s).", DiagnosticSeverity.ERROR),
    SDL_SCHEMA_101("SDL_SCHEMA_101", "No Ballerina services found with name \"%s\" to generate SDL schema. " +
            "These services are available in ballerina file. %s", DiagnosticSeverity.ERROR),
    SDL_SCHEMA_102("SDL_SCHEMA_102", "SDL Schema generation failed due to an error occurred " +
            "in Ballerina GraphQL Package: %s", DiagnosticSeverity.ERROR),
    SDL_SCHEMA_103("SDL_SCHEMA_103", "SDL schema generation failed: %s", DiagnosticSeverity.ERROR),
    SDL_SCHEMA_104("SDL_SCHEMA_104", "Schema generation cancelled by user - file already exists and overwrite declined", 
            DiagnosticSeverity.INFO);

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
