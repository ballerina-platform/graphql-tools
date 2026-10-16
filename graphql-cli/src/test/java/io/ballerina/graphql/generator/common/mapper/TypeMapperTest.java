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

package io.ballerina.graphql.generator.common.mapper;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import io.ballerina.graphql.generator.common.model.TypeModel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests mapping GraphQL types to Ballerina types.
 */
public class TypeMapperTest {

    private static final boolean NULLABLE = true;
    private static final boolean NON_NULL = false;
    private static final boolean ID = true;

    private static final String SCHEMA = """
            scalar Upload
            scalar DateTime

            type Query {
                count: Int!
                price: Float
                book: Book
                books: [Book!]
                grid: [[Int]!]
                ids: [ID!]!
                file: Upload!
                created: DateTime
                entry: map
            }

            type Book {
                id: ID!
            }

            type map {
                id: ID!
            }
            """;
    private static final GraphQLObjectType QUERY = UnExecutableSchemaGenerator
            .makeUnExecutableSchema(new SchemaParser().parse(SCHEMA))
            .getQueryType();

    @DataProvider(name = "fieldTypes")
    public Object[][] fieldTypes() {
        return new Object[][]{
                {"count", TypeModel.named("int", NON_NULL)},
                {"price", TypeModel.named("float", NULLABLE)},
                {"book", TypeModel.named("Book", NULLABLE)},
                {"books", TypeModel.list(TypeModel.named("Book", NON_NULL), NULLABLE)},
                {"grid", TypeModel.list(TypeModel.list(TypeModel.named("int", NULLABLE), NON_NULL), NULLABLE)},
                {"ids", TypeModel.list(TypeModel.named("string", NON_NULL, ID), NON_NULL)},
                {"file", TypeModel.named("graphql:Upload", NON_NULL)},
                {"entry", TypeModel.named("'map", NULLABLE)}
        };
    }

    @Test(description = "Test mapping GraphQL field types to Ballerina types", dataProvider = "fieldTypes")
    public void testMap(String fieldName, TypeModel expectedType) {
        Assert.assertEquals(TypeMapper.map(fieldType(fieldName)), expectedType);
    }

    @DataProvider(name = "names")
    public Object[][] names() {
        return new Object[][]{
                {"type", "'type"},
                {"map", "'map"},
                {"error", "'error"},
                {"book", "book"},
                {"graphql", "graphql"}
        };
    }

    @Test(description = "Test that only Ballerina keywords are quoted", dataProvider = "names")
    public void testEscapeName(String name, String expectedName) {
        Assert.assertEquals(TypeMapper.escapeName(name), expectedName);
    }

    @Test(description = "Test that a custom scalar is rejected")
    public void testMapCustomScalar() {
        IllegalArgumentException error = Assert.expectThrows(IllegalArgumentException.class,
                () -> TypeMapper.map(fieldType("created")));
        Assert.assertTrue(error.getMessage().contains("DateTime"), error.getMessage());
    }

    private static GraphQLType fieldType(String fieldName) {
        return QUERY.getFieldDefinition(fieldName).getType();
    }
}
