/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.graphql.cmd.GraphqlCmd;
import io.ballerina.graphql.common.GraphqlTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_SCHEMA_URL;

/**
 * This class is used to test the functionality of the GraphQL configuration file validator.
 */
public class ConfigValidatorTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(ConfigValidatorTest.class);

    @Test(description = "Test graphql command execution with invalid schema URL")
    public void testValidateWithInvalidSchemaUrl() {
        Path graphqlConfigYaml =
                resourceDir.resolve(Paths.get("specs", "graphql-config-with-invalid-sdl-url.yaml"));
        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
        GraphqlCmd graphqlCmd = new GraphqlCmd(this.printStream, this.tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        String output = "";
        try {
            graphqlCmd.execute();
            output = readOutput(true);

            log.info(output);
            log.info(MESSAGE_FOR_INVALID_SCHEMA_URL);

            Assert.assertTrue(output.contains(MESSAGE_FOR_INVALID_SCHEMA_URL));
        } catch (BLauncherException | IOException e) {
            output = e.toString();
            Assert.fail(e.getMessage());
        }
    }

//    @Test(description = "Test graphql command execution with invalid schema path",
//            dependsOnMethods = "testValidateWithInvalidSchemaUrl")
//    public void testValidateWithInvalidSchemaPath() {
//        Path graphqlConfigYaml =
//                resourceDir.resolve(Paths.get("specs", "graphql-config-with-invalid-schema-path.yaml"));
//        String[] args = {"-i", graphqlConfigYaml.toString(), "-o", this.tmpDir.toString()};
//        GraphqlCmd graphqlCmd = new GraphqlCmd(this.printStream, this.tmpDir, false);
//        new CommandLine(graphqlCmd).parseArgs(args);
//        String output = "";
//        try {
//            graphqlCmd.execute();
//            output = readOutput(true);
//
//            log.info(output);
//
//            Assert.assertTrue(output.contains(MESSAGE_FOR_INVALID_SCHEMA_PATH));
//        } catch (BLauncherException | IOException e) {
//            output = e.toString();
//            Assert.fail(e.getMessage());
//        }
//    }
}
