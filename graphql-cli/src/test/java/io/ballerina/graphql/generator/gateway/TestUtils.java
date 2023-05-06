/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.gateway;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlCmd;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import io.ballerina.projects.DiagnosticResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Utility class for gateway tests.
 */
public class TestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);
    private static final Path sampleRequestResourceDir =
            Path.of("src", "test", "resources", "federationGateway", "sampleRequests").toAbsolutePath();
    private static final Path schemaResourceDir =
            Path.of("src", "test", "resources", "federationGateway", "supergraphSchemas").toAbsolutePath();

    public static File generateGatewayJar(Path supergraphSdl, Path tmpDir) {
        String[] args = {"-i", supergraphSdl.toString(), "-o", tmpDir.toString(), "-m",
                CodeGeneratorConstants.MODE_GATEWAY};
        GraphqlCmd graphqlCmd = new GraphqlCmd(new PrintStream(new ByteArrayOutputStream()),
                tmpDir, false);
        new CommandLine(graphqlCmd).parseArgs(args);
        graphqlCmd.execute();
        File generatedGateway = new File(tmpDir + File.separator + "Supergraph-gateway.jar");
        if (generatedGateway.exists()) {
            return generatedGateway;
        }
        throw new RuntimeException("Gateway jar not generated");
    }

    public static File getBallerinaExecutableJar(Path projectDir, Path tmpDir) throws GatewayGenerationException {
        return CommonUtils.getCompiledBallerinaProject(projectDir.toAbsolutePath(), tmpDir,
                projectDir.getFileName().toString());
    }

    public static void waitTillUrlIsAvailable(Process process, String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection;
        boolean available = false;
        do {
            try {
                Thread.sleep(1000);
                connection = (HttpURLConnection) urlObj.openConnection();
                connection.getResponseCode();
                available = true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ConnectException ignored) {
            }

        } while (!available && process.isAlive());

        if (!process.isAlive()) {
            throw new RuntimeException("Process terminated before the url is available");
        }
    }

    private static void checkDiagnosticResultsForErrors(DiagnosticResult diagnosticResult) {
        if (diagnosticResult.hasErrors()) {
            throw new RuntimeException("Compilation contains errors");
        }
    }

    public static void deleteDirectory(Path tmpDir) throws IOException {
        Files.walk(tmpDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
    }

    public static String getRequestContent(String filename) throws IOException {
        return Files.readString(sampleRequestResourceDir.resolve(filename + ".graphql"));
    }

    public static String getResponseContent(String filename) throws IOException {
        return Files.readString(sampleRequestResourceDir.resolve(filename + ".json"));
    }

    public static String getGraphqlQueryResponse(String graphqlUrl, String query) throws IOException {
        return getGraphqlResponse(graphqlUrl, ("{\"query\":\"{" + query + "}\"}").getBytes());
    }

    public static String getGraphqlMutationResponse(String grapqlUrl, String query) throws IOException {
        return getGraphqlResponse(grapqlUrl, ("{\"query\":\"" + query + "\"}").getBytes());
    }

    private static String getGraphqlResponse(String grapqlUrl, byte[] body) throws IOException {
        URL url = new URL(grapqlUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.getOutputStream().write(body);

        // read and assert the response from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }

    /**
     * @param files     List of files to be copied
     * @param targetDir Target destination
     * @throws IOException If an error occurs while copying files
     */
    public static void copyFilesToTarget(Path[] files, Path targetDir) throws IOException {
        for (Path file : files) {
            Files.copy(file, targetDir.resolve(file.getFileName()));
        }
    }

}
