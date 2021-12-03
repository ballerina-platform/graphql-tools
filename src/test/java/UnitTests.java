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

import graphql.language.Document;
import graphql.parser.Parser;
import io.ballerina.graphql.generators.graphql.QueryReader;
import io.ballerina.graphql.generators.graphql.components.ExtendedOperationDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Unit tests.
 */
public class UnitTests {
    private static final Log log = LogFactory.getLog(UnitTests.class);

    @Test
    public void testQueryReader() {
        try {
            String queryFile = IOUtils.toString(
                    Objects.requireNonNull(this.getClass().getResourceAsStream("country-queries.graphql")),
                    StandardCharsets.UTF_8
            );

            Parser parser = new Parser();
            Document parseDocument = parser.parseDocument(queryFile);
            QueryReader reader = new QueryReader(parseDocument);
//            for (ExtendedFragmentDefinition def:reader.getExtendedFragmentDefinitions()) {
//                log.info(def.getName());
//                log.info(def.getOperationType());
//                log.info(def.getExtendedFieldDefinitions());
//            }

            for (ExtendedOperationDefinition def:reader.getExtendedOperationDefinitions()) {
//                log.info(def.getName());
//                log.info(def.getOperationType());
//                log.info(def.getExtendedFieldDefinitions());
//                log.info(def.getVariableDefinitions());
                log.info(def.getQueryString());

                Map<String, String> variableDefinitionsMap = def.getVariableDefinitionsMap();
                for (String variableName:variableDefinitionsMap.keySet()) {
                    log.info(variableDefinitionsMap.get(variableName) + " " + variableName);
                }
            }
        } catch (IOException e) {
            log.info(e.getSuppressed());
        }
    }
}
