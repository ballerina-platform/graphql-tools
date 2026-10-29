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

import java.util.List;

/**
 * The DataLoader a resolver is wired to.
 *
 * @param name           the loader name, used to register the loader and as the name of its batch function
 * @param prefetchMethod the name of the prefetch method, e.g. {@code preProfile} for the field {@code profile}
 * @param keys           the parameters that make up the loader key; empty if the key cannot be known from the field
 * @param loadsValue     whether the resolver returns the loaded value, which needs a key and a field type that a
 *                       DataLoader can hold
 */
public record DataLoaderModel(String name, String prefetchMethod, List<String> keys, boolean loadsValue) {
}
