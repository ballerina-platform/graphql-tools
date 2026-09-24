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

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Holds the resolved inputs for a single GraphQL generation operation.
 */
public class GenerationContext {

    private final OperationMode operationMode;
    private final String inputPath;
    private final Path targetOutputPath;
    private final String serviceBasePath;
    private final boolean useRecordsForObjects;
    private final PrintStream outStream;

    public GenerationContext(OperationMode operationMode, String inputPath, Path targetOutputPath,
                             String serviceBasePath, boolean useRecordsForObjects, PrintStream outStream) {
        this.operationMode = operationMode;
        this.inputPath = inputPath;
        this.targetOutputPath = targetOutputPath;
        this.serviceBasePath = serviceBasePath;
        this.useRecordsForObjects = useRecordsForObjects;
        this.outStream = outStream;
    }

    public OperationMode getOperationMode() {
        return operationMode;
    }

    public String getInputPath() {
        return inputPath;
    }

    public Path getTargetOutputPath() {
        return targetOutputPath;
    }

    public String getServiceBasePath() {
        return serviceBasePath;
    }

    public boolean isUseRecordsForObjects() {
        return useRecordsForObjects;
    }

    public PrintStream getOutStream() {
        return outStream;
    }
}
