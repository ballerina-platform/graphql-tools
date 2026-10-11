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

package io.ballerina.graphql.generator.common.emitter;

import io.ballerina.graphql.generator.common.model.FieldModel;
import io.ballerina.graphql.generator.common.model.NamedType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests writing Ballerina record types.
 */
public class RecordEmitterTest {

    @Test(description = "Test writing a record with docs, a deprecated field and a default value")
    public void testEmit() {
        List<FieldModel> fields = List.of(
                new FieldModel("title", new NamedType("string", false), null, null, null),
                new FieldModel("author", new NamedType("string", true), null, "The author of the book",
                        "No longer needed"),
                new FieldModel("pages", new NamedType("int", false), "100", null, null));

        Assert.assertEquals(RecordEmitter.emit("BookInfo", fields, "Represents new book information"), """
                # Represents new book information
                public type BookInfo record {|
                    string title;
                    # The author of the book
                    # # Deprecated
                    # No longer needed
                    @deprecated
                    string? author;
                    int pages = 100;
                |};""");
    }

    @Test(description = "Test writing a record without docs")
    public void testEmitWithoutDocs() {
        List<FieldModel> fields = List.of(new FieldModel("id", new NamedType("int", false), null, null, null));

        Assert.assertEquals(RecordEmitter.emit("Book", fields, null), """
                public type Book record {|
                    int id;
                |};""");
    }
}
