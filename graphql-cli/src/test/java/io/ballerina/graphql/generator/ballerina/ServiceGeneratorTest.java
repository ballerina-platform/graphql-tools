/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceGenerator;
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.common.TestUtils.writeContentTo;

/**
 * Test class for ServiceGenerator.
 * Test the successful generation of service file code
 */
public class ServiceGeneratorTest extends GraphqlTest {
    @Test(description = "Test the successful generation of service code")
    public void testGenerateSrc() {
        String fileName = "SchemaWithSingleObjectApi";
        String expectedFile = "serviceForSchemaWithSingleObject.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            serviceTypesGenerator.generateSrc(newContentSyntaxTree);

            ServiceGenerator serviceGenerator = new ServiceGenerator();
            serviceGenerator.setFileName(fileName);
            serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());
            ModulePartNode serviceContent = serviceGenerator.generateContentNode();
            SyntaxTree serviceSyntaxTree = serviceGenerator.generateSyntaxTree(serviceContent);
            String generatedServiceContent = serviceGenerator.generateSrc(serviceSyntaxTree);
            writeContentTo(generatedServiceContent, this.tmpDir);
            Path expectedServiceFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceContent = readContentWithFormat(expectedServiceFile);
            String writtenServiceTypesContent =
                    readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceContent, writtenServiceTypesContent);
        } catch (ServiceGenerationException | ServiceTypesGenerationException | IOException | ValidationException e) {
            Assert.fail(e.getMessage());
        }
    }
}
