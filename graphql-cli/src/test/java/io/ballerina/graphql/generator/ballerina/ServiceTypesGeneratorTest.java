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
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.common.TestUtils.writeContentTo;

/**
 * Test class for ServiceTypesGenerator.
 * Test the successful generation of service types file code
 */
public class ServiceTypesGeneratorTest extends GraphqlTest {
    @Test(description = "Test for schema with basic, method - default")
    public void testGenerateSrc() {
        String fileName = "SchemaWithBasic01Api";
        String expectedFile = "typesWithBasic01Default.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with more types, method - default")
    public void testGenerateSrcForMoreTypes() {
        String fileName = "SchemaWithBasic02Api";
        String expectedFile = "typesWithBasic02Default.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with mutation types, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with subscription types, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with lists, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with input types, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for simple schema, method - use record objects")
    public void testGenerateSrcForRecordForced() {
        String fileName = "SchemaWithBasic03Api";
        String expectedFile = "typesWithBasic03RecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for simple schema with enum, method - default")
    public void testGenerateSrcForEnum() {
        String fileName = "SchemaWithEnumApi";
        String expectedFile = "typesWithEnumDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for simple schema with union, method - default")
    public void testGenerateSrcForUnion() {
        String fileName = "SchemaWithUnionApi";
        String expectedFile = "typesWithUnionDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent.trim(), writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for simple schema with interface, method - default")
    public void testGenerateSrcForInterface() {
        String fileName = "SchemaWithInterfaceApi";
        String expectedFile = "typesWithInterfaceDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for simple schema with multiple interfaces, method - default")
    public void testGenerateSrcForMultipleInterface() {
        String fileName = "SchemaWithMultipleInterfacesApi";
        String expectedFile = "typesWithMultipleInterfacesDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with interfaces implementing interfaces, method - default")
    public void testGenerateSrcForInterfacesImplementingInterfaces() {
        String fileName = "SchemaWithInterfacesImplementingInterfacesApi";
        String expectedFile = "typesWithInterfacesImplementingInterfacesDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema multi-dimensional lists, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "schemasWithDefaultParameterValuesAndExpectedFiles")
    public Object[][] createSchemasWithDefaultParameterValuesAndExpectedFilesData() {
        return new Object[][]{{"SchemaWithDefaultParameters01Api", "typesWithDefaultParameters01Default.bal"},
                {"SchemaWithDefaultParameters02Api", "typesWithDefaultParameters02Default.bal"},
                {"SchemaWithDefaultParameters03Api", "typesWithDefaultParameters03Default.bal"},
                {"SchemaWithDefaultParameters04Api", "typesWithDefaultParameters04Default.bal"}};
    }

    @Test(description = "Test for schema with default parameter values, method - default",
            dataProvider = "schemasWithDefaultParameterValuesAndExpectedFiles")
    public void testGenerateSrcForSchemaWithDefaultParameterValues(String fileName, String expectedFile) {
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in query resolvers, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test(description = "Test for schema with docs in arguments in resolver functions, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with multiple line docs in resolver function arguments, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in output types, method - default")
    public void testGenerateSrcForSchemaWithDocsInOutputTypes() {
        String fileName = "SchemaDocsWithOutputsApi";
        String expectedFile = "typesDocsWithOutputsDefault.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in output types, method - use records objects")
    public void testGenerateSrcForSchemaWithDocsInOutputTypesWithRecords() {
        String fileName = "SchemaDocsWithOutputsApi";
        String expectedFile = "typesDocsWithOutputsRecordsAllowed.bal";
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setUseRecordsForObjects(true);
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in output types, method - default")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in Enum types")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in input types")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with docs in interfaces")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with deprecated directive fields",
            dataProvider = "schemaFileNamesWithDeprecationAndExpectedFiles")
    public void testGenerateSrcForSchemaWithDeprecatedDirective(String fileName, String expectedFile) {
        try {
            GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                    this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid", fileName + ".graphql"))
                            .toString(), this.tmpDir);
            GraphQLSchema graphQLSchema = project.getGraphQLSchema();

            ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
            serviceTypesGenerator.setFileName(fileName);
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "schemaFileNamesWithDeprecationAndExpectedFiles")
    public Object[][] getSchemaFileNamesWithDeprecationAndExpectedFiles() {
        return new Object[][]{{"SchemaDocsWithDeprecated01Api", "typesDocsWithDeprecated01Default.bal"},
                {"SchemaDocsWithDeprecated02Api", "typesDocsWithDeprecated02Default.bal"},
                {"SchemaDocsWithDeprecated03Api", "typesDocsWithDeprecated03Default.bal"}};
    }

    @Test(description = "Test for schema with deprecated directive fields - method records allowed")
    public void testGenerateSrcForSchemaWithDeprecatedAllowRecords() {
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test for schema with file upload fields")
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
            String generatedServiceTypesContent = serviceTypesGenerator.generateSrc(graphQLSchema);
            writeContentTo(generatedServiceTypesContent, this.tmpDir);

            Path expectedServiceTypesFile =
                    resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
            String expectedServiceTypesContent = readContentWithFormat(expectedServiceTypesFile);
            String writtenServiceTypesContent = readContentWithFormat(this.tmpDir.resolve("types.bal"));
            Assert.assertEquals(expectedServiceTypesContent, writtenServiceTypesContent);
        } catch (ValidationException | IOException | ServiceTypesGenerationException e) {
            Assert.fail(e.getMessage());
        }
    }
}
