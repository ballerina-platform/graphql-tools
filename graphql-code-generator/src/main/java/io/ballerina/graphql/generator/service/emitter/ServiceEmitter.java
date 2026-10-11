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

package io.ballerina.graphql.generator.service.emitter;

import io.ballerina.graphql.generator.common.emitter.TemplateLoader;
import io.ballerina.graphql.generator.service.model.ResolverModel;
import io.ballerina.graphql.generator.service.model.ServiceModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Writes the {@code service.bal} file: the GraphQL service with the Query, Mutation and Subscription resolvers.
 */
public final class ServiceEmitter {

    private static final String TEMPLATE = "templates/service/service_file.bal";
    private static final String RESOLVERS = "resolvers";
    private static final String NEW_LINE = "\n";

    private ServiceEmitter() {
    }

    /**
     * Writes the service file.
     *
     * @param model the service model
     * @return the file content
     */
    public static String emit(ServiceModel model) {
        List<ResolverModel> resolvers = Stream.of(model.queries(), model.mutations(), model.subscriptions())
                .flatMap(List::stream)
                .toList();
        return TemplateLoader.fill(TEMPLATE, Map.of(RESOLVERS, ResolverEmitter.emit(resolvers))) + NEW_LINE;
    }
}
