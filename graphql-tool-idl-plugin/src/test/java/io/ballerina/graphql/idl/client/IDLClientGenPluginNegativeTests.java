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

import java.io.IOException;

import static io.ballerina.graphql.idl.client.TestUtils.DISTRIBUTION_FILE_NAME;
import static io.ballerina.graphql.idl.client.TestUtils.checkModuleAvailability;
import static io.ballerina.graphql.idl.client.TestUtils.executeRun;
import static io.ballerina.graphql.idl.client.TestUtils.logOutput;

/**
 * Client IDL import integration tests.
 *
 * @since 0.3.0
 */
public class IDLClientGenPluginNegativeTests extends GraphqlIDLTest {

    @Test(description = "Project structured configuration")
    public void testProjectStructuredConfiguration() throws IOException, InterruptedException {
        Assert.assertFalse(checkModuleAvailability("project_03"));
    }

    @Test(description = "URL for config file")
    public void testInvalidConfigDefinition() throws IOException, InterruptedException {
        Assert.assertFalse(checkModuleAvailability("project_04"));
    }

    @Test(description = "invalid query file name")  // todo: Add a test for invalid schema content
    public void testInvalidQueryDefinition() throws IOException, InterruptedException {
        Assert.assertFalse(checkModuleAvailability("project_05"));
    }

    @Test(description = "invalid schema file name") // todo: Add a test for invalid schema content
    public void testInvalidSchemaDefinition() throws IOException, InterruptedException {
        Assert.assertFalse(checkModuleAvailability("project_06"));
    }

    @Test(description = "invalid config file name")
    public void testInvalidConfigName() throws IOException, InterruptedException {
        Assert.assertFalse(checkModuleAvailability("project_08"));
    }

    @Test(description = "function level client declaration")
    public void testFunctionLevelClientDeclaration() throws IOException, InterruptedException {
        Assert.assertFalse(checkModuleAvailability("project_09"));
        Process process = executeRun(DISTRIBUTION_FILE_NAME, RESOURCE.resolve("project_09"));
        String logOutput = logOutput(process.getErrorStream());
        String errorMsg = "ERROR [main.bal:(2:5,2:11)] 'client' qualifier not allowed";
        Assert.assertTrue(logOutput.contains(errorMsg));
    }

    @AfterTest
    @Override
    public void removeGeneratedFile() throws IOException {
        super.removeGeneratedFile();
    }
}
