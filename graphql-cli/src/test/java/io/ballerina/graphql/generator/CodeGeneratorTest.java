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

package io.ballerina.graphql.generator;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL code generator.
 */
public class CodeGeneratorTest extends GraphqlTest {

    @Test(description = "Test the functionality of the GraphQL code generator")
    public void testGenerate() throws CmdException, IOException, ParseException, ValidationException {
        try {
            List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                    this.tmpDir);
            for (GraphqlProject project : projects) {
                CodeGenerator.getInstance().generate(project);
            }
            Assert.assertTrue(true);
        } catch (GenerationException e) {
            Assert.fail("Error while generating the code. " + e.getMessage());
        }
    }
}
