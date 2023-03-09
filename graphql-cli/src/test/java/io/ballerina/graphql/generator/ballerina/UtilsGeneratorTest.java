package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.cmd.GraphqlClientProject;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.model.AuthConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL utils code generator.
 */
public class UtilsGeneratorTest extends GraphqlTest {

    @Test(description = "Test the functionality of the GraphQL utils code generator with API keys config")
    public void testGenerateSrcWithApiKeysConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql-config-with-auth-apikeys-config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedUtilsContent = UtilsGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedUtilsFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "apiKeysConfig",
                            "utils.bal"));
            String expectedUtilsContent = readContent(expectedUtilsFile);

            Assert.assertEquals(expectedUtilsContent, generatedUtilsContent);

        } catch (UtilsGenerationException e) {
            Assert.fail("Error while generating the utils code. " + e.getMessage());
        }
    }

    @Test(description = "Test the functionality of the GraphQL utils code generator with client config")
    public void testGenerateSrcWithClientConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql-config-with-auth-client-config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedUtilsContent = UtilsGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedUtilsFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "clientConfig",
                            "utils.bal"));
            String expectedUtilsContent = readContent(expectedUtilsFile);

            Assert.assertEquals(expectedUtilsContent, generatedUtilsContent);

        } catch (UtilsGenerationException e) {
            Assert.fail("Error while generating the utils code. " + e.getMessage());
        }
    }
}
