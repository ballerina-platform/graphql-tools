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

import io.ballerina.graphql.generator.common.model.TypeModel;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests writing Ballerina type descriptors.
 */
public class TypeEmitterTest {

    private static final boolean NULLABLE = true;
    private static final boolean NON_NULL = false;
    private static final boolean ID = true;

    @DataProvider(name = "types")
    public Object[][] types() {
        return new Object[][]{
                {TypeModel.named("int", NON_NULL), "int"},
                {TypeModel.named("Book", NULLABLE), "Book?"},
                {TypeModel.named("graphql:Upload", NON_NULL), "graphql:Upload"},
                {TypeModel.list(TypeModel.named("Book", NON_NULL), NULLABLE), "Book[]?"},
                {TypeModel.list(TypeModel.named("Book", NULLABLE), NULLABLE), "Book?[]?"},
                {TypeModel.list(TypeModel.named("string", NON_NULL), NON_NULL), "string[]"},
                {TypeModel.list(TypeModel.list(TypeModel.named("int", NULLABLE), NON_NULL), NULLABLE), "int?[][]?"}
        };
    }

    @Test(description = "Test writing type descriptors", dataProvider = "types")
    public void testEmit(TypeModel type, String expectedDescriptor) {
        Assert.assertEquals(TypeEmitter.emit(type), expectedDescriptor);
    }

    @DataProvider(name = "annotatedTypes")
    public Object[][] annotatedTypes() {
        return new Object[][]{
                {TypeModel.named("string", NON_NULL, ID), "@graphql:ID string"},
                {TypeModel.list(TypeModel.named("string", NULLABLE, ID), NULLABLE), "@graphql:ID string?[]?"},
                {TypeModel.named("graphql:Upload", NON_NULL), "graphql:Upload"},
                {TypeModel.named("string", NON_NULL), "string"}
        };
    }

    @Test(description = "Test writing ID types with the graphql:ID annotation", dataProvider = "annotatedTypes")
    public void testEmitAnnotated(TypeModel type, String expectedDescriptor) {
        Assert.assertEquals(TypeEmitter.emitAnnotated(type), expectedDescriptor);
    }

    @Test(description = "Test writing a union type descriptor")
    public void testEmitUnion() {
        Assert.assertEquals(TypeEmitter.emitUnion(List.of("Book", "Magazine")), "Book|Magazine");
    }
}
