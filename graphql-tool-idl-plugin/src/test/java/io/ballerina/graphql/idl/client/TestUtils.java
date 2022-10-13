package io.ballerina.graphql.idl.client;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Integration tests for IDL support.
 */
public class TestUtils {
    public static final PrintStream OUT = System.out;
    public static final Path RESOURCE = Paths.get("src/test/resources/graphql-client-projects").toAbsolutePath();
    public static final Path TARGET_DIR = Paths.get(System.getProperty("target.dir"));
    public static final Path TEST_DISTRIBUTION_PATH = TARGET_DIR.resolve("extracted-distribution");
    public static final String DISTRIBUTION_FILE_NAME = System.getProperty("ballerina.version");
    private static String balFile = "bal";

    public static File[] getMatchingFiles(String project) throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();

        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, RESOURCE.resolve(project), buildArgs);
        File dir = new File(new File(String.valueOf(RESOURCE.resolve(project).resolve("generated"))).toString());
        final String id = "graphql_client";
        File[] matchingFiles = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains(id);
            }
        });
        return matchingFiles;
    }

    /**
     * Ballerina run command.
     */
    public static boolean executeRun(String distributionName, Path sourceDirectory,
                                     List<String> args) throws IOException, InterruptedException {
        args.add(0, "run");
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode == 0;
    }

    /**
     *  Get Process from given arguments.
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return process
     * @throws IOException          Error executing build command.
     */
    public static Process getProcessBuilderResults(String distributionName, Path sourceDirectory, List<String> args)
            throws IOException {

        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        args.add(0, TEST_DISTRIBUTION_PATH.resolve(distributionName).resolve("bin").resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(args, ' '));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(sourceDirectory.toFile());
        Process process = pb.start();
        process.getErrorStream();
        return process;
    }

    /**
     * Log the output of an input stream.
     *
     * @param inputStream The stream.
     * @throws IOException Error reading the stream.
     */
    private static void logOutput(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.lines().forEach(OUT::println);
        }
    }
}
