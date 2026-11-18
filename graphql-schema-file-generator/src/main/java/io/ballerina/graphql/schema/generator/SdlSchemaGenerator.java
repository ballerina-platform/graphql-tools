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

import io.ballerina.graphql.schema.SchemaProject;
import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.graphql.schema.generator.GraphqlServiceFinder.GraphqlService;
import io.ballerina.stdlib.graphql.commons.types.Schema;
import io.ballerina.stdlib.graphql.commons.utils.SdlSchemaStringGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.schema.Constants.PERIOD;
import static io.ballerina.graphql.schema.generator.GraphqlServiceFinder.formatBasePath;
import static io.ballerina.graphql.schema.utils.Utils.getDecodedSchema;
import static io.ballerina.graphql.schema.utils.Utils.getSdlFileName;

/**
 * This class implements the GraphQL SDL schema generation.
 */
public class SdlSchemaGenerator {

    private SdlSchemaGenerator() {}

    public static List<SdlSchema> generateSchemaDefinitions(SchemaProject project, String serviceBasePath)
            throws SchemaGenerationException {
        return generateSdlSchema(project, serviceBasePath);
    }

    private static List<SdlSchema> generateSdlSchema(SchemaProject project, String serviceBasePath)
            throws SchemaGenerationException {
        Map<String, String> servicesToGenerate = new HashMap<>();
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
                servicesToGenerate.put(serviceName, service.encodedSchema());
                continue;
            }
            availableServices.add(service.basePath());
            if (formatBasePath(serviceBasePath).equals(service.basePath())) {
                servicesToGenerate.put(serviceName, service.encodedSchema());
            }
        }
        if (serviceBasePath != null && servicesToGenerate.isEmpty()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_101, null, serviceBasePath,
                    availableServices.toString());
        }
        List<SdlSchema> outputs = new ArrayList<>();
        for (Map.Entry<String, String> schema : servicesToGenerate.entrySet()) {
            String sdlFileName = getSdlFileName(project.syntaxTree().filePath(), schema.getKey());
            Schema schemaObject = getDecodedSchema(schema.getValue());
            outputs.add(new SdlSchema(sdlFileName, SdlSchemaStringGenerator.generate(schemaObject)));
        }
        return outputs;
    }

    private static String getUpdatedServiceName(String serviceName, int duplicateCount) {
        if (serviceName.isBlank()) {
            return PERIOD + duplicateCount;
        } else {
            return serviceName + PERIOD + duplicateCount;
        }
    }
}
