/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.service.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.emitter.ServiceEmitter;
import io.ballerina.graphql.generator.service.emitter.TypesEmitter;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.mapper.ServiceModelMapper;
import io.ballerina.graphql.generator.service.model.ServiceModel;
import io.ballerina.graphql.generator.utils.NullLocation;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates the {@code types.bal} and {@code service.bal} files of a Ballerina GraphQL service from a schema.
 */
public class ServiceCodeGenerator extends CodeGenerator {

    private static final String TYPES_FILE_NAME = "types.bal";
    private static final String SERVICE_FILE_NAME = "service.bal";
    private static final boolean SERVICE_CLASSES_FOR_OBJECTS = false;

    private final boolean useRecordsForObjects;

    /**
     * Creates a generator that generates object types as service classes.
     */
    public ServiceCodeGenerator() {
        this(SERVICE_CLASSES_FOR_OBJECTS);
    }

    /**
     * Creates a generator.
     *
     * @param useRecordsForObjects whether object types are generated as records where possible
     */
    public ServiceCodeGenerator(boolean useRecordsForObjects) {
        this.useRecordsForObjects = useRecordsForObjects;
    }

    @Override
    public void generate(GraphqlProject project) throws ServiceGenerationException {
        try {
            writeGeneratedSources(generateBalSources(project), Path.of(project.getOutputPath()));
        } catch (IOException e) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100,
                    NullLocation.getInstance(), e.getMessage());
        }
    }

    /**
     * Generates the service sources for a project. The service object type is named after the schema file.
     *
     * @param project the project with a validated GraphQL schema
     * @return the {@code types.bal} source followed by the {@code service.bal} source
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent, or the
     *                                    generated code cannot be formatted
     */
    public List<SrcFilePojo> generateBalSources(GraphqlProject project) throws ServiceGenerationException {
        ServiceModel model = ServiceModelMapper.map(project.getGraphQLSchema(), project.getFileName(),
                this.useRecordsForObjects);
        return List.of(typesSource(project.getName(), model),
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, project.getName(), SERVICE_FILE_NAME,
                        format(ServiceEmitter.emit(model), ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_101)));
    }

    /**
     * Generates the types file only.
     *
     * @param projectName   the project name
     * @param fileName      the schema file name, used as the service object type name
     * @param graphQLSchema the GraphQL schema
     * @return the {@code types.bal} source
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent, or the
     *                                    generated code cannot be formatted
     *
     * @since 0.14.0
     */
    public SrcFilePojo generateServiceTypes(String projectName, String fileName, GraphQLSchema graphQLSchema)
            throws ServiceGenerationException {
        return typesSource(projectName, ServiceModelMapper.map(graphQLSchema, fileName, this.useRecordsForObjects));
    }

    private static SrcFilePojo typesSource(String projectName, ServiceModel model)
            throws ServiceGenerationException {
        return new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName, TYPES_FILE_NAME,
                format(TypesEmitter.emit(model), ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_102));
    }

    private static String format(String source, ServiceDiagnosticMessages error) throws ServiceGenerationException {
        try {
            return Formatter.format(source);
        } catch (FormatterException e) {
            throw new ServiceGenerationException(error, NullLocation.getInstance(), e.getMessage());
        }
    }
}
