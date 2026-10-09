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

package io.ballerina.graphql.generator.service.model;

import io.ballerina.graphql.generator.common.model.BalType;

import java.util.List;

/**
 * A resolver: a query, mutation or subscription field, or a field of a service class or interface.
 *
 * @param kind              how the resolver is written
 * @param name              the resolver name
 * @param parameters        the parameters
 * @param returnType        the return type
 * @param description       the GraphQL description, or null
 * @param deprecationReason the deprecation reason, or null if the field is not deprecated
 */
public record ResolverModel(ResolverKind kind, String name, List<ParameterModel> parameters, BalType returnType,
                            String description, String deprecationReason) {
}
