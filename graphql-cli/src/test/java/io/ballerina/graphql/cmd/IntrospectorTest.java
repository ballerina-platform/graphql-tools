/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package io.ballerina.graphql.cmd;

import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.IntospectionException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.URL_RECOGNIZER;

/**
 * This class is used to test the functionality of the GraphQL introspector.
 */
public class IntrospectorTest extends GraphqlTest {

    @Test(description = "Test successful introspection")
    public void testGetIntrospectionResult()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-extensions.yaml")).toString(),
                this.tmpDir);

        String schema = projects.get(0).getSchema();
        Extension extensions = projects.get(0).getExtensions();

        try {
            if (schema.startsWith(URL_RECOGNIZER)) {
                Map<String, Object> introspectionResult =
                        Introspector.getInstance().getIntrospectionResult(schema, extensions);
            }
            Assert.assertTrue(true);
        } catch (IntospectionException e) {
            Assert.fail("Error while introspecting. " + e.getMessage());
        }
    }

    @Test(description = "Test successful introspection with empty headers")
    public void testGetIntrospectionResultWithEmptyHeaders()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-empty-headers.yaml")).toString(),
                this.tmpDir);

        String schema = projects.get(0).getSchema();
        Extension extensions = projects.get(0).getExtensions();

        try {
            if (schema.startsWith(URL_RECOGNIZER)) {
                Map<String, Object> introspectionResult =
                        Introspector.getInstance().getIntrospectionResult(schema, extensions);
            }
            Assert.assertTrue(true);
        } catch (IntospectionException e) {
            Assert.fail("Error while introspecting. " + e.getMessage());
        }
    }
}
