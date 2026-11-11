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
 * Tests merging a newly generated types file into an existing one.
 */
public class TypesMergerTest {

    private static final String SERVICE_OBJECT = """
            type BookApiService service object {
                *graphql:Service;
                resource function get book(string id) returns Book?|error;
            };
            """;

    @Test(description = "Test that the resolvers of a service class are merged as in the service")
    public void testMergesServiceClassResolvers() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public distinct service class Book {
                    resource function get title() returns string|error {
                        return self.title;
                    }

                    resource function get pages() returns int|error {
                        return 10;
                    }
                }
                """), types("""
                public distinct service class Book {
                    resource function get title() returns string|error {
                        return error("Not implemented");
                    }

                    resource function get pages() returns int?|error {
                        return error("Not implemented");
                    }

                    resource function get isbn() returns string|error {
                        return error("Not implemented");
                    }
                }
                """));

        Assert.assertTrue(result.source().contains("return self.title;"), result.source());
        Assert.assertTrue(result.source().contains("return 10;"), result.source());
        Assert.assertTrue(result.source().contains("resource function get isbn() returns string|error {"),
                result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("Book.get title", Outcome.KEPT),
                new MergedResolver("Book.get pages", Outcome.OUTDATED),
                new MergedResolver("Book.get isbn", Outcome.ADDED)));
    }

    @Test(description = "Test that the developer's fields, init and helper methods in a service class are kept")
    public void testKeepsServiceClassCode() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public distinct service class Book {
                    private final string title;

                    function init(string title) {
                        self.title = title;
                    }

                    resource function get title() returns string|error {
                        return self.upper();
                    }

                    function upper() returns string {
                        return self.title.toUpperAscii();
                    }
                }
                """), types("""
                public distinct service class Book {
                    resource function get title() returns string|error {
                        return error("Not implemented");
                    }

                    resource function get pages() returns int|error {
                        return error("Not implemented");
                    }
                }
                """));

        // The developer's order is kept, and the new resolver is added last.
        Assert.assertTrue(result.source().contains("""
                public distinct service class Book {
                    private final string title;

                    function init(string title) {
                        self.title = title;
                    }

                    resource function get title() returns string|error {
                        return self.upper();
                    }

                    function upper() returns string {
                        return self.title.toUpperAscii();
                    }

                    resource function get pages() returns int|error {
                        return error("Not implemented");
                    }
                }
                """), result.source());
    }

    @Test(description = "Test that a generated field the developer edited is kept once and not added again")
    public void testKeepsEditedField() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public distinct service class Book {
                    private final string title = "Dune";

                    resource function get title() returns string|error {
                        return self.title;
                    }
                }
                """), types("""
                public distinct service class Book {
                    string title;

                    resource function get title() returns string|error {
                        return error("Not implemented");
                    }
                }
                """));

        Assert.assertTrue(result.source().contains("private final string title = \"Dune\";"), result.source());
        Assert.assertFalse(result.source().contains("    string title;"), result.source());
    }

    @Test(description = "Test that the records, enums, unions and service object type are regenerated")
    public void testRegeneratesSchemaTypes() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public type BookInput record {|
                    string title;
                |};

                public enum Genre {
                    FICTION
                }

                public type SearchResult Book;
                """), """
                import ballerina/graphql;

                type BookApiService service object {
                    *graphql:Service;
                    resource function get book(int id) returns Book?|error;
                };

                public type BookInput record {|
                    string title;
                    int pages;
                |};

                public enum Genre {
                    FICTION,
                    POETRY
                }

                public type SearchResult Book|Author;
                """);

        Assert.assertTrue(result.source().contains("resource function get book(int id) returns Book?|error;"),
                result.source());
        Assert.assertFalse(result.source().contains("get book(string id)"), result.source());
        Assert.assertTrue(result.source().contains("int pages;"), result.source());
        Assert.assertTrue(result.source().contains("POETRY"), result.source());
        Assert.assertTrue(result.source().contains("public type SearchResult Book|Author;"), result.source());
        Assert.assertEquals(result.source().split("public type BookInput").length, 2, result.source());
    }

    @Test(description = "Test that the records, enums and unions the schema does not have are kept and reported")
    public void testKeepsTypesNotInSchema() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                const DEFAULT_PAGES = 100;

                public type BookInput record {|
                    string title;
                |};

                public type AuthorInput record {|
                    string name;
                |};

                public enum Genre {
                    FICTION
                }

                public type SearchResult Book;

                function toTitle(string value) returns string {
                    return value.trim();
                }
                """), types("""
                public type BookInput record {|
                    string title;
                |};
                """));

        Assert.assertEquals(result.typesNotInSchema(), List.of("AuthorInput", "Genre", "SearchResult"));
        Assert.assertEquals(result.source().split("public type BookInput").length, 2, result.source());
        Assert.assertTrue(result.source().contains("public type AuthorInput record {|"), result.source());
        Assert.assertTrue(result.source().contains("public enum Genre {"), result.source());
        Assert.assertTrue(result.source().contains("public type SearchResult Book;"), result.source());
        Assert.assertTrue(result.source().contains("const DEFAULT_PAGES = 100;"), result.source());
        Assert.assertTrue(result.source().contains("function toTitle(string value) returns string {"),
                result.source());
    }

    @Test(description = "Test that a new service class is added with all its resolvers")
    public void testAddsServiceClass() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types(""), types("""
                public distinct service class Author {
                    resource function get name() returns string|error {
                        return error("Not implemented");
                    }
                }
                """));

        Assert.assertTrue(result.source().contains("public distinct service class Author {"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("Author.get name", Outcome.ADDED)));
    }

    @Test(description = "Test that a service class no longer in the schema is reported and not removed silently")
    public void testServiceClassNotInSchema() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> TypesMerger.merge(types("""
                        public distinct service class Author {
                            resource function get name() returns string|error {
                                return "Jane";
                            }
                        }
                        """), types("")));

        Assert.assertTrue(error.getMessage().contains(
                "Service classes not in the schema: \"Author\". Update or delete them."), error.getMessage());
    }

    @Test(description = "Test that a class the developer wrote is kept and not reported as not in the schema")
    public void testKeepsDeveloperClasses() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                class Cache {
                    map<string> entries = {};
                }

                service class Handler {
                    resource function get status() returns string {
                        return "ok";
                    }
                }

                distinct service class Empty {
                }
                """), types(""));

        Assert.assertTrue(result.source().contains("class Cache {"), result.source());
        Assert.assertTrue(result.source().contains("service class Handler {"), result.source());
        Assert.assertTrue(result.source().contains("distinct service class Empty {"), result.source());
        Assert.assertTrue(result.resolvers().isEmpty(), result.resolvers().toString());
    }

    @Test(description = "Test that a generated class is merged even when the developer removed all its resolvers")
    public void testMergesGeneratedClassWithoutResolvers() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public distinct service class Book {
                    private final string title = "Dune";
                }
                """), types("""
                public distinct service class Book {
                    resource function get title() returns string|error {
                        return error("Not implemented");
                    }
                }
                """));

        Assert.assertEquals(result.source().split("class Book").length, 2, result.source());
        Assert.assertTrue(result.source().contains("private final string title = \"Dune\";"), result.source());
        Assert.assertEquals(result.resolvers(), List.of(new MergedResolver("Book.get title", Outcome.ADDED)));
    }

    @Test(description = "Test that a service class resolver no longer in the schema is reported with its class")
    public void testServiceClassResolverNotInSchema() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> TypesMerger.merge(types("""
                        public distinct service class Book {
                            resource function get title() returns string|error {
                                return "Dune";
                            }

                            resource function get pages() returns int|error {
                                return 10;
                            }
                        }
                        """), types("""
                        public distinct service class Book {
                            resource function get title() returns string|error {
                                return error("Not implemented");
                            }
                        }
                        """)));

        Assert.assertTrue(error.getMessage().contains(
                "Resolvers not in the schema: \"Book.get pages\". Update or delete them."), error.getMessage());
    }

    @Test(description = "Test that an interface the schema no longer implements is removed from the service class")
    public void testRegeneratesTypeInclusions() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public distinct service class Book {
                    *Node;
                    *Item;

                    resource function get title() returns string|error {
                        return "Dune";
                    }
                }
                """), types("""
                public distinct service class Book {
                    *Node;

                    resource function get title() returns string|error {
                        return error("Not implemented");
                    }
                }
                """));

        Assert.assertTrue(result.source().contains("*Node;"), result.source());
        Assert.assertFalse(result.source().contains("*Item;"), result.source());
        Assert.assertTrue(result.source().contains("return \"Dune\";"), result.source());
    }

    @Test(description = "Test that an interface the schema newly implements is added first in the service class")
    public void testAddsTypeInclusion() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                public distinct service class Book {
                    resource function get title() returns string|error {
                        return "Dune";
                    }
                }
                """), types("""
                public distinct service class Book {
                    *Node;

                    resource function get title() returns string|error {
                        return error("Not implemented");
                    }
                }
                """));

        Assert.assertTrue(result.source().contains("""
                public distinct service class Book {
                    *Node;

                    resource function get title() returns string|error {
                        return "Dune";
                    }
                }"""), result.source());
    }

    @Test(description = "Test that the developer's own code and imports in the types file are kept")
    public void testKeepsDeveloperCode() throws ServiceGenerationException {
        MergeResult result = TypesMerger.merge(types("""
                const DEFAULT_PAGES = 100;

                function toTitle(string value) returns string {
                    return value.trim();
                }
                """).replace("import ballerina/graphql;", "import ballerina/graphql;\nimport ballerina/time;"),
                types(""));

        Assert.assertTrue(result.typesNotInSchema().isEmpty(), result.typesNotInSchema().toString());
        Assert.assertTrue(result.source().contains("import ballerina/time;"), result.source());
        Assert.assertTrue(result.source().contains("const DEFAULT_PAGES = 100;"), result.source());
        Assert.assertTrue(result.source().contains("function toTitle(string value) returns string {"),
                result.source());
    }

    @Test(description = "Test that an existing types file with a syntax error is not merged")
    public void testSyntaxError() {
        ServiceGenerationException error = Assert.expectThrows(ServiceGenerationException.class,
                () -> TypesMerger.merge(types("""
                        public distinct service class Book {
                            resource function get title() returns string|error {
                                return "Dune"
                            }
                        }
                        """), types("")));

        Assert.assertTrue(error.getMessage().contains("The existing types file has a syntax error at line"),
                error.getMessage());
    }

    private static String types(String members) {
        return "import ballerina/graphql;\n\n" + SERVICE_OBJECT + "\n" + members;
    }
}
