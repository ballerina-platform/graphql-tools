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

package io.ballerina.graphql.generator.service.generator;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.service.ServiceBaseName;
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
import java.util.Map;

/**
 * Generates the {@code types.bal} and {@code service.bal} files of a Ballerina GraphQL service from a schema.
 */
public class ServiceCodeGenerator extends CodeGenerator {

    public static final String TYPES_FILE_NAME = "types.bal";
    public static final String SERVICE_FILE_NAME = "service.bal";
    private static final boolean SERVICE_CLASSES_FOR_OBJECTS = false;

    private final boolean useRecordsForObjects;
    private final Map<String, String> idTypes;
    private final Map<String, String> dataLoaders;

    /**
     * Creates a generator that generates object types as service classes.
     */
    public ServiceCodeGenerator() {
        this(SERVICE_CLASSES_FOR_OBJECTS);
    }

    /**
     * Creates a generator that uses {@code string} for every GraphQL {@code ID}.
     *
     * @param useRecordsForObjects whether object types are generated as records where possible
     */
    public ServiceCodeGenerator(boolean useRecordsForObjects) {
        this(useRecordsForObjects, Map.of());
    }

    /**
     * Creates a generator with no DataLoaders.
     *
     * @param useRecordsForObjects whether object types are generated as records where possible
     * @param idTypes              the Ballerina type of each GraphQL {@code ID} field or argument, keyed by its path,
     *                             e.g. {@code Profile.id} or {@code Query.profile.id}
     */
    public ServiceCodeGenerator(boolean useRecordsForObjects, Map<String, String> idTypes) {
        this(useRecordsForObjects, idTypes, Map.of());
    }

    /**
     * Creates a generator.
     *
     * @param useRecordsForObjects whether object types are generated as records where possible
     * @param idTypes              the Ballerina type of each GraphQL {@code ID} field or argument, keyed by its path,
     *                             e.g. {@code Profile.id} or {@code Query.profile.id}
     * @param dataLoaders          the DataLoader name of each field that is wired to one, keyed by its path,
     *                             e.g. {@code Query.profile}
     */
    public ServiceCodeGenerator(boolean useRecordsForObjects, Map<String, String> idTypes,
                                Map<String, String> dataLoaders) {
        this.useRecordsForObjects = useRecordsForObjects;
        this.idTypes = idTypes;
        this.dataLoaders = dataLoaders;
    }

    /**
     * Generates the service sources for a project and writes them to its output path. The service object type is
     * named after the schema file.
     *
     * @param project the project with a validated GraphQL schema
     * @throws ServiceGenerationException if the sources cannot be generated or written
     * @deprecated Use only for file schemas. For a URL or introspection schema the name would be made from the URL,
     *             which can include an API key. Use
     *             {@link #generateBalSources(GraphqlProject, ServiceBaseName, Map)} instead.
     */
    // TODO: Remove once nothing uses it.
    @Deprecated
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
     * @deprecated Use only for file schemas. For a URL or introspection schema the name would be made from the URL,
     *             which can include an API key. Use
     *             {@link #generateBalSources(GraphqlProject, ServiceBaseName, Map)} instead.
     */
    // TODO: Remove once nothing uses it.
    @Deprecated
    public List<SrcFilePojo> generateBalSources(GraphqlProject project) throws ServiceGenerationException {
        return generateBalSources(project,
                new ServiceBaseName(project.getFileName(), ServiceBaseName.Source.SCHEMA_FILE), Map.of());
    }

    /**
     * Generates the service sources for a project.
     *
     * @param project           the project with a validated GraphQL schema
     * @param baseName          the name the service object type is named after and where it came from, e.g.
     *                          {@code book-api} gives {@code BookApiService}
     * @param moduleDefinitions the top-level names the target module already defines outside the generated files,
     *                          each mapped to the file that defines it
     * @return the {@code types.bal} source followed by the {@code service.bal} source
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent, no valid
     *                                    service name can be made from the base name, the service name is already
     *                                    defined in the module, or the generated code cannot be formatted
     */
    public List<SrcFilePojo> generateBalSources(GraphqlProject project, ServiceBaseName baseName,
                                                Map<String, String> moduleDefinitions)
            throws ServiceGenerationException {
        ServiceModel model = new ServiceModelMapper(project.getGraphQLSchema(), this.useRecordsForObjects,
                this.idTypes, this.dataLoaders).map(baseName, moduleDefinitions);
        return List.of(typesSource(project.getName(), model),
                new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, project.getName(), SERVICE_FILE_NAME,
                        format(ServiceEmitter.emit(model), ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_101)));
    }

    /**
     * Generates the types file only.
     *
     * @param projectName   the project name
     * @param baseName      the name the service object type is named after, usually the schema file name, e.g.
     *                      {@code book-api} gives {@code BookApiService}
     * @param graphQLSchema the GraphQL schema
     * @return the {@code types.bal} source
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent, no valid
     *                                    service name can be made from the base name, or the generated code
     *                                    cannot be formatted
     *
     * @since 0.14.0
     */
    public SrcFilePojo generateServiceTypes(String projectName, String baseName, GraphQLSchema graphQLSchema)
            throws ServiceGenerationException {
        return typesSource(projectName,
                new ServiceModelMapper(graphQLSchema, this.useRecordsForObjects, this.idTypes, this.dataLoaders)
                        .map(new ServiceBaseName(baseName, ServiceBaseName.Source.SCHEMA_FILE), Map.of()));
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
