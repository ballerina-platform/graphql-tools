package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayServiceGenerator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Class for testing the generation of Ballerina services for a supergraph.
 */
public class GatewayServiceGeneratorTest extends GraphqlTest {
    @Test(description = "Test service generation for gateway 01")
    public void testGatewayServiceGeneration01()
            throws ValidationException, IOException, GatewayServiceGenerationException {
        String fileName = "Supergraph01";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen", "supergraphSchemas", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayServiceGenerator(graphQLSchema)).generateSrc();
    }

    @Test(description = "Test service generation for gateway 02")
    public void testGatewayServiceGeneration02()
            throws ValidationException, IOException, GatewayServiceGenerationException {
        String fileName = "Supergraph02";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen", "supergraphSchemas", fileName + ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayServiceGenerator(graphQLSchema)).generateSrc();
    }
}
