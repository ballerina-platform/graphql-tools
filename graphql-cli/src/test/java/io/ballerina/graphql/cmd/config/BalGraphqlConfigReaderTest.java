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

package io.ballerina.graphql.cmd.config;

import io.ballerina.graphql.exception.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the functionality of the BalGraphqlConfigReader.
 */
public class BalGraphqlConfigReaderTest {

    private static final Path CONFIG_DIR = Paths.get("src/test/resources/balGraphqlConfigs").toAbsolutePath();
    private static final Path INVALID_CONFIG_DIR = CONFIG_DIR.resolve("invalid");

    private BalGraphqlConfig read(String fileName) throws IOException, ParseException {
        return read(fileName, new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }

    private BalGraphqlConfig read(String fileName, PrintStream outStream) throws IOException, ParseException {
        return BalGraphqlConfigReader.read(CONFIG_DIR.resolve(fileName), outStream);
    }

    @Test(description = "Test reading a configuration file with a local schema file")
    public void testReadSchemaFromFile() throws IOException, ParseException {
        BalGraphqlConfig config = read("schema-from-file.toml");
        Assert.assertEquals(config.schema().source(), SchemaSource.FILE);
        Assert.assertEquals(config.schema().path(), "./schema.graphql");
        Assert.assertNull(config.schema().url());
        Assert.assertNull(config.schema().endpoint());
    }

    @Test(description = "Test reading a configuration file with a hosted schema URL and headers")
    public void testReadSchemaFromUrl() throws IOException, ParseException {
        BalGraphqlConfig config = read("schema-from-url.toml");
        Assert.assertEquals(config.schema().source(), SchemaSource.URL);
        Assert.assertEquals(config.schema().url(), "https://api.example.com/schema.graphql");
        Assert.assertEquals(config.schema().headers().get("Authorization"), "Bearer token");
    }

    @Test(description = "Test reading a configuration file with an introspection endpoint")
    public void testReadSchemaFromIntrospection() throws IOException, ParseException {
        BalGraphqlConfig config = read("schema-from-introspection.toml");
        Assert.assertEquals(config.schema().source(), SchemaSource.INTROSPECTION);
        Assert.assertEquals(config.schema().endpoint(), "https://api.example.com/graphql");
    }

    @Test(description = "Test reading a client configuration file with documents")
    public void testReadClientConfig() throws IOException, ParseException {
        BalGraphqlConfig config = read("client-config.toml");
        Assert.assertTrue(config.hasDocuments());
        Assert.assertEquals(config.documents().size(), 2);
        Assert.assertEquals(config.documents().get(0), "./queries/getUser.graphql");
        Assert.assertEquals(config.documents().get(1), "./mutations/createUser.graphql");
    }

    @Test(description = "Test reading a service configuration file with ID type and DataLoader mappings")
    public void testReadServiceConfig() throws IOException, ParseException {
        BalGraphqlConfig config = read("service-config.toml");
        Assert.assertFalse(config.hasDocuments());
        Assert.assertEquals(config.idTypes().get("Profile.id"), "int");
        Assert.assertEquals(config.idTypes().get("Query.profile.id"), "int");
        Assert.assertEquals(config.idTypes().get("Query.getFloatId"), "float");
        Assert.assertEquals(config.dataloaders().get("Query.profile"), "profileLoader");
    }

    @Test(description = "Test reading a configuration file with a cleartext schema URL logs a warning but still "
            + "generates, since using HTTP is the user's own responsibility")
    public void testReadInsecureUrl() throws IOException, ParseException {
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(console, true, StandardCharsets.UTF_8);
        BalGraphqlConfig config = read("insecure-url.toml", outStream);
        Assert.assertEquals(config.schema().url(), "http://api.example.com/schema.graphql");
        String output = console.toString(StandardCharsets.UTF_8);
        Assert.assertTrue(output.contains("uses HTTP instead of HTTPS"), "Unexpected output: " + output);
    }

    @Test(description = "Test reading a configuration file with a missing schema URL")
    public void testReadMissingUrl() throws IOException {
        assertReadFails("missing-url.toml", "Required field \"url\" is not provided in the config file.");
    }

    @Test(description = "Test reading a configuration file without a schema source")
    public void testReadMissingSource() throws IOException {
        assertReadFails("missing-source.toml", "Required field \"source\" is not provided in the config file.");
    }

    @Test(description = "Test reading a configuration file with an unsupported schema source")
    public void testReadInvalidSource() throws IOException {
        assertReadFails("invalid-source.toml",
                "\"introspecton\" is not a supported value for the schema source.");
    }

    @Test(description = "Test reading a configuration file without the schema section")
    public void testReadMissingSchemaSection() throws IOException {
        assertReadFails("missing-schema-section.toml",
                "The balGraphQL.toml file is missing the [schema] section.");
    }

    @Test(description = "Test that every non-string value in the mapping entries is reported")
    public void testReadNonStringMappingFields() throws IOException {
        assertReadFails("non-string-id-types.toml",
                "The \"type\" field in [[id-types]] entry 1 should be a string. Found \"[int]\".");
        assertReadFails("non-string-id-types.toml",
                "The \"field\" field in [[id-types]] entry 2 should be a string. Found \"5\".");
    }

    @Test(description = "Test that missing and duplicate fields in the mapping entries are reported")
    public void testReadInvalidMappingEntries() throws IOException {
        assertReadFails("invalid-id-type-entries.toml",
                "Required field \"type\" is not provided in [[id-types]] entry 1.");
        assertReadFails("invalid-id-type-entries.toml",
                "Required field \"field\" is not provided in [[id-types]] entry 2.");
        assertReadFails("invalid-id-type-entries.toml", "\"Book.id\" is given more than once in [[id-types]].");
    }

    @Test(description = "Test that a mapping section written as a table instead of an array of tables is reported")
    public void testReadMappingSectionAsTable() throws IOException {
        assertReadFails("id-types-as-table.toml",
                "The \"id-types\" section should be a list of [[id-types]] entries, each with \"field\" and \"type\".");
        assertReadFails("id-types-as-table.toml", String.format(
                "For example:%n[[id-types]]%nfield = \"Profile.id\"%ntype = \"int\""));
    }

    private void assertReadFails(String fileName, String expectedMessage) throws IOException {
        try {
            BalGraphqlConfigReader.read(INVALID_CONFIG_DIR.resolve(fileName),
                    new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
            Assert.fail("Expected a ParseException containing: " + expectedMessage);
        } catch (ParseException e) {
            Assert.assertTrue(e.getMessage().contains(expectedMessage), "Unexpected message: " + e.getMessage());
        }
    }
}
