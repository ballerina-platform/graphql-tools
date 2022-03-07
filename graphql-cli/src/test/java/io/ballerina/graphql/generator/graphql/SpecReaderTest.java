/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.graphql;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.model.FieldType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This class is used to test the functionality of the GraphQL spec reader.
 */
public class SpecReaderTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(SpecReaderTest.class);

    @Test
    public void testGetInputObjectTypeNames() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();
        List<String> generatedInputObjectTypes = SpecReader.getInputObjectTypeNames(schema);
        List<String> expectedInputObjectTypes = Arrays.asList("ContinentFilterInput", "CountryFilterInput",
                "LanguageFilterInput", "StringQueryOperatorInput");
        for (String generatedInputObjectType: generatedInputObjectTypes) {
            assertTrue(expectedInputObjectTypes.contains(generatedInputObjectType));
        }
    }

    @Test
    public void testGetInputTypeFieldsMap() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();
        List<String> generatedInputObjectTypes = SpecReader.getInputObjectTypeNames(schema);
        List<String> expectedInputTypeFields = Arrays.asList("code StringQueryOperatorInput?",
                "continent StringQueryOperatorInput?", "code StringQueryOperatorInput?",
                "currency StringQueryOperatorInput?", "code StringQueryOperatorInput?", "nin string?[]?",
                "regex string?", "ne string?", "glob string?", "eq string?", "'in string?[]?");
        for (String generatedInputObjectType: generatedInputObjectTypes) {
            Map<String, FieldType> generatedInputTypeFieldsMap =
                    SpecReader.getInputTypeFieldsMap(schema, generatedInputObjectType);
            for (Map.Entry<String, FieldType> generatedInputTypeFields: generatedInputTypeFieldsMap.entrySet()) {
                String generatedFieldName = generatedInputTypeFields.getKey();
                String generatedTypeName = generatedInputTypeFields.getValue().getFieldTypeAsString();
                String generatedInputField = generatedFieldName + " " + generatedTypeName;
                assertTrue(expectedInputTypeFields.contains(generatedInputField));
            }
        }
    }

    @Test
    public void testGetObjectTypeNames() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();
        List<String> generatedObjectTypes = SpecReader.getObjectTypeNames(schema);
        List<String> expectedObjectTypes = Arrays.asList("Continent", "Country",
                "Language", "Query", "State");
        for (String generatedObjectType: generatedObjectTypes) {
            assertTrue(expectedObjectTypes.contains(generatedObjectType));
        }
    }

    @Test
    public void testGetObjectTypeFieldsMap() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();
        List<String> generatedObjectTypes = SpecReader.getObjectTypeNames(schema);
        List<String> expectedObjectTypeFields = Arrays.asList("code string", "name string", "countries Country[]",
                "continent Continent", "capital string?", "emojiU string", "code string", "emoji string",
                "languages Language[]", "native string", "phone string", "name string", "currency string?",
                "states State[]", "code string", "native string?", "name string?", "rtl boolean",
                "continent Continent?", "country Country?", "languages Language[]", "language Language?",
                "countries Country[]", "continents Continent[]", "country Country", "code string?", "name string");
        for (String generatedObjectType: generatedObjectTypes) {
            Map<String, FieldType> generatedObjectTypeFieldsMap =
                    SpecReader.getObjectTypeFieldsMap(schema, generatedObjectType);
            for (Map.Entry<String, FieldType> generatedObjectTypeFields: generatedObjectTypeFieldsMap.entrySet()) {
                String generatedFieldName = generatedObjectTypeFields.getKey();
                String generatedTypeName = generatedObjectTypeFields.getValue().getFieldTypeAsString();
                String generatedObjectTypeField = generatedFieldName + " " + generatedTypeName;
                assertTrue(expectedObjectTypeFields.contains(generatedObjectTypeField));
            }
        }
    }

    @Test
    public void testGetCustomScalarTypeNames() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();
        List<String> generatedCustomScalarTypes = SpecReader.getCustomScalarTypeNames(schema);
        List<String> expectedCustomScalarTypes = Arrays.asList("Upload");
        for (String generatedCustomScalarType: generatedCustomScalarTypes) {
            assertTrue(expectedCustomScalarTypes.contains(generatedCustomScalarType));
        }
    }

    @Test
    public void testGetEnumTypeNames() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();
        List<String> generatedEnumTypes = SpecReader.getEnumTypeNames(schema);
        List<String> expectedEnumTypes = Arrays.asList("CacheControlScope");
        for (String generatedEnumType: generatedEnumTypes) {
            assertTrue(expectedEnumTypes.contains(generatedEnumType));
        }
    }
}
