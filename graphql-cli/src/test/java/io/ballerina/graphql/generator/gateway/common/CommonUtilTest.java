/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.gateway.common;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLFieldDefinition;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GatewayTestUtils;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayCodeGenerator;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Class to test common utils used in graphql gateway generation.
 */
public class CommonUtilTest extends GraphqlTest {

    @Test(description = "Test getting defined type objects", dataProvider = "SchemaAndTypeNamesProvider")
    public void testGetCustomDefinedObjectTypeNames(GraphqlGatewayProject project, String[] typeNames) {
        Object[] namesFound = CommonUtils.getCustomDefinedObjectTypeNames(project.getGraphQLSchema()).toArray();
        Assert.assertEqualsNoOrder(typeNames, namesFound);
    }

    @DataProvider(name = "SchemaAndTypeNamesProvider")
    public Object[][] getSchemaAndTypeNames() throws ValidationException, IOException {
        return new Object[][] {
                {GatewayTestUtils.getGatewayProject("SupergraphWithTwoEntities", tmpDir),
                        new String[] {"Astronaut", "Mission"}},
                {GatewayTestUtils.getGatewayProject("SupergraphWithIDTypeFields", tmpDir),
                        new String[] {"Astronaut", "Mission"}},
                {GatewayTestUtils.getGatewayProject("SupergraphWithThreeEntities", tmpDir),
                        new String[] {"Review", "Product", "Category"}},

        };
    }

    @Test(description = "Test get query types", dataProvider = "SchemaAndQueryTypesProvider")
    public void textGetQueryTypes(GraphqlGatewayProject project, String[] queryTypes) {
        Object[] queryTypeNames = CommonUtils.getQueryTypes(project.getGraphQLSchema()).stream().map(
                field -> ((GraphQLFieldDefinition) field).getName()
        ).toArray();
        Assert.assertEqualsNoOrder(queryTypeNames, queryTypes);
    }

    @DataProvider(name = "SchemaAndQueryTypesProvider")
    public Object[][] getSchemaAndQueryTypes() throws ValidationException, IOException {
        return new Object[][] {
                {GatewayTestUtils.getGatewayProject("SupergraphWithTwoEntities", tmpDir), new String[] {
                        "astronaut", "astronauts", "mission", "missions"
                }}
        };
    }

    @Test(description = "Test get mutation types", dataProvider = "SchemaAndMutationTypesProvider")
    public void testGetMutationTypes(GraphqlGatewayProject project, String[] queryTypes) {
        Object[] queryTypeNames = CommonUtils.getMutationTypes(project.getGraphQLSchema()).stream().map(
                field -> ((GraphQLFieldDefinition) field).getName()
        ).toArray();
        Assert.assertEqualsNoOrder(queryTypeNames, queryTypes);
    }

    @DataProvider(name = "SchemaAndMutationTypesProvider")
    public Object[][] getSchemaAndMutationTypes() throws ValidationException, IOException {
        return new Object[][] {
                {GatewayTestUtils.getGatewayProject("SupergraphWithTwoEntities", tmpDir), new String[] {
                        "addMission"
                }},
                {GatewayTestUtils.getGatewayProject("SupergraphWithIDTypeFields", tmpDir),
                        new String[] {}}
        };
    }

    @Test(description = "Test successful compilation of a ballerina gateway projects",
            dataProvider = "GatewayProjectFilesProvider")
    public void testGetCompiledBallerinaProject(File folder, String folderName)
            throws GatewayGenerationException, IOException {
        Path projectDir = tmpDir.resolve(folderName);
        if (!projectDir.toFile().mkdir()) {
            throw new RuntimeException("Error while creating project directory");
        }
        GatewayCodeGenerator.copyTemplateFiles(projectDir);
        GatewayTestUtils.copyFilesToTarget(Objects.requireNonNull(folder.listFiles()), projectDir);
        File executable = CommonUtils.getCompiledBallerinaProject(projectDir, tmpDir, folderName);
        Assert.assertTrue(executable.exists());
    }

    @Test(description = "test getValue function", dataProvider = "ValueTypesProvider")
    public void testGetValue(Value value, String stringValue) throws GatewayGenerationException {
        Assert.assertEquals(CommonUtils.getValue(value), stringValue);
    }

    @DataProvider(name = "ValueTypesProvider")
    public Object[][] getValueTypes() {
        return new Object[][] {
                {new IntValue(BigInteger.ONE), "1"},
                {new StringValue("Hello"), "\"Hello\""},
                {new BooleanValue(true), "true"},
                {new FloatValue(BigDecimal.ONE), "1"},
        };
    }

    @Test(description = "Test failure in compilation of a ballerina gateway projects",
            dataProvider = "InvalidGatewayProjectFilesProvider")
    public void testUnsuccessfulCompiledBallerinaProject(Path[] files, String folderName)
            throws GatewayGenerationException, IOException {
        Path projectDir = tmpDir.resolve(folderName);
        if (!projectDir.toFile().mkdir()) {
            throw new RuntimeException("Error while creating project directory");
        }
        GatewayCodeGenerator.copyTemplateFiles(projectDir);
        GatewayTestUtils.copyFilesToTarget(files, projectDir);
        try {
            CommonUtils.getCompiledBallerinaProject(projectDir, tmpDir, folderName);
        } catch (GatewayGenerationException e) {
            Assert.assertEquals(e.getMessage(),
                    "ERROR [(null:):(1:1,1:1)] Ballerina federation gateway generation related error" +
                    " occurred. Error while generating the executable.");
        }
    }

    @DataProvider(name = "GatewayProjectFilesProvider")
    public Object[][] getProjects() {
        Path gatewayResourceDir = Paths.get(resourceDir.toAbsolutePath().toString(), "federationGateway",
                "expectedResults");
        return new Object[][] {
                {
                        new File(gatewayResourceDir.resolve("supergraphWithTwoEntities")
                                .toAbsolutePath().toString()),
                        "project01"
                },
                {
                        new File(gatewayResourceDir.resolve("supergraphWithIDTypeFields")
                                .toAbsolutePath().toString()),
                        "project02"
                },
                {
                        new File(gatewayResourceDir.resolve("supergraphWithThreeEntities")
                                .toAbsolutePath().toString()),
                        "project03"
                }
        };
    }

    @DataProvider(name = "InvalidGatewayProjectFilesProvider")
    public Object[][] getinvalidProjects() {
        return new Object[][] {
                {
                        new Path[] {},
                        "projectWithMissingFiles"
                }
        };
    }
}
