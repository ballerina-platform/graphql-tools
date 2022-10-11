/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.idl.client;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;

//import static io.ballerina.graphql.idl.client.TestUtils.DISTRIBUTIONS_DIR;
import static io.ballerina.graphql.idl.client.TestUtils.getMatchingFiles;

/**
 * Client IDL import integration tests.
 */
public class IDLClientGenPluginTests extends GraphqlIDLTest {

//    @BeforeClass
//    public void setupDistributions() throws IOException {
//        TestUtil.cleanDistribution();
//    }

    @Test(description = "Single client declaration in module level")
    public void singleClientDeclaration() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_01");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "two declarations in module and class level")
    public void multiClientDeclaration() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_02");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 2); // this should be 4 after the plugin fix
    }

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

    @Test(description = "config file contains in a subdirectory of bal script level")
    public void relativePathTest() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_07");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1); // this should be 4 after the plugin fix
    }

    @Test(description = "invalid config file name")
    public void invalidConfigName() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_08");
        assert matchingFiles == null;
    }
}
