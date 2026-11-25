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

package io.ballerina.graphql.schema.generator;

import io.ballerina.graphql.schema.SchemaFileName;
import io.ballerina.graphql.schema.SchemaProject;
import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.graphql.schema.generator.GraphqlServiceFinder.GraphqlService;
import io.ballerina.stdlib.graphql.commons.types.Schema;
import io.ballerina.stdlib.graphql.commons.utils.SdlSchemaStringGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.schema.generator.GraphqlServiceFinder.formatBasePath;

/**
 * This class implements the GraphQL SDL schema generation.
 */
public class SdlSchemaGenerator {

    private static final String ERROR_INVALID_SCHEMA_STRING = "Invalid schema string found";
    private static final String ERROR_CANNOT_READ_SCHEMA_STRING = "Cannot read decoded schema string";
    private static final String ROOT_BASE_PATH = "/";

    private SdlSchemaGenerator() {}

    public static List<SdlSchema> generateSchemaDefinitions(SchemaProject project, String serviceBasePath)
            throws SchemaGenerationException {
        return generateSdlSchema(project, serviceBasePath);
    }

    private static List<SdlSchema> generateSdlSchema(SchemaProject project, String serviceBasePath)
            throws SchemaGenerationException {
        Map<String, GraphqlService> servicesToGenerate = new HashMap<>();
        List<String> availableServices = new ArrayList<>();
        int duplicateCount = 0;
        for (GraphqlService service : GraphqlServiceFinder.find(project)) {
            // A service object has no base path, so it is skipped when a service base path is given.
            if (service.serviceObject() && serviceBasePath != null) {
                continue;
            }
            String serviceName = service.basePath();
            if (servicesToGenerate.containsKey(serviceName)) {
                duplicateCount += 1;
                serviceName = getUpdatedServiceName(serviceName, duplicateCount);
            }
            if (serviceBasePath == null) {
                servicesToGenerate.put(serviceName, service);
                continue;
            }
            // A root service has an empty base path, so it is listed as "/".
            availableServices.add(service.basePath().isEmpty() ? ROOT_BASE_PATH : service.basePath());
            if (formatBasePath(serviceBasePath).equals(service.basePath())) {
                servicesToGenerate.put(serviceName, service);
            }
        }
        if (serviceBasePath != null && servicesToGenerate.isEmpty()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_101, null, serviceBasePath,
                    availableServices.toString());
        }
        List<SdlSchema> outputs = new ArrayList<>();
        for (Map.Entry<String, GraphqlService> schema : servicesToGenerate.entrySet()) {
            String sdlFileName = SchemaFileName.of(schema.getValue().filePath(), schema.getKey());
            Schema schemaObject = decodeSchema(schema.getValue().encodedSchema());
            outputs.add(new SdlSchema(sdlFileName, SdlSchemaStringGenerator.generate(schemaObject)));
        }
        return outputs;
    }

    private static String getUpdatedServiceName(String serviceName, int duplicateCount) {
        if (serviceName.isBlank()) {
            return SchemaFileName.DUPLICATE_SEPARATOR + duplicateCount;
        } else {
            return serviceName + SchemaFileName.DUPLICATE_SEPARATOR + duplicateCount;
        }
    }

    private static Schema decodeSchema(String encodedSchema) throws SchemaGenerationException {
        if (encodedSchema == null || encodedSchema.isBlank()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null,
                    ERROR_INVALID_SCHEMA_STRING);
        }
        byte[] decodedSchema = Base64.getDecoder().decode(encodedSchema.getBytes(StandardCharsets.UTF_8));
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(decodedSchema))) {
            return (Schema) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null,
                    ERROR_CANNOT_READ_SCHEMA_STRING);
        }
    }
}
