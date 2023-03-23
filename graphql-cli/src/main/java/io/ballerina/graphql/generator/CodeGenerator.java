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

package io.ballerina.graphql.generator;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.ClientTypesGenerationException;
import io.ballerina.graphql.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ServiceGenerationException;
import io.ballerina.graphql.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.ballerina.ServiceGenerator;
import io.ballerina.graphql.generator.ballerina.ServiceTypesGenerator;
import io.ballerina.graphql.generator.model.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * This class implements the GraphQL client code generator tool.
 */
public abstract class CodeGenerator {
    private static CodeGenerator codeGenerator = null;
    private ServiceGenerator serviceGenerator;
    private ServiceTypesGenerator serviceTypesGenerator;

    /**
     * Generates the code for a given GraphQL project.
     *
     * @param project the instance of the GraphQL project
     * @throws GenerationException when a code generation error occurs
     */
    public void generate(GraphqlProject project) throws GenerationException {
        String outputPath = project.getOutputPath();
        try {
            List<SrcFilePojo> genSources = generateBalSources(project, GeneratorContext.CLI);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (ServiceGenerationException | ClientGenerationException | UtilsGenerationException |
                 ClientTypesGenerationException | IOException e) {
            throw new GenerationException(e.getMessage(), project.getName());
        }
    }

    public abstract List<SrcFilePojo> generateBalSources(GraphqlProject project, GeneratorContext generatorContext)
            throws ServiceGenerationException, ClientGenerationException, UtilsGenerationException, IOException,
            ConfigTypesGenerationException, ClientTypesGenerationException, ServiceTypesGenerationException;

    /**
     * Writes the generated Ballerina source codes to the files in the specified {@code outputPath}.
     *
     * @param sources    the list of generated Ballerina source file pojo
     * @param outputPath the target output path for the code generation
     * @throws IOException If an I/O error occurs
     */
    protected void writeGeneratedSources(List<SrcFilePojo> sources, Path outputPath) throws IOException {
        if (!sources.isEmpty()) {
            for (SrcFilePojo file : sources) {
                if (file.getType().isOverwritable()) {
                    Path filePath = CodeGeneratorUtils.getAbsoluteFilePath(file, outputPath);
                    String fileContent = file.getContent();
                    CodeGeneratorUtils.writeFile(filePath, fileContent);
                }
            }
        }
    }
}
