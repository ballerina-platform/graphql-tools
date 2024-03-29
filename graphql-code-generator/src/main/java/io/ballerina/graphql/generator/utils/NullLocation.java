/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.utils;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * This {@code NullLocation} represents the null location allocation for scenarios which has no location.
 */
public final class NullLocation implements Location {

    private static NullLocation nullLocation = null;

    private NullLocation() {
    }

    public static NullLocation getInstance() {
        if (nullLocation == null) {
            nullLocation = new NullLocation();
        }
        return nullLocation;
    }

    @Override
    public LineRange lineRange() {
        LinePosition from = LinePosition.from(-2, -2);
        return LineRange.from("", from, from);
    }

    @Override
    public TextRange textRange() {
        return TextRange.from(-2, -2);
    }
}
