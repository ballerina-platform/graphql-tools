package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.UtilsGenerationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the functionality of the GraphQL utils code generator.
 */
public class UtilsGeneratorTest extends GraphqlTest {

    @Test(description = "Test the functionality of the GraphQL utils code generator")
    public void testGenerateSrc() throws IOException {
        try {
            String generatedUtilsContent = UtilsGenerator.getInstance().generateSrc()
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");;

            Path expectedUtilsFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "github", "types.bal"));
            String expectedUtilsContent = readContent(expectedUtilsFile);

            Assert.assertEquals(expectedUtilsContent, generatedUtilsContent);

        } catch (UtilsGenerationException e) {
            Assert.fail("Error while generating the utils code. " + e.getMessage());
        }
    }
}
