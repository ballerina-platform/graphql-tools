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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests writing Ballerina documentation lines.
 */
public class DocEmitterTest {

    private static final String NO_INDENT = "";
    private static final String MEMBER_INDENT = "    ";

    @Test(description = "Test that nothing is written when there is nothing to document")
    public void testEmitNothing() {
        Assert.assertEquals(DocEmitter.emit(null, null, NO_INDENT), "");
    }

    @Test(description = "Test writing a multi-line description with indentation")
    public void testEmitDescription() {
        Assert.assertEquals(DocEmitter.emit("Line one\n\nLine two", null, MEMBER_INDENT), """
                    # Line one
                    #
                    # Line two
                """);
    }

    @Test(description = "Test writing parameter descriptions, skipping parameters without one")
    public void testEmitParameters() {
        Map<String, String> parameterDescriptions = new LinkedHashMap<>();
        parameterDescriptions.put("id", "The id of the book");
        parameterDescriptions.put("title", null);
        Assert.assertEquals(DocEmitter.emit("Fetch a book", parameterDescriptions, null, NO_INDENT), """
                # Fetch a book
                # + id - The id of the book
                """);
    }

    @Test(description = "Test writing a deprecation with its reason and annotation")
    public void testEmitDeprecation() {
        Assert.assertEquals(DocEmitter.emit("The title of the book", "Use `name` instead", MEMBER_INDENT), """
                    # The title of the book
                    # # Deprecated
                    # Use `name` instead
                    @deprecated
                """);
    }
}
