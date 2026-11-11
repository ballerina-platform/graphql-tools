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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tests how members are identified when merging.
 */
public class MemberIdentityTest {

    @Test(description = "Test that each kind of module-level declaration is identified by its name")
    public void testModuleMemberNames() {
        ModulePartNode module = SourceMerger.parse("""
                public type Book record {|
                    string title;
                |};

                public enum Genre {
                    FICTION
                }

                public distinct service class Author {
                }

                const DEFAULT_PAGES = 100;

                int count = 0;

                listener graphql:Listener gql = new (9090);

                function toTitle(string value) returns string {
                    return value;
                }
                """);

        Assert.assertEquals(module.members().stream().map(MemberIdentity::name).toList(),
                List.of("Book", "Genre", "Author", "DEFAULT_PAGES", "count", "gql", "toTitle"));
    }

    @Test(description = "Test that the members of a class are identified by name and resolvers by their key")
    public void testClassMembers() {
        List<Node> members = classMembers("""
                public distinct service class Book {
                    *Node;
                    private final string title = "Dune";

                    resource function get title() returns string {
                        return self.title;
                    }

                    resource function get author/name() returns string {
                        return "Herbert";
                    }

                    remote function rate(int stars) returns int {
                        return stars;
                    }

                    function upper() returns string {
                        return self.title.toUpperAscii();
                    }
                }
                """);

        Assert.assertEquals(members.stream().map(MemberIdentity::resolverKey).toList(), List.of(Optional.empty(),
                Optional.empty(), Optional.of("get title"), Optional.of("get author/name"),
                Optional.of("remote rate"), Optional.empty()));
        Assert.assertEquals(MemberIdentity.name(members.get(0)), "Node");
        Assert.assertEquals(MemberIdentity.name(members.get(1)), "title");
        Assert.assertEquals(MemberIdentity.name(members.get(5)), "upper");
    }

    @Test(description = "Test that renaming a path parameter does not change the resolver key")
    public void testPathParameterRename() {
        List<Node> members = classMembers("""
                service class Library {
                    resource function get book/[string id]() returns string {
                        return id;
                    }

                    resource function get books/[string... ids]() returns string[] {
                        return ids;
                    }
                }
                """);
        List<Node> renamed = classMembers("""
                service class Library {
                    resource function get book/[ string bookId ]() returns string {
                        return bookId;
                    }

                    resource function get books/[string... bookIds]() returns string[] {
                        return bookIds;
                    }
                }
                """);

        Assert.assertEquals(MemberIdentity.resolverKey(members.get(0)), Optional.of("get book/[string]"));
        Assert.assertEquals(MemberIdentity.resolverKey(members.get(1)), Optional.of("get books/[string...]"));
        Assert.assertEquals(MemberIdentity.resolverKey(renamed.get(0)), MemberIdentity.resolverKey(members.get(0)));
        Assert.assertEquals(MemberIdentity.resolverKey(renamed.get(1)), MemberIdentity.resolverKey(members.get(1)));
    }

    @Test(description = "Test which declarations the tool owns and which are the developer's")
    public void testToolOwnership() {
        ModulePartNode module = SourceMerger.parse("""
                type BookApiService service object {
                    *graphql:Service;
                };

                public type Book record {|
                    string title;
                |};

                public enum Genre {
                    FICTION
                }

                public type SearchResult Book|Author;

                public distinct service class Author {
                    resource function get name() returns string {
                        return "Herbert";
                    }
                }

                public distinct service class Publisher {
                    resource function get name() returns string {
                        return "Chilton";
                    }
                }

                public type AuditEntry record {|
                    string id;
                |};

                class Cache {
                }

                distinct service class Empty {
                }

                const DEFAULT_PAGES = 100;

                function Review() {
                }
                """);
        Set<String> schemaNames = Set.of("BookApiService", "Book", "Genre", "SearchResult", "Author", "Review");

        Map<String, Boolean> owned = new LinkedHashMap<>();
        module.members().forEach(member -> owned.put(MemberIdentity.name(member),
                MemberIdentity.isToolOwned(member, schemaNames)));

        // A GraphQL service class the schema no longer has is still the tool's, so the merge can report it.
        Assert.assertEquals(owned, Map.ofEntries(Map.entry("BookApiService", true), Map.entry("Book", true),
                Map.entry("Genre", true), Map.entry("SearchResult", true), Map.entry("Author", true),
                Map.entry("Publisher", true), Map.entry("AuditEntry", false), Map.entry("Cache", false),
                Map.entry("Empty", false), Map.entry("DEFAULT_PAGES", false), Map.entry("Review", false)));
    }

    @Test(description = "Test that a field is still identified by its name after the developer edits it")
    public void testEditedFieldKeepsName() {
        Node generated = classMembers("class Book {\n    string title;\n}").get(0);
        Node edited = classMembers("class Book {\n    private final string title = \"Dune\";\n}").get(0);

        Assert.assertEquals(MemberIdentity.name(edited), MemberIdentity.name(generated));
    }

    private static List<Node> classMembers(String source) {
        ClassDefinitionNode classDefinition = (ClassDefinitionNode) SourceMerger.parse(source).members().get(0);
        return classDefinition.members().stream().toList();
    }
}
