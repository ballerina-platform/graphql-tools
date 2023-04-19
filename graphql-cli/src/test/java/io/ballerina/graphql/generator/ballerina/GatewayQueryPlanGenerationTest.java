package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayQueryPlanGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayQueryPlanGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class for testing the generation of query plan for a supergraph.
 */
public class GatewayQueryPlanGenerationTest extends GraphqlTest {
    private final Path resources = this.resourceDir.resolve(Paths.get("federationGatewayGen",
            "expectedResults", "queryPlans"));

    @Test(description = "Test query plan for gateway", dataProvider = "GatewayQueryPlanGenerationDataProvider")
    public void testQueryPlanGeneration(String supergraphFileName, String expectedFileName)
            throws ValidationException, IOException, GatewayQueryPlanGenerationException, GatewayGenerationException {
        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen",
                                "supergraphSchemas", supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve(expectedFileName));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayQueryPlanGenerationDataProvider")
    public Object[][] getGatewayQueryPlanGenerationTestData() {
        return new Object[][]{
                {"Supergraph01", "queryPlan01.bal"},
                {"Supergraph02", "queryPlan02.bal"},
                {"Supergraph03", "queryPlan03.bal"},
        };
    }
}
