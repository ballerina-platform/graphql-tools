/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    private static final Path CONFIG_DIR = Paths.get("src/test/resources/balGraphqlConfigs").toAbsolutePath();
    private static final boolean NO_FORCE = false;
    private static final boolean NO_DRY_RUN = false;

    private GenerationContext createContext(String inputPath) {
        return createContext(inputPath, null, null);
    }

    private GenerationContext createContext(String inputPath, OperationMode declaredOperationMode) {
        return createContext(inputPath, declaredOperationMode, null);
    }

    private GenerationContext createContext(String inputPath, ObjectType declaredObjectType) {
        return createContext(inputPath, null, declaredObjectType);
    }

    private GenerationContext createContext(String inputPath, OperationMode declaredOperationMode,
                                            ObjectType declaredObjectType) {
        return createContext(inputPath, declaredOperationMode, declaredObjectType, null);
    }

    private GenerationContext createContext(String inputPath, OperationMode declaredOperationMode,
                                            ObjectType declaredObjectType, String serviceName) {
        return new GenerationContext(inputPath, declaredOperationMode, OUTPUT_PATH, null, declaredObjectType, null,
                serviceName, NO_FORCE, NO_DRY_RUN, System.out);
    }

    private Generator getGeneratorForConfig(String fileName) throws GenerationException {
        return GeneratorFactory.getGenerator(createContext(CONFIG_DIR.resolve(fileName).toString()));
    }

    private Generator getGeneratorForConfigWithRecords(String fileName) throws GenerationException {
        return GeneratorFactory.getGenerator(createContext(CONFIG_DIR.resolve(fileName).toString(),
                ObjectType.RECORD));
    }

    @Test(description = "Test that an object type is rejected for a client configuration file")
    public void testGetGeneratorForClientConfigWithObjectType() {
        try {
            getGeneratorForConfigWithRecords("client-config.toml");
            Assert.fail("Expected a GenerationException for --object-type with a client configuration");
        } catch (GenerationException e) {
            Assert.assertTrue(e.getMessage().contains("only applies to service generation"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    @Test(description = "Test that a service name is rejected for a client configuration file")
    public void testGetGeneratorForClientConfigWithServiceName() {
        String configPath = CONFIG_DIR.resolve("client-config.toml").toString();
        GenerationException error = Assert.expectThrows(GenerationException.class,
                () -> GeneratorFactory.getGenerator(createContext(configPath, null, null, "orders")));

        Assert.assertTrue(error.getMessage().contains(String.format("The --service-name flag only applies to service "
                + "generation. \"%s\" resolves to client generation.", configPath)), error.getMessage());
    }

    @Test(description = "Test that the object type defaults to service when the flag is not given")
    public void testObjectTypeDefaultsToService() {
        GenerationContext context = createContext(CONFIG_DIR.resolve("service-config.toml").toString());
        Assert.assertEquals(context.getObjectType(), ObjectType.SERVICE);
        Assert.assertFalse(context.isObjectTypeDeclared());
    }

    @Test(description = "Test generating a client generator for a GraphQL configuration file")
    public void testGetGeneratorForConfigurationFile() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(createContext("graphql.config.yaml"));
        Assert.assertTrue(generator instanceof ClientGeneration);
    }

    @Test(description = "Test generating a service generator for a GraphQL schema file")
    public void testGetGeneratorForSchemaFile() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(createContext("schema.graphql"));
        Assert.assertTrue(generator instanceof ServiceGeneration);
    }

    @Test(description = "Test generating a schema generator for a Ballerina service file")
    public void testGetGeneratorForBallerinaFile() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(createContext("service.bal"));
        Assert.assertTrue(generator instanceof SchemaGeneration);
    }

    @Test(description = "Test generating a client generator for a configuration file with documents")
    public void testGetGeneratorForBalGraphqlConfigWithDocuments() throws GenerationException {
        Assert.assertTrue(getGeneratorForConfig("client-config.toml") instanceof ClientGeneration);
    }

    @Test(description = "Test generating a service generator for a configuration file without documents")
    public void testGetGeneratorForBalGraphqlConfigWithoutDocuments() throws GenerationException {
        Assert.assertTrue(getGeneratorForConfig("service-config.toml") instanceof ServiceGeneration);
    }

    @Test(description = "Test generating a generator for a configuration file with an invalid schema source")
    public void testGetGeneratorForInvalidConfig() {
        try {
            getGeneratorForConfig("invalid/invalid-source.toml");
            Assert.fail("Expected a GenerationException for an unsupported schema source");
        } catch (GenerationException e) {
            Assert.assertTrue(e.getMessage().contains("is not a supported value for the schema source"),
                    "Unexpected message: " + e.getMessage());
        }
    }

    @Test(description = "Test generating a generator when the declared mode matches the configuration file")
    public void testGetGeneratorWithMatchingDeclaredMode() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(
                createContext(CONFIG_DIR.resolve("client-config.toml").toString(), OperationMode.CLIENT));
        Assert.assertTrue(generator instanceof ClientGeneration);
    }

    @Test(description = "Test generating a generator when the declared mode overrides a configuration file that "
            + "has documents configured")
    public void testGetGeneratorWithDeclaredModeOverridingConfig() throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(
                createContext(CONFIG_DIR.resolve("client-config.toml").toString(), OperationMode.SERVICE));
        Assert.assertTrue(generator instanceof ServiceGeneration);
    }

    @Test(description = "Test generating a generator for an input the operation mode cannot be resolved from")
    public void testGetGeneratorForUnsupportedInput() {
        try {
            GeneratorFactory.getGenerator(createContext("notes.txt"));
            Assert.fail("Expected a GenerationException for an unsupported input");
        } catch (GenerationException e) {
            Assert.assertTrue(e.getMessage().contains("could not be resolved"),
                    "Unexpected message: " + e.getMessage());
        }
    }
}
