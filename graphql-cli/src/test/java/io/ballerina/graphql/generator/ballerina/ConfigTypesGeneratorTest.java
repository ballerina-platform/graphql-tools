/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.exception.ConfigTypesGenerationException;
import io.ballerina.graphql.generator.client.generator.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generator.client.generator.ballerina.ConfigTypesGenerator;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.utils.model.AuthConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL config types code generator.
 */
public class ConfigTypesGeneratorTest extends GraphqlTest {

    @Test(description = "Test the functionality of the GraphQL config types code generator with no auth config")
    public void testGenerateSrcWithNoAuthConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql.config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedConfigTypesContent = ConfigTypesGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedConfigTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client",
                            "config_types.bal"));
            String expectedConfigTypesContent = readContent(expectedConfigTypesFile);

            Assert.assertEquals(expectedConfigTypesContent, generatedConfigTypesContent);

        } catch (ConfigTypesGenerationException e) {
            Assert.fail("Error while generating the config types code. " + e.getMessage());
        }
    }

    @Test(description = "Test the functionality of the GraphQL config types code generator with API keys config")
    public void testGenerateSrcWithApiKeysConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql-config-with-auth-apikeys-config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedConfigTypesContent = ConfigTypesGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedConfigTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "apiKeysConfig",
                            "config_types.bal"));
            String expectedConfigTypesContent = readContent(expectedConfigTypesFile);

            Assert.assertEquals(expectedConfigTypesContent, generatedConfigTypesContent);

        } catch (ConfigTypesGenerationException e) {
            Assert.fail("Error while generating the config types code. " + e.getMessage());
        }
    }

    @Test(description = "Test the functionality of the GraphQL config types code generator with client config")
    public void testGenerateSrcWithClientConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql-config-with-auth-client-config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedConfigTypesContent = ConfigTypesGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedConfigTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "clientConfig",
                            "config_types.bal"));
            String expectedConfigTypesContent = readContent(expectedConfigTypesFile);

            Assert.assertEquals(expectedConfigTypesContent, generatedConfigTypesContent);

        } catch (ConfigTypesGenerationException e) {
            Assert.fail("Error while generating the config types code. " + e.getMessage());
        }
    }

    @Test(description = "Test the functionality of the GraphQL config types code generator with basic token config")
    public void testGenerateSrcWithBasicTokenClientConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql-config-with-basic-auth-client-config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedConfigTypesContent = ConfigTypesGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedConfigTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "basicAuthClientConfig",
                            "config_types.bal"));
            String expectedConfigTypesContent = readContent(expectedConfigTypesFile);

            Assert.assertEquals(expectedConfigTypesContent, generatedConfigTypesContent);

        } catch (ConfigTypesGenerationException e) {
            Assert.fail("Error while generating the config types code. " + e.getMessage());
        }
    }

    @Test(description = "Test the functionality of the GraphQL config types code generator with client config and API" +
            "keys config")
    public void testGenerateSrcWithClientConfigAndAPIKey()
            throws ValidationException, CmdException, IOException, ParseException {
        try {
            List<GraphqlClientProject> projects = TestUtils.getValidatedMockProjects(
                    this.resourceDir.resolve(Paths.get("specs",
                            "graphql-config-with-auth-apikeys-and-client-config.yaml")).toString(),
                    this.tmpDir);

            Extension extensions = projects.get(0).getExtensions();

            AuthConfig authConfig = new AuthConfig();

            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            String generatedConfigTypesContent = ConfigTypesGenerator.getInstance().generateSrc(authConfig)
                    .trim().replaceAll("\\s+", "")
                    .replaceAll(System.lineSeparator(), "");

            Path expectedConfigTypesFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "clientAndAPIKeysConfig",
                            "config_types.bal"));
            String expectedConfigTypesContent = readContent(expectedConfigTypesFile);

            Assert.assertEquals(expectedConfigTypesContent, generatedConfigTypesContent);

        } catch (ConfigTypesGenerationException e) {
            Assert.fail("Error while generating the config types code. " + e.getMessage());
        }
    }
}
