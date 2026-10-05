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

package io.ballerina.graphql.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.common.GraphqlTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// This class is used to test that only a file named "balGraphQL.toml" is accepted as a GraphQL configuration file.
public class BalGraphqlConfigFileNameTest extends GraphqlTest {

    private static final String CONFIG_CONTENT = "[schema]\nsource = \"file\"\npath = \"./schema.graphql\"\n";

    @Test(description = "Test graphql command execution with a correctly named balGraphQL.toml file")
    public void testExecuteWithCorrectConfigFileName() throws IOException {
        Path configFile = writeConfigFile("balGraphQL.toml");
        String[] args = {configFile.toString(), "-o", this.tmpDir.toString()};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertFalse(output.contains("should be named"),
                    "A correctly named configuration file should not be rejected for its name: " + output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with an incorrectly named .toml file")
    public void testExecuteWithIncorrectConfigFileName() throws IOException {
        Path configFile = writeConfigFile("myconfig.toml");
        String[] args = {configFile.toString(), "-o", this.tmpDir.toString()};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("should be named \"balGraphQL.toml\""),
                    "Unexpected output: " + output);
            Assert.assertTrue(output.contains("myconfig.toml"), "Unexpected output: " + output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1,
                    "An incorrectly named configuration file should fail the command");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test graphql command execution with a .toml file name that differs only in case")
    public void testExecuteWithWrongCaseConfigFileName() throws IOException {
        Path configFile = writeConfigFile("balgraphql.toml");
        String[] args = {configFile.toString(), "-o", this.tmpDir.toString()};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("should be named \"balGraphQL.toml\""),
                    "The file name check should be case-sensitive. Unexpected output: " + output);
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Test that the schema mode is rejected for a balGraphQL.toml input")
    public void testExecuteWithSchemaModeForConfigFile() throws IOException {
        Path configFile = writeConfigFile("balGraphQL.toml");
        String[] args = {configFile.toString(), "-o", this.tmpDir.toString(), "-m", "schema"};
        try {
            ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
            GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
            new CommandLine(graphqlCmd).parseArgs(args);
            graphqlCmd.execute();
            String output = readOutput(true);
            Assert.assertTrue(output.contains("\"schema\" is not a supported argument for mode flag"), 
                    "Unexpected output: " + output);
            Assert.assertEquals(exitCaptor.getExitCode(), 1, "The schema mode should fail the command");
        } catch (BLauncherException | IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    private Path writeConfigFile(String fileName) throws IOException {
        Path configFile = this.tmpDir.resolve(fileName);
        Files.writeString(configFile, CONFIG_CONTENT);
        return configFile;
    }
}
