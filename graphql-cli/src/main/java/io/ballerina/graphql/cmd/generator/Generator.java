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

import io.ballerina.graphql.exception.GenerationException;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a single GraphQL generation operation.
 */
public interface Generator {

    void validate() throws GenerationException;

    void generate() throws GenerationException;

    /**
     * Returns the files {@link #write()} writes. It is called after {@link #generate()}.
     */
    List<Path> outputFiles();

    void write() throws GenerationException;

    /**
     * Returns whether the generator can merge its output into existing files with --merge. Only a generated service
     * has code the developer writes into, so only service generation can.
     */
    default boolean canMerge() {
        return false;
    }
}
