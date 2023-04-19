package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayServiceGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class for testing the generation of Ballerina services for a supergraph.
 */
public class GatewayServiceGeneratorTest extends GraphqlTest {

    private final Path resources = this.resourceDir.resolve(Paths.get("federationGatewayGen",
            "expectedResults", "services"));

    @Test(description = "Test service generation for gateway", dataProvider = "serviceGenerationDataProvider")
    public void testGatewayServiceGeneration(String supergraphFileName, String expectedFileName)
            throws ValidationException, IOException, GatewayServiceGenerationException {
        String fileName = "Supergraph01";

        GraphqlGatewayProject project = TestUtils.getValidatedMockGatewayProject(
                this.resourceDir.resolve(Paths.get("federationGatewayGen",
                                "supergraphSchemas", supergraphFileName + ".graphql"))
                        .toString(), this.tmpDir);
        String generatedSrc = (new GatewayServiceGenerator(project)).generateSrc();
        String expectedSrc = Files.readString(resources.resolve(expectedFileName));
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
}
