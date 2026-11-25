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
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.ballerina.graphql.schema.generator.GraphqlServiceFinder.formatBasePath;

/**
 * This class implements the GraphQL SDL schema generation.
 */
public class SdlSchemaGenerator {

    private static final String ERROR_INVALID_SCHEMA_STRING = "Invalid schema string found";
    private static final String ERROR_CANNOT_READ_SCHEMA_STRING = "Cannot read decoded schema string";
    private static final String ROOT_BASE_PATH = "/";
    private static final String UNIQUE_NAME_SEPARATOR = "_";

    private SdlSchemaGenerator() {}

    public static List<SdlSchema> generateSchemaDefinitions(SchemaProject project, String serviceBasePath)
            throws SchemaGenerationException {
        return generateSdlSchema(project, serviceBasePath);
    }

    private static List<SdlSchema> generateSdlSchema(SchemaProject project, String serviceBasePath)
            throws SchemaGenerationException {
        // The services keep the order they are found in, so the same service gets the added number on every run.
        Map<String, GraphqlService> servicesToGenerate = new LinkedHashMap<>();
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
        Set<String> usedFileNames = new HashSet<>();
        for (Map.Entry<String, GraphqlService> schema : servicesToGenerate.entrySet()) {
            String sdlFileName = uniqueFileName(SchemaFileName.of(schema.getValue().filePath(), schema.getKey()),
                    usedFileNames);
            Schema schemaObject = decodeSchema(schema.getValue().encodedSchema());
            outputs.add(new SdlSchema(sdlFileName, SdlSchemaStringGenerator.generate(schemaObject)));
        }
        return outputs;
    }

    // Different base paths can give the same file name, e.g. "/" in main.bal and "/main", and names that differ only
    // in case are the same file on some file systems. A number is added until the name is free, so no schema is lost.
    private static String uniqueFileName(String fileName, Set<String> usedFileNames) {
        String baseName = FilenameUtils.removeExtension(fileName);
        String extension = FilenameUtils.EXTENSION_SEPARATOR + FilenameUtils.getExtension(fileName);
        String uniqueName = fileName;
        for (int number = 1; !usedFileNames.add(uniqueName.toLowerCase(Locale.ROOT)); number++) {
            uniqueName = baseName + UNIQUE_NAME_SEPARATOR + number + extension;
        }
        return uniqueName;
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
