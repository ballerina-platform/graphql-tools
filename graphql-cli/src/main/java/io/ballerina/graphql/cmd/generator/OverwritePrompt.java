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

import java.util.Optional;

/**
 * Asks the user whether existing files can be overwritten.
 */
@FunctionalInterface
public interface OverwritePrompt {

    /**
     * Shows the question and returns the user's answer, or an empty value if the run is not interactive.
     *
     * @param question the question to show
     * @return the answer, or empty if there is no terminal to ask on
     */
    Optional<String> ask(String question);
}
