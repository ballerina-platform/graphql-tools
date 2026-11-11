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

package io.ballerina.graphql.generator.service.merge;

import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.merge.MergeResult.MergedResolver;
import io.ballerina.graphql.generator.service.merge.MergeResult.Outcome;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests merging a newly generated service into an existing one.
 */
public class ServiceMergerTest {

    @Test(description = "Test that a new resolver is added and an unchanged one keeps the developer's code")
    public void testMerge() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    # Finds a book.
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(string id) returns Book?|error {
                        return error("Not implemented");
                    }

                    resource function get authors() returns Author[]|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertEquals(result.source(), """
                import ballerina/graphql;

                configurable int port = 9090;

                service BookApiService on new graphql:Listener(port) {
                    # Finds a book.
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }

                    resource function get authors() returns Author[]|error {
                        return error("Not implemented");
                    }
                }
                """);
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.KEPT),
                new MergedResolver("get authors", Outcome.ADDED)));
    }

    @Test(description = "Test that a resolver whose signature changed is kept as written and reported as outdated")
    public void testKeepsChangedResolver() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(int id) returns Book?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertTrue(result.source().contains("resource function get book(string id) returns Book?|error {"),
                result.source());
        Assert.assertTrue(result.source().contains("return getBookFromDb(id);"), result.source());
        Assert.assertFalse(result.source().contains("get book(int id)"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.OUTDATED)));
    }

    @Test(description = "Test that a resolver is kept when a DataLoader adds a context parameter to its signature")
    public void testKeepsResolverWhenDataLoaderAdded() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(graphql:Context context, string id) returns Book?|error {
                        return context.getDataLoader("bookLoader").get(id);
                    }
                """));

        Assert.assertTrue(result.source().contains("return getBookFromDb(id);"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.OUTDATED)));
    }

    @Test(description = "Test that a changed return type also counts as a changed signature")
    public void testReportsChangedReturnType() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(string id) returns Book|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.OUTDATED)));
    }

    @Test(description = "Test that spacing, line breaks and comments in a signature do not count as a change")
    public void testIgnoresSpacingAndComments() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource   function get book(
                            string id // the book's id
                        )   returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(string id) returns Book?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertTrue(result.source().contains("return getBookFromDb(id);"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.KEPT)));
    }

    @Test(description = "Test that an ID annotation is part of the signature")
    public void testIdAnnotationIsPartOfSignature() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(@graphql:ID string id) returns Book?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.OUTDATED)));
    }

    @Test(description = "Test that remote and subscribe resolvers are matched by their own keys")
    public void testRemoteAndSubscribeResolvers() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    remote function addBook(string title) returns Book|error {
                        return saveBook(title);
                    }

                    resource function subscribe bookAdded() returns stream<Book, error?>|error {
                        return bookStream();
                    }
                """), service("""
                    remote function addBook(string title) returns Book|error {
                        return error("Not implemented");
                    }

                    resource function subscribe bookAdded() returns stream<Book, error?>|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertTrue(result.source().contains("return saveBook(title);"), result.source());
        Assert.assertTrue(result.source().contains("return bookStream();"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("remote addBook", Outcome.KEPT),
                new MergedResolver("subscribe bookAdded", Outcome.KEPT)));
    }

    @Test(description = "Test that a resolver no longer in the schema is reported and not removed silently")
    public void testResolverNotInSchema() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }

                    resource function get books() returns Book[]|error {
                        return getBooksFromDb();
                    }

                    remote function removeBook(string id) returns boolean|error {
                        return deleteBook(id);
                    }
                """), service("""
                    resource function get book(string id) returns Book?|error {
                        return error("Not implemented");
                    }
                """)));

        Assert.assertTrue(error.getMessage().contains(
                "Resolvers not in the schema: \"get books\", \"remote removeBook\". Update or delete them."),
                error.getMessage());
    }

    @Test(description = "Test that a function the developer added to the service is kept")
    public void testKeepsDeveloperFunction() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return self.find(id);
                    }

                    function find(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(string id) returns Book?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertTrue(result.source().contains("function find(string id) returns Book?|error {"),
                result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.KEPT)));
    }

    @Test(description = "Test that a generated DataLoader prefetch function keeps the developer's changes")
    public void testKeepsPrefetchFunction() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    function preProfile(graphql:Context context, string id) {
                        context.getDataLoader("profileLoader").add(id.toUpperAscii());
                    }

                    resource function get profile(graphql:Context context, string id) returns Profile?|error {
                        return error("Not implemented");
                    }
                """), service("""
                    function preProfile(graphql:Context context, string id) {
                        context.getDataLoader("profileLoader").add(id);
                    }

                    resource function get profile(graphql:Context context, string id) returns Profile?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertTrue(result.source().contains("add(id.toUpperAscii());"), result.source());
        Assert.assertEquals(result.source().split("function preProfile").length, 2, result.source());
    }

    @Test(description = "Test that every resolver is added when the existing file has no service")
    public void testExistingFileWithoutService() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge("import ballerina/graphql;\n", service("""
                    resource function get book(string id) returns Book?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.ADDED)));
    }

    @Test(description = "Test that an existing service with a syntax error is not merged")
    public void testSyntaxError() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id)
                    }
                """), service("""
                    resource function get book(string id) returns Book?|error {
                        return error("Not implemented");
                    }
                """)));

        // The missing semicolon is reported at the next token, the closing brace on line 8.
        Assert.assertTrue(error.getMessage().contains(
                "The existing service has a syntax error at line 8: missing semicolon token. Fix it before merging."),
                error.getMessage());
    }

    @Test(description = "Test that the developer's imports are kept and the imports the schema needs are added")
    public void testMergesImports() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge("""
                import ballerina/graphql;
                import ballerina/sql;

                service BookApiService on new graphql:Listener(9090) {
                }
                """, """
                import ballerina/graphql;
                import ballerina/graphql.dataloader;
                import ballerina/http;

                service BookApiService on new graphql:Listener(9090) {
                }
                """);

        // The formatter sorts the imports.
        Assert.assertTrue(result.source().startsWith("""
                import ballerina/graphql;
                import ballerina/graphql.dataloader;
                import ballerina/http;
                import ballerina/sql;
                """), result.source());
    }

    @Test(description = "Test that the developer's port, listener and service annotation are kept")
    public void testKeepsPortListenerAndAnnotation() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge("""
                import ballerina/graphql;

                configurable int port = 8080;

                @graphql:ServiceConfig {maxQueryDepth: 5}
                service BookApiService on new graphql:Listener(port, host = "0.0.0.0") {
                }
                """, service(""));

        Assert.assertTrue(result.source().contains("configurable int port = 8080;"), result.source());
        Assert.assertFalse(result.source().contains("9090"), result.source());
        Assert.assertTrue(result.source().contains("""
                @graphql:ServiceConfig {maxQueryDepth: 5}
                service BookApiService on new graphql:Listener(port, host = "0.0.0.0") {"""), result.source());
    }

    @Test(description = "Test that the generated service annotation is added when the service has none")
    public void testAddsServiceAnnotation() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service(""), dataLoaderService("profileLoader"));

        Assert.assertTrue(result.source().contains("""
                @graphql:ServiceConfig {contextInit: initContext}
                service BookApiService on new graphql:Listener(port) {"""), result.source());
        Assert.assertTrue(result.source().contains("isolated function initContext("), result.source());
        Assert.assertTrue(result.source().indexOf("function initContext(")
                < result.source().indexOf("service BookApiService"), result.source());
        Assert.assertTrue(result.source().contains("isolated function profileLoader("), result.source());
    }

    @Test(description = "Test that the developer's context init code is kept and new DataLoaders are registered")
    public void testAddsDataLoaderRegistration() throws ServiceGenerationException {
        String existing = dataLoaderService("profileLoader").replace("graphql:Context context = new;",
                "graphql:Context context = new;\n    context.set(\"token\", request.getHeader(\"Authorization\"));");

        MergeResult result = ServiceMerger.merge(existing, dataLoaderService("profileLoader", "authorLoader"));

        Assert.assertTrue(result.source().contains("""
                    graphql:Context context = new;
                    context.set("token", request.getHeader("Authorization"));
                    context.registerDataLoader("profileLoader", new dataloader:DefaultDataLoader(profileLoader));
                    context.registerDataLoader("authorLoader", new dataloader:DefaultDataLoader(authorLoader));
                    return context;
                """), result.source());
        Assert.assertEquals(result.source().split("registerDataLoader\\(\"profileLoader\"").length, 2,
                result.source());
    }

    @Test(description = "Test that the developer's batch function is kept and a new one is added")
    public void testMergesBatchFunctions() throws ServiceGenerationException {
        String existing = dataLoaderService("profileLoader").replace("return error(\"Not implemented\");",
                "return getProfiles(keys);");

        MergeResult result = ServiceMerger.merge(existing, dataLoaderService("profileLoader", "authorLoader"));

        Assert.assertTrue(result.source().contains("return getProfiles(keys);"), result.source());
        Assert.assertTrue(result.source().contains("isolated function authorLoader("), result.source());
        Assert.assertTrue(result.source().indexOf("function authorLoader(")
                > result.source().indexOf("service BookApiService"), result.source());
    }

    @Test(description = "Test that the developer's module-level code is kept in place")
    public void testKeepsModuleLevelCode() throws ServiceGenerationException {
        String existing = service("").replace("configurable int port = 9090;",
                "configurable int port = 9090;\n\nfinal string dbUrl = \"jdbc:books\";") + """

                function getDb() returns string {
                    return dbUrl;
                }
                """;

        MergeResult result = ServiceMerger.merge(existing, service(""));

        Assert.assertTrue(result.source().contains("final string dbUrl = \"jdbc:books\";"), result.source());
        Assert.assertTrue(result.source().contains("function getDb() returns string {"), result.source());
        Assert.assertTrue(result.source().indexOf("dbUrl =") < result.source().indexOf("service BookApiService"),
                result.source());
    }

    // A generated service file whose resolver uses the given DataLoaders, as the service emitter writes it.
    private static String dataLoaderService(String... dataLoaderNames) {
        StringBuilder source = new StringBuilder("""
                import ballerina/graphql;
                import ballerina/graphql.dataloader;
                import ballerina/http;

                configurable int port = 9090;

                isolated function initContext(http:RequestContext requestContext, http:Request request) \
                returns graphql:Context|error {
                    graphql:Context context = new;
                """);
        for (String name : dataLoaderNames) {
            source.append(String.format(
                    "    context.registerDataLoader(\"%s\", new dataloader:DefaultDataLoader(%s));%n", name, name));
        }
        source.append("""
                    return context;
                }

                @graphql:ServiceConfig {contextInit: initContext}
                service BookApiService on new graphql:Listener(port) {
                }
                """);
        for (String name : dataLoaderNames) {
            source.append(String.format("""

                    isolated function %s(readonly & anydata[] keys) returns anydata[]|error {
                        return error("Not implemented");
                    }
                    """, name));
        }
        return source.toString();
    }

    private static String service(String members) {
        return """
                import ballerina/graphql;

                configurable int port = 9090;

                service BookApiService on new graphql:Listener(port) {
                """ + members + "}\n";
    }
}
