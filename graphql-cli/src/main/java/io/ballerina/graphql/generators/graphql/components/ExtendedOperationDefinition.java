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

import graphql.language.AstPrinter;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.VariableDefinition;
import io.ballerina.graphql.generators.graphql.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExtendedOperationDefinition class to extract necessary components from an GraphQL OperationDefinition.
 */
public class ExtendedOperationDefinition {
    private final OperationDefinition definition;
    private final Document queryFileAst;

    public ExtendedOperationDefinition(Document queryFileAst, OperationDefinition definition) {
        this.definition = definition;
        this.queryFileAst = queryFileAst;
    }

    public String getOperationType() {
        return this.definition.getOperation().name();
    }

    public String getName() {
        return this.definition.getName();
    }

    public List<ExtendedVariableDefinition> getVariableDefinitions() {
        List<ExtendedVariableDefinition> variableDefinitions = new ArrayList<>();
        for (VariableDefinition variableDefinition:this.definition.getVariableDefinitions()) {
            variableDefinitions.add(new ExtendedVariableDefinition(variableDefinition));
        }
        return variableDefinitions;
    }

    public Map<String, String> getVariableDefinitionsMap() {
        Map<String, String> variableDefinitionsMap = new HashMap<>();
        for (VariableDefinition variableDefinition:this.definition.getVariableDefinitions()) {
            variableDefinitionsMap.put(variableDefinition.getName(),
                    Utils.getVariableType(variableDefinition.getType()));
        }
        return variableDefinitionsMap;
    }

    public List<ExtendedFieldDefinition> getExtendedFieldDefinitions() {
        List<ExtendedFieldDefinition> fieldDefinitionList = new ArrayList<>();
        for (Selection<?> selection: this.definition.getSelectionSet().getSelections()) {
            Field field = (Field) selection;
            ExtendedFieldDefinition extendedFieldDefinition = new ExtendedFieldDefinition(field);
            fieldDefinitionList.add(extendedFieldDefinition);
        }
        return fieldDefinitionList;
    }

    public String getQueryString() {
        List<FragmentDefinition> fragmentDefinitions = this.queryFileAst.getDefinitionsOfType(FragmentDefinition.class);
        List<Definition> definitionArrayList = new ArrayList<>(fragmentDefinitions);
        Document document = Document.newDocument().definitions(definitionArrayList).definition(this.definition).build();
        return AstPrinter.printAstCompact(document);
    }
}
