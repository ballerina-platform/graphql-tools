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

package io.ballerina.graphql.cmd.generator;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.graphql.cmd.Constants.BAL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MODE_CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MODE_SCHEMA;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MODE_SERVICE;

/**
 * The operation mode for the GraphQL code generator.
 */
public enum OperationMode {

    CLIENT(MODE_CLIENT, Set.of(YAML_EXTENSION, YML_EXTENSION)),
    SCHEMA(MODE_SCHEMA, Set.of(BAL_EXTENSION)),
    SERVICE(MODE_SERVICE, Set.of(GRAPHQL_EXTENSION));

    private final String modeFlag;
    private final Set<String> extensions;

    OperationMode(String modeFlag, Set<String> extensions) {
        this.modeFlag = modeFlag;
        this.extensions = extensions;
    }

    public String getModeFlag() {
        return modeFlag;
    }

    public boolean accepts(String inputPath) {
        for (String extension : extensions) {
            if (inputPath.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<OperationMode> fromInputPath(String inputPath) {
        return Arrays.stream(OperationMode.values())
                .filter(mode -> mode.accepts(inputPath))
                .findFirst();
    }

    public static Optional<OperationMode> fromModeFlag(String modeFlag) {
        if (modeFlag == null) {
            return Optional.empty();
        }
        return Arrays.stream(OperationMode.values())
                .filter(mode -> mode.modeFlag.equals(modeFlag))
                .findFirst();
    }

    public static boolean isKnownExtension(String inputPath) {
        return fromInputPath(inputPath).isPresent();
    }
}
