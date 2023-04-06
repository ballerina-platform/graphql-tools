package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayTypeGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayTypeGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to test the generation of Ballerina types for a supergraph.
 */
public class GatewayTypeGenerationTest extends GraphqlTest {

    private final Path resources = this.resourceDir.resolve(Paths.get("federationGatewayGen",
            "expectedResults", "types"));

    @Test(description = "Test supergraph types generation 01")
    public void testSupergraphTypeGeneration01()
            throws IOException, ValidationException, GatewayTypeGenerationException {
        String fileName = "Supergraph01";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen", "supergraphSchemas", fileName +
                                ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve("types01.bal"));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }

    @Test(description = "Test supergraph types generation 02")
    public void testSupergraphTypeGeneration02()
            throws IOException, ValidationException, GatewayTypeGenerationException {
        String fileName = "Supergraph02";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen", "supergraphSchemas", fileName +
                                ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve("types02.bal"));
        Assert.assertEquals(generatedSrc, expectedSrc);
    }
}
