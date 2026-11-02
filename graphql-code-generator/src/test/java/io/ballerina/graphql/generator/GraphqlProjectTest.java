/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.generator;

import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Tests the schema file name of a GraphQL project.
 */
public class GraphqlProjectTest {

    private static final String OUTPUT_PATH = "build";

    @DataProvider
    public Object[][] schemaPaths() {
        return new Object[][]{
                {"book-api.graphql", "book-api"},
                {"schemas/book-api.graphql", "book-api"},
                {"/home/user/schemas/book-api.graphql", "book-api"},
                {"C:\\Users\\user\\schemas\\book-api.graphql", "book-api"},
                {"C:\\schemas\\books.v2.graphql", "books.v2"},
                {"schemas\\nested/mixed\\book-api.graphql", "book-api"}
        };
    }

    @Test(description = "Test that the file name is the last path part without its extension, for any separator",
            dataProvider = "schemaPaths")
    public void testFileName(String schemaPath, String fileName) {
        Assert.assertEquals(new GraphqlServiceProject(ROOT_PROJECT_NAME, schemaPath, OUTPUT_PATH).getFileName(),
                fileName);
    }
}
