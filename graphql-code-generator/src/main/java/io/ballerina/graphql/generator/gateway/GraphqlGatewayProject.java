/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.generator.gateway;

import io.ballerina.graphql.generator.GraphqlProject;
import io.ballerina.graphql.generator.utils.GenerationType;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class to represent GraphQL federation gateway generation project.
 */
public class GraphqlGatewayProject extends GraphqlProject {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphqlGatewayProject.class);

    public static GenerationType generationType = GenerationType.GATEWAY;
    private final Path tempDir;

    public GraphqlGatewayProject(String name, String schema, String outputPath) throws IOException {
        super(name, schema, outputPath);
        tempDir = Files.createTempDirectory(".gateway-tmp" + System.nanoTime());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException ex) {
                LOGGER.error("Unable to delete the temporary directory : " + tempDir, ex);
            }
        }));
    }

    @Override
    public GenerationType getGenerationType() {
        return generationType;
    }

    public Path getTempDir() {
        return tempDir;
    }
}
