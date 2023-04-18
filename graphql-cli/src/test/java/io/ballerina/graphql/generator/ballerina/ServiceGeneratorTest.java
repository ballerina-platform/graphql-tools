package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceGenerator;
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
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
            throws IOException, ValidationException, ServiceTypesGenerationException {
        try {
            String fileName = "SchemaWithBasic01Api";
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            serviceTypesGenerator.generateSrc(graphQLSchema);
            ServiceGenerator serviceGenerator = new ServiceGenerator();
            serviceGenerator.setFileName(fileName);
            String generatedServiceContent =
                    serviceGenerator.generateSrc(serviceTypesGenerator.getServiceMethodDeclarations()).trim()
                            .replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");

            Path expectedServiceFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", "serviceForBasicSchema01.bal"));
            String expectedServiceContent = readContent(expectedServiceFile);

            Assert.assertEquals(expectedServiceContent, generatedServiceContent);
        } catch (ServiceGenerationException e) {
            Assert.fail("Error while generating the service code. " + e.getMessage());
        }
    }
}
