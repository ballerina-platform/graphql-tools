/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.graphql.generator.model;

/**
 * Model class representing generated source file information.
 */
public class SrcFilePojo {
    private GenFileType type;
    private String moduleName;
    private String fileName;
    private String content;

    /**
     * Type specifier for generated source files.
     */
    public enum GenFileType {
        GEN_SRC,
        MODEL_SRC,
        IMPL_SRC,
        TEST_SRC,
        RES;

        public boolean isOverwritable() {
            if (this == GEN_SRC || this == RES || this == MODEL_SRC) {
                return true;
            }

            return false;
        }
    }

    public SrcFilePojo(GenFileType type, String moduleName, String fileName, String content) {
        this.type = type;
        this.moduleName = moduleName;
        this.fileName = fileName;
        this.content = content;
    }

    public GenFileType getType() {
        return type;
    }

    public void setType(GenFileType type) {
        this.type = type;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
