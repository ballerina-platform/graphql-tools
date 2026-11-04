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

    private static final String NOT_IMPLEMENTED = "return error(\"Not implemented\");";

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

    @Test(description = "Test that a resolver whose signature changed is replaced with the schema version")
    public void testReplacesChangedResolver() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(int id) returns Book?|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertTrue(result.source().contains("resource function get book(int id) returns Book?|error {"),
                result.source());
        Assert.assertTrue(result.source().contains(NOT_IMPLEMENTED), result.source());
        Assert.assertFalse(result.source().contains("getBookFromDb"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.REPLACED)));
    }

    @Test(description = "Test that a changed return type also counts as a changed signature")
    public void testReplacesChangedReturnType() throws ServiceGenerationException {
        MergeResult result = ServiceMerger.merge(service("""
                    resource function get book(string id) returns Book?|error {
                        return getBookFromDb(id);
                    }
                """), service("""
                    resource function get book(string id) returns Book|error {
                        return error("Not implemented");
                    }
                """));

        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.REPLACED)));
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

        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("get book", Outcome.REPLACED)));
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

    private static String service(String members) {
        return """
                import ballerina/graphql;

                configurable int port = 9090;

                service BookApiService on new graphql:Listener(port) {
                """ + members + "}\n";
    }
}
