/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.idl.client;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterTest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class for storing the test utils for IDL tests.
 *
 * @since 0.3.0
 */
public class GraphqlIDLTest {
    public static final Path RESOURCE = Paths.get("build/resources/test/graphql-client-projects").toAbsolutePath();

    @AfterTest
    public void removeGeneratedFile() throws IOException {
        deleteGeneratedFiles();
    }

    /**
     * Deletes the generated files during tests.
     */
    public void deleteGeneratedFiles() throws IOException {
        File[] matchingGeneratedDirs = RESOURCE.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("generated") || pathname.getName().contains("target");
            }
        });
        if (matchingGeneratedDirs != null) {
            for (File generatedDir : matchingGeneratedDirs) {
                FileUtils.deleteDirectory(generatedDir);
            }
        }
    }
}
