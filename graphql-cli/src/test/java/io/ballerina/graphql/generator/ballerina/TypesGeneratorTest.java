package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import io.ballerina.graphql.validator.SDLValidator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL types generator.
 */
public class TypesGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/recordTypesTests/").toAbsolutePath();

    @Test(description = "Generate input records from the GraphQL Schema")
    public void getInputRecords() throws IOException, ParseException, CmdException, ValidationException,
            TypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        Path tempPath = Paths.get("");
        List<GraphqlProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlProject project : projects) {
            SDLValidator.getInstance().validate(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = TypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedInputRecords.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate query response records from the GraphQL Schema")
    public void getQueryResponseRecords() throws IOException, ParseException, CmdException, ValidationException,
            TypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlProject project : projects) {
            SDLValidator.getInstance().validate(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = TypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedQueryResponseRecords.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate all the records (input records and query response records)")
    public void getAllRecords() throws IOException, ParseException, CmdException, ValidationException,
            TypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlProject project : projects) {
            SDLValidator.getInstance().validate(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = TypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedTypes.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }
}
