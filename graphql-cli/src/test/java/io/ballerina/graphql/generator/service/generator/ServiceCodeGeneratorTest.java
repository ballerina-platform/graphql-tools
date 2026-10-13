/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.graphql.generator.service.generator;

import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests the types and service files generated for the GraphQL schemas in the test resources.
 */
public class ServiceCodeGeneratorTest extends GraphqlTest {

    private static final boolean SERVICE_CLASSES = false;
    private static final boolean RECORDS = true;
    private static final String TYPES_FILE = "types.bal";
    private static final String SERVICE_FILE = "service.bal";
    private static final String SCHEMA_EXTENSION = ".graphql";

    @DataProvider(name = "typesFiles")
    public Object[][] typesFiles() {
        return new Object[][]{
                {"SchemaWithSingleObjectApi", SERVICE_CLASSES, "typesWithSingleObjectDefault.bal"},
                {"SchemaWithMultipleObjectsApi", SERVICE_CLASSES, "typesWithMultipleObjectsDefault.bal"},
                {"SchemaWithMutationApi", SERVICE_CLASSES, "typesWithMutationDefault.bal"},
                {"SchemaWithSubscriptionApi", SERVICE_CLASSES, "typesWithSubscriptionDefault.bal"},
                {"SchemaWithListsApi", SERVICE_CLASSES, "typesWithListsDefault.bal"},
                {"SchemaWithInputsApi", SERVICE_CLASSES, "typesWithInputsDefault.bal"},
                {"SchemaWithObjectTakingInputArgumentApi", RECORDS,
                        "typesWithObjectTakingInputArgumentRecordsAllowed.bal"},
                {"SchemaWithInterfaceApi", RECORDS, "typesWithInterfaceRecordsAllowed.bal"},
                {"SchemaWithUnionApi", RECORDS, "typesWithUnionRecordsAllowed.bal"},
                {"SchemaWithEnumApi", SERVICE_CLASSES, "typesWithEnumDefault.bal"},
                {"SchemaWithUnionApi", SERVICE_CLASSES, "typesWithUnionDefault.bal"},
                {"SchemaWithInterfaceApi", SERVICE_CLASSES, "typesWithInterfaceDefault.bal"},
                {"SchemaWithMultipleInterfacesApi", SERVICE_CLASSES, "typesWithMultipleInterfacesDefault.bal"},
                {"SchemaWithInterfacesImplementingInterfacesApi", SERVICE_CLASSES,
                        "typesWithInterfacesImplementingInterfacesDefault.bal"},
                {"SchemaWithMultiDimensionalListsApi", SERVICE_CLASSES, "typesWithMultiDimensionalListsDefault.bal"},
                {"SchemaWithDefaultParameters01Api", SERVICE_CLASSES, "typesWithDefaultParameters01Default.bal"},
                {"SchemaWithDefaultParameters02Api", SERVICE_CLASSES, "typesWithDefaultParameters02Default.bal"},
                {"SchemaWithDefaultParameters03Api", SERVICE_CLASSES, "typesWithDefaultParameters03Default.bal"},
                {"SchemaWithDefaultParameters04Api", SERVICE_CLASSES, "typesWithDefaultParameters04Default.bal"},
                {"SchemaDocsWithQueryResolversApi", SERVICE_CLASSES, "typesDocsWithQueryResolversDefault.bal"},
                {"SchemaDocsWithResolverArgumentsApi", SERVICE_CLASSES, "typesDocsWithResolverArgumentsDefault.bal"},
                {"SchemaDocsWithMultipleLinesApi", SERVICE_CLASSES, "typesDocsWithMultipleLinesDefault.bal"},
                {"SchemaDocsWithObjectsApi", SERVICE_CLASSES, "typesDocsWithObjectsDefault.bal"},
                {"SchemaDocsWithObjectsApi", RECORDS, "typesDocsWithObjectsRecordsAllowed.bal"},
                {"SchemaDocsWithUnionApi", SERVICE_CLASSES, "typesDocsWithUnionDefault.bal"},
                {"SchemaDocsWithEnumApi", SERVICE_CLASSES, "typesDocsWithEnumDefault.bal"},
                {"SchemaDocsWithInputsApi", SERVICE_CLASSES, "typesDocsWithInputsDefault.bal"},
                {"SchemaDocsWithInterfacesApi", SERVICE_CLASSES, "typesDocsWithInterfacesDefault.bal"},
                {"SchemaDocsWithDeprecated01Api", SERVICE_CLASSES, "typesDocsWithDeprecated01Default.bal"},
                {"SchemaDocsWithDeprecated02Api", SERVICE_CLASSES, "typesDocsWithDeprecated02Default.bal"},
                {"SchemaDocsWithDeprecated03Api", SERVICE_CLASSES, "typesDocsWithDeprecated03Default.bal"},
                {"SchemaDocsWithDeprecated01Api", RECORDS, "typesDocsWithDeprecated01RecordsAllowed.bal"},
                {"SchemaWithFileUploadApi", SERVICE_CLASSES, "typesWithFileUploadDefault.bal"},
                {"SchemaWithInputTypeDefaultParametersApi", SERVICE_CLASSES,
                        "typesWithInputTypeDefaultParameters.bal"},
                {"SchemaWithIDTypeApi", RECORDS, "typesWithIDTypeRecordsAllowed.bal"}
        };
    }

    @Test(description = "Test the generated types file", dataProvider = "typesFiles")
    public void testGenerateTypes(String schemaName, boolean useRecordsForObjects, String expectedFile)
            throws ValidationException, IOException, ServiceGenerationException {
        Assert.assertEquals(generate(schemaName, useRecordsForObjects, TYPES_FILE), expected(expectedFile));
    }

    @DataProvider(name = "serviceFiles")
    public Object[][] serviceFiles() {
        return new Object[][]{
                {"SchemaWithSingleObjectApi", "serviceForSchemaWithSingleObject.bal"},
                {"SchemaDocsWithMutationAndSubscriptionResolversApi",
                        "serviceForSchemaDocsWithMutationAndSubscriptionResolvers.bal"},
                {"SchemaWithIDTypeApi", "serviceForSchemaWithIDTypeApi.bal"}
        };
    }

    @Test(description = "Test the generated service file", dataProvider = "serviceFiles")
    public void testGenerateService(String schemaName, String expectedFile)
            throws ValidationException, IOException, ServiceGenerationException {
        Assert.assertEquals(generate(schemaName, SERVICE_CLASSES, SERVICE_FILE), expected(expectedFile));
    }

    @DataProvider(name = "invalidSchemas")
    public Object[][] invalidSchemas() {
        return new Object[][]{
                {"InvalidSchemaWithUnionMemberTypeOfEnum", "The member types of a Union type must all be Object " +
                        "base types. member type Gender in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInterface", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfUnion", "The member types of a Union type must all be Object " +
                        "base types. member type Internal in Union Profile is invalid."},
                {"InvalidSchemaWithUnionMemberTypeOfInputType", "The member types of a Union type must all be " +
                        "Object base types. member type Info in Union PersonInfo is invalid."}
        };
    }

    @Test(description = "Test that an invalid schema is rejected before generation", dataProvider = "invalidSchemas")
    public void testInvalidSchema(String schemaName, String expectedMessage) {
        ValidationException error = Assert.expectThrows(ValidationException.class,
                () -> project("invalid", schemaName));
        Assert.assertTrue(error.getMessage().contains(expectedMessage), error.getMessage());
    }

    private String generate(String schemaName, boolean useRecordsForObjects, String fileName)
            throws ValidationException, IOException, ServiceGenerationException {
        List<SrcFilePojo> sources = new ServiceCodeGenerator(useRecordsForObjects)
                .generateBalSources(project("valid", schemaName));
        return sources.stream()
                .filter(source -> source.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No generated file named " + fileName))
                .getContent();
    }

    private GraphqlServiceProject project(String folder, String schemaName) throws ValidationException, IOException {
        Path schemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", folder, schemaName + SCHEMA_EXTENSION));
        return TestUtils.getValidatedMockServiceProject(schemaPath.toString(), this.tmpDir);
    }

    private String expected(String expectedFile) throws IOException {
        return Files.readString(this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile)));
    }
}
