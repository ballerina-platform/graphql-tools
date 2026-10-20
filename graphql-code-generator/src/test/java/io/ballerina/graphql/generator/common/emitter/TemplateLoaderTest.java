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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Tests loading and filling Ballerina code templates.
 */
public class TemplateLoaderTest {

    private static final String ENUM_TEMPLATE = "templates/common/enum.bal";

    @Test(description = "Test that placeholder text inside a value is written as it is")
    public void testFillKeepsPlaceholderTextInValues() {
        String filled = TemplateLoader.fill(ENUM_TEMPLATE, Map.of(
                "docs", "# Use {{name}} in the query\n",
                "name", "Genre",
                "values", "    FICTION"));

        Assert.assertEquals(filled, """
                # Use {{name}} in the query
                public enum Genre {
                    FICTION
                }""");
    }

    @Test(description = "Test that a missing template is reported")
    public void testFillMissingTemplate() {
        IllegalStateException error = Assert.expectThrows(IllegalStateException.class,
                () -> TemplateLoader.fill("templates/common/missing.bal", Map.of()));
        Assert.assertEquals(error.getMessage(), "Template \"templates/common/missing.bal\" was not found.");
    }

    @Test(description = "Test that a placeholder without a value is reported")
    public void testFillMissingValue() {
        IllegalStateException error = Assert.expectThrows(IllegalStateException.class,
                () -> TemplateLoader.fill(ENUM_TEMPLATE, Map.of("docs", "", "name", "Genre")));
        Assert.assertEquals(error.getMessage(),
                "No value was given for \"values\" in template \"templates/common/enum.bal\".");
    }
}
