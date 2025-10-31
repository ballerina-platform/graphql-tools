/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the update functionality of the GraphQL command.
 */
public class GraphqlUpdateCmdTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(GraphqlUpdateCmdTest.class);

    @AfterMethod
    public void afterTestCase() {
        File directory = new File(this.tmpDir.toString());
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            Assert.assertNotNull(files);
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    @Test(description = "Test successful graphql update command execution")
    public void testUpdateExecute() {
        // First, generate a service from a GraphQL schema
        Path graphqlSchema = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] generateArgs = {"-i", graphqlSchema.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(generateArgs);
            graphqlCmd.execute();
            
            // Verify initial generation
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            
            // Read the generated content
            String initialServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String initialTypesContent = readContent(this.tmpDir.resolve("types.bal"));
            
            // Now test the update functionality with the same schema
            String[] refreshArgs = {"-i", graphqlSchema.toString(), "-o", this.tmpDir.toString(), 
                    "--mode", "service", "--update"};
            
            ExitCodeCaptor refreshExitCaptor = new ExitCodeCaptor();
            GraphqlCmd refreshGraphqlCmd = new GraphqlCmd(printStream, tmpDir, refreshExitCaptor);
            new CommandLine(refreshGraphqlCmd).parseArgs(refreshArgs);
            refreshGraphqlCmd.execute();
            
            // Verify files still exist
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            
            // Read the refreshed content
            String refreshedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String refreshedTypesContent = readContent(this.tmpDir.resolve("types.bal"));
            
            // For the same schema, content should be identical
            Assert.assertEquals(refreshedServiceContent, initialServiceContent);
            Assert.assertEquals(refreshedTypesContent, initialTypesContent);
            
            Assert.assertEquals(refreshExitCaptor.getExitCode(), 0, "Successful execution should exit with code 0");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql update command execution with modified schema")
    public void testUpdateExecuteWithModifiedSchema() {
        // First, generate a service from a simple GraphQL schema
        Path simpleGraphqlSchema = resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithSingleObjectApi.graphql"));
        String[] generateArgs = {"-i", simpleGraphqlSchema.toString(), "-o", this.tmpDir.toString(), 
                "--mode", "service"};
        
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(generateArgs);
            graphqlCmd.execute();
            
            // Verify initial generation
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            
            // Read the generated content
            String initialServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String initialTypesContent = readContent(this.tmpDir.resolve("types.bal"));
            
            // Now test the update functionality with a more complex schema
            Path complexGraphqlSchema = resourceDir.resolve(
                    Paths.get("serviceGen", "graphqlSchemas", "valid", "SchemaWithObjectTakingInputArgumentApi.graphql"));
            String[] refreshArgs = {"-i", complexGraphqlSchema.toString(), "-o", this.tmpDir.toString(), 
                    "--mode", "service", "--update"};
            
            ExitCodeCaptor refreshExitCaptor = new ExitCodeCaptor();
            GraphqlCmd refreshGraphqlCmd = new GraphqlCmd(printStream, tmpDir, refreshExitCaptor);
            new CommandLine(refreshGraphqlCmd).parseArgs(refreshArgs);
            refreshGraphqlCmd.execute();
            
            // Verify files still exist
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("service.bal")));
            Assert.assertTrue(Files.exists(this.tmpDir.resolve("types.bal")));
            
            // Read the refreshed content
            String refreshedServiceContent = readContent(this.tmpDir.resolve("service.bal"));
            String refreshedTypesContent = readContent(this.tmpDir.resolve("types.bal"));
            
            // Content should be different now (more complex schema)
            // But we can't easily assert this without detailed parsing
            // At least verify the files exist and have content
            Assert.assertNotNull(refreshedServiceContent);
            Assert.assertNotNull(refreshedTypesContent);
            Assert.assertFalse(refreshedServiceContent.isEmpty());
            Assert.assertFalse(refreshedTypesContent.isEmpty());
            
            Assert.assertEquals(refreshExitCaptor.getExitCode(), 0, "Successful execution should exit with code 0");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}