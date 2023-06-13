package io.ballerina.graphql.generator.ballerina;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.ServiceFileCombiner;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceGenerator;
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Test class for ServiceFileCombiner.
 * Test the successful combination of available service file and generated service file from GraphQL schema.
 */
public class ServiceFileCombinerTest extends GraphqlTest {
    @Test(description = "Test combining updated schema with added query fields")
    public void testCombiningUpdatedSchemaWithAddedQueryFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "serviceBeforeAddingQueryFields";
        String expectedBalFileName = "serviceWithAddedQueryFields";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.generateSrc(newGraphqlProject.getGraphQLSchema());

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test(description = "Test combining updated schema with added mutation fields")
    public void testCombiningUpdatedSchemaWithAddedMutationFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewMutationFieldsApi";
        String beforeBalFileName = "serviceBeforeAddingMutationFields";
        String expectedBalFileName = "serviceWithAddedMutationFields";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.generateSrc(newGraphqlProject.getGraphQLSchema());

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test(description = "Test combining updated schema with added subscription fields")
    public void testCombiningUpdatedSchemaWithAddedSubscriptionFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewSubscriptionFieldsApi";
        String beforeBalFileName = "serviceBeforeAddingSubscriptionFields";
        String expectedBalFileName = "serviceWithAddedSubscriptionFields";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.generateSrc(newGraphqlProject.getGraphQLSchema());

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }
}
