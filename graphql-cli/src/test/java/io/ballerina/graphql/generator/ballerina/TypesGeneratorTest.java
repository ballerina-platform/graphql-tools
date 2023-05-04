package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.GraphqlClientProject;
import io.ballerina.graphql.generator.client.exception.ClientTypesGenerationException;
import io.ballerina.graphql.generator.client.generator.ballerina.ClientTypesGenerator;
import io.ballerina.graphql.validator.ConfigValidator;
import io.ballerina.graphql.validator.QueryValidator;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL types generator.
 */
public class TypesGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @Test(description = "Generate input records from the GraphQL Schema")
    public void getInputRecords() throws IOException, ParseException, CmdException, ValidationException,
            ClientTypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("specs/typesGenTests/graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlClientProject project : projects) {
            Utils.validateGraphqlProject(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = ClientTypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedGenCode/types/expectedInputRecords.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate query response records from the GraphQL Schema")
    public void getQueryResponseRecords() throws IOException, ParseException, CmdException, ValidationException,
            ClientTypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("specs/typesGenTests/graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlClientProject project : projects) {
            Utils.validateGraphqlProject(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = ClientTypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedGenCode/types/expectedQueryResponseRecords.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate fragment records")
    public void createFragmentRecords() throws IOException, ParseException, CmdException, ValidationException,
            ClientTypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("specs/typesGenTests/fragment-graphql.config.yaml")
                .toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlClientProject project : projects) {
            Utils.validateGraphqlProject(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = ClientTypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedGenCode/types/expectedFragmentTypes.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }

    @Test(description = "Generate all the records (input records and query response records)")
    public void getAllRecords() throws IOException, ParseException, CmdException, ValidationException,
            ClientTypesGenerationException {
        Config config = TestUtils.readConfig(RES_DIR.resolve("specs/typesGenTests/graphql.config.yaml").toString());
        ConfigValidator.getInstance().validate(config);
        List<GraphqlClientProject> projects = TestUtils.populateProjects(config, Paths.get(""));
        for (GraphqlClientProject project : projects) {
            Utils.validateGraphqlProject(project);
            QueryValidator.getInstance().validate(project);
        }
        String typesFileContent = ClientTypesGenerator.getInstance().generateSrc(projects.get(0).getGraphQLSchema(),
                projects.get(0).getDocuments());
        Path expectedFilePath = RES_DIR.resolve("expectedGenCode/types/expectedTypes.bal");
        String expectedFileContent = TestUtils.getStringFromGivenBalFile(expectedFilePath);
        TestUtils.compareGeneratedFileWithExpectedFile(typesFileContent, expectedFileContent);
    }
}
