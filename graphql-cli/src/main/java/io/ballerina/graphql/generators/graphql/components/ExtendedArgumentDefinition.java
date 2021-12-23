/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.generators.graphql.components;

import graphql.language.Argument;
import graphql.language.Value;

/**
 * ExtendedArgumentDefinition class to extract necessary components from an GraphQL Argument.
 */
public class ExtendedArgumentDefinition {
    private final Argument argument;

    public ExtendedArgumentDefinition(Argument argument) {
        this.argument = argument;
    }

    public String getName() {
        return this.argument.getName();
    }

    public Value<?> getArgumentValue() {
        return this.argument.getValue();
    }
}
