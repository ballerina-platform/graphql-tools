package io.ballerina.graphql.generator.ballerina;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.combiner.ServiceFileCombiner;
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
    @Test
    public void testCombiningUpdatedSchemaWithAddedQueryFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi.graphql";
        String beforeBalFileName = "serviceBeforeAddingQueryFields.bal";
        String expectedBalFileName = "serviceAfterAddingQueryFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedMutationFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewMutationFieldsApi.graphql";
        String beforeBalFileName = "serviceBeforeAddingMutationFields.bal";
        String expectedBalFileName = "serviceAfterAddingMutationFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedSubscriptionFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewSubscriptionFieldsApi.graphql";
        String beforeBalFileName = "serviceBeforeAddingSubscriptionFields.bal";
        String expectedBalFileName = "serviceAfterAddingSubscriptionFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithChangedReturnTypeInFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithChangedReturnTypeInFunctionsApi.graphql";
        String beforeBalFileName = "serviceBeforeChangingReturnTypeInFunctions.bal";
        String expectedBalFileName = "serviceAfterChangingReturnTypeInFunctions.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithChangedParametersInFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithChangedParametersInFunctionsApi.graphql";
        String beforeBalFileName = "serviceBeforeChangingParametersInFunctions.bal";
        String expectedBalFileName = "serviceAfterChangingParametersInFunctions.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithChangedQualifiersInFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithChangedQualifiersInFunctionsApi.graphql";
        String beforeBalFileName = "serviceBeforeChangingQualifiersInFunctions.bal";
        String expectedBalFileName = "serviceAfterChangingQualifiersInFunctions.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithInterchangedQueryAndSubscriptionFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithInterchangedQueryAndSubscriptionFieldsApi.graphql";
        String beforeBalFileName = "serviceBeforeInterchangingQueryAndSubscriptionFields.bal";
        String expectedBalFileName = "serviceAfterInterchangingQueryAndSubscriptionFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedMetadataInResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedMetadataInResolverFunctionsApi.graphql";
        String beforeBalFileName = "serviceBeforeAddingMetadataInResolverFunctions.bal";
        String expectedBalFileName = "serviceAfterAddingMetadataInResolverFunctions.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaIntoServiceWithNonResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi.graphql";
        String beforeBalFileName = "serviceWithNonResolverFunctionsBeforeAddingQueryFields.bal";
        String expectedBalFileName = "serviceWithNonResolverFunctionsAfterAddingQueryFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceWithMultipleImports()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi.graphql";
        String beforeBalFileName = "serviceWithMultipleImportsBeforeAddingQueryFields.bal";
        String expectedBalFileName = "serviceWithMultipleImportsAfterAddingQueryFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceWithAdditionalTable()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi.graphql";
        String beforeBalFileName = "serviceWithAdditionalTableBeforeAddingQueryFields.bal";
        String expectedBalFileName = "serviceWithAdditionalTableAfterAddingQueryFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceWithComments()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi.graphql";
        String beforeBalFileName = "serviceWithCommentsBeforeAddingQueryFields.bal";
        String expectedBalFileName = "serviceWithCommentsAfterAddingQueryFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        serviceGenerator.setMethodDeclarations(serviceTypesGenerator.getServiceMethodDeclarations());

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ModulePartNode nextBalFileNode = serviceGenerator.generateContentNode();
        ServiceFileCombiner serviceFileCombiner = new ServiceFileCombiner(updateBalFileNode, nextBalFileNode);
        String result = serviceFileCombiner.generateMergedSrc().trim();
        String expectedServiceContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceContent);
    }

    @Test
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceRunOnDifferentPort()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi.graphql";
        String beforeBalFileName = "serviceRunOnDifferentPortBeforeAddingQueryFields.bal";
        String expectedBalFileName = "serviceRunOnDifferentPortAfterAddingQueryFields.bal";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName));
        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

        ServiceGenerator serviceGenerator = new ServiceGenerator();
        serviceGenerator.setFileName(newSchemaFileName.split("\\.")[0]);
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
