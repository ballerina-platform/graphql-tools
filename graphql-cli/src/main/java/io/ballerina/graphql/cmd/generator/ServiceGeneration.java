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

import graphql.schema.idl.errors.SchemaProblem;
import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.config.SchemaConfig;
import io.ballerina.graphql.cmd.config.SchemaSource;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.SDLValidationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.exception.IntospectionException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceCodeGenerator;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Generates a Ballerina service for a given GraphQL schema file.
 */
public class ServiceGeneration implements Generator {

    private final GenerationContext context;
    private final BalGraphqlConfig balGraphqlConfig;
    private final ServiceCodeGenerator serviceCodeGenerator;
    private GraphqlServiceProject project;
    private List<SrcFilePojo> sources;

    public ServiceGeneration(GenerationContext context, BalGraphqlConfig balGraphqlConfig) {
        this.context = context;
        this.balGraphqlConfig = balGraphqlConfig;
        this.serviceCodeGenerator = new ServiceCodeGenerator(context.isUseRecordsForObjects());
    }

    @Override
    public void validate() throws GenerationException {
        if (this.balGraphqlConfig != null
                && this.balGraphqlConfig.schema().source() != SchemaSource.FILE) {
            validateFromRemoteSchema(this.balGraphqlConfig.schema());
            return;
        }
        validateFromFile();
    }

    private void validateFromFile() throws GenerationException {
        String schemaPath = resolveSchemaPath();
        File graphqlFile = new File(schemaPath);
        if (!graphqlFile.exists()) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_MISSING_SCHEMA_FILE, schemaPath)));
        }
        if (!graphqlFile.canRead()) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_CAN_NOT_READ_SCHEMA_FILE, schemaPath)));
        }
        this.project = new GraphqlServiceProject(ROOT_PROJECT_NAME, schemaPath,
                context.getTargetOutputPath().toString());
        try {
            Utils.validateGraphqlProject(this.project);
        } catch (IOException | ValidationException e) {
            throw new GenerationException(e);
        }
    }

    private void validateFromRemoteSchema(SchemaConfig schemaConfig) throws GenerationException {
        String schemaLocation = schemaConfig.source() == SchemaSource.URL
                ? schemaConfig.url() : schemaConfig.endpoint();
        this.project = new GraphqlServiceProject(ROOT_PROJECT_NAME, schemaLocation,
                context.getTargetOutputPath().toString());
        try {
            this.project.setGraphQLSchema(Utils.resolveGraphQLSchema(schemaConfig));
        } catch (IntospectionException e) {
            throw new GenerationException(new ValidationException(e.getMessage(), this.project.getName()));
        } catch (SchemaProblem e) {
            throw new GenerationException(new SDLValidationException("GraphQL SDL validation failed.",
                    e.getErrors(), this.project.getName()));
        }
    }

    private String resolveSchemaPath() {
        if (this.balGraphqlConfig == null) {
            return context.getInputPath();
        }
        Path configDirectory = Paths.get(context.getInputPath()).toAbsolutePath().getParent();
        return configDirectory.resolve(this.balGraphqlConfig.schema().path()).normalize().toString();
    }

    @Override
    public void generate() throws GenerationException {
        try {
            this.sources = this.serviceCodeGenerator.generateBalSources(this.project);
        } catch (ServiceGenerationException e) {
            throw new GenerationException(e);
        }
    }

    @Override
    public void write() throws GenerationException {
        try {
            this.serviceCodeGenerator.writeGeneratedSources(this.sources, Path.of(this.project.getOutputPath()));
        } catch (IOException e) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null, e.getMessage()));
        }
    }
}
