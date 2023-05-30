package io.ballerina.graphql.generator.gateway.common;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.GatewayTestUtils;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.FieldData;
import io.ballerina.graphql.generator.gateway.generator.common.SchemaTypes;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Test class for SchemaTypes.
 * */
public class SchemaTypesTest extends GraphqlTest {
    @Test(description = "Test schema types on a given graphql schema", dataProvider = "SchemaTypesDataProvider")
    public void testSchemaTypes(String graphQLSchemaFileName, String[] typeNames)
            throws GatewayGenerationException, ValidationException, IOException {
        GraphQLSchema graphQLSchema = GatewayTestUtils.getGatewayProject(graphQLSchemaFileName, tmpDir)
                .getGraphqlSchema();
        SchemaTypes schemaTypes = new SchemaTypes(graphQLSchema);
        for (String typeName : typeNames) {
            Assert.assertNotNull(schemaTypes.getFieldsOfType(typeName));
        }
    }

    @DataProvider(name = "SchemaTypesDataProvider")
    public Object[][] schemaTypesDataProvider() {
        return new Object[][] {
                {"SupergraphWithTwoEntities", new String[] {"Astronaut", "Mission"}},
                {"SupergraphWithIDTypeFields", new String[] {"Astronaut", "Mission"}},
                {"SupergraphWithThreeEntities", new String[] {"Product", "Category", "Review"}}
        };
    }

    @Test(description = "Test field names of a given type on a graphql schema", dataProvider = "FieldNameProvider")
    public void testFieldData(String graphQLSchemaFileName, String typeName, Map<String, Object[]> fieldData)
            throws GatewayGenerationException, ValidationException, IOException {
        GraphQLSchema graphQLSchema = GatewayTestUtils.getGatewayProject(graphQLSchemaFileName, tmpDir)
                .getGraphqlSchema();
        SchemaTypes schemaTypes = new SchemaTypes(graphQLSchema);
        List<FieldData> fieldDataList = schemaTypes.getFieldsOfType(typeName);
        for (FieldData data : fieldDataList) {
            Object[] expected = fieldData.get(data.getFieldName());
            Assert.assertEquals(data.getType(), expected[0]);
            Assert.assertEquals(data.getClient(), expected[1]);
            Assert.assertEquals(data.isID(), expected[2]);
        }
    }

    @DataProvider(name = "FieldNameProvider")
    public Object[][] getFieldData() {
        return new Object[][] {
                {"SupergraphWithTwoEntities", "Astronaut", Map.ofEntries(
                        Map.entry("id", new Object[] {"ID", "ASTRONAUTS", true}),
                        Map.entry("name", new Object[] {"String", "ASTRONAUTS", false}),
                        Map.entry("missions", new Object[] {"Mission", "MISSIONS", false})
                )},
                {"SupergraphWithTwoEntities", "Mission", Map.ofEntries(
                        Map.entry("id", new Object[] {"Int", "MISSIONS", false}),
                        Map.entry("designation", new Object[] {"String", "MISSIONS", false}),
                        Map.entry("startDate", new Object[] {"String", "MISSIONS", false}),
                        Map.entry("endDate", new Object[] {"String", "MISSIONS", false}),
                        Map.entry("crew", new Object[] {"Astronaut", "MISSIONS", false})
                )}
        };
    }
}
