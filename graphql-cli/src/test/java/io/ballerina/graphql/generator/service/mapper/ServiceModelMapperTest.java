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

package io.ballerina.graphql.generator.service.mapper;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.EnumValueModel;
import io.ballerina.graphql.generator.common.model.FieldModel;
import io.ballerina.graphql.generator.common.model.InputTypeModel;
import io.ballerina.graphql.generator.common.model.ListType;
import io.ballerina.graphql.generator.common.model.NamedType;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.model.ParameterModel;
import io.ballerina.graphql.generator.service.model.ResolverKind;
import io.ballerina.graphql.generator.service.model.ResolverModel;
import io.ballerina.graphql.generator.service.model.ServiceModel;
import io.ballerina.graphql.generator.service.model.TypeDefinitionModel;
import io.ballerina.graphql.generator.service.model.TypeKind;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests building the service model from a GraphQL schema.
 */
public class ServiceModelMapperTest {

    private static final String SERVICE_NAME = "BookApi";
    private static final boolean SERVICE_CLASSES = false;
    private static final boolean RECORDS = true;

    @Test(description = "Test that Query, Mutation and Subscription fields become the matching resolver kinds")
    public void testRootResolvers() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { book(id: Int!): Book }
                type Mutation { addBook(title: String!): Book! }
                type Subscription { bookAdded: Book! }
                type Book { title: String! }
                """, SERVICE_CLASSES);

        Assert.assertEquals(model.serviceName(), SERVICE_NAME);
        Assert.assertEquals(model.queries(), List.of(new ResolverModel(ResolverKind.RESOURCE_GET, "book",
                List.of(new ParameterModel("id", new NamedType("int", false), null, null)),
                new NamedType("Book", true), null, null)));
        Assert.assertEquals(model.mutations().get(0).kind(), ResolverKind.REMOTE);
        Assert.assertEquals(model.subscriptions().get(0).kind(), ResolverKind.RESOURCE_SUBSCRIBE);
    }

    @Test(description = "Test that root and introspection types are not generated as type definitions")
    public void testRootAndIntrospectionTypesSkipped() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { book: Book }
                type Book { title: String! }
                """, SERVICE_CLASSES);

        Assert.assertEquals(definitionNames(model), List.of("Book"));
    }

    @Test(description = "Test that object types are generated as service classes by default")
    public void testObjectTypeAsServiceClass() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { book: Book }
                type Book { title: String! }
                """, SERVICE_CLASSES);

        Assert.assertEquals(definition(model, "Book").kind(), TypeKind.SERVICE_CLASS);
    }

    @Test(description = "Test that object types are generated as records only where a record can represent them")
    public void testObjectTypeAsRecord() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { book: Book, author: Author, result: Result, item: Item }
                type Book { title: String! }
                type Author { books(limit: Int): [Book!]! }
                union Result = Book | Magazine
                type Magazine { issue: Int! }
                interface Named { name: String! }
                type Item implements Named { name: String! }
                """, RECORDS);

        Assert.assertEquals(definition(model, "Book").kind(), TypeKind.SERVICE_CLASS,
                "A union member must be a service class");
        Assert.assertEquals(definition(model, "Author").kind(), TypeKind.SERVICE_CLASS,
                "A type with field arguments must be a service class");
        Assert.assertEquals(definition(model, "Item").kind(), TypeKind.SERVICE_CLASS,
                "A type implementing an interface must be a service class");

        ServiceModel recordModel = map("""
                type Query { book: Book }
                type Book { title: String! }
                """, RECORDS);
        Assert.assertEquals(definition(recordModel, "Book").kind(), TypeKind.RECORD);
    }

    @Test(description = "Test that interfaces and unions keep their related types")
    public void testInterfaceAndUnion() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { named: Named, result: Result }
                interface Node { id: ID! }
                interface Named implements Node { id: ID!, name: String! }
                type Book implements Named & Node { id: ID!, name: String! }
                type Magazine { issue: Int! }
                union Result = Book | Magazine
                """, SERVICE_CLASSES);

        TypeDefinitionModel named = definition(model, "Named");
        Assert.assertEquals(named.kind(), TypeKind.INTERFACE);
        Assert.assertEquals(named.relatedTypes(), List.of("Node"));

        TypeDefinitionModel result = definition(model, "Result");
        Assert.assertEquals(result.kind(), TypeKind.UNION);
        Assert.assertEquals(result.relatedTypes(), List.of("Book", "Magazine"));
        Assert.assertTrue(result.fields().isEmpty());
    }

    @Test(description = "Test that only the interfaces and fields not already inherited are kept")
    public void testInheritedInterfacesAndFieldsSkipped() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { named: Named }
                interface Node { id: ID! }
                interface Named implements Node { id: ID!, name: String! }
                type Book implements Named & Node { id: ID!, name: String! }
                """, SERVICE_CLASSES);

        TypeDefinitionModel named = definition(model, "Named");
        Assert.assertEquals(named.fields().stream().map(ResolverModel::name).toList(), List.of("name"));

        TypeDefinitionModel book = definition(model, "Book");
        Assert.assertEquals(book.relatedTypes(), List.of("Named"));
        Assert.assertEquals(book.fields().stream().map(ResolverModel::name).toList(), List.of("id", "name"));
    }

    @Test(description = "Test that input types, enums and default values are mapped")
    public void testInputTypesEnumsAndDefaults() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { books(filter: Filter = {genre: FICTION, tags: ["new"]}, limit: Int = 10): [String!]! }
                input Filter { genre: Genre = FICTION, tags: [String!], minRating: Float = 4.5 }
                enum Genre { FICTION, HISTORY }
                """, SERVICE_CLASSES);

        Assert.assertEquals(model.queries().get(0).parameters().stream().map(ParameterModel::defaultValue).toList(),
                List.of("{genre: FICTION, tags: [\"new\"]}", "10"));
        Assert.assertEquals(model.inputTypes(), List.of(new InputTypeModel("Filter", List.of(
                new FieldModel("genre", new NamedType("Genre", true), "FICTION", null, null),
                new FieldModel("tags", new ListType(new NamedType("string", false), true), null, null, null),
                new FieldModel("minRating", new NamedType("float", true), "4.5", null, null)), null)));
        Assert.assertEquals(model.enums(), List.of(new EnumModel("Genre",
                List.of(new EnumValueModel("FICTION", null, null), new EnumValueModel("HISTORY", null, null)),
                null)));
    }

    @Test(description = "Test that names that are Ballerina keywords are escaped")
    public void testKeywordNamesEscaped() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { type(map: Int): String }
                """, SERVICE_CLASSES);

        ResolverModel resolver = model.queries().get(0);
        Assert.assertEquals(resolver.name(), "'type");
        Assert.assertEquals(resolver.parameters().get(0).name(), "'map");
    }

    @Test(description = "Test that descriptions and deprecation reasons are kept")
    public void testDescriptionsAndDeprecation() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query {
                    "The book with the given ID"
                    book("The book ID" id: Int!): String
                    oldBook: String @deprecated(reason: "Use book")
                }
                """, SERVICE_CLASSES);

        ResolverModel book = model.queries().get(0);
        Assert.assertEquals(book.description(), "The book with the given ID");
        Assert.assertEquals(book.parameters().get(0).description(), "The book ID");
        Assert.assertNull(book.deprecationReason());
        Assert.assertEquals(model.queries().get(1).deprecationReason(), "Use book");
    }

    @Test(description = "Test that a custom scalar is rejected with its name")
    public void testCustomScalarRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                scalar DateTime
                type Query { now: DateTime }
                """, SERVICE_CLASSES));
        Assert.assertTrue(error.getMessage().contains("DateTime"), error.getMessage());
    }

    private static ServiceModel map(String sdl, boolean useRecordsForObjects) throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return ServiceModelMapper.map(schema, SERVICE_NAME, useRecordsForObjects);
    }

    private static TypeDefinitionModel definition(ServiceModel model, String name) {
        return model.typeDefinitions().stream()
                .filter(definition -> definition.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No type definition named " + name));
    }

    private static List<String> definitionNames(ServiceModel model) {
        return model.typeDefinitions().stream().map(TypeDefinitionModel::name).toList();
    }
}
