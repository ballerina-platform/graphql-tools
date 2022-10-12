package io.ballerina.graphql.idl.client;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static io.ballerina.graphql.idl.client.TestUtils.getMatchingFiles;

/**
 * Client IDL import integration tests.
 */
public class IDLClientGenPluginNegativeTests extends GraphqlIDLTest {
//        @BeforeClass
//    public void setupDistributions() throws IOException {
//        TestUtil.cleanDistribution();
//    }

    @Test(description = "Project structured configuration")
    public void projectStructuredConfiguration() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_03");
        assert matchingFiles == null;
    }

    @Test(description = "URL for config file")
    public void urlConfigDefinition() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_04");
        assert matchingFiles == null;
    }

    @Test(description = "invalid query file name")  // todo: Add a test for invalid schema content
    public void invalidQueryDefinition() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_05");
        assert matchingFiles == null;
    }

    @Test(description = "invalid schema file name") // todo: Add a test for invalid schema content
    public void invalidSchemaDefinition() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_06");
        assert matchingFiles == null;
    }

    @Test(description = "invalid config file name")
    public void invalidConfigName() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_08");
        assert matchingFiles == null;
    }
}
