/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.generator.service.mapper;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.EnumValueModel;
import io.ballerina.graphql.generator.common.model.FieldModel;
import io.ballerina.graphql.generator.common.model.InputTypeModel;
import io.ballerina.graphql.generator.common.model.TypeModel;
import io.ballerina.graphql.generator.service.ServiceBaseName;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.model.DataLoaderModel;
import io.ballerina.graphql.generator.service.model.ParameterModel;
import io.ballerina.graphql.generator.service.model.ResolverKind;
import io.ballerina.graphql.generator.service.model.ResolverModel;
import io.ballerina.graphql.generator.service.model.ServiceModel;
import io.ballerina.graphql.generator.service.model.TypeDefinitionModel;
import io.ballerina.graphql.generator.service.model.TypeKind;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Tests building the service model from a GraphQL schema.
 */
public class ServiceModelMapperTest {

    private static final ServiceBaseName BASE_NAME =
            new ServiceBaseName("BookApi", ServiceBaseName.Source.SCHEMA_FILE);
    private static final String SERVICE_NAME = "BookApiService";
    private static final String BOOK_QUERY = "type Query { book: String }";
    private static final boolean SERVICE_CLASSES = false;
    private static final boolean RECORDS = true;
    private static final boolean NULLABLE = true;
    private static final boolean NON_NULL = false;
    private static final boolean ID = true;
    private static final boolean LOADS_VALUE = true;
    private static final boolean NO_LOADED_VALUE = false;
    private static final String NO_DEFAULT = null;
    private static final String NO_DESCRIPTION = null;
    private static final String NOT_DEPRECATED = null;

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
                List.of(new ParameterModel("id", TypeModel.named("int", NON_NULL), NO_DEFAULT, NO_DESCRIPTION)),
                TypeModel.named("Book", NULLABLE), NO_DESCRIPTION, NOT_DEPRECATED)));
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

    @Test(description = "Test that interfaces and unions keep their related types, skipping inherited ones")
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
        Assert.assertEquals(named.fields().stream().map(ResolverModel::name).toList(), List.of("name"));

        TypeDefinitionModel book = definition(model, "Book");
        Assert.assertEquals(book.relatedTypes(), List.of("Named"));
        Assert.assertEquals(book.fields().stream().map(ResolverModel::name).toList(), List.of("id", "name"));

        TypeDefinitionModel result = definition(model, "Result");
        Assert.assertEquals(result.kind(), TypeKind.UNION);
        Assert.assertEquals(result.relatedTypes(), List.of("Book", "Magazine"));
        Assert.assertTrue(result.fields().isEmpty());
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
                new FieldModel("genre", TypeModel.named("Genre", NULLABLE), "FICTION", NO_DESCRIPTION, NOT_DEPRECATED),
                new FieldModel("tags", TypeModel.list(TypeModel.named("string", NON_NULL), NULLABLE), NO_DEFAULT,
                        NO_DESCRIPTION, NOT_DEPRECATED),
                new FieldModel("minRating", TypeModel.named("float", NULLABLE), "4.5", NO_DESCRIPTION, NOT_DEPRECATED)),
                NO_DESCRIPTION)));
        Assert.assertEquals(model.enums(), List.of(new EnumModel("Genre",
                List.of(new EnumValueModel("FICTION", NO_DESCRIPTION, NOT_DEPRECATED),
                        new EnumValueModel("HISTORY", NO_DESCRIPTION, NOT_DEPRECATED)),
                NO_DESCRIPTION)));
    }

    @Test(description = "Test that a null default value is kept apart from no default value")
    public void testNilDefaultValue() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { book(title: String = null, author: String): String }
                input Filter { title: String = null, author: String }
                """, SERVICE_CLASSES);

        Assert.assertEquals(model.queries().get(0).parameters().stream().map(ParameterModel::defaultValue).toList(),
                Arrays.asList("()", NO_DEFAULT));
        Assert.assertEquals(model.inputTypes().get(0).fields().stream().map(FieldModel::defaultValue).toList(),
                Arrays.asList("()", NO_DEFAULT));
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

    @Test(description = "Test that a type implementing two interfaces with the same field is rejected")
    public void testSharedInterfaceFieldRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { book: Book }
                interface Named { name: String! }
                interface Labeled { name: String!, label: String! }
                type Book implements Named & Labeled { name: String!, label: String! }
                """, SERVICE_CLASSES));
        Assert.assertTrue(error.getMessage().contains(
                "Type \"Book\" cannot implement \"Named\" and \"Labeled\" as both have the field \"name\". "
                        + "Do not implement multiple interfaces that have the same field."),
                error.getMessage());
    }

    @Test(description = "Test that a type implementing two interfaces with a shared parent interface is rejected")
    public void testSharedParentInterfaceFieldRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { item: Item }
                interface Base { id: Int! }
                interface A implements Base { id: Int!, a: String! }
                interface B implements Base { id: Int!, b: String! }
                type Item implements A & B & Base { id: Int!, a: String!, b: String! }
                """, SERVICE_CLASSES));
        Assert.assertTrue(error.getMessage().contains(
                "Type \"Item\" cannot implement \"A\" and \"B\" as both inherit the field \"id\" from \"Base\". "
                        + "Do not implement multiple interfaces that have the same field."),
                error.getMessage());
    }

    @Test(description = "Test that an interface implementing two interfaces with the same field is rejected")
    public void testInterfaceWithSharedInterfaceFieldRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { item: Item }
                interface Named { name: String! }
                interface Labeled { name: String!, label: String! }
                interface Tagged implements Named & Labeled { name: String!, label: String! }
                type Item implements Tagged & Named & Labeled { name: String!, label: String! }
                """, SERVICE_CLASSES));
        Assert.assertTrue(error.getMessage().contains(
                "Type \"Tagged\" cannot implement \"Named\" and \"Labeled\" as both have the field \"name\". "
                        + "Do not implement multiple interfaces that have the same field."),
                error.getMessage());
    }

    @Test(description = "Test that configured ID types are used for fields, arguments and input fields")
    public void testIdTypes() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { profile(id: ID!): Profile, getFloatId: ID! }
                type Mutation { updateProfile(input: ProfileInput!): Profile }
                type Profile { id: ID!, name: String!, friendIds: [ID!]! }
                input ProfileInput { id: ID! }
                """, Map.of("Profile.id", "int", "Query.profile.id", "int", "Query.getFloatId", "float",
                "ProfileInput.id", "decimal"));

        Assert.assertEquals(resolver(model.queries(), "profile").parameters().get(0).type(),
                TypeModel.named("int", NON_NULL, ID));
        Assert.assertEquals(resolver(model.queries(), "getFloatId").returnType(),
                TypeModel.named("float", NON_NULL, ID));
        Assert.assertEquals(resolver(definition(model, "Profile").fields(), "id").returnType(),
                TypeModel.named("int", NON_NULL, ID));
        Assert.assertEquals(resolver(definition(model, "Profile").fields(), "friendIds").returnType(),
                TypeModel.list(TypeModel.named("string", NON_NULL, ID), NON_NULL), "An unmapped ID stays a string");
        Assert.assertEquals(model.inputTypes().get(0).fields().get(0).type(),
                TypeModel.named("decimal", NON_NULL, ID));
    }

    @Test(description = "Test that uuid:Uuid can be configured as an ID type")
    public void testUuidIdType() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { profile: Profile }
                type Profile { id: ID! }
                """, Map.of("Profile.id", "uuid:Uuid"));

        Assert.assertEquals(resolver(definition(model, "Profile").fields(), "id").returnType(),
                TypeModel.named("uuid:Uuid", NON_NULL, ID));
    }

    @Test(description = "Test that a configured ID type applies to every item of an ID list")
    public void testIdTypeForList() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { profile: Profile }
                type Profile { friendIds: [ID!]! }
                """, Map.of("Profile.friendIds", "int"));

        Assert.assertEquals(resolver(definition(model, "Profile").fields(), "friendIds").returnType(),
                TypeModel.list(TypeModel.named("int", NON_NULL, ID), NON_NULL));
    }

    @Test(description = "Test that a type implementing an interface uses the ID type configured on the interface")
    public void testIdTypeFromInterface() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { node: Node }
                interface Node { id: ID! }
                type Profile implements Node { id: ID! }
                """, Map.of("Node.id", "int"));

        Assert.assertEquals(resolver(definition(model, "Node").fields(), "id").returnType(),
                TypeModel.named("int", NON_NULL, ID));
        Assert.assertEquals(resolver(definition(model, "Profile").fields(), "id").returnType(),
                TypeModel.named("int", NON_NULL, ID));
    }

    @Test(description = "Test that every invalid ID type mapping is reported")
    public void testInvalidIdTypesRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { profile(id: ID!): Profile }
                type Profile { id: ID!, name: String! }
                """, Map.of("Profile.nope", "int", "Profile.name", "int", "Profile.id", "long")));

        Assert.assertTrue(error.getMessage().contains(
                "\"Profile.nope\" in [[id-types]] does not match any field or argument in the schema."),
                error.getMessage());
        Assert.assertTrue(error.getMessage().contains(
                "\"Profile.name\" in [[id-types]] is not an ID field or argument."), error.getMessage());
        Assert.assertTrue(error.getMessage().contains(
                "\"long\" is not a supported ID type for \"Profile.id\". Use string, int, float, decimal or "
                        + "uuid:Uuid."),
                error.getMessage());
    }

    @Test(description = "Test that an ID type that differs from the implemented interface is rejected")
    public void testIdTypeDifferentFromInterfaceRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { node: Node }
                interface Node { id: ID! }
                type Profile implements Node { id: ID! }
                """, Map.of("Profile.id", "int")));

        Assert.assertTrue(error.getMessage().contains(
                "\"Profile.id\" in [[id-types]] must use the same type as \"Node.id\" (\"string\")."),
                error.getMessage());
    }

    @Test(description = "Test that a field wired to a DataLoader uses its arguments as the key")
    public void testDataLoaders() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { profile(id: ID!): Profile search(name: String!, age: Int): Profile }
                type Profile { name: String! }
                """, RECORDS, Map.of("Query.profile", "profileLoader", "Query.search", "searchLoader"));

        Assert.assertEquals(resolver(model.queries(), "profile").dataLoader(),
                new DataLoaderModel("profileLoader", "preProfile", List.of("id"), LOADS_VALUE));
        Assert.assertEquals(resolver(model.queries(), "search").dataLoader(),
                new DataLoaderModel("searchLoader", "preSearch", List.of("name", "age"), LOADS_VALUE));
        Assert.assertEquals(model.dataLoaderNames(), List.of("profileLoader", "searchLoader"));
    }

    @Test(description = "Test that a DataLoader field returns no loaded value without a key or a plain data type")
    public void testDataLoadersWithoutLoadedValue() throws ServiceGenerationException {
        ServiceModel model = map("""
                type Query { profile(id: ID!): Profile book(id: ID!): Book }
                type Profile { name: String! }
                type Book { author: Profile! }
                """, SERVICE_CLASSES, Map.of("Query.profile", "profileLoader", "Book.author", "profileLoader"));

        Assert.assertEquals(resolver(model.queries(), "profile").dataLoader(),
                new DataLoaderModel("profileLoader", "preProfile", List.of("id"), NO_LOADED_VALUE));
        Assert.assertEquals(resolver(definition(model, "Book").fields(), "author").dataLoader(),
                new DataLoaderModel("profileLoader", "preAuthor", List.of(), NO_LOADED_VALUE));
        Assert.assertEquals(model.dataLoaderNames(), List.of("profileLoader"));
    }

    @Test(description = "Test that every invalid DataLoader mapping is reported")
    public void testInvalidDataLoadersRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { profile(id: ID!): Profile node: Node }
                type Mutation { addProfile(name: String!): Profile }
                interface Node { id: ID! }
                type Profile implements Node { id: ID! name: String! }
                """, SERVICE_CLASSES, Map.of("Query.nope", "nopeLoader", "Query.profile.id", "idLoader",
                "Mutation.addProfile", "addLoader", "Profile.id", "idLoader", "Profile.name", "type",
                "Query.profile", "profile-loader")));

        Assert.assertTrue(error.getMessage().contains(
                "\"Query.nope\" in [[dataloaders]] does not match any object type field in the schema."),
                error.getMessage());
        Assert.assertTrue(error.getMessage().contains(
                "\"Query.profile.id\" in [[dataloaders]] does not match any object type field in the schema."),
                error.getMessage());
        Assert.assertTrue(error.getMessage().contains("\"Mutation.addProfile\" in [[dataloaders]] cannot use a "
                        + "DataLoader. Only Query fields and object type fields can."), error.getMessage());
        Assert.assertTrue(error.getMessage().contains("\"Profile.id\" in [[dataloaders]] cannot use a DataLoader, as "
                        + "the field comes from the interface \"Node\"."), error.getMessage());
        Assert.assertTrue(error.getMessage().contains(
                "\"type\" in [[dataloaders]] is not a valid Ballerina function name for \"Profile.name\"."),
                error.getMessage());
        Assert.assertTrue(error.getMessage().contains(
                "\"profile-loader\" in [[dataloaders]] is not a valid Ballerina function name for \"Query.profile\"."),
                error.getMessage());
    }

    @Test(description = "Test that a DataLoader on a field of a type generated as a record is rejected")
    public void testDataLoaderOnRecordFieldRejected() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class, () -> map("""
                type Query { book: Book }
                type Book { author: String! }
                """, RECORDS, Map.of("Book.author", "authorLoader")));

        Assert.assertTrue(error.getMessage().contains(
                "\"Book.author\" in [[dataloaders]] cannot use a DataLoader, as \"Book\" is generated as a record."),
                error.getMessage());
    }

    @Test(description = "Test that the service is named in PascalCase with a Service suffix",
            dataProvider = "serviceNames")
    public void testServiceName(String baseName, String serviceName) throws ServiceGenerationException {
        Assert.assertEquals(mapWithBaseName(BOOK_QUERY, baseName).serviceName(), serviceName);
    }

    @DataProvider
    public Object[][] serviceNames() {
        return new Object[][]{
                {"schema", "SchemaService"},
                {"book-api", "BookApiService"},
                {"book_api", "BookApiService"},
                {"book api", "BookApiService"},
                {"bookApi", "BookApiService"},
                {"book-service", "BookService"},
                {"books.v2", "BooksV2Service"},
                {"café-api", "CaféApiService"},
                {"café-api", "CaféApiService"},
                {"книга", "КнигаService"},
                {"图书-api", "图书ApiService"},
                {"𠀀-api", "𠀀ApiService"},
                {"book٢", "Book٢Service"},
                {"BookService", "BookService"},
                {"bookservice", "BookserviceService"},
                {"book-services", "BookServicesService"},
                {"service", "Service"}
        };
    }

    @Test(description = "Test that a base name that does not start with a letter is rejected with advice for its "
            + "source", dataProvider = "invalidServiceNames")
    public void testInvalidServiceName(String baseName, ServiceBaseName.Source source, String message) {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> mapWithBaseName(BOOK_QUERY, baseName, source));

        Assert.assertTrue(error.getMessage().contains(message), error.getMessage());
    }

    @DataProvider
    public Object[][] invalidServiceNames() {
        return new Object[][]{
                {"1books", ServiceBaseName.Source.SCHEMA_FILE, "Cannot name the service from the schema file "
                        + "\"1books\". The name must start with a letter. Rename the schema file, or set a name "
                        + "with --service-name."},
                {"---", ServiceBaseName.Source.SCHEMA_FILE, "Cannot name the service from the schema file "
                        + "\"---\". The name must start with a letter. Rename the schema file, or set a name "
                        + "with --service-name."},
                {"123shop", ServiceBaseName.Source.PACKAGE, "Cannot name the service from the package "
                        + "\"123shop\". The name must start with a letter. Rename the package in Ballerina.toml, "
                        + "or set a name with --service-name."},
                {"1shop", ServiceBaseName.Source.GIVEN, "Cannot name the service from the given name "
                        + "\"1shop\". The name must start with a letter. Choose another name."},
                {"٢books", ServiceBaseName.Source.SCHEMA_FILE, "Cannot name the service from the schema file "
                        + "\"٢books\". The name must start with a letter. Rename the schema file, or set a name "
                        + "with --service-name."}
        };
    }

    @Test(description = "Test that a service name used by a schema type is rejected with advice for its source",
            dataProvider = "clashingServiceNames")
    public void testServiceNameClash(String baseName, ServiceBaseName.Source source, String message) {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> mapWithBaseName("""
                        type Query { book: SchemaService }
                        type SchemaService { name: String! }
                        """, baseName, source));

        Assert.assertTrue(error.getMessage().contains(message), error.getMessage());
    }

    @DataProvider
    public Object[][] clashingServiceNames() {
        return new Object[][]{
                {"schema", ServiceBaseName.Source.SCHEMA_FILE, "Service name \"SchemaService\", made from the "
                        + "schema file \"schema\", is already used by a type in the schema. Rename the schema file, "
                        + "or set a name with --service-name."},
                {"schema", ServiceBaseName.Source.MODULE, "Service name \"SchemaService\", made from the module "
                        + "\"schema\", is already used by a type in the schema. Rename the module, or set a name "
                        + "with --service-name."},
                {"schema", ServiceBaseName.Source.PACKAGE, "Service name \"SchemaService\", made from the package "
                        + "\"schema\", is already used by a type in the schema. Rename the package in Ballerina.toml, "
                        + "or set a name with --service-name."},
                {"schema", ServiceBaseName.Source.DIRECTORY, "Service name \"SchemaService\", made from the "
                        + "directory \"schema\", is already used by a type in the schema. Rename the directory, or "
                        + "set a name with --service-name."},
                {"schema", ServiceBaseName.Source.GIVEN, "Service name \"SchemaService\", made from the given "
                        + "name \"schema\", is already used by a type in the schema. Choose another name."}
        };
    }

    @Test(description = "Test that a service name the target module already defines is rejected")
    public void testServiceNameDefinedInModule() {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(BOOK_QUERY));
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> new ServiceModelMapper(schema, SERVICE_CLASSES, Map.of(), Map.of()).map(
                        new ServiceBaseName("orders", ServiceBaseName.Source.MODULE),
                        Map.of("OrdersService", "main.bal")));

        Assert.assertTrue(error.getMessage().contains("Service name \"OrdersService\", made from the module "
                + "\"orders\", is already defined in \"main.bal\". Rename the module, or set a name with "
                + "--service-name."), error.getMessage());
    }

    @Test(description = "Test that other names the target module defines do not block the service name")
    public void testServiceNameNotDefinedInModule() throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(BOOK_QUERY));
        ServiceModel model = new ServiceModelMapper(schema, SERVICE_CLASSES, Map.of(), Map.of()).map(
                new ServiceBaseName("orders", ServiceBaseName.Source.MODULE), Map.of("Orders", "main.bal"));

        Assert.assertEquals(model.serviceName(), "OrdersService");
    }

    private static ServiceModel mapWithBaseName(String sdl, String baseName) throws ServiceGenerationException {
        return mapWithBaseName(sdl, baseName, ServiceBaseName.Source.SCHEMA_FILE);
    }

    private static ServiceModel mapWithBaseName(String sdl, String baseName, ServiceBaseName.Source source)
            throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return new ServiceModelMapper(schema, SERVICE_CLASSES, Map.of(), Map.of())
                .map(new ServiceBaseName(baseName, source), Map.of());
    }

    private static ServiceModel map(String sdl, boolean useRecordsForObjects) throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return new ServiceModelMapper(schema, useRecordsForObjects, Map.of(), Map.of()).map(BASE_NAME, Map.of());
    }

    private static ServiceModel map(String sdl, Map<String, String> idTypes) throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return new ServiceModelMapper(schema, SERVICE_CLASSES, idTypes, Map.of()).map(BASE_NAME, Map.of());
    }

    private static ServiceModel map(String sdl, boolean useRecordsForObjects, Map<String, String> dataLoaders)
            throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return new ServiceModelMapper(schema, useRecordsForObjects, Map.of(), dataLoaders).map(BASE_NAME, Map.of());
    }

    private static ResolverModel resolver(List<ResolverModel> resolvers, String name) {
        return resolvers.stream()
                .filter(resolver -> resolver.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No resolver named " + name));
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
