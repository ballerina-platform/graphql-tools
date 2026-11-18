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

package io.ballerina.graphql.schema.utils;

import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.stdlib.graphql.commons.types.Schema;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import static io.ballerina.graphql.schema.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.schema.Constants.MESSAGE_CANNOT_READ_SCHEMA_STRING;
import static io.ballerina.graphql.schema.Constants.MESSAGE_INVALID_SCHEMA_STRING;
import static io.ballerina.graphql.schema.Constants.PERIOD;
import static io.ballerina.graphql.schema.Constants.SCHEMA_PREFIX;
import static io.ballerina.graphql.schema.Constants.SLASH;
import static io.ballerina.graphql.schema.Constants.UNDERSCORE;

/**
 * Utility class for Ballerina GraphQL SDL schema generation.
 */
public final class Utils {

    private Utils() {}

    public static String getSdlFileName(String servicePath, String serviceName) {
        String sdlFileName;
        if (serviceName.isBlank()) {
            sdlFileName = FilenameUtils.removeExtension(servicePath);
        } else if (serviceName.startsWith(PERIOD)) {
            sdlFileName = FilenameUtils.removeExtension(servicePath) + serviceName;
        } else {
            if (serviceName.startsWith(SLASH)) {
                serviceName = serviceName.substring(1);
            }
            sdlFileName = serviceName.replaceAll(SLASH, "_");
        }
        sdlFileName =  getNormalizedFileName(sdlFileName);
        return String.join("", SCHEMA_PREFIX, UNDERSCORE, sdlFileName, GRAPHQL_EXTENSION);
    }

    public static String getNormalizedFileName(String sdlFileName) {
        String[] splitNames = sdlFileName.split("[^a-zA-Z0-9]");
        if (splitNames.length > 0) {
            return Arrays.stream(splitNames)
                    .filter(namePart -> !namePart.isBlank())
                    .collect(Collectors.joining(UNDERSCORE));
        }
        return sdlFileName;
    }

    public static Schema getDecodedSchema(String schemaString) throws SchemaGenerationException {
        if (schemaString == null || schemaString.isBlank() || schemaString.isEmpty()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null,
                    MESSAGE_INVALID_SCHEMA_STRING);
        }
        byte[] decodedString = Base64.getDecoder().decode(schemaString.getBytes(StandardCharsets.UTF_8));
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(decodedString);
            ObjectInputStream inputStream = new ObjectInputStream(byteStream);
            return (Schema) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null,
                    MESSAGE_CANNOT_READ_SCHEMA_STRING);
        }
    }

    public static void writeFile(Path filePath, String content) throws SchemaGenerationException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, e.getMessage());
        }
    }

    public static void createOutputDirectory(Path outputPath) {
        File outputDir = new File(outputPath.toString());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }
}
