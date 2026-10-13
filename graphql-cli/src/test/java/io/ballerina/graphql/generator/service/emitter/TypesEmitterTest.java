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
 * Tests writing the types file of a service.
 */
public class TypesEmitterTest {

    private static final String SERVICE_NAME = "BookApi";
    private static final boolean SERVICE_CLASSES = false;
    private static final boolean RECORDS = true;

    @Test(description = "Test writing interfaces, unions and service classes that include interfaces")
    public void testEmitServiceClasses() throws ServiceGenerationException {
        String types = emit("""
                type Query { profile(id: ID!): Profile }
                interface Info { name: String! }
                union Profile = Student | Teacher
                type Student implements Info { id: ID! name: String! }
                type Teacher implements Info { name: String! }
                """, SERVICE_CLASSES);

        Assert.assertEquals(types, """
                import ballerina/graphql;

                type BookApi service object {
                    *graphql:Service;
                    resource function get profile(@graphql:ID string id) returns Profile?|error;
                };

                public type Info distinct service object {
                    resource function get name() returns string|error;
                };

                public type Profile Student|Teacher;

                public distinct service class Student {
                    *Info;

                    resource function get id() returns @graphql:ID string|error {
                        return error("Not implemented");
                    }

                    resource function get name() returns string|error {
                        return error("Not implemented");
                    }
                }

                public distinct service class Teacher {
                    *Info;

                    resource function get name() returns string|error {
                        return error("Not implemented");
                    }
                }
                """);
    }

    @Test(description = "Test writing records, input types and enums with docs")
    public void testEmitRecords() throws ServiceGenerationException {
        String types = emit("""
                type Query { book(input: BookInput, genre: Genre): Book }
                "A book"
                type Book {
                    "The book ID"
                    id: ID!
                    title: String @deprecated(reason: "Use name")
                }
                input BookInput { title: String! year: Int = 2020 }
                enum Genre { FICTION POETRY }
                """, RECORDS);

        Assert.assertEquals(types, """
                import ballerina/graphql;

                type BookApi service object {
                    *graphql:Service;
                    resource function get book(BookInput? input, Genre? genre) returns Book?|error;
                };

                # A book
                public type Book record {|
                    # The book ID
                    @graphql:ID string id;
                    # # Deprecated
                    # Use name
                    @deprecated
                    string? title;
                |};

                public type BookInput record {|
                    string title;
                    int? year = 2020;
                |};

                public enum Genre {
                    FICTION,
                    POETRY
                }
                """);
    }

    @Test(description = "Test writing the service object type with Query, Mutation and Subscription resolvers")
    public void testEmitServiceObject() throws ServiceGenerationException {
        String types = emit("""
                type Query {
                    "Finds a book title"
                    title(id: ID!): String
                }
                type Mutation { addTitle(title: String!): String! }
                type Subscription { titles: String! }
                """, SERVICE_CLASSES);

        Assert.assertEquals(types, """
                import ballerina/graphql;

                type BookApi service object {
                    *graphql:Service;
                    # Finds a book title
                    resource function get title(@graphql:ID string id) returns string?|error;
                    remote function addTitle(string title) returns string|error;
                    resource function subscribe titles() returns stream<string>|error;
                };
                """);
    }

    private static String emit(String sdl, boolean useRecordsForObjects) throws ServiceGenerationException {
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(new SchemaParser().parse(sdl));
        return TypesEmitter.emit(ServiceModelMapper.map(schema, SERVICE_NAME, useRecordsForObjects));
    }
}
