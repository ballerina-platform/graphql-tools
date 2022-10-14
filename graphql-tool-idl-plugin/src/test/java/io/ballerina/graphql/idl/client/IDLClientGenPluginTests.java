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
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static io.ballerina.graphql.idl.client.TestUtils.getMatchingFiles;

/**
 * Client IDL import integration tests.
 *
 * @since 0.3.0
 */
public class IDLClientGenPluginTests extends GraphqlIDLTest {

    @Test(description = "Single client declaration in module level")
    public void testSingleClientDeclaration() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_01");
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "two declarations in module and class level")
    public void testMultiClientDeclaration() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_02");
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "config file contains in a subdirectory of bal script level")
    public void testRemoteSchema() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_07");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1);
    }

    //todo: Add absolute config path test case

    @AfterTest
    @Override
    public void removeGeneratedFile() throws IOException {
        super.removeGeneratedFile();
    }
}
