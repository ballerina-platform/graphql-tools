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

package io.ballerina.graphql.generator;

import io.ballerina.graphql.generator.client.exception.ClientCodeGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.utils.BallerinaFileMerger;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This class implements the GraphQL code generator tool.
 */
public abstract class CodeGenerator {

    /**
     * Generates the code for a given GraphQL project.
     *
     * @param project the instance of the GraphQL project
     * @throws ClientCodeGenerationException when a code generation error occurs
     */
    public abstract void generate(GraphqlProject project)
            throws ClientCodeGenerationException, ServiceGenerationException;

    /**
     * Writes the generated Ballerina source codes to the files in the specified {@code outputPath}.
     *
     * @param sources    the list of generated Ballerina source file pojo
     * @param outputPath the target output path for the code generation
     * @throws IOException If an I/O error occurs
     */
    protected void writeGeneratedSources(List<SrcFilePojo> sources, Path outputPath) throws IOException {
        writeGeneratedSources(sources, outputPath, false);
    }

    /**
     * Writes the generated Ballerina source codes to the files in the specified {@code outputPath}.
     *
     * @param sources    the list of generated Ballerina source file pojo
     * @param outputPath the target output path for the code generation
     * @param refresh    whether to refresh existing files or overwrite them
     * @throws IOException If an I/O error occurs
     */
    protected void writeGeneratedSources(List<SrcFilePojo> sources, Path outputPath,
            boolean refresh) throws IOException {
        if (sources == null) {
            throw new IllegalArgumentException("Sources list cannot be null");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path cannot be null");
        }
        if (sources.isEmpty()) {
            return;
        }
        for (SrcFilePojo file : sources) {
            if (file == null) {
                continue;
            }
            if (!file.getType().isOverwritable()) {
                continue;
            }
            Path filePath = CodeGeneratorUtils.getAbsoluteFilePath(file, outputPath);
            if (filePath == null) {
                continue;
            }
            String fileContent = file.getContent();
            if (fileContent == null) {
                fileContent = "";
            }
            if (refresh && Files.exists(filePath)) {
                try {
                    // For refresh, merge with existing file
                    fileContent = BallerinaFileMerger.mergeFiles(filePath, fileContent);
                } catch (Exception e) {
                    // If merge fails, log the error and continue with generated content
                }
            }
            CodeGeneratorUtils.writeFile(filePath, fileContent);
        }
    }
}
