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

package io.ballerina.graphql.generator.gateway;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayQueryPlanGenerator;
import io.ballerina.graphql.generator.gateway.generator.GatewayServiceGenerator;
import io.ballerina.graphql.generator.gateway.generator.GatewayTypeGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to test code generation related to gateway.
 */
public class GatewayCodeGenerationTest extends GraphqlTest {
    private final Path schemaFiles = this.resourceDir.resolve(Paths.get("federationGateway",
            "supergraphSchemas"));
    private final Path expectedResources = this.resourceDir.resolve(Paths.get("federationGateway",
            "expectedResults"));

    @Test(description = "Test query plan generation for gateway", dataProvider =
            "GatewayQueryPlanGenerationDataProvider")
    public void testQueryPlanGeneration(String supergraphFileName)
            throws ValidationException, IOException, GatewayGenerationException {
        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                schemaFiles.resolve(Paths.get(supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphqlSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get(GatewayTestUtils.getCorrespondingFolderName(supergraphFileName), "queryPlan.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayQueryPlanGenerationDataProvider")
    public Object[][] getGatewayQueryPlanGenerationTestData() {
        return new Object[][] {
                {"SupergraphWithTwoEntities"},
                {"SupergraphWithIDTypeFields"},
                {"SupergraphWithThreeEntities"}
        };
    }

    @Test(description = "Test service generation for gateway", dataProvider = "serviceGenerationDataProvider")
    public void testGatewayServiceGeneration(String supergraphFileName)
            throws ValidationException, IOException, GatewayGenerationException {

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                schemaFiles.resolve(Paths.get(supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        String generatedSrc = (new GatewayServiceGenerator(project)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get(GatewayTestUtils.getCorrespondingFolderName(supergraphFileName), "service.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "serviceGenerationDataProvider")
    public Object[][] getServiceGenerationDataProvider() {
        return new Object[][] {
                {"SupergraphWithTwoEntities"},
                {"SupergraphWithIDTypeFields"},
                {"SupergraphWithThreeEntities"}
        };
    }

    @Test(description = "Test gateway types generation", dataProvider = "GatewayTypeGenerationDataProvider")
    public void testGatewayTypeGeneration(String supergraphFileName)
            throws IOException, ValidationException, GatewayGenerationException {
        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                schemaFiles.resolve(Paths.get(supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphqlSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get(GatewayTestUtils.getCorrespondingFolderName(supergraphFileName), "types.bal")));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayTypeGenerationDataProvider")
    public Object[][] getGatewayTypeGenerationTestData() {
        return new Object[][] {
                {"SupergraphWithTwoEntities"},
                {"SupergraphWithIDTypeFields"},
                {"SupergraphWithThreeEntities"}
        };
    }
}
