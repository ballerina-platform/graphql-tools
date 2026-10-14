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

import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.EnumValueModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests writing Ballerina enums.
 */
public class EnumEmitterTest {

    private static final String NO_DESCRIPTION = null;
    private static final String NOT_DEPRECATED = null;

    @Test(description = "Test writing an enum with docs and a deprecated value")
    public void testEmit() {
        EnumModel status = new EnumModel("Status", List.of(
                new EnumValueModel("AVAILABLE", "The book is available", NOT_DEPRECATED),
                new EnumValueModel("RESERVED", NO_DESCRIPTION, "Use AVAILABLE instead"),
                new EnumValueModel("NOT_AVAILABLE", NO_DESCRIPTION, NOT_DEPRECATED)), "Availability of the book");

        Assert.assertEquals(EnumEmitter.emit(status), """
                # Availability of the book
                public enum Status {
                    # The book is available
                    AVAILABLE,
                    # # Deprecated
                    # Use AVAILABLE instead
                    @deprecated
                    RESERVED,
                    NOT_AVAILABLE
                }""");
    }
}
