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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.config.BalGraphqlConfigReader;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is used to test that ServiceGeneration and ClientGeneration correctly resolve a schema (and, for
 * client generation, documents) from a balGraphQL.toml configuration file, rather than from the input path itself.
 */
public class BalGraphqlConfigGenerationTest {

    private static final Path CONFIG_DIR = Paths.get("src/test/resources/balGraphqlConfigs").toAbsolutePath();
    private static final Path OUTPUT_PATH = Paths.get("build");
    private static final String INVALID_SDL = "type Query {\n  broken(: String\n}\n";

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> receivedAuthorization = new AtomicReference<>();

    /**
     * Starts a local HTTP server standing in for remote schema sources: a hosted SDL file for the "url" source
     * and a GraphQL endpoint answering introspection queries for the "introspection" source, plus malformed
     * responses for the failure cases. Paths with no handler respond with 404.
     */
    @BeforeClass
    public void startServer() throws IOException {
        String sdl = Files.readString(CONFIG_DIR.resolve("schema.graphql"));
        String introspectionResponse = createIntrospectionResponse(sdl);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/schema.graphql", exchange -> {
            receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, sdl);
        });
        server.createContext("/bad.graphql", exchange -> respond(exchange, INVALID_SDL));
        server.createContext("/graphql", exchange -> {
            receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, introspectionResponse);
        });
        server.createContext("/not-json", exchange -> respond(exchange, "<html>not graphql</html>"));
        server.createContext("/data-null", exchange -> respond(exchange, "{\"data\":null}"));
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterClass
    public void stopServer() {
        server.stop(0);
    }

    @Test(description = "Test validating a service generator built from a configuration file with a local schema")
    public void testValidateServiceFromConfig() throws IOException, ParseException, GenerationException {
        Path configPath = CONFIG_DIR.resolve("service-config.toml");
        // Should resolve "./schema.graphql" relative to the configuration file directory and validate successfully.
        serviceGeneration(configPath).validate();
    }

    @Test(description = "Test validating a client generator built from a configuration file with a local schema "
            + "and documents")
    public void testValidateClientFromConfig() throws IOException, ParseException, GenerationException {
        Path configPath = CONFIG_DIR.resolve("valid-client-config.toml");
        // Should resolve both the schema and the document relative to the configuration file directory.
        clientGeneration(configPath).validate();
    }

    @Test(description = "Test validating a client generator built from a configuration file without documents")
    public void testValidateClientFromConfigWithoutDocuments() throws IOException, ParseException {
        assertValidationFails(clientGeneration(CONFIG_DIR.resolve("service-config.toml")), "documents");
    }

    @Test(description = "Test validating a service generator whose schema is fetched from a url")
    public void testValidateServiceFromUrlSchema() throws IOException, ParseException, GenerationException {
        serviceGeneration(writeConfig(urlConfig("/schema.graphql"))).validate();
    }

    @Test(description = "Test validating a client generator whose schema is fetched from a url")
    public void testValidateClientFromUrlSchema() throws IOException, ParseException, GenerationException {
        clientGeneration(writeConfig(withDocuments(urlConfig("/schema.graphql")))).validate();
    }

    @Test(description = "Test that the configured headers are sent when fetching a schema from a url")
    public void testUrlSchemaSendsHeaders() throws IOException, ParseException, GenerationException {
        receivedAuthorization.set(null);
        String config = urlConfig("/schema.graphql") + "\n[schema.headers]\nAuthorization = \"Bearer abc123\"\n";
        serviceGeneration(writeConfig(config)).validate();
        Assert.assertEquals(receivedAuthorization.get(), "Bearer abc123");
    }

    @Test(description = "Test validating a service generator whose schema url responds with a non-200 status")
    public void testValidateServiceFromMissingUrlSchema() throws IOException, ParseException {
        assertValidationFails(serviceGeneration(writeConfig(urlConfig("/missing.graphql"))),
                "Received HTTP status 404");
    }

    @Test(description = "Test validating a service generator whose schema url responds with invalid SDL")
    public void testValidateServiceFromInvalidUrlSchema() throws IOException, ParseException {
        assertValidationFails(serviceGeneration(writeConfig(urlConfig("/bad.graphql"))),
                "GraphQL SDL validation failed");
    }

    @Test(description = "Test validating a service generator whose schema is introspected from an endpoint")
    public void testValidateServiceFromIntrospection() throws IOException, ParseException, GenerationException {
        serviceGeneration(writeConfig(introspectionConfig("/graphql"))).validate();
    }

    @Test(description = "Test validating a client generator whose schema is introspected from an endpoint")
    public void testValidateClientFromIntrospection() throws IOException, ParseException, GenerationException {
        clientGeneration(writeConfig(withDocuments(introspectionConfig("/graphql")))).validate();
    }

    @Test(description = "Test that the configured headers are sent when introspecting an endpoint")
    public void testIntrospectionSendsHeaders() throws IOException, ParseException, GenerationException {
        receivedAuthorization.set(null);
        String config = introspectionConfig("/graphql") + "\n[schema.headers]\nAuthorization = \"Bearer abc123\"\n";
        serviceGeneration(writeConfig(config)).validate();
        Assert.assertEquals(receivedAuthorization.get(), "Bearer abc123");
    }

    @Test(description = "Test validating a service generator whose introspection endpoint does not respond with JSON")
    public void testValidateServiceFromNonJsonIntrospection() throws IOException, ParseException {
        assertValidationFails(serviceGeneration(writeConfig(introspectionConfig("/not-json"))),
                "did not return a JSON response");
    }

    @Test(description = "Test validating a service generator whose introspection endpoint responds with null data")
    public void testValidateServiceFromNullDataIntrospection() throws IOException, ParseException {
        assertValidationFails(serviceGeneration(writeConfig(introspectionConfig("/data-null"))),
                "Failed to retrieve SDL");
    }

    private ServiceGeneration serviceGeneration(Path configPath) throws IOException, ParseException {
        return new ServiceGeneration(createContext(configPath), readConfig(configPath));
    }

    private ClientGeneration clientGeneration(Path configPath) throws IOException, ParseException {
        return new ClientGeneration(createContext(configPath), readConfig(configPath));
    }

    private GenerationContext createContext(Path configPath) {
        return new GenerationContext(configPath.toString(), null, OUTPUT_PATH, null, false, System.out);
    }

    private BalGraphqlConfig readConfig(Path configPath) throws IOException, ParseException {
        return BalGraphqlConfigReader.read(configPath, System.out);
    }

    private static void assertValidationFails(Generator generator, String expectedMessage) {
        try {
            generator.validate();
            Assert.fail("Expected a GenerationException containing \"" + expectedMessage + "\"");
        } catch (GenerationException e) {
            Assert.assertTrue(e.getMessage().contains(expectedMessage), "Unexpected message: " + e.getMessage());
        }
    }

    private String urlConfig(String path) {
        return "[schema]\nsource = \"url\"\nurl = \"" + baseUrl + path + "\"\n";
    }

    private String introspectionConfig(String path) {
        return "[schema]\nsource = \"introspection\"\nendpoint = \"" + baseUrl + path + "\"\n";
    }

    private static String withDocuments(String config) {
        // A TOML literal string, so a Windows path's backslashes are kept as-is.
        return "documents = ['" + CONFIG_DIR.resolve("query.graphql") + "']\n\n" + config;
    }

    private static Path writeConfig(String content) throws IOException {
        Path configPath = Files.createTempDirectory("bal-graphql-config").resolve(BalGraphqlConfig.FILE_NAME);
        Files.writeString(configPath, content);
        return configPath;
    }

    /**
     * Builds the response a real GraphQL server would send for an introspection query against the given schema.
     */
    private static String createIntrospectionResponse(String sdl) {
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(sdl),
                RuntimeWiring.MOCKED_WIRING);
        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(IntrospectionQuery.INTROSPECTION_QUERY);
        return new JSONObject(result.toSpecification()).toString();
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
