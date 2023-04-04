package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayTypeGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayTypeGenerator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class GatewayTypeGenerationTest extends GraphqlTest {
    @Test(description = "Test supergraph types generation 01")
    public void testSupergraphTypeGeneration01()
            throws CmdException, IOException, ParseException, ValidationException,
            GatewayTypeGenerationException {
        String fileName = "Supergraph01";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen", "supergraphSchemas", fileName +
                                ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();

        System.out.println(generatedSrc);
    }

    @Test(description = "Test supergraph types generation 02")
    public void testSupergraphTypeGeneration02()
            throws CmdException, IOException, ParseException, ValidationException,
            GatewayTypeGenerationException {
        String fileName = "Supergraph02";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen", "supergraphSchemas", fileName +
                                ".graphql"))
                        .toString(), this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();
        String generatedSrc = (new GatewayTypeGenerator(graphQLSchema)).generateSrc();

        System.out.println(generatedSrc);
    }
}
