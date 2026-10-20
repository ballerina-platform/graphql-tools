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
import io.ballerina.graphql.generator.common.model.TypeModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests writing Ballerina record types.
 */
public class RecordEmitterTest {

    private static final boolean NULLABLE = true;
    private static final boolean NON_NULL = false;
    private static final String NO_DEFAULT = null;
    private static final String NO_DESCRIPTION = null;
    private static final String NOT_DEPRECATED = null;

    @Test(description = "Test writing a record with docs, a deprecated field and a default value")
    public void testEmit() {
        List<FieldModel> fields = List.of(
                new FieldModel("title", TypeModel.named("string", NON_NULL), NO_DEFAULT, NO_DESCRIPTION,
                        NOT_DEPRECATED),
                new FieldModel("author", TypeModel.named("string", NULLABLE), NO_DEFAULT, "The author of the book",
                        "No longer needed"),
                new FieldModel("pages", TypeModel.named("int", NON_NULL), "100", NO_DESCRIPTION, NOT_DEPRECATED));

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
        List<FieldModel> fields = List.of(
                new FieldModel("id", TypeModel.named("int", NON_NULL), NO_DEFAULT, NO_DESCRIPTION, NOT_DEPRECATED));

        Assert.assertEquals(RecordEmitter.emit("Book", fields, NO_DESCRIPTION), """
                public type Book record {|
                int id;
                |};""");
    }
}
