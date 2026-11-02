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

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
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

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Tests the types and service files generated for the GraphQL schemas in the test resources.
 */
public class ServiceCodeGeneratorTest {

    private static final Path RESOURCE_DIR = Paths.get("src/test/resources").toAbsolutePath();
    private static final String OUTPUT_PATH = "build";
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
                {"SchemaDocsWithMutationAndSubscriptionResolversApi", SERVICE_CLASSES,
                        "typesDocsWithMutationAndSubscriptionResolversDefault.bal"},
                {"SchemaDocsWithResolverMultipleLinesApi", SERVICE_CLASSES,
                        "typesDocsWithResolverMultipleLinesDefault.bal"},
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
            throws IOException, ServiceGenerationException {
        Assert.assertEquals(generate(schemaName, useRecordsForObjects, TYPES_FILE), expected(expectedFile));
    }

    @DataProvider(name = "serviceFiles")
    public Object[][] serviceFiles() {
        return new Object[][]{
                {"SchemaWithSingleObjectApi", "serviceForSchemaWithSingleObject.bal"},
                {"SchemaDocsWithMutationAndSubscriptionResolversApi",
                        "serviceForSchemaDocsWithMutationAndSubscriptionResolvers.bal"},
                {"SchemaWithIDTypeApi", "serviceForSchemaWithIDTypeApi.bal"},
                {"SchemaWithObjectTakingInputArgumentApi", "serviceForSchemaWithObjectTakingInputArgument.bal"}
        };
    }

    @Test(description = "Test the generated service file", dataProvider = "serviceFiles")
    public void testGenerateService(String schemaName, String expectedFile)
            throws IOException, ServiceGenerationException {
        Assert.assertEquals(generate(schemaName, SERVICE_CLASSES, SERVICE_FILE), expected(expectedFile));
    }

    @Test(description = "Test that a schema file name that is not a valid identifier gives a PascalCase service name")
    public void testGenerateWithHyphenatedFileName() throws IOException, ServiceGenerationException {
        String types = generate("SchemaWith-HyphenApi", SERVICE_CLASSES, TYPES_FILE);
        String service = generate("SchemaWith-HyphenApi", SERVICE_CLASSES, SERVICE_FILE);

        Assert.assertTrue(types.contains("type SchemaWithHyphenApiService service object {"), types);
        Assert.assertTrue(service.contains("service SchemaWithHyphenApiService on new graphql:Listener(port) {"),
                service);
    }

    @Test(description = "Test that only the last extension is removed from a schema file name that has dots")
    public void testGenerateWithDottedFileName() throws ServiceGenerationException {
        GraphqlServiceProject project = new GraphqlServiceProject(ROOT_PROJECT_NAME,
                Paths.get("schemas", "books.v2.graphql").toString(), OUTPUT_PATH);
        project.setGraphQLSchema(UnExecutableSchemaGenerator.makeUnExecutableSchema(
                new SchemaParser().parse("type Query { book: String }")));

        String types = content(new ServiceCodeGenerator().generateBalSources(project), TYPES_FILE);

        Assert.assertTrue(types.contains("type BooksV2Service service object {"), types);
    }

    @Test(description = "Test that non-ASCII letters in a schema file name are kept in the service name")
    public void testGenerateWithUnicodeFileName() throws ServiceGenerationException {
        GraphqlServiceProject project = new GraphqlServiceProject(ROOT_PROJECT_NAME,
                Paths.get("schemas", "café-api.graphql").toString(), OUTPUT_PATH);
        project.setGraphQLSchema(UnExecutableSchemaGenerator.makeUnExecutableSchema(
                new SchemaParser().parse("type Query { book: String }")));

        List<SrcFilePojo> sources = new ServiceCodeGenerator().generateBalSources(project);

        Assert.assertTrue(content(sources, TYPES_FILE).contains("type CaféApiService service object {"),
                content(sources, TYPES_FILE));
        Assert.assertTrue(content(sources, SERVICE_FILE).contains(
                "service CaféApiService on new graphql:Listener(port) {"), content(sources, SERVICE_FILE));
    }

    private String generate(String schemaName, boolean useRecordsForObjects, String fileName)
            throws IOException, ServiceGenerationException {
        return content(new ServiceCodeGenerator(useRecordsForObjects).generateBalSources(project(schemaName)),
                fileName);
    }

    private static String content(List<SrcFilePojo> sources, String fileName) {
        return sources.stream()
                .filter(source -> source.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No generated file named " + fileName))
                .getContent();
    }

    private static GraphqlServiceProject project(String schemaName) throws IOException {
        Path schemaPath = RESOURCE_DIR.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", schemaName + SCHEMA_EXTENSION));
        GraphqlServiceProject project =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, schemaPath.toString(), OUTPUT_PATH);
        project.setGraphQLSchema(UnExecutableSchemaGenerator.makeUnExecutableSchema(
                new SchemaParser().parse(Files.readString(schemaPath))));
        return project;
    }

    private static String expected(String expectedFile) throws IOException {
        return Files.readString(RESOURCE_DIR.resolve(Paths.get("serviceGen", "expectedServices", expectedFile)));
    }
}
