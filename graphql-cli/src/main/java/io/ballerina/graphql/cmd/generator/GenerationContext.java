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
import java.util.Optional;

/**
 * Holds the resolved inputs for a single GraphQL generation operation.
 */
public class GenerationContext {

    private final String inputPath;
    private final OperationMode declaredOperationMode;
    private final Path targetOutputPath;
    private final String serviceBasePath;
    private final ObjectType declaredObjectType;
    private final boolean force;
    private final boolean dryRun;
    private final PrintStream outStream;
    private final DryRunReport dryRunReport = new DryRunReport();

    public GenerationContext(String inputPath, OperationMode declaredOperationMode, Path targetOutputPath,
                             String serviceBasePath, ObjectType declaredObjectType, boolean force, boolean dryRun,
                             PrintStream outStream) {
        this.inputPath = inputPath;
        this.declaredOperationMode = declaredOperationMode;
        this.targetOutputPath = targetOutputPath;
        this.serviceBasePath = serviceBasePath;
        this.declaredObjectType = declaredObjectType;
        this.force = force;
        this.dryRun = dryRun;
        this.outStream = outStream;
    }

    public String getInputPath() {
        return inputPath;
    }

    /**
     * Returns the operation mode the user declared with the mode flag, if one was given. The mode to generate is
     * resolved from the input, and this is used to check that the input matches what the user asked for.
     */
    public Optional<OperationMode> getDeclaredOperationMode() {
        return Optional.ofNullable(declaredOperationMode);
    }

    public Path getTargetOutputPath() {
        return targetOutputPath;
    }

    public String getServiceBasePath() {
        return serviceBasePath;
    }

    public ObjectType getObjectType() {
        return declaredObjectType != null ? declaredObjectType : ObjectType.SERVICE;
    }

    public boolean isObjectTypeDeclared() {
        return declaredObjectType != null;
    }

    public boolean isUseRecordsForObjects() {
        return getObjectType() == ObjectType.RECORD;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public DryRunReport getDryRunReport() {
        return dryRunReport;
    }

    public PrintStream getOutStream() {
        return outStream;
    }
}
