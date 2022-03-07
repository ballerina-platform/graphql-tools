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

package io.ballerina.graphql.generator.ballerina;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.model.AuthConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL client code generator.
 */
public class ClientGeneratorTest extends GraphqlTest {

    @Test(description = "Test the functionality of the GraphQL client code generator")
    public void testGenerateSrc() throws CmdException, IOException, ParseException, ValidationException {
        try {
            List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();
            List<String> documents = projects.get(0).getDocuments();
            GraphQLSchema schema = projects.get(0).getGraphQLSchema();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
            String queryDocumentName = CodeGeneratorUtils.getDocumentName(new File(documents.get(0)));

            String generatedClientContent = ClientGenerator.getInstance().
                    generateSrc(queryDocument, queryDocumentName, schema, authConfig)
                    .trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "country_queries_client.bal"));
            String expectedClientContent = readContent(expectedClientFile);

            Assert.assertEquals(expectedClientContent, generatedClientContent);

        } catch (ClientGenerationException e) {
            Assert.fail("Error while generating the client code. " + e.getMessage());
        }
    }
}
