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

import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.SelectionSet;

import java.util.ArrayList;
import java.util.List;

/**
 * ExtendedFieldDefinition class to extract necessary components from an GraphQL FieldDefinition.
 */
public class ExtendedFieldDefinition {
    private final Field definition;

    public ExtendedFieldDefinition(Field definition) {
        this.definition = definition;
    }

    public String getAlias() {
        return this.definition.getAlias();
    }

    public String getName() {
        return this.definition.getName();
    }

//    public List<ExtendedArgumentDefinition> getArguments() {
//        List<ExtendedArgumentDefinition> extendedArgumentDefinitions = new ArrayList<>();
//        for (Argument argument:this.definition.getArguments()) {
//            ExtendedArgumentDefinition argumentDefinition = new ExtendedArgumentDefinition(argument);
//            extendedArgumentDefinitions.add(argumentDefinition);
//        }
//        return extendedArgumentDefinitions;
//    }

    public SelectionSet getSelectionSet() {
        return this.definition.getSelectionSet();
    }

    public List<ExtendedFieldDefinition> getExtendedFieldDefinitions() {
        List<ExtendedFieldDefinition> fieldDefinitionList = new ArrayList<>();
        if (definition.getSelectionSet() != null) {
            for (Selection<?> selection: this.definition.getSelectionSet().getSelections()) {
                Field field = (Field) selection;
                ExtendedFieldDefinition extendedFieldDefinition = new ExtendedFieldDefinition(field);
                fieldDefinitionList.add(extendedFieldDefinition);
            }
        }
        return fieldDefinitionList;
    }
}
