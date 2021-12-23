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

import graphql.language.NonNullType;
import graphql.language.TypeName;
import graphql.language.VariableDefinition;

import java.util.List;

/**
 * ExtendedVariableDefinition class to extract necessary components from a GraphQL VariableDefinition.
 */
public class ExtendedVariableDefinition {
    private boolean isNullable = false;
    private final VariableDefinition definition;

    public ExtendedVariableDefinition(VariableDefinition definition) {
        this.definition = definition;
    }

    public String getOriginalName() {
        return this.definition.getName();
    }

    public String getDataType() {
       List<?> children = this.definition.getType().getChildren();
        for (Object child:children) {
            if (child.getClass() == TypeName.class) {
                return ((TypeName) child).getName();
            }
        }
        return null;
    }

    public Object getDefaultValue() {
        return this.definition.getDefaultValue();
    }

    public boolean isNullable() {
        if (this.definition.getType().getClass() != NonNullType.class) {
            this.isNullable = true;
        }
        return this.isNullable;
    }
}
