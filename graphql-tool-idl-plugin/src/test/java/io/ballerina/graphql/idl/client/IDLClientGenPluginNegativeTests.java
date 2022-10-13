package io.ballerina.graphql.idl.client;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static io.ballerina.graphql.idl.client.TestUtils.getMatchingFiles;

/**
 * Client IDL import integration tests.
 */
public class IDLClientGenPluginNegativeTests extends GraphqlIDLTest {

    @Test(description = "Project structured configuration")
    public void testProjectStructuredConfiguration() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_03");
        Assert.assertNull(matchingFiles);
    }

    @Test(description = "URL for config file")
    public void testInvalidConfigDefinition() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_04");
        Assert.assertNull(matchingFiles);
    }

    @Test(description = "invalid query file name")  // todo: Add a test for invalid schema content
    public void testInvalidQueryDefinition() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_05");
        Assert.assertNull(matchingFiles);
    }

    @Test(description = "invalid schema file name") // todo: Add a test for invalid schema content
    public void testInvalidSchemaDefinition() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_06");
        Assert.assertNull(matchingFiles);
    }

    @Test(description = "invalid config file name")
    public void testInvalidConfigName() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_08");
        Assert.assertNull(matchingFiles);
    }

    @AfterTest
    @Override
    public void removeGeneratedFile() throws IOException {
        super.removeGeneratedFile();
    }
}
