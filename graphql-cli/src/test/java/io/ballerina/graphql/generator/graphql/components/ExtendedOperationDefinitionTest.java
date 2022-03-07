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

package io.ballerina.graphql.generator.graphql.components;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generator.ballerina.AuthConfigGeneratorTest;
import io.ballerina.graphql.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.model.AuthConfig;
import io.ballerina.graphql.generator.model.FieldType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This class is used to test the functionality of the GraphQL query reader ExtendedOperationDefinition class.
 */
public class ExtendedOperationDefinitionTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(AuthConfigGeneratorTest.class);

    @Test
    public void testGetOperationType() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        String generatedOperationType = queryOperation1Definition.getOperationType();
        String expectedOperationType = "QUERY";
        Assert.assertEquals(generatedOperationType, expectedOperationType);
    }

    @Test
    public void testGetName() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        String generatedQueryName = queryOperation1Definition.getName();
        String expectedQueryName = "operation1";
        Assert.assertEquals(generatedQueryName, expectedQueryName);
    }

    @Test
    public void testGetVariableDefinitions() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition operation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        List<ExtendedVariableDefinition> generatedVariableDefinitions = operation1Definition.getVariableDefinitions();
        List<String> expectedVariables = Arrays.asList("argument1 Boolean", "argument2 String",
                "argument3 Int", "argument4 Float", "argument5 ID", "argument6 CustomScalar", "argument7 CustomInput",
                "argument8 null", "argument9 null");
        for (ExtendedVariableDefinition generatedVariableDefinition : generatedVariableDefinitions) {
            String generatedVariableName = generatedVariableDefinition.getOriginalName();
            String generatedVariableType = generatedVariableDefinition.getDataType();
            String generatedVariable = generatedVariableName + " " + generatedVariableType;
            assertTrue(expectedVariables.contains(generatedVariable));
        }
    }

    @Test
    public void testGetVariableDefinitionsMap()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        Map<String, FieldType> generatedVariableDefinitionsMap =
                queryOperation1Definition.getVariableDefinitionsMap(schema);
        List<String> expectedVariables = Arrays.asList("argument1 boolean", "argument2 string", "argument3 int",
                "argument4 float", "argument5 string", "argument6 anydata", "argument7 CustomInput",
                "argument8 CustomInput?[]", "argument9 CustomInput[]");
        for (Map.Entry<String, FieldType> variableDefinitions: generatedVariableDefinitionsMap.entrySet()) {
            String generatedVariableName = variableDefinitions.getKey();
            String generatedVariableTypeName = variableDefinitions.getValue().getFieldTypeAsString();
            String generatedVariable = generatedVariableName + " " + generatedVariableTypeName;
            assertTrue(expectedVariables.contains(generatedVariable));
        }
    }

    @Test
    public void testGetExtendedFieldDefinitions()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        List<ExtendedFieldDefinition> generatedExtendedFieldDefinitions =
                queryOperation1Definition.getExtendedFieldDefinitions();
        List<String> expectedVariables = Arrays.asList("argument1 boolean", "argument2 string", "argument3 int",
                "argument4 float", "argument5 string", "argument6 anydata", "argument7 CustomInput",
                "argument8 CustomInput?[]", "argument9 CustomInput[]");

        for (ExtendedFieldDefinition extendedFieldDefinition: generatedExtendedFieldDefinitions) {
            String fieldName = extendedFieldDefinition.getName();
            log.info(fieldName);
        }
    }

    @Test
    public void testGetQueryString()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        String generatedQueryString = queryOperation1Definition.getQueryString();
        String expectedQueryString = "query operation1($argument1:Boolean!,$argument2:String!,$argument3:Int!," +
                "$argument4:Float!,$argument5:ID!,$argument6:CustomScalar!,$argument7:CustomInput!," +
                "$argument8:[CustomInput]!,$argument9:[CustomInput!]!) {operation1(argument1:$argument1," +
                "argument2:$argument2,argument3:$argument3,argument4:$argument4,argument5:$argument5," +
                "argument6:$argument6,argument7:$argument7,argument8:$argument8,argument9:$argument9) {field1 field2}}";
        Assert.assertEquals(generatedQueryString, expectedQueryString);
    }
}
