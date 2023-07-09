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
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.common.TestUtils.writeContentTo;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;

/**
 * Test class for ServiceTypesGenerator.
 * Test the successful generation of service types file code
 */
public class ServiceTypesGeneratorTest extends GraphqlTest {
    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithSingleObjectType() {
        String fileName = "SchemaWithSingleObjectApi";
        String expectedFile = "typesWithSingleObjectDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithMultipleObjectTypes() {
        String fileName = "SchemaWithMultipleObjectsApi";
        String expectedFile = "typesWithMultipleObjectsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithMutationTypes() {
        String fileName = "SchemaWithMutationApi";
        String expectedFile = "typesWithMutationDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithSubscriptionTypes() {
        String fileName = "SchemaWithSubscriptionApi";
        String expectedFile = "typesWithSubscriptionDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithLists() {
        String fileName = "SchemaWithListsApi";
        String expectedFile = "typesWithListsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithInputTypes() {
        String fileName = "SchemaWithInputsApi";
        String expectedFile = "typesWithInputsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"record-type-for-objects"})
    public void testGenerateSrcForSchemaWithObjectFieldTakingInputArgumentsRecordsAllowed() {
        String fileName = "SchemaWithObjectTakingInputArgumentApi";
        String expectedFile = "typesWithObjectTakingInputArgumentRecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"record-type-for-objects"})
    public void testGenerateSrcForSchemaWithObjectImplementingInterfaceRecordsAllowed() {
        String fileName = "SchemaWithInterfaceApi";
        String expectedFile = "typesWithInterfaceRecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"record-type-for-objects"})
    public void testGenerateSrcForSchemaWithUnionRecordsAllowed() {
        String fileName = "SchemaWithUnionApi";
        String expectedFile = "typesWithUnionRecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithEnum() {
        String fileName = "SchemaWithEnumApi";
        String expectedFile = "typesWithEnumDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithUnion() {
        String fileName = "SchemaWithUnionApi";
        String expectedFile = "typesWithUnionDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent.trim(), writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithInterface() {
        String fileName = "SchemaWithInterfaceApi";
        String expectedFile = "typesWithInterfaceDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithMultipleInterfaces() {
        String fileName = "SchemaWithMultipleInterfacesApi";
        String expectedFile = "typesWithMultipleInterfacesDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithInterfacesImplementingInterfaces() {
        String fileName = "SchemaWithInterfacesImplementingInterfacesApi";
        String expectedFile = "typesWithInterfacesImplementingInterfacesDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithMultiDimensionalLists() {
        String fileName = "SchemaWithMultiDimensionalListsApi";
        String expectedFile = "typesWithMultiDimensionalListsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "schemasWithDefaultParameterValuesAndExpectedFiles")
    public Object[][] getSchemasWithDefaultParameterValuesAndExpectedFilesData() {
        return new Object[][]{{"SchemaWithDefaultParameters01Api", "typesWithDefaultParameters01Default.bal"},
                {"SchemaWithDefaultParameters02Api", "typesWithDefaultParameters02Default.bal"},
                {"SchemaWithDefaultParameters03Api", "typesWithDefaultParameters03Default.bal"},
                {"SchemaWithDefaultParameters04Api", "typesWithDefaultParameters04Default.bal"}};
    }

    @Test(
        groups = {"service-type-for-objects"},
        dataProvider = "schemasWithDefaultParameterValuesAndExpectedFiles"
    )
    public void testGenerateSrcForSchemaWithDefaultParameterValues(String fileName, String expectedFile) {
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInQueryResolverFunctions() {
        String fileName = "SchemaDocsWithQueryResolversApi";
        String expectedFile = "typesDocsWithQueryResolversDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInArgumentsInResolverFunctions() {
        String fileName = "SchemaDocsWithResolverArgumentsApi";
        String expectedFile = "typesDocsWithResolverArgumentsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithMultipleLineDocsInResolverFunctionArguments() {
        String fileName = "SchemaDocsWithMultipleLinesApi";
        String expectedFile = "typesDocsWithMultipleLinesDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInOutputTypes() {
        String fileName = "SchemaDocsWithObjectsApi";
        String expectedFile = "typesDocsWithObjectsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"record-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInOutputTypesWithRecords() {
        String fileName = "SchemaDocsWithObjectsApi";
        String expectedFile = "typesDocsWithObjectsRecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInUnionTypes() {
        String fileName = "SchemaDocsWithUnionApi";
        String expectedFile = "typesDocsWithUnionDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInEnumTypes() {
        String fileName = "SchemaDocsWithEnumApi";
        String expectedFile = "typesDocsWithEnumDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInInputTypes() {
        String fileName = "SchemaDocsWithInputsApi";
        String expectedFile = "typesDocsWithInputsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithDocsInInterfaces() {
        String fileName = "SchemaDocsWithInterfacesApi";
        String expectedFile = "typesDocsWithInterfacesDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "schemaFileNamesWithDeprecationAndExpectedFiles")
    public Object[][] getSchemaFileNamesWithDeprecationAndExpectedFiles() {
        return new Object[][]{{"SchemaDocsWithDeprecated01Api", "typesDocsWithDeprecated01Default.bal"},
                {"SchemaDocsWithDeprecated02Api", "typesDocsWithDeprecated02Default.bal"},
                {"SchemaDocsWithDeprecated03Api", "typesDocsWithDeprecated03Default.bal"}};
    }

    @Test(
        groups = {"service-type-for-objects"},
        dataProvider = "schemaFileNamesWithDeprecationAndExpectedFiles"
    )
    public void testGenerateSrcForSchemaWithDeprecatedDirectiveFields(String fileName, String expectedFile) {
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"record-type-for-objects"})
    public void testGenerateSrcForSchemaWithDeprecatedDirectiveFieldsRecordsAllowed() {
        String fileName = "SchemaDocsWithDeprecated01Api";
        String expectedFile = "typesDocsWithDeprecated01RecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithFileUploadFields() {
        String fileName = "SchemaWithFileUploadApi";
        String expectedFile = "typesWithFileUploadDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"service-type-for-objects"})
    public void testGenerateSrcForSchemaWithInputTypeFieldsHavingDefaultValues() {
        String fileName = "SchemaWithInputTypeDefaultParametersApi";
        String expectedFile = "typesWithInputTypeDefaultParameters.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            ModulePartNode newContent = serviceTypesGenerator.generateContentNode(graphQLSchema);
            SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(newContentSyntaxTree);
            writeContentTo(generatedServiceTypesContent, this.tmpDir, TYPES_FILE_NAME);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve(TYPES_FILE_NAME));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "invalidSchemasWithExpectedErrorMessages")
    public Object[][] getInvalidSchemasWithExpectedErrorMessages() {
        return new Object[][]{
                {"InvalidSchemaWithUnionMemberTypeOfEnum",
                        "The member types of a Union type must all be Object base types. member type Gender in Union " +
                                "Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInterface", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfUnion", "The member types of a Union type must all be Object " +
                        "base types. member type Internal in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInputType", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union PersonInfo is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInputType", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union PersonInfo is invalid."}};
    }

    @Test(dataProvider = "invalidSchemasWithExpectedErrorMessages")
    public void testGenerateSrcForInvalidSchemas(String fileName, String errorMessage) {
        try {
            TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "invalid", fileName +
                                    ".graphql"))
                            .toString(), this.tmpDir);
        } catch (ValidationException | IOException e) {
            Assert.assertTrue(e.getMessage().contains(errorMessage));
        }
    }
}
