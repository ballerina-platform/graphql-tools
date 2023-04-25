package io.ballerina.graphql.generator.gateway;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayQueryPlanGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayTypeGenerationException;
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

public class GatewayCodeGenerationTest extends GraphqlTest {
    private final Path schemaFiles = this.resourceDir.resolve(Paths.get("federationGatewayGen",
            "supergraphSchemas"));
    private final Path expectedResources = this.resourceDir.resolve(Paths.get("federationGatewayGen",
            "expectedResults"));

    @Test(description = "Test query plan generation for gateway", dataProvider =
            "GatewayQueryPlanGenerationDataProvider")
    public void testQueryPlanGeneration(String supergraphFileName, String expectedFileName)
            throws ValidationException, IOException, GatewayQueryPlanGenerationException, GatewayGenerationException {
        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                schemaFiles.resolve(Paths.get( supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayQueryPlanGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get("queryPlans", expectedFileName)));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayQueryPlanGenerationDataProvider")
    public Object[][] getGatewayQueryPlanGenerationTestData() {
        return new Object[][] {
                {"Supergraph01", "queryPlan01.bal"},
                {"Supergraph02", "queryPlan02.bal"},
                {"Supergraph03", "queryPlan03.bal"},
        };
    }

    @Test(description = "Test service generation for gateway", dataProvider = "serviceGenerationDataProvider")
    public void testGatewayServiceGeneration(String supergraphFileName, String expectedFileName)
            throws ValidationException, IOException, GatewayServiceGenerationException {

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                schemaFiles.resolve(Paths.get( supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        String generatedSrc = (new GatewayServiceGenerator(project)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get("services", expectedFileName)));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "serviceGenerationDataProvider")
    public Object[][] getServiceGenerationDataProvider() {
        return new Object[][]{
                {"Supergraph01", "service01.bal"},
                {"Supergraph02", "service02.bal"},
                {"Supergraph03", "service03.bal"},
        };
    }

    @Test(description = "Test supergraph types generation", dataProvider = "GatewayTypeGenerationDataProvider")
    public void testSupergraphTypeGeneration(String supergraphFileName, String expectedFileName)
            throws IOException, ValidationException, GatewayTypeGenerationException {
        String fileName = "Supergraph01";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                schemaFiles.resolve(Paths.get(supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(expectedResources.resolve(
                Paths.get("types", expectedFileName)));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @DataProvider(name = "GatewayTypeGenerationDataProvider")
    public Object[][] getGatewayTypeGenerationTestData() {
        return new Object[][] {
                {"Supergraph01", "types01.bal"},
                {"Supergraph02", "types02.bal"},
                {"Supergraph03", "types03.bal"},
        };
    }
}
