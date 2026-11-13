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

package io.ballerina.graphql.cmd.generator;

/**
 * What a run does with the output files that already exist. The engine decides it once for the run, so the plan,
 * the prompt and the dry run all follow the same decision.
 */
enum ExistingFileAction {

    /** The files are overwritten, and the generator cannot merge into them. */
    OVERWRITE,
    /** The files are overwritten, but the generator could merge into them with --merge. */
    OVERWRITE_OR_MERGE,
    /** The generated code is merged into the files with --merge. */
    MERGE;

    static ExistingFileAction of(GenerationContext context, Generator generator) {
        if (context.isMerge()) {
            return MERGE;
        }
        return generator.canMerge() ? OVERWRITE_OR_MERGE : OVERWRITE;
    }
}
