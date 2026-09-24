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

import io.ballerina.graphql.exception.GenerationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the functionality of the GeneratorFactory.
 */
public class GeneratorFactoryTest {

    private static final Path OUTPUT_PATH = Paths.get("build");

    private GenerationContext createContext(OperationMode operationMode, String inputPath) {
        return new GenerationContext(operationMode, inputPath, OUTPUT_PATH, null, false, System.out);
    }

    @Test(description = "Test generating a client generator for the client operation mode")
    public void testGetGeneratorForClientMode() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(
                createContext(OperationMode.CLIENT, "graphql.config.yaml"));
        Assert.assertTrue(generator instanceof ClientGeneration);
    }

    @Test(description = "Test generating a service generator for the service operation mode")
    public void testGetGeneratorForServiceMode() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(
                createContext(OperationMode.SERVICE, "schema.graphql"));
        Assert.assertTrue(generator instanceof ServiceGeneration);
    }

    @Test(description = "Test generating a schema generator for the schema operation mode")
    public void testGetGeneratorForSchemaMode() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(
                createContext(OperationMode.SCHEMA, "service.bal"));
        Assert.assertTrue(generator instanceof SchemaGeneration);
    }

    @Test(description = "Test whether the created generator is given the same generation context")
    public void testGeneratorReceivesContext() throws GenerationException {
        GenerationContext context = createContext(OperationMode.SERVICE, "schema.graphql");
        Generator generator = GeneratorFactory.getGenerator(context);
        Assert.assertNotNull(generator);
        Assert.assertEquals(context.getOperationMode(), OperationMode.SERVICE);
        Assert.assertEquals(context.getInputPath(), "schema.graphql");
    }
}
