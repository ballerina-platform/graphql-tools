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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with changed return type in functions")
    public void testCombiningUpdatedSchemaWithChangedReturnTypeInFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithChangedReturnTypeInFunctionsApi";
        String beforeBalFileName = "serviceBeforeChangingReturnTypeInFunctions";
        String expectedBalFileName = "serviceWithChangedReturnTypeInFunctions";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with changed parameters in functions")
    public void testCombiningUpdatedSchemaWithChangedParametersInFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithChangedParametersInFunctionsApi";
        String beforeBalFileName = "serviceBeforeChangingParametersInFunctions";
        String expectedBalFileName = "serviceWithChangedParametersInFunctions";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with changed qualifiers in functions")
    public void testCombiningUpdatedSchemaWithChangedQualifiersInFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithChangedQualifiersInFunctionsApi";
        String beforeBalFileName = "serviceBeforeChangingQualifiersInFunctions";
        String expectedBalFileName = "serviceWithChangedQualifiersInFunctions";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with interchanged query and subscription fields")
    public void testCombiningUpdatedSchemaWithInterchangedQueryAndSubscriptionFields()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithInterchangedQueryAndSubscriptionFieldsApi";
        String beforeBalFileName = "serviceBeforeInterchangingQueryAndSubscriptionFields";
        String expectedBalFileName = "serviceWithInterchangedQueryAndSubscriptionFields";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with added metadata in resolver functions")
    public void testCombiningUpdatedSchemaWithAddedMetadataInResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedMetadataInResolverFunctionsApi";
        String beforeBalFileName = "serviceBeforeAddingMetadataInResolverFunctions";
        String expectedBalFileName = "serviceAfterAddingMetadataInResolverFunctions";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema into service with basic functions")
    public void testCombiningUpdatedSchemaIntoServiceWithNonResolverFunctions()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "serviceWithNonResolverFunctionsBeforeAddingQueryFields";
        String expectedBalFileName = "serviceWithNonResolverFunctionsWithAddedQueryFields";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with added query fields into service with multiple imports")
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceWithMultipleImports()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "serviceWithMultipleImportsBeforeAddingQueryFields";
        String expectedBalFileName = "serviceWithMultipleImportsAfterAddingQueryFields";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with added query fields into service with additional table")
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceWithAdditionalTable()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "serviceWithAdditionalTableBeforeAddingQueryFields";
        String expectedBalFileName = "serviceWithAdditionalTableAfterAddingQueryFields";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with added query fields into service with comments")
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceWithComments()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "serviceWithCommentsBeforeAddingQueryFields";
        String expectedBalFileName = "serviceWithCommentsAfterAddingQueryFields";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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

    @Test(description = "Test combining updated schema with added query fields into service run on different port")
    public void testCombiningUpdatedSchemaWithAddedQueryFieldsIntoServiceRunOnDifferentPort()
            throws ValidationException, IOException, ServiceTypesGenerationException, FormatterException {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "serviceRunOnDifferentPortBeforeAddingQueryFields";
        String expectedBalFileName = "serviceRunOnDifferentPortAfterAddingQueryFields";
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
        ModulePartNode newContent = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());
        SyntaxTree newContentSyntaxTree = serviceTypesGenerator.generateSyntaxTree(newContent);
        serviceTypesGenerator.generateSrc(newContentSyntaxTree);

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
