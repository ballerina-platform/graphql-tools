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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.graphql.idl.client.TestUtils.checkModuleAvailability;
import static io.ballerina.graphql.idl.client.TestUtils.getMatchingFiles;

/**
 * Client IDL import integration tests.
 *
 * @since 0.3.0
 */
public class IDLClientGenPluginTests extends GraphqlIDLTest {

    @Test(description = "Single client declaration in module level")
    public void testSingleClientDeclaration() throws IOException, InterruptedException {
        Assert.assertTrue(checkModuleAvailability("project_01"));
        List<String> ids = new LinkedList<>();
        ids.add("foo");
        File[] matchingFiles = getMatchingFiles("project_01", ids);
        Assert.assertNotNull(matchingFiles);
    }

    @Test(description = "two declarations in module level")
    public void testMultiClientDeclaration() throws IOException, InterruptedException {
        Assert.assertTrue(checkModuleAvailability("project_02"));
        List<String> ids = new LinkedList<>();
        ids.add("foo");
        File[] matchingFiles = getMatchingFiles("project_02", ids);
        Assert.assertNotNull(matchingFiles);
    }

    @Test(description = "Remote schema definition")
    public void testRemoteSchema() throws IOException, InterruptedException {
        Assert.assertTrue(checkModuleAvailability("project_07"));
        List<String> ids = new LinkedList<>();
        ids.add("foo");
        File[] matchingFiles = getMatchingFiles("project_07", ids);
        Assert.assertNotNull(matchingFiles);
    }

    @Test(description = "Validate client class signature")
    public void testClientClassSignature() throws IOException, InterruptedException {
        String clientClassSignature = "public isolated client class Client {";
        Assert.assertTrue(checkModuleAvailability("project_10"));
        Path clientFile =
                Path.of(RESOURCE.resolve("project_10/generated/foo/client.bal").toString());
        Stream<String> generatedServiceLines = Files.lines(clientFile);
        String generatedContent = generatedServiceLines.collect(Collectors.joining(System.lineSeparator()));
        generatedServiceLines.close();
        Assert.assertTrue(generatedContent.contains(clientClassSignature));
    }

    //todo: Add absolute config path test case

    @AfterTest
    @Override
    public void removeGeneratedFile() throws IOException {
        super.removeGeneratedFile();
    }
}
