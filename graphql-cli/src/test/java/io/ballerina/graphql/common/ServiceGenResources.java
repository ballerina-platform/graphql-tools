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

package io.ballerina.graphql.common;

import io.ballerina.graphql.cmd.config.BalGraphqlConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Paths to the service generation test resources in src/test/resources/serviceGen.
 */
public final class ServiceGenResources {
    private static final Path SERVICE_GEN_DIR = Paths.get("src/test/resources", "serviceGen").toAbsolutePath();
    private static final String CONFIGS_DIR = "configs";
    private static final String SCHEMAS_DIR = "graphqlSchemas";
    private static final String EXPECTED_SERVICES_DIR = "expectedServices";
    private static final String VALID_DIR = "valid";
    private static final String INVALID_DIR = "invalid";
    private static final String MISSING_DIR = "missing";
    private static final String SCHEMA_EXTENSION = ".graphql";
    private static final String SINGLE_OBJECT_SCHEMA = "SchemaWithSingleObjectApi";
    private static final String MISSING_CHAR_SCHEMA = "SchemaWithMissingCharApi";

    private ServiceGenResources() {
    }

    public static Path validConfig(String schemaName) {
        return serviceGenPath(CONFIGS_DIR, VALID_DIR, schemaName, BalGraphqlConfig.FILE_NAME);
    }

    public static Path invalidConfig(String schemaName) {
        return serviceGenPath(CONFIGS_DIR, INVALID_DIR, schemaName, BalGraphqlConfig.FILE_NAME);
    }

    public static Path singleObjectConfig() {
        return validConfig(SINGLE_OBJECT_SCHEMA);
    }

    public static Path missingCharConfig() {
        return invalidConfig(MISSING_CHAR_SCHEMA);
    }

    public static Path missingSchemaConfig() {
        return serviceGenPath(CONFIGS_DIR, MISSING_DIR, BalGraphqlConfig.FILE_NAME);
    }

    public static Path serviceGenConfig(String directoryName) {
        return serviceGenPath(directoryName, BalGraphqlConfig.FILE_NAME);
    }

    public static Path validSchemaFile(String schemaName) {
        return serviceGenPath(SCHEMAS_DIR, VALID_DIR, schemaName + SCHEMA_EXTENSION);
    }

    public static Path invalidSchemaFile(String schemaName) {
        return serviceGenPath(SCHEMAS_DIR, INVALID_DIR, schemaName + SCHEMA_EXTENSION);
    }

    public static Path singleObjectSchemaFile() {
        return validSchemaFile(SINGLE_OBJECT_SCHEMA);
    }

    public static Path expectedFile(String fileName) {
        return serviceGenPath(EXPECTED_SERVICES_DIR, fileName);
    }

    private static Path serviceGenPath(String... parts) {
        return SERVICE_GEN_DIR.resolve(Paths.get("", parts));
    }
}
