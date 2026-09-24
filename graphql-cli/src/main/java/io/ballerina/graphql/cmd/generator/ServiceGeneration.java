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

import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceCodeGenerator;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Generates a Ballerina service for a given GraphQL schema file.
 */
public class ServiceGeneration implements Generator {

    private final GenerationContext context;
    private final ServiceCodeGenerator serviceCodeGenerator;
    private GraphqlServiceProject project;
    private List<SrcFilePojo> sources;

    public ServiceGeneration(GenerationContext context) {
        this.context = context;
        this.serviceCodeGenerator = new ServiceCodeGenerator(context.isUseRecordsForObjects());
    }

    @Override
    public void validate() throws GenerationException {
        String inputPath = context.getInputPath();
        File graphqlFile = new File(inputPath);
        if (!graphqlFile.exists()) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_MISSING_SCHEMA_FILE, inputPath)));
        }
        if (!graphqlFile.canRead()) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_CAN_NOT_READ_SCHEMA_FILE, inputPath)));
        }
        this.project = new GraphqlServiceProject(ROOT_PROJECT_NAME, inputPath,
                context.getTargetOutputPath().toString());
        try {
            Utils.validateGraphqlProject(this.project);
        } catch (IOException | ValidationException e) {
            throw new GenerationException(e);
        }
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
