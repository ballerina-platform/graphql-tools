package io.ballerina.graphql.generator.ballerina;

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.model.AuthConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL auth config generator.
 */
public class AuthConfigGeneratorTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(AuthConfigGeneratorTest.class);

    @Test(description = "Test the successful generation of ApiKeysConfig record")
    public void testGenerateApiKeysConfigRecord()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-auth-apikeys-config.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        TypeDefinitionNode authConfigRecord = AuthConfigGenerator.getInstance()
                .generateAuthConfigRecord("ApiKeysConfig", authConfig);
        String generatedAuthConfigRecord = authConfigRecord.toString();

        Path expectedAuthConfigRecordFile =
                resourceDir.resolve(Paths.get("expectedGenCode", "auth", "apiKeysConfig.bal"));
        String expectedAuthConfigRecord = readContent(expectedAuthConfigRecordFile);

        Assert.assertEquals(expectedAuthConfigRecord, generatedAuthConfigRecord);
    }

    @Test(description = "Test the successful generation of ClientConfig record")
    public void testGenerateClientConfigRecord() throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-auth-client-config.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        TypeDefinitionNode authConfigRecord = AuthConfigGenerator.getInstance()
                .generateAuthConfigRecord("ClientConfig", authConfig);
        String generatedAuthConfigRecord = authConfigRecord.toString()
                .trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");
        log.info(generatedAuthConfigRecord);

        Path expectedAuthConfigRecordFile =
                resourceDir.resolve(Paths.get("expectedGenCode", "auth", "clientConfig.bal"));
        String expectedAuthConfigRecord = readContent(expectedAuthConfigRecordFile);
        log.info(expectedAuthConfigRecord);

        Assert.assertEquals(generatedAuthConfigRecord, expectedAuthConfigRecord);
    }
}
