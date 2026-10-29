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

package io.ballerina.graphql.generator.service.emitter;

import io.ballerina.graphql.generator.common.emitter.TemplateLoader;
import io.ballerina.graphql.generator.service.model.ServiceModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes the {@code service.bal} file: the GraphQL service that implements the service object type, with the
 * Query, Mutation and Subscription resolvers. When resolvers use DataLoaders, it also writes the context init
 * function that registers them and a batch function for each one.
 */
public final class ServiceEmitter {

    private static final String TEMPLATE = "templates/service/service_file.bal";
    private static final String CONTEXT_INIT_TEMPLATE = "templates/service/context_init.bal";
    private static final String BATCH_FUNCTION_TEMPLATE = "templates/service/batch_function.bal";
    private static final String IMPORTS = "imports";
    private static final String CONTEXT_INIT = "contextInit";
    private static final String SERVICE_CONFIG = "serviceConfig";
    private static final String NAME = "name";
    private static final String RESOLVERS = "resolvers";
    private static final String BATCH_FUNCTIONS = "batchFunctions";
    private static final String REGISTRATIONS = "registrations";
    private static final String SERVICE_CONFIG_ANNOTATION = "@graphql:ServiceConfig {contextInit: initContext}";
    private static final String REGISTRATION_FORMAT =
            "context.registerDataLoader(\"%s\", new dataloader:DefaultDataLoader(%s));";
    private static final List<String> DATALOADER_MODULES = List.of("graphql.dataloader", "http");
    private static final String NEW_LINE = System.lineSeparator();
    private static final String BLANK_LINE = NEW_LINE + NEW_LINE;

    private ServiceEmitter() {
    }

    /**
     * Writes the service file.
     *
     * @param model the service model
     * @return the file content
     */
    public static String emit(ServiceModel model) {
        List<String> dataLoaderNames = model.dataLoaderNames();
        boolean usesDataLoaders = !dataLoaderNames.isEmpty();
        return TemplateLoader.fill(TEMPLATE, Map.of(
                IMPORTS, ImportEmitter.emit(ImportEmitter.types(model.rootResolvers()),
                        usesDataLoaders ? DATALOADER_MODULES : List.of()),
                CONTEXT_INIT, usesDataLoaders ? BLANK_LINE + contextInit(dataLoaderNames) : "",
                SERVICE_CONFIG, usesDataLoaders ? SERVICE_CONFIG_ANNOTATION + NEW_LINE : "",
                NAME, model.serviceName(),
                RESOLVERS, ResolverEmitter.emit(model.rootResolvers()),
                BATCH_FUNCTIONS, dataLoaderNames.stream()
                        .map(name -> BLANK_LINE + TemplateLoader.fill(BATCH_FUNCTION_TEMPLATE, Map.of(NAME, name)))
                        .collect(Collectors.joining()))) + NEW_LINE;
    }

    private static String contextInit(List<String> dataLoaderNames) {
        return TemplateLoader.fill(CONTEXT_INIT_TEMPLATE, Map.of(REGISTRATIONS, dataLoaderNames.stream()
                .map(name -> String.format(REGISTRATION_FORMAT, name, name))
                .collect(Collectors.joining(NEW_LINE))));
    }
}
