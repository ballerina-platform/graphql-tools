package io.ballerina.graphql.generator.gateway.common;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.gateway.TestUtils;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.FieldData;
import io.ballerina.graphql.generator.gateway.generator.common.SchemaTypes;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class SchemaTypesTest extends GraphqlTest {
    @Test(description = "Test schema types on a given graphql schema", dataProvider = "SchemaTypesDataProvider")
    public void testSchemaTypes(String graphQLSchemaFileName, String[] typeNames)
            throws GatewayGenerationException, ValidationException, IOException {
        GraphQLSchema graphQLSchema = TestUtils.getGatewayProject(graphQLSchemaFileName, tmpDir).getGraphQLSchema();
        SchemaTypes schemaTypes = new SchemaTypes(graphQLSchema);
        for (String typeName : typeNames) {
            Assert.assertNotNull(schemaTypes.getFieldsOfType(typeName));
        }
    }

    @DataProvider(name = "SchemaTypesDataProvider")
    public Object[][] schemaTypesDataProvider() {
        return new Object[][] {
                {"Supergraph", new String[] {"Astronaut", "Mission"}},
                {"Supergraph01", new String[] {"Astronaut", "Mission"}},
                {"Supergraph02", new String[] {"Product", "User", "ProductDimension", "Review"}},
                {"Supergraph03", new String[] {"Product", "Category", "Review"}}
        };
    }

    @Test(description = "Test field names of a given type on a graphql schema", dataProvider = "FieldNameProvider")
    public void testFieldData(String graphQLSchemaFileName, String typeName, Map<String, Object[]> fieldData)
            throws GatewayGenerationException, ValidationException, IOException {
        GraphQLSchema graphQLSchema = TestUtils.getGatewayProject(graphQLSchemaFileName, tmpDir).getGraphQLSchema();
        SchemaTypes schemaTypes = new SchemaTypes(graphQLSchema);
        List<FieldData> fieldDataList = schemaTypes.getFieldsOfType(typeName);
        for (FieldData data : fieldDataList) {
            Object[] expected = fieldData.get(data.getFieldName());
            Assert.assertEquals(data.getType(), expected[0]);
            Assert.assertEquals(data.getClient(), expected[1]);
            Assert.assertEquals(data.getRequires(), expected[2]);
            Assert.assertEquals(data.isID(), expected[3]);
        }
    }

    @DataProvider(name = "FieldNameProvider")
    public Object[][] getFieldData() {
        return new Object[][] {
                {"Supergraph", "Astronaut", Map.ofEntries(
                        Map.entry("id", new Object[] {"ID", "ASTRONAUTS", null, true}),
                        Map.entry("name", new Object[] {"String", "ASTRONAUTS", null, false}),
                        Map.entry("missions", new Object[] {"Mission", "MISSIONS", null, false})
                )},
                {"Supergraph", "Mission", Map.ofEntries(
                        Map.entry("id", new Object[] {"Int", "MISSIONS", null, false}),
                        Map.entry("designation", new Object[] {"String", "MISSIONS", null, false}),
                        Map.entry("startDate", new Object[] {"String", "MISSIONS", null, false}),
                        Map.entry("endDate", new Object[] {"String", "MISSIONS", null, false}),
                        Map.entry("crew", new Object[] {"Astronaut", "MISSIONS", null, false})
                )},
                {"Supergraph02", "Product", Map.ofEntries(
                        Map.entry("upc", new Object[] {"String", "PRODUCTS", null, true}),
                        Map.entry("name", new Object[] {"String", "PRODUCTS", null, false}),
                        Map.entry("price", new Object[] {"Int", "PRODUCTS", null, false}),
                        Map.entry("weight", new Object[] {"Int", "PRODUCTS", null, false}),
                        Map.entry("dimensions", new Object[] {"ProductDimension", "PRODUCTS", null, false}),
                        Map.entry("reviews", new Object[] {"Review", "REVIEWS", null, false}),
                        Map.entry("shippingEstimate", new Object[] {"Float", "INVENTORY",
                                Map.ofEntries(Map.entry("PRODUCTS", "price weight dimensions {length width}")),
                                false}),
                        Map.entry("inStock", new Object[] {"Boolean", "INVENTORY", null, false})
                )}
        };
    }
}
