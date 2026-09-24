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

package io.ballerina.graphql.cmd.generator;

import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaFileGenerationException;
import io.ballerina.graphql.schema.generator.SdlSchema;
import io.ballerina.graphql.schema.generator.SdlSchemaGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.graphql.schema.Constants.MESSAGE_CANNOT_READ_BAL_FILE;
import static io.ballerina.graphql.schema.Constants.MESSAGE_MISSING_BAL_FILE;

/**
 * Generates the SDL schema for a given Ballerina GraphQL service file.
 */
public class SchemaGeneration implements Generator {

    private final GenerationContext context;
    private Path balFilePath;
    private List<SdlSchema> schemas;

    public SchemaGeneration(GenerationContext context) {
        this.context = context;
    }

    @Override
    public void validate() throws GenerationException {
        File balFile = new File(context.getInputPath());
        if (!balFile.exists()) {
            throw new GenerationException(new SchemaFileGenerationException(
                    DiagnosticMessages.SDL_SCHEMA_103, null, MESSAGE_MISSING_BAL_FILE));
        }
        if (!balFile.canRead()) {
            throw new GenerationException(new SchemaFileGenerationException(
                    DiagnosticMessages.SDL_SCHEMA_103, null, MESSAGE_CANNOT_READ_BAL_FILE));
        }
        try {
            this.balFilePath = Paths.get(balFile.getCanonicalPath());
        } catch (IOException e) {
            throw new GenerationException(new SchemaFileGenerationException(
                    DiagnosticMessages.SDL_SCHEMA_103, null, e.toString()));
        }
    }

    @Override
    public void generate() throws GenerationException {
        try {
            this.schemas = SdlSchemaGenerator.generateSchemaDefinitions(this.balFilePath,
                    context.getServiceBasePath());
        } catch (SchemaFileGenerationException e) {
            throw new GenerationException(e);
        }
    }

    @Override
    public void write() throws GenerationException {
        try {
            SdlSchemaGenerator.writeSchemaFiles(this.schemas, context.getTargetOutputPath(), context.getOutStream());
        } catch (SchemaFileGenerationException e) {
            throw new GenerationException(e);
        }
    }
}
