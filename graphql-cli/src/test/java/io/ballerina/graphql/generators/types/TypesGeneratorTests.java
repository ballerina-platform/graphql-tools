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

package io.ballerina.graphql.generators.types;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.ballerina.TypesGenerator;
import io.ballerina.graphql.generators.common.TestUtils;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import io.ballerina.graphql.validator.SDLValidator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests related to Types generator.
 */
public class TypesGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/recordTypesTests/").toAbsolutePath();

    @Test(description = "Generate input records from the GraphQL Schema")
    public void getInputRecords() throws IOException, ParseException, CmdException, ValidationException,
            TypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlProject> projects = TestUtils.populateProjects(config);
        for (GraphqlProject project : projects) {
             SDLValidator.getInstance().validate(project);
             QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = TypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedInputRecords.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate query response records from the GraphQL Schema")
    public void getQueryResponseRecords() throws IOException, ParseException, CmdException, ValidationException,
            TypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlProject> projects = TestUtils.populateProjects(config);
        for (GraphqlProject project : projects) {
            SDLValidator.getInstance().validate(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = TypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedQueryResponseRecords.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate all the records (input records and query response records)")
    public void getAllRecords() throws IOException, ParseException, CmdException, ValidationException,
            TypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlProject> projects = TestUtils.populateProjects(config);
        for (GraphqlProject project : projects) {
            SDLValidator.getInstance().validate(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = TypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedTypes.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }
}
