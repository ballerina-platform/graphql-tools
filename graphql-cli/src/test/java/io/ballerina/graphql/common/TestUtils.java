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

package io.ballerina.graphql.common;

import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Project;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.pojo.Extension;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.graphql.generator.utils.SrcFilePojo;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_EMPTY_CONFIGURATION_FILE;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT;
import static io.ballerina.graphql.cmd.Constants.MESSAGE_FOR_INVALID_FILE_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Utility class for tests.
 */
public class TestUtils {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    public static final PrintStream OUT = System.out;
    public static final Path TARGET_DIR = Paths.get(System.getProperty("target.dir"));
    public static final Path TEST_DISTRIBUTION_PATH = TARGET_DIR.resolve("extracted-distribution");
    public static final String DISTRIBUTION_FILE_NAME = System.getProperty("ballerina.version");
    private static String balFile = "bal";
    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String ERROR_FOR_RESOURCE_FUNCTION_NO_RETURN = "this resource_func must return a result";
    public static final String ERROR_FOR_REMOTE_FUNCTION_NO_RETURN = "this function must return a result";

    /**
     * Constructs an instance of the `Config` reading the given GraphQL config file.
     *
     * @param filePath                      the path to the Graphql config file
     * @return                              the instance of the Graphql config file
     * @throws FileNotFoundException        when the GraphQL config file doesn't exist
     * @throws ParseException               when a parsing related error occurs
     * @throws CmdException                 when a graphql command related error occurs
     */
    public static Config readConfig(String filePath) throws FileNotFoundException, ParseException, CmdException {
        try {
            if (filePath.endsWith(YAML_EXTENSION) || filePath.endsWith(YML_EXTENSION)) {
                InputStream inputStream = new FileInputStream(new File(filePath));
                Constructor constructor = Utils.getProcessedConstructor();
                Yaml yaml = new Yaml(constructor);
                Config config = yaml.load(inputStream);
                if (config == null) {
                    throw new ParseException(MESSAGE_FOR_EMPTY_CONFIGURATION_FILE);
                }
                return config;
            } else {
                throw new CmdException(String.format(MESSAGE_FOR_INVALID_FILE_EXTENSION, filePath));
            }
        } catch (YAMLException e) {
            throw new ParseException(MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT + e.getMessage());
        }
    }

    /**
     * Populate the projects with information given in the GraphQL config file.
     *
     * @param config         the instance of the Graphql config file
     * @param outputPath     the target output path for the code generation
     * @return               the list of instances of the GraphQL projects
     */
    public static List<GraphqlClientProject> populateProjects(Config config, Path outputPath) {
        List<GraphqlClientProject> graphqlProjects = new ArrayList<>();
        String schema = config.getSchema();
        List<String> documents = config.getDocuments();
        Extension extensions = config.getExtensions();
        Map<String, Project> projects = config.getProjects();

        if (schema != null || documents != null || extensions != null) {
            graphqlProjects.add(new GraphqlClientProject(ROOT_PROJECT_NAME, schema, documents, extensions,
                    outputPath.toString()));
        }

        if (projects != null) {
            for (String projectName : projects.keySet()) {
                graphqlProjects.add(new GraphqlClientProject(projectName,
                        projects.get(projectName).getSchema(),
                        projects.get(projectName).getDocuments(),
                        projects.get(projectName).getExtensions(),
                        outputPath.toString()));
            }
        }
        return graphqlProjects;
    }

    /**
     * Get a list of instances of Mock GraphQL projects for a given GraphQL config.
     *
     * @param filePath       the path to the Graphql config file
     * @param outputPath     the target output path for the code generation
     * @return               the list of instances of the GraphQL projects
     */
    public static List<GraphqlClientProject> getValidatedMockProjects(String filePath, Path outputPath)
            throws CmdException, IOException, ParseException, ValidationException {
        Config config = TestUtils.readConfig(filePath);
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = TestUtils.populateProjects(config, outputPath);
        for (GraphqlClientProject project : projects) {
            Utils.validateGraphqlProject(project);
            QueryValidator.getInstance().validate(project);
        }
        return projects;
    }

    public static GraphqlServiceProject getValidatedMockServiceProject(String filePath, Path outputPath)
            throws ValidationException, IOException {
        GraphqlServiceProject graphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, filePath, outputPath.toString());
        Utils.validateGraphqlProject(graphqlProject);
        return graphqlProject;
    }

    // Get string as a content of ballerina file
    public static String getStringFromGivenBalFile(Path expectedFile) throws IOException {
        Stream<String> expectedLines = Files.lines(expectedFile);
        String expectedContent = expectedLines.collect(Collectors.joining(LINE_SEPARATOR));
        expectedLines.close();
        return expectedContent.replaceAll(LINE_SEPARATOR, "");
    }

    // Compare generated file with the expected file
    public static void compareGeneratedFileWithExpectedFile(String generatedFile, String expectedFile) {
        generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
        expectedFile = (expectedFile.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedFile.contains(expectedFile));
    }

    /**
     * Execute ballerina graphql command.
     *
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return True if build is successful, else false.
     * @throws IOException          Error executing build command.
     * @throws InterruptedException Interrupted error executing build command.
     */
    public static boolean executeGraphql(String distributionName, Path sourceDirectory, List<String> args) throws
            IOException, InterruptedException {
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode == 0;
    }

    /**
     * Execute ballerina graphql command with bal file that consist errors.
     *
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return InputStream that include the error message.
     * @throws IOException          Error executing build command.
     */
    public static InputStream executeGraphqlWithErrors(String distributionName, Path sourceDirectory, List<String> args)
            throws IOException {
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        return process.getErrorStream();
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
        return pb.start();
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

    public static void writeSources(List<SrcFilePojo> sources, Path outputPath) throws IOException {
        if (!sources.isEmpty()) {
            for (SrcFilePojo file : sources) {
                if (file.getType().isOverwritable()) {
                    Path filePath = CodeGeneratorUtils.getAbsoluteFilePath(file, outputPath);
                    String fileContent = file.getContent();
                    CodeGeneratorUtils.writeFile(filePath, fileContent);
                }
            }
        }
    }

    public static DiagnosticResult getDiagnosticResult(Path projectDirPath) {
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage().getCompilation().diagnosticResult();
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(
                        TestUtils.TEST_DISTRIBUTION_PATH.resolve(Paths.get(TestUtils.DISTRIBUTION_FILE_NAME))
                                .toAbsolutePath()).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    public static boolean hasOnlyFuncMustReturnResultErrors(Collection<Diagnostic> errors) {
        for (Diagnostic error : errors) {
            if (!error.message().contains(ERROR_FOR_RESOURCE_FUNCTION_NO_RETURN) &&
                    !error.message().contains(ERROR_FOR_REMOTE_FUNCTION_NO_RETURN)) {
                return false;
            }
        }
        return true;
    }

    public static void writeContentTo(String content, Path projectDir, String fileName) throws IOException {
        SrcFilePojo srcFile = new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, "root", fileName, content);
        List<SrcFilePojo> srcFiles = new ArrayList<>();
        srcFiles.add(srcFile);
        writeSources(srcFiles, projectDir);
    }
}
