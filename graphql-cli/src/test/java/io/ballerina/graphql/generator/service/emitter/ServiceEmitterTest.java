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

package io.ballerina.graphql.generator.service.emitter;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.mapper.ServiceModelMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests writing the service file.
 */
public class ServiceEmitterTest {

    private static final boolean SERVICE_CLASSES = false;

    @Test(description = "Test writing Query, Mutation and Subscription resolvers into the service")
    public void testEmit() throws ServiceGenerationException {
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

                service on new graphql:Listener(port) {
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

    private static String emit(String sdl) throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return ServiceEmitter.emit(ServiceModelMapper.map(schema, SERVICE_CLASSES));
    }
}
