/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.cmd;

import io.ballerina.graphql.cmd.GraphqlCmd.ExitHandler;

/**
 * Public test helper for capturing exit codes in tests.
 * This class implements the ExitHandler interface to capture the exit code
 * without actually terminating the JVM.
 */
public class ExitCodeCaptor implements ExitHandler {
    private int exitCode = -1;
    private boolean exitCalled = false;

    @Override
    public void exit(int code) {
        this.exitCode = code;
        this.exitCalled = true;
    }

    /**
     * Get the captured exit code.
     *
     * @return the exit code that was passed to exit()
     * @throws IllegalStateException if exit() was not called
     */
    public int getExitCode() {
        if (!exitCalled) {
            throw new IllegalStateException("exit() was not called");
        }
        return exitCode;
    }

    /**
     * Check if exit() was called.
     *
     * @return true if exit() was called, false otherwise
     */
    public boolean wasExitCalled() {
        return exitCalled;
    }
}
