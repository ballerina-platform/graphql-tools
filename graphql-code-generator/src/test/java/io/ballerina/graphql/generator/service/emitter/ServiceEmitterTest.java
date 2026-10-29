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

package io.ballerina.graphql.generator.service.emitter;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.mapper.ServiceModelMapper;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Tests writing the service file.
 */
public class ServiceEmitterTest {

    private static final String BASE_NAME = "BookApi";
    private static final boolean SERVICE_CLASSES = false;
    private static final boolean RECORDS = true;

    @Test(description = "Test writing Query, Mutation and Subscription resolvers into the service")
    public void testEmit() throws ServiceGenerationException, FormatterException {
        String service = emit("""
                type Query {
                    "Finds books"
                    books("Max results" count: Int = 10, "The author" author: String!): [Book!]!
                    book(id: ID!): Book
                }
                type Mutation { addBook(title: String!): Book! }
                type Subscription { bookAdded: Book! }
                type Book { title: String! }
                """);

        Assert.assertEquals(service, """
                import ballerina/graphql;

                configurable int port = 9090;

                service BookApiService on new graphql:Listener(port) {
                    # Finds books
                    # + author - The author
                    # + count - Max results
                    resource function get books(string author, int? count = 10) returns Book[]|error {
                        return error("Not implemented");
                    }

                    resource function get book(@graphql:ID string id) returns Book?|error {
                        return error("Not implemented");
                    }

                    remote function addBook(string title) returns Book|error {
                        return error("Not implemented");
                    }

                    resource function subscribe bookAdded() returns stream<Book>|error {
                        return error("Not implemented");
                    }
                }
                """);
    }

    @Test(description = "Test that a null default value is written as nil")
    public void testEmitNilDefaultValue() throws ServiceGenerationException, FormatterException {
        String service = emit("type Query { book(title: String = null): String }");

        Assert.assertTrue(service.contains("resource function get book(string? title = ()) returns string?|error {"),
                service);
    }

    @Test(description = "Test that the module of a type used by a root resolver is imported into the service")
    public void testImportForRootResolverType() throws ServiceGenerationException, FormatterException {
        String service = emit("type Query { book(id: ID!): String }", Map.of("Query.book.id", "uuid:Uuid"));

        Assert.assertTrue(service.contains("import ballerina/uuid;"), service);
        Assert.assertTrue(
                service.contains("resource function get book(@graphql:ID uuid:Uuid id) returns string?|error {"),
                service);
    }

    @Test(description = "Test that a module used only outside the root resolvers is not imported into the service")
    public void testNoImportForModuleUsedOnlyInTypes() throws ServiceGenerationException, FormatterException {
        String service = emit("""
                type Query { book: Book }
                type Book { id: ID! }
                """, Map.of("Book.id", "uuid:Uuid"));

        Assert.assertFalse(service.contains("import ballerina/uuid;"), service);
    }

    @Test(description = "Test writing the DataLoader registration, prefetch methods and batch functions")
    public void testEmitDataLoaders() throws ServiceGenerationException, FormatterException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse("""
                type Query { profile(id: ID!): Profile profiles(name: String!, age: Int): [Profile!]! }
                type Profile { name: String! }
                """));
        String service = Formatter.format(ServiceEmitter.emit(new ServiceModelMapper(schema, RECORDS, Map.of(),
                Map.of("Query.profile", "profileLoader", "Query.profiles", "profilesLoader")).map(BASE_NAME)));

        Assert.assertEquals(service, """
                import ballerina/graphql;
                import ballerina/graphql.dataloader;
                import ballerina/http;

                configurable int port = 9090;

                isolated function initContext(http:RequestContext requestContext, http:Request request) \
                returns graphql:Context|error {
                    graphql:Context context = new;
                    context.registerDataLoader("profileLoader", new dataloader:DefaultDataLoader(profileLoader));
                    context.registerDataLoader("profilesLoader", new dataloader:DefaultDataLoader(profilesLoader));
                    return context;
                }

                @graphql:ServiceConfig {contextInit: initContext}
                service BookApiService on new graphql:Listener(port) {
                    function preProfile(graphql:Context context, @graphql:ID string id) {
                        context.getDataLoader("profileLoader").add(id);
                    }

                    resource function get profile(graphql:Context context, @graphql:ID string id) \
                returns Profile?|error {
                        return context.getDataLoader("profileLoader").get(id);
                    }

                    function preProfiles(graphql:Context context, string name, int? age) {
                        context.getDataLoader("profilesLoader").add([name, age]);
                    }

                    resource function get profiles(graphql:Context context, string name, int? age) \
                returns Profile[]|error {
                        return context.getDataLoader("profilesLoader").get([name, age]);
                    }
                }

                isolated function profileLoader(readonly & anydata[] keys) returns anydata[]|error {
                    return error("Not implemented");
                }

                isolated function profilesLoader(readonly & anydata[] keys) returns anydata[]|error {
                    return error("Not implemented");
                }
                """);
    }

    private static String emit(String sdl) throws ServiceGenerationException, FormatterException {
        return emit(sdl, Map.of());
    }

    private static String emit(String sdl, Map<String, String> idTypes)
            throws ServiceGenerationException, FormatterException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return Formatter.format(
                ServiceEmitter.emit(new ServiceModelMapper(schema, SERVICE_CLASSES, idTypes, Map.of())
                        .map(BASE_NAME)));
    }
}
