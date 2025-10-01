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

package io.ballerina.graphql.validator;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.graphql.cmd.ExitCodeCaptor;
import io.ballerina.graphql.cmd.GraphqlCmd;
import io.ballerina.graphql.common.GraphqlTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the functionality of the GraphQL query files validator.
 */
public class QueryValidatorTest extends GraphqlTest {

    @Test(description = "Test graphql command execution with invalid query file")
    public void testValidate() {
        Path graphqlConfigYaml =
                resourceDir.resolve(Paths.get("specs", "graphql-config-with-invalid-query-file.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        ExitCodeCaptor exitCaptor = new ExitCodeCaptor();
        GraphqlCmd graphqlCmd = new GraphqlCmd(printStream, tmpDir, exitCaptor);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);
            Assert.assertTrue(output.contains("Graph query validation failed."));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(e.getMessage());
        }
    }
}
