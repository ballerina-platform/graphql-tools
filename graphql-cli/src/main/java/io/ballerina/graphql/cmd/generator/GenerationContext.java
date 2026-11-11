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

package io.ballerina.graphql.cmd.generator;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Holds the resolved inputs for a single GraphQL generation operation. It is created with {@link #builder(String)},
 * so each input is set by name and an input that was not given keeps its default.
 */
public class GenerationContext {

    private final String inputPath;
    private final OperationMode declaredOperationMode;
    private final Path targetOutputPath;
    private final String serviceBasePath;
    private final ObjectType declaredObjectType;
    private final String moduleName;
    private final String serviceName;
    private final boolean force;
    private final boolean dryRun;
    private final boolean merge;
    private final OverwritePrompt overwritePrompt;
    private final PrintStream outStream;
    private final DryRunReport dryRunReport = new DryRunReport();

    private GenerationContext(Builder builder) {
        this.inputPath = builder.inputPath;
        this.declaredOperationMode = builder.declaredOperationMode;
        this.targetOutputPath = builder.targetOutputPath;
        this.serviceBasePath = builder.serviceBasePath;
        this.declaredObjectType = builder.declaredObjectType;
        this.moduleName = builder.moduleName;
        this.serviceName = builder.serviceName;
        this.force = builder.force;
        this.dryRun = builder.dryRun;
        this.merge = builder.merge;
        this.overwritePrompt = builder.overwritePrompt;
        this.outStream = builder.outStream;
    }

    /**
     * Starts a context for the given input. The flags default to off, and the other inputs to not given.
     *
     * @param inputPath the path of the input file
     * @return a builder for the context
     */
    public static Builder builder(String inputPath) {
        return new Builder(inputPath);
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

    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns the name the user gave to name the generated service after, or {@code null} if none was given.
     */
    public String getServiceName() {
        return serviceName;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Returns whether existing output files are merged with the generated code instead of overwritten.
     */
    public boolean isMerge() {
        return merge;
    }

    public OverwritePrompt getOverwritePrompt() {
        return overwritePrompt;
    }

    public DryRunReport getDryRunReport() {
        return dryRunReport;
    }

    public PrintStream getOutStream() {
        return outStream;
    }

    /**
     * Builds a {@link GenerationContext} one input at a time.
     */
    public static final class Builder {

        private final String inputPath;
        private OperationMode declaredOperationMode;
        private Path targetOutputPath;
        private String serviceBasePath;
        private ObjectType declaredObjectType;
        private String moduleName;
        private String serviceName;
        private boolean force;
        private boolean dryRun;
        private boolean merge;
        private OverwritePrompt overwritePrompt;
        private PrintStream outStream;

        private Builder(String inputPath) {
            this.inputPath = inputPath;
        }

        public Builder declaredOperationMode(OperationMode declaredOperationMode) {
            this.declaredOperationMode = declaredOperationMode;
            return this;
        }

        public Builder targetOutputPath(Path targetOutputPath) {
            this.targetOutputPath = targetOutputPath;
            return this;
        }

        public Builder serviceBasePath(String serviceBasePath) {
            this.serviceBasePath = serviceBasePath;
            return this;
        }

        public Builder declaredObjectType(ObjectType declaredObjectType) {
            this.declaredObjectType = declaredObjectType;
            return this;
        }

        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder merge(boolean merge) {
            this.merge = merge;
            return this;
        }

        public Builder overwritePrompt(OverwritePrompt overwritePrompt) {
            this.overwritePrompt = overwritePrompt;
            return this;
        }

        public Builder outStream(PrintStream outStream) {
            this.outStream = outStream;
            return this;
        }

        public GenerationContext build() {
            return new GenerationContext(this);
        }
    }
}
