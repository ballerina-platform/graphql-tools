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

package io.ballerina.graphql.generator.service.merge;

import java.util.List;

/**
 * The result of merging a newly generated service into an existing one.
 *
 * @param source       the merged source
 * @param resolvers    what happened to each resolver of the generated service, in order
 * @param removedTypes the names of the existing types that were removed because the schema no longer has them
 */
public record MergeResult(String source, List<MergedResolver> resolvers, List<String> removedTypes) {

    public MergeResult(String source, List<MergedResolver> resolvers) {
        this(source, resolvers, List.of());
    }

    /**
     * What happened to one resolver.
     *
     * @param key     the resolver's key, e.g. {@code get books} or {@code remote addBook}
     * @param outcome whether the existing resolver was kept, replaced or added
     */
    public record MergedResolver(String key, Outcome outcome) {
    }

    /**
     * What a merge did with a resolver.
     */
    public enum Outcome {
        /** The existing resolver matched the schema and was kept as written. */
        KEPT,
        /** The existing resolver's signature no longer matched the schema, so it was regenerated. */
        REPLACED,
        /** The resolver is new in the schema. */
        ADDED
    }
}
