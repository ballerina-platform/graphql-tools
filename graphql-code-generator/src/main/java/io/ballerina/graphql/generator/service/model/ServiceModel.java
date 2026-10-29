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

package io.ballerina.graphql.generator.service.model;

import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.InputTypeModel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Everything needed to generate a Ballerina GraphQL service for one schema.
 *
 * @param serviceName     the name of the service object type the service implements
 * @param queries         the Query resolvers
 * @param mutations       the Mutation resolvers
 * @param subscriptions   the Subscription resolvers
 * @param typeDefinitions the service classes, records, interfaces and unions
 * @param inputTypes      the input types
 * @param enums           the enums
 */
public record ServiceModel(String serviceName, List<ResolverModel> queries, List<ResolverModel> mutations,
                           List<ResolverModel> subscriptions, List<TypeDefinitionModel> typeDefinitions,
                           List<InputTypeModel> inputTypes, List<EnumModel> enums) {

    /**
     * Returns the Query, Mutation and Subscription resolvers, in that order.
     *
     * @return the resolvers of the service
     */
    public List<ResolverModel> rootResolvers() {
        return Stream.of(queries, mutations, subscriptions).flatMap(List::stream).toList();
    }

    /**
     * Returns the name of every DataLoader the resolvers use, once each, in the order they are first used.
     *
     * @return the DataLoader names
     */
    public List<String> dataLoaderNames() {
        return Stream.concat(rootResolvers().stream(),
                        typeDefinitions.stream().flatMap(definition -> definition.fields().stream()))
                .map(ResolverModel::dataLoader)
                .filter(Objects::nonNull)
                .map(DataLoaderModel::name)
                .distinct()
                .toList();
    }
}
