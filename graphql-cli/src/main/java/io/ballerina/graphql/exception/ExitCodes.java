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
 * The exit codes of the command, following the Ballerina CLI Tool Design Guide.
 */
public final class ExitCodes {

    /**
     * The command finished without errors.
     */
    public static final int SUCCESS = 0;

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

    /**
     * An existing file was not overwritten.
     */
    public static final int OVERWRITE_REFUSED = 4;

    /**
     * The command was interrupted, e.g. with Ctrl+C.
     */
    public static final int INTERRUPTED = 130;

    private ExitCodes() {
    }
}
