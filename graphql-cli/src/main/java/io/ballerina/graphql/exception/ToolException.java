/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.exception;

/**
 * An error that ends the command with a given exit code, following the exit codes of the Ballerina CLI Tool Design
 * Guide.
 */
public final class ToolException extends RuntimeException {

    /**
     * Any failure that is not a usage error or an invalid target.
     */
    public static final int RUNTIME = 1;

    /**
     * An invalid invocation, such as an unknown flag, a missing input or an invalid value.
     */
    public static final int USAGE = 2;

    /**
     * The target is not a valid Ballerina package.
     */
    public static final int INVALID_TARGET = 3;

    private final int exitCode;

    public ToolException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public int exitCode() {
        return exitCode;
    }
}
