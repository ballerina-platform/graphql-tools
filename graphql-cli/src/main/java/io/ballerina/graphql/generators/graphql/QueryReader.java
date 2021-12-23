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

package io.ballerina.graphql.generators.graphql;

import graphql.language.Document;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;
import io.ballerina.graphql.generators.graphql.components.ExtendedFragmentDefinition;
import io.ballerina.graphql.generators.graphql.components.ExtendedOperationDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the IQueryReader.
 */
public class QueryReader implements IQueryReader {
    private final Document document;

    public QueryReader(Document document) {
        this.document = document;
    }

    @Override
    public List<ExtendedOperationDefinition> getExtendedOperationDefinitions() {
        List<OperationDefinition> operationDefinitions = this.document.getDefinitionsOfType(OperationDefinition.class);
        List<ExtendedOperationDefinition> extendedOperationDefinitions = new ArrayList<>();
        for (OperationDefinition definition: operationDefinitions) {
            ExtendedOperationDefinition extendedOperationDefinition =
                    new ExtendedOperationDefinition(this.document, definition);
            extendedOperationDefinitions.add(extendedOperationDefinition);
        }
        return extendedOperationDefinitions;
    }

    @Override
    public List<ExtendedFragmentDefinition> getExtendedFragmentDefinitions() {
        List<FragmentDefinition> fragmentDefinitions = document.getDefinitionsOfType(FragmentDefinition.class);
        List<ExtendedFragmentDefinition> extendedFragmentDefinitions = new ArrayList<>();
        for (FragmentDefinition definition: fragmentDefinitions) {
            ExtendedFragmentDefinition fragmentDefinition = new ExtendedFragmentDefinition(definition);
            extendedFragmentDefinitions.add(fragmentDefinition);
        }
        return extendedFragmentDefinitions;
    }
}
