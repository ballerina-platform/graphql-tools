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
import io.ballerina.graphql.generator.common.model.BalType;
import io.ballerina.graphql.generator.common.model.ListType;
import io.ballerina.graphql.generator.common.model.NamedType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests mapping GraphQL types to Ballerina types.
 */
public class TypeMapperTest {

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
                {"count", new NamedType("int", false)},
                {"price", new NamedType("float", true)},
                {"book", new NamedType("Book", true)},
                {"books", new ListType(new NamedType("Book", false), true)},
                {"grid", new ListType(new ListType(new NamedType("int", true), false), true)},
                {"ids", new ListType(new NamedType("string", false, true), false)},
                {"file", new NamedType("graphql:Upload", false)},
                {"entry", new NamedType("'map", true)}
        };
    }

    @Test(description = "Test mapping GraphQL field types to Ballerina types", dataProvider = "fieldTypes")
    public void testMap(String fieldName, BalType expectedType) {
        Assert.assertEquals(TypeMapper.map(fieldType(fieldName)), expectedType);
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
