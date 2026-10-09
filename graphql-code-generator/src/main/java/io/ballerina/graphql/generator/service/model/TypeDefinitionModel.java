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

import java.util.List;

/**
 * A named type definition: a service class, record, interface or union.
 *
 * @param name         the type name
 * @param kind         how the type is generated
 * @param fields       the fields; empty for a union
 * @param relatedTypes the interfaces it implements, or a union's member types
 * @param description  the GraphQL description, or null
 */
public record TypeDefinitionModel(String name, TypeKind kind, List<ResolverModel> fields, List<String> relatedTypes,
                                  String description) {
}
