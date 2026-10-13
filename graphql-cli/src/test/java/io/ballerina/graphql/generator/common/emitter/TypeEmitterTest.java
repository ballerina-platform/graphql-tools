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

import io.ballerina.graphql.generator.common.model.BalType;
import io.ballerina.graphql.generator.common.model.ListType;
import io.ballerina.graphql.generator.common.model.NamedType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests writing Ballerina type descriptors.
 */
public class TypeEmitterTest {

    @DataProvider(name = "types")
    public Object[][] types() {
        return new Object[][]{
                {new NamedType("int", false), "int"},
                {new NamedType("Book", true), "Book?"},
                {new NamedType("graphql:Upload", false), "graphql:Upload"},
                {new ListType(new NamedType("Book", false), true), "Book[]?"},
                {new ListType(new NamedType("Book", true), true), "Book?[]?"},
                {new ListType(new NamedType("string", false), false), "string[]"},
                {new ListType(new ListType(new NamedType("int", true), false), true), "int?[][]?"}
        };
    }

    @Test(description = "Test writing type descriptors", dataProvider = "types")
    public void testEmit(BalType type, String expectedDescriptor) {
        Assert.assertEquals(TypeEmitter.emit(type), expectedDescriptor);
    }

    @DataProvider(name = "annotatedTypes")
    public Object[][] annotatedTypes() {
        return new Object[][]{
                {new NamedType("string", false, true), "@graphql:ID string"},
                {new ListType(new NamedType("string", true, true), true), "@graphql:ID string?[]?"},
                {new NamedType("graphql:Upload", false), "graphql:Upload"},
                {new NamedType("string", false), "string"}
        };
    }

    @Test(description = "Test writing ID types with the graphql:ID annotation", dataProvider = "annotatedTypes")
    public void testEmitAnnotated(BalType type, String expectedDescriptor) {
        Assert.assertEquals(TypeEmitter.emitAnnotated(type), expectedDescriptor);
    }

    @Test(description = "Test writing a union type descriptor")
    public void testEmitUnion() {
        Assert.assertEquals(TypeEmitter.emitUnion(List.of("Book", "Magazine")), "Book|Magazine");
    }
}
