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
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.graphql.schema.SchemaProject;
import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.graphql.schema.generator.SdlSchema;
import io.ballerina.graphql.schema.generator.SdlSchemaGenerator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the SDL schema for a given Ballerina GraphQL service file.
 */
public class SchemaGeneration implements Generator {

    private static final String SUCCESS_MESSAGE = "SDL Schema(s) generated successfully and copied to :";
    private static final String NO_SERVICES_MESSAGE = "Given Ballerina file does not contain any GraphQL services";
    private static final String WRITTEN_FILE_FORMAT = "-- %s";

    private final GenerationContext context;
    private SchemaProject project;
    private List<SdlSchema> schemas;

    public SchemaGeneration(GenerationContext context) {
        this.context = context;
    }

    @Override
    public void validate() throws GenerationException {
        try {
            this.project = SchemaProject.load(Paths.get(context.getInputPath()));
        } catch (SchemaGenerationException e) {
            throw new GenerationException(e);
        }
    }

    @Override
    public void generate() throws GenerationException {
        try {
            this.schemas = SdlSchemaGenerator.generateSchemaDefinitions(this.project,
                    context.getServiceBasePath());
        } catch (SchemaGenerationException e) {
            throw new GenerationException(e);
        }
    }

    @Override
    public List<Path> outputFiles() {
        return this.schemas.stream().map(schema -> context.getTargetOutputPath().resolve(schema.name())).toList();
    }

    @Override
    public void write() throws GenerationException {
        PrintStream outStream = context.getOutStream();
        if (this.schemas.isEmpty()) {
            outStream.println(NO_SERVICES_MESSAGE);
            return;
        }
        Map<Path, String> files = new LinkedHashMap<>();
        for (SdlSchema schema : this.schemas) {
            files.put(context.getTargetOutputPath().resolve(schema.name()), schema.schema());
        }
        try {
            CodeGeneratorUtils.writeFiles(files, context.getTargetOutputPath());
        } catch (IOException e) {
            throw new GenerationException(new SchemaGenerationException(
                    SchemaDiagnosticMessages.SDL_SCHEMA_103, null, e.getMessage()));
        }
        outStream.println(SUCCESS_MESSAGE);
        this.schemas.forEach(schema -> outStream.println(String.format(WRITTEN_FILE_FORMAT, schema.name())));
    }
}
