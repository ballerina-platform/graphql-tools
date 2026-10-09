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

package io.ballerina.graphql.generator.common.mapper;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests escaping GraphQL names that are Ballerina keywords.
 */
public class NameEscaperTest {

    @DataProvider(name = "names")
    public Object[][] names() {
        return new Object[][]{
                {"type", "'type"},
                {"map", "'map"},
                {"error", "'error"},
                {"book", "book"},
                {"graphql", "graphql"}
        };
    }

    @Test(description = "Test that only Ballerina keywords are quoted", dataProvider = "names")
    public void testEscape(String name, String expectedName) {
        Assert.assertEquals(NameEscaper.escape(name), expectedName);
    }
}
