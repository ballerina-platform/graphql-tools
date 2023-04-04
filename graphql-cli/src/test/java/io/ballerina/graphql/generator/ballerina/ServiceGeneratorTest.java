package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.exception.ClientGenerationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for ServiceGenerator.
 * Test the successful generation of service code
 */
public class ServiceGeneratorTest extends GraphqlTest {
    @Test(description = "Test the successful generation of service code")
    public void testGenerateSrc()
            throws CmdException, IOException, ParseException, ValidationException, ClientGenerationException {
        try {
            String fileName = "Schema01Api";
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceGenerator serviceGenerator = new ServiceGenerator();
            String generatedServiceContent =
                    serviceGenerator.generateSrc(fileName, graphQLSchema).trim()
                            .replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");

            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "service01.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);

            Assert.assertEquals(expectedServiceContent, generatedServiceContent);
        } catch (ServiceGenerationException e) {
            Assert.fail("Error while generating the service code. " + e.getMessage());
        }
    }
}
