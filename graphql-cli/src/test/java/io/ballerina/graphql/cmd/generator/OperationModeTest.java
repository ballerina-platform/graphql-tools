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

package io.ballerina.graphql.cmd.generator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;

/**
 * This class is used to test the functionality of the OperationMode.
 */
public class OperationModeTest {

    @Test(description = "Test resolving the operation mode from the input file extension")
    public void testFromInputPath() {
        Assert.assertEquals(OperationMode.fromInputPath("schema.graphql"), Optional.of(OperationMode.SERVICE));
        Assert.assertEquals(OperationMode.fromInputPath("service.bal"), Optional.of(OperationMode.SCHEMA));
        Assert.assertEquals(OperationMode.fromInputPath("graphql.config.yaml"), Optional.of(OperationMode.CLIENT));
        Assert.assertEquals(OperationMode.fromInputPath("graphql.config.yml"), Optional.of(OperationMode.CLIENT));
    }

    @Test(description = "Test resolving the operation mode from an unsupported input file extension")
    public void testFromInputPathWithUnsupportedExtension() {
        Assert.assertTrue(OperationMode.fromInputPath("notes.txt").isEmpty());
        Assert.assertTrue(OperationMode.fromInputPath("schema").isEmpty());
    }

    @Test(description = "Test resolving the operation mode from a path with directories")
    public void testFromInputPathWithDirectories() {
        Assert.assertEquals(OperationMode.fromInputPath("./resources/schema.graphql"),
                Optional.of(OperationMode.SERVICE));
        Assert.assertEquals(OperationMode.fromInputPath("/home/user/project/service.bal"),
                Optional.of(OperationMode.SCHEMA));
    }

    @Test(description = "Test resolving the operation mode from the mode flag")
    public void testFromModeFlag() {
        Assert.assertEquals(OperationMode.fromModeFlag("client"), Optional.of(OperationMode.CLIENT));
        Assert.assertEquals(OperationMode.fromModeFlag("service"), Optional.of(OperationMode.SERVICE));
        Assert.assertEquals(OperationMode.fromModeFlag("schema"), Optional.of(OperationMode.SCHEMA));
    }

    @Test(description = "Test resolving the operation mode from an invalid mode flag")
    public void testFromModeFlagWithInvalidValue() {
        Assert.assertTrue(OperationMode.fromModeFlag("clinet").isEmpty());
        Assert.assertTrue(OperationMode.fromModeFlag("").isEmpty());
        Assert.assertTrue(OperationMode.fromModeFlag(null).isEmpty());
    }

    @Test(description = "Test whether an input file extension is supported")
    public void testIsKnownExtension() {
        Assert.assertTrue(OperationMode.isKnownExtension("schema.graphql"));
        Assert.assertTrue(OperationMode.isKnownExtension("service.bal"));
        Assert.assertTrue(OperationMode.isKnownExtension("graphql.config.yaml"));
        Assert.assertTrue(OperationMode.isKnownExtension("graphql.config.yml"));
        Assert.assertFalse(OperationMode.isKnownExtension("notes.txt"));
    }

    @Test(description = "Test whether each operation mode accepts only its own input file extensions")
    public void testAccepts() {
        Assert.assertTrue(OperationMode.SERVICE.accepts("schema.graphql"));
        Assert.assertFalse(OperationMode.SERVICE.accepts("service.bal"));

        Assert.assertTrue(OperationMode.SCHEMA.accepts("service.bal"));
        Assert.assertFalse(OperationMode.SCHEMA.accepts("schema.graphql"));

        Assert.assertTrue(OperationMode.CLIENT.accepts("graphql.config.yaml"));
        Assert.assertTrue(OperationMode.CLIENT.accepts("graphql.config.yml"));
        Assert.assertFalse(OperationMode.CLIENT.accepts("schema.graphql"));
    }

    @Test(description = "Test retrieving the mode flag value of each operation mode")
    public void testGetModeFlag() {
        Assert.assertEquals(OperationMode.CLIENT.getModeFlag(), "client");
        Assert.assertEquals(OperationMode.SERVICE.getModeFlag(), "service");
        Assert.assertEquals(OperationMode.SCHEMA.getModeFlag(), "schema");
    }
}
