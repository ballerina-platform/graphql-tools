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

    @Test(description = "Test query plan for gateway 01")
    public void testQueryPlanGeneration01()
            throws ValidationException, IOException, GatewayQueryPlanGenerationException, GatewayGenerationException {
        String fileName = "Supergraph01";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen",
                                "supergraphSchemas", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve("queryPlan01.bal"));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @Test(description = "Test query plan for gateway 02")
    public void testQueryPlanGeneration02()
            throws ValidationException, IOException, GatewayQueryPlanGenerationException, GatewayGenerationException {
        String fileName = "Supergraph02";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen",
                                "supergraphSchemas", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve("queryPlan02.bal"));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @Test(description = "Test query plan for gateway 03")
    public void testQueryPlanGeneration03()
            throws ValidationException, IOException, GatewayQueryPlanGenerationException, GatewayGenerationException {
        String fileName = "Supergraph03";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen",
                                "supergraphSchemas", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve("queryPlan02.bal"));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

}
