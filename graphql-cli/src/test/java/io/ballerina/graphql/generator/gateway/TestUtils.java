package io.ballerina.graphql.generator.gateway;

import io.ballerina.graphql.cmd.GraphqlCmd;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
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
    private static final Path resourceDir =
            Path.of("src", "test", "resources", "federationGateway", "sampleRequests").toAbsolutePath();

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

    public static File getBallerinaExecutableJar(Path file, Path tmpDir) {
        BuildOptions buildOptions = BuildOptions.builder().build();
        BuildProject buildProject = BuildProject.load(file.toAbsolutePath(), buildOptions);
        checkDiagnosticResultsForErrors(buildProject.currentPackage().runCodeGenAndModifyPlugins());
        PackageCompilation packageCompilation = buildProject.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        checkDiagnosticResultsForErrors(jBallerinaBackend.diagnosticResult());

        String execFileName = file.getFileName().toString() + ".jar";
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC,
                tmpDir.resolve(execFileName));
        return tmpDir.resolve(execFileName).toFile();
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
                e.printStackTrace();
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
        return Files.readString(resourceDir.resolve(filename + ".graphql"));
    }

    public static String getResponseContent(String filename) throws IOException {
        return Files.readString(resourceDir.resolve(filename + ".json"));
    }

    public static String getGraphqlQueryResponse(String graphqlUrl, String query) throws IOException {
        URL url = new URL(graphqlUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        byte[] body = ("{\"query\":\"{" + query + "}\"}").getBytes();
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
}
