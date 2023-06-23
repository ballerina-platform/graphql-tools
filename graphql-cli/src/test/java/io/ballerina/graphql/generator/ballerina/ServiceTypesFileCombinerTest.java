package io.ballerina.graphql.generator.ballerina;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.combiner.ServiceTypesFileCombiner;
import io.ballerina.graphql.generator.service.generator.ServiceTypesGenerator;
import org.ballerinalang.formatter.core.Formatter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Test class for ServiceCombiner.
 * Test the successful combination of available service file and generated service file from schema
 */
public class ServiceTypesFileCombinerTest extends GraphqlTest {
    @Test(description = "Test combining updated schema with added new object type")
    public void testCombiningUpdatedSchemaWithAddedNewObjectType() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewObjectTypeApi";
        String beforeBalFileName = "typesBeforeAddingNewObjectTypeDefault";
        String expectedBalFileName = "typesWithAddedNewObjectTypeDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new enum")
    public void testCombiningUpdatedSchemaWithAddedNewEnum() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewEnumApi";
        String beforeBalFileName = "typesBeforeAddingNewEnumDefault";
        String expectedBalFileName = "typesWithAddedNewEnumDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new interface")
    public void testCombiningUpdatedSchemaWithAddedNewInterface() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewInterfaceApi";
        String beforeBalFileName = "typesBeforeAddingNewInterfaceDefault";
        String expectedBalFileName = "typesWithAddedNewInterfaceDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new input type")
    public void testCombiningUpdatedSchemaWithAddedNewInputType() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewInputTypeApi";
        String beforeBalFileName = "typesBeforeAddingNewInputTypeDefault";
        String expectedBalFileName = "typesWithAddedNewInputTypeDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new union")
    public void testCombiningUpdatedSchemaWithAddedNewUnion() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewUnionApi";
        String beforeBalFileName = "typesBeforeAddingNewUnionDefault";
        String expectedBalFileName = "typesWithAddedNewUnionDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new object type using records if possible")
    public void testCombiningUpdatedSchemaWithAddedNewObjectTypeUsingRecordsIfPossible() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewObjectTypeUsingRecordsIfPossibleApi";
        String beforeBalFileName = "typesBeforeAddingNewObjectTypeUsingRecordsIfPossibleDefault";
        String expectedBalFileName = "typesWithAddedNewObjectTypeUsingRecordsIfPossibleDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new query fields")
    public void testCombiningUpdatedSchemaWithAddedNewQueryFields() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        String beforeBalFileName = "typesBeforeAddingNewQueryFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewQueryFieldsDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new mutation fields")
    public void testCombiningUpdatedSchemaWithAddedNewMutationFields() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewMutationFieldsApi";
        String beforeBalFileName = "typesBeforeAddingNewMutationFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewMutationFieldsDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new subscription fields")
    public void testCombiningUpdatedSchemaWithAddedNewSubscriptionFields() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewSubscriptionFieldsApi";
        String beforeBalFileName = "typesBeforeAddingNewSubscriptionFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewSubscriptionFieldsDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new enum fields")
    public void testCombiningUpdatedSchemaWithAddedNewEnumFields() throws Exception {
        String newSchemaFileName = "SchemaWithAddedNewEnumFieldsApi";
        String beforeBalFileName = "typesBeforeAddingNewEnumFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewEnumFieldsDefault";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with new added input type fields")
    public void testCombiningUpdatedSchemaWithAddedNewInputTypeFields() throws Exception {
        String beforeBalFileName = "typesBeforeAddingNewInputTypeFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewInputTypeFieldsDefault";
        String newSchemaFileName = "SchemaWithAddedNewInputTypeFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> warnings = new ArrayList<>();
        warnings.add("warning: In 'CreateAuthorInput' input type 'address' field is introduced without " +
                "a default value. This can brake available clients");
        warnings.add("warning: In 'CreateBookInput' input type 'version' field is introduced without a " +
                "default value. This can brake available clients");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warnings.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(warnings.get(i), breakingChangeWarnings.get(i));
        }
    }

    @Test(description = "Test combining updated schema with added new union members")
    public void testCombiningUpdatedSchemaWithAddedNewUnionMembers() throws Exception {
        String beforeBalFileName = "typesBeforeAddingNewUnionMembersDefault";
        String expectedBalFileName = "typesWithAddedNewUnionMembersDefault";
        String newSchemaFileName = "SchemaWithAddedNewUnionMembersApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new object type fields")
    public void testCombiningUpdatedSchemaWithAddedNewObjectTypeFields() throws Exception {
        String beforeBalFileName = "typesBeforeAddingNewObjectTypeFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewObjectTypeFieldsDefault";
        String newSchemaFileName = "SchemaWithAddedNewObjectTypeFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new object type fields when represented in records")
    public void testCombiningUpdatedSchemaWithAddedNewObjectTypeFieldsWhenRepresentedInRecords() throws Exception {
        String beforeBalFileName = "typesBeforeAddingNewObjectTypeFieldsRecordsAllowed";
        String expectedBalFileName = "typesAfterAddingNewObjectTypeFieldsRecordsAllowed";
        String newSchemaFileName = "SchemaWithAddedNewObjectTypeFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.setUseRecordsForObjects(true);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with removed object type fields when represented in records")
    public void testCombiningUpdatedSchemaWithRemovedObjectTypeFieldsWhenRepresentedInRecords() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingObjectTypeFieldsRecordsAllowed";
        String expectedBalFileName = "typesAfterRemovingObjectTypeFieldsRecordsAllowed";
        String newSchemaFileName = "SchemaWithRemovedObjectTypeFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.setUseRecordsForObjects(true);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(
                "warning: In 'Book' record type 'price' field has removed. This can brake clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with changed object field type when represented in records")
    public void testCombiningUpdatedSchemaWithChangedObjectFieldTypeWhenRepresentedInRecords() throws Exception {
        String beforeBalFileName = "typesBeforeChangingObjectFieldTypeRecordsAllowed";
        String expectedBalFileName = "typesAfterChangingObjectFieldTypeRecordsAllowed";
        String newSchemaFileName = "SchemaWithChangedObjectFieldTypeApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.setUseRecordsForObjects(true);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(
                "warning: In 'Book' record type 'id' field type has changed from 'int' to 'string'. This can break " +
                        "existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with added new object type fields in multiple objects")
    public void testCombiningUpdatedSchemaWithAddedNewObjectTypeFieldsInMultipleObjects() throws Exception {
        String beforeBalFileName = "typesBeforeAddingNewObjectTypeFieldsInMultipleObjectsDefault";
        String expectedBalFileName = "typesWithAddedNewObjectTypeFieldsInMultipleObjectsDefault";
        String newSchemaFileName = "SchemaWithAddedNewObjectTypeFieldsInMultipleObjectsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with added new interface type fields")
    public void testCombiningUpdatedSchemaWithAddedNewInterfaceTypeFields() throws Exception {
        String beforeBalFileName = "typesBeforeAddingNewInterfaceTypeFieldsDefault";
        String expectedBalFileName = "typesWithAddedNewInterfaceTypeFieldsDefault";
        String newSchemaFileName = "SchemaWithAddedNewInterfaceTypeFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with removed input type fields")
    public void testCombiningUpdatedSchemaWithRemovedInputTypeFields() throws Exception {
        String balFileName = "typesWithInputsDefault";
        String newSchemaFileName = "SchemaWithInputsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(
                "warning: In 'CreateAuthorInput' input type 'email' field has removed. This can brake clients.");
        warningMessages.add(
                "warning: In 'CreateBookInput' input type 'authorId' field has removed. This can brake clients.");
        warningMessages.add(
                "warning: In 'CreateBookInput' input type 'price' field has removed. This can brake clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 3);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed enum fields")
    public void testCombiningUpdatedSchemaWithRemovedEnumFields() throws Exception {
        String balFileName = "typesWithEnumDefault";
        String newSchemaFileName = "SchemaWithEnumApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Gender' enum 'FEMALE' member has removed. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < 1; i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed object fields represented in service class")
    public void testCombiningUpdatedSchemaWithRemovedObjectFieldsRepresentedInServiceClass() throws Exception {
        String beforeBalFileName = "typesBeforeSingleObjectRemoveFieldDefault";
        String expectedBalFileName = "typesWithSingleObjectRemoveFieldDefault";
        String newSchemaFileName = "SchemaWithSingleObjectRemoveFieldApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Book' service class 'title' function definition has removed. " +
                "This can break available clients");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        Assert.assertEquals(breakingChangeWarnings.get(0), warningMessages.get(0));
    }

    @Test(description = "Test combining updated schema with changed type in object field represented in service class")
    public void testCombiningUpdatedSchemaWithChangedTypeInObjectFieldRepresentedInServiceClass() throws Exception {
        String beforeBalFileName = "typesBeforeSingleObjectChangedTypeInObjectFieldDefault";
        String expectedBalFileName = "typesWithSingleObjectChangedTypeInObjectFieldDefault";
        String newSchemaFileName = "SchemaWithSingleObjectChangedTypeInObjectFieldApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(
                "warning: In 'Book' class 'price' function definition return type has changed from 'int' to 'float'. " +
                        "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < warningMessages.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(0), warningMessages.get(0));
        }
    }

    @Test(description = "Test combining updated schema with changed type in multiple object fields represented in " +
            "service class")
    public void testCombiningUpdatedSchemaWithChangedTypeInMultipleObjectFieldsRepresentedInServiceClass()
            throws Exception {
        String beforeBalFileName = "typesBeforeMultipleChangedTypesInObjectFieldsDefault";
        String expectedBalFileName = "typesWithMultipleChangedTypesInObjectFieldsDefault";
        String newSchemaFileName = "SchemaWithMultipleChangedTypesInObjectFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Author' class 'id' function definition return type has changed from 'int' " +
                "to 'string'. This can break existing clients.");
        warningMessages.add(
                "warning: In 'Book' class 'id' function definition return type has changed from 'int' to 'string'. " +
                        "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 2);
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed parameters in object fields " +
            "represented in service class")
    public void testCombiningUpdatedSchemaWithRemovedParametersInObjectFieldsRepresentedInServiceClass()
            throws Exception {
        String beforeBalFileName = "typesBeforeRemovingParametersInObjectFieldsDefault";
        String expectedBalFileName = "typesWithRemovedParametersInObjectFieldsDefault";
        String newSchemaFileName = "SchemaWithRemovedParametersInObjectFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Adult' class 'age' function definition 'nic' parameter removed. " +
                "This can break existing clients.");
        warningMessages.add("warning: In 'Child' class 'pass' function definition 'score3' parameter removed. " +
                "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with added parameters in object fields " +
            "represented in service class")
    public void testCombiningUpdatedSchemaWithAddedParametersInObjectFieldsRepresentedInServiceClass()
            throws Exception {
        String beforeBalFileName = "typesBeforeAddingParametersInObjectFieldsDefault";
        String expectedBalFileName = "typesWithAddedParametersInObjectFieldsDefault";
        String newSchemaFileName = "SchemaWithAddedParametersInObjectFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Child' class 'knowsWords' function definition 'word1' parameter added " +
                "without default value. This can break existing clients.");
        warningMessages.add(
                "warning: In 'Child' class 'knowsWords' function definition 'word2' parameter added without default " +
                        "value. This can break existing clients.");
        warningMessages.add(
                "warning: In 'Child' class 'knowsWords' function definition 'word' parameter removed. This can break " +
                        "existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed fields in query, mutation and subscription")
    public void testCombiningUpdatedSchemaWithRemovedFieldsInQueryMutationAndSubscription() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingFieldsInQueryMutationAndSubscriptionDefault";
        String expectedBalFileName = "typesWithRemovedFieldsInQueryMutationAndSubscriptionDefault";
        String newSchemaFileName = "SchemaWithRemovedFieldsInQueryMutationAndSubscriptionApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithRemovedFieldsInQueryMutationAndSubscriptionApi' service object " +
                "'books' method declaration has removed. This can break existing clients.");
        warningMessages.add(
                "warning: In 'SchemaWithRemovedFieldsInQueryMutationAndSubscriptionApi' service object 'updateBook' " +
                        "method declaration has removed. This can break existing clients.");
        warningMessages.add(
                "warning: In 'SchemaWithRemovedFieldsInQueryMutationAndSubscriptionApi' service object 'bookIds' " +
                        "method declaration has removed. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithRemovedFieldsInQueryMutationAndSubscriptionApi' service object " +
                "'authorIds' method declaration has removed. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed parameters in query, mutation and subscription " +
            "fields")
    public void testCombiningUpdatedSchemaWithRemovedParametersInQueryMutationAndSubscriptionFields() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingParametersInQueryMutationAndSubscriptionFieldsDefault";
        String expectedBalFileName = "typesWithRemovedParametersInQueryMutationAndSubscriptionFieldsDefault";
        String newSchemaFileName = "SchemaWithRemovedParametersInQueryMutationAndSubscriptionFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithRemovedParametersInQueryMutationAndSubscriptionFieldsApi' service" +
                " object 'book' method declaration 'title' parameter has removed. This can break existing clients.");
        warningMessages.add(
                "warning: In 'SchemaWithRemovedParametersInQueryMutationAndSubscriptionFieldsApi' service object " +
                        "'addBook' method declaration 'authorId' parameter has removed. This can break existing " +
                        "clients.");
        warningMessages.add(
                "warning: In 'SchemaWithRemovedParametersInQueryMutationAndSubscriptionFieldsApi' service object " +
                        "'bookTitles' method declaration 'ids' parameter has removed. " +
                        "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with types changed in query, mutation and subscription fields")
    public void testCombiningUpdatedSchemaWithTypesChangedInQueryMutationAndSubscriptionFields() throws Exception {
        String beforeBalFileName = "typesBeforeChangingTypesInQueryMutationAndSubscriptionFieldsDefault";
        String expectedBalFileName = "typesWithTypesChangedInQueryMutationAndSubscriptionFieldsDefault";
        String newSchemaFileName = "SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "removeField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "removeField", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'book' method declaration 'id' parameter type change from 'int' to 'string'. This can break " +
                "existing clients.");
        warningMessages.add(
                "warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service object 'book' " +
                        "method declaration return type has changed from 'Book?' to 'Book'. This can break existing " +
                        "clients.");
        warningMessages.add(
                "warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service object 'author' " +
                        "method declaration 'id' parameter type change from 'int' to 'string?'. This can break " +
                        "existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'author' method declaration return type has changed from 'Author?' to 'Author'. This can " +
                "break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'authors' method declaration return type has changed from 'Author[]' to 'Author?[]?'. This " +
                "can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'addBook' method declaration 'title' parameter type change from 'string' to 'string?'. This " +
                "can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'addBook' method declaration return type has changed from 'Book?' to 'Book'. This can break " +
                "existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'addAuthor' method declaration 'name' parameter type change from 'string' to 'string?'. This " +
                "can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'addAuthor' method declaration return type has changed from 'Author?' to 'Author'. This can " +
                "break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'bookTitles' method declaration 'ids' parameter type change from 'int?[]' to 'int?[]?'. This " +
                "can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'bookTitles' method declaration return type has changed from 'stream<string>' to " +
                "'stream<string?>'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'authorNames' method declaration 'ids' parameter type change from 'int?[]' to 'int?[]?'. This" +
                " can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithTypesChangedInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'authorNames' method declaration return type has changed from 'stream<string>' to " +
                "'stream<string?>'. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with added parameters in query, mutation and subscription " +
            "fields")
    public void testCombiningUpdatedSchemaWithAddedParametersInQueryMutationAndSubscriptionFields() throws Exception {
        String beforeBalFileName = "typesBeforeAddingParametersInQueryMutationAndSubscriptionFieldsDefault";
        String expectedBalFileName = "typesWithAddedParametersInQueryMutationAndSubscriptionFieldsDefault";
        String newSchemaFileName = "SchemaWithAddedParametersInQueryMutationAndSubscriptionFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithAddedParametersInQueryMutationAndSubscriptionFieldsApi' service " +
                "object 'book' method declaration 'title' parameter added without default value. This can break " +
                "existing clients.");
        warningMessages.add(
                "warning: In 'SchemaWithAddedParametersInQueryMutationAndSubscriptionFieldsApi' service object " +
                        "'addBook' method declaration 'authorId' parameter added without default value. This can " +
                        "break existing clients.");
        warningMessages.add(
                "warning: In 'SchemaWithAddedParametersInQueryMutationAndSubscriptionFieldsApi' service object " +
                        "'authorNames' method declaration 'ids' parameter added without default value. This can break" +
                        " existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed parameter default values")
    public void testCombiningUpdatedSchemaWithRemovedParameterDefaultValues() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingParameterDefaultValuesDefault";
        String expectedBalFileName = "typesWithRemovedParameterDefaultValuesDefault";
        String newSchemaFileName = "SchemaWithRemovedParameterDefaultValuesApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithRemovedParameterDefaultValuesApi' service object 'book' method " +
                "declaration 'id' parameter assigned '1' default value has removed. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithRemovedParameterDefaultValuesApi' service object 'book' method " +
                "declaration 'title' parameter assigned '\"No title\"' default value has removed. This can break " +
                "existing clients.");
        warningMessages.add("warning: In 'SchemaWithRemovedParameterDefaultValuesApi' service object 'authors' method" +
                " declaration 'ids' parameter assigned '[1]' default value has removed. This can break existing " +
                "clients.");
        warningMessages.add("warning: In 'SchemaWithRemovedParameterDefaultValuesApi' service object 'addBook' method" +
                " declaration 'authorId' parameter assigned '1' default value has removed. This can break existing " +
                "clients.");
        warningMessages.add("warning: In 'SchemaWithRemovedParameterDefaultValuesApi' service object 'addAuthor' " +
                "method declaration 'name' parameter assigned '\"No name\"' default value has removed. This can break" +
                " existing clients.");
        warningMessages.add("warning: In 'SchemaWithRemovedParameterDefaultValuesApi' service object 'bookTitles' " +
                "method declaration 'ids' parameter assigned '[]' default value has removed. This can break existing " +
                "clients.");
        warningMessages.add("warning: In 'CreateBookInput' record type 'price' field assigned '150.0' default value " +
                "has removed. This can break existing clients.");
        warningMessages.add("warning: In 'CreateBookInput' record type 'version' field assigned '\"v1.0\"' default " +
                "value has removed. This can break existing clients.");
        warningMessages.add("warning: In 'Author' service class 'name' function 'designation' parameter assigned " +
                "'\"\"' default value has removed. This can break existing clients.");
        warningMessages.add("warning: In 'Book' service class 'price' function 'copiesSold' parameter assigned '0' " +
                "default value has removed. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed union members")
    public void testCombiningUpdatedSchemaWithRemovedUnionMembers() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingUnionMembersDefault";
        String expectedBalFileName = "typesWithRemovedUnionMembersDefault";
        String newSchemaFileName = "SchemaWithRemovedUnionMembersApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Profile' union type 'Parent' member has removed. This can break existing " +
                "clients.");
        warningMessages.add("warning: In 'Profile' union type 'Clerk' member has removed. This can break existing " +
                "clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed interface fields")
    public void testCombiningUpdatedSchemaWithRemovedInterfaceFields() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingInterfaceFieldsDefault";
        String expectedBalFileName = "typesWithRemovedInterfaceFieldsDefault";
        String newSchemaFileName = "SchemaWithRemovedInterfaceFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Info' interface service object 'phone' method declaration has removed. This" +
                " can break existing clients.");
        warningMessages.add("warning: In 'Info' interface service object 'email' method declaration has removed. This" +
                " can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with record type fields type changed")
    public void testCombiningUpdatedSchemaWithRecordTypeFieldsTypeChanged() throws Exception {
        String beforeBalFileName = "typesBeforeChangingRecordTypeFieldsTypeDefault";
        String expectedBalFileName = "typesWithChangedRecordTypeFieldsTypeDefault";
        String newSchemaFileName = "SchemaWithChangedRecordTypeFieldsTypeApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Author' record type 'id' field type has changed from 'string' to 'int'. " +
                "This can break existing clients.");
        warningMessages.add("warning: In 'Author' record type 'age' field type has changed from 'string' to 'int'. " +
                "This can break existing clients.");
        warningMessages.add("warning: In 'Book' record type 'id' field type has changed from 'string' to 'int'. This " +
                "can break existing clients.");
        warningMessages.add("warning: In 'Book' record type 'price' field type has changed from 'int' to 'float'. " +
                "This can break existing clients.");
        warningMessages.add("warning: In 'Book' record type 'soldAmount' field type has changed from 'float' to 'int'" +
                ". This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with changed main qualifier in service object and service " +
            "class functions")
    public void testCombiningUpdatedSchemaWithChangedQualifiers() throws Exception {
        String beforeBalFileName = "typesBeforeChangingQualifiersDefault";
        String expectedBalFileName = "typesWithChangedQualifiersDefault";
        String newSchemaFileName = "SchemaWithChangedQualifiersApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithChangedQualifiersApi' service object 'book' " +
                "function qualifier list changed from 'remote' to 'resource'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithChangedQualifiersApi' service object 'author' " +
                "function qualifier list changed from 'remote' to 'resource'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithChangedQualifiersApi' service object 'addBook' " +
                "function qualifier list changed from 'resource' to 'remote'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithChangedQualifiersApi' service object 'addAuthor' " +
                "function qualifier list changed from 'resource' to 'remote'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithChangedQualifiersApi' service object 'authorNames'" +
                " function qualifier list changed from 'remote' to 'resource'. This can break existing clients.");
        warningMessages.add("warning: In 'Info' interface 'name' function qualifier list changed from 'remote' to " +
                "'resource'. This can break existing clients.");
        warningMessages.add("warning: In 'Author' service class 'name' function qualifier list changed from 'remote' " +
                "to 'resource'. This can break existing clients.");
        warningMessages.add("warning: In 'Book' service class 'price' function qualifier list changed from 'remote' " +
                "to 'resource'. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with interchanged query and subscription methods")
    public void testCombiningUpdatedSchemaWithInterchangedQueryAndSubscriptionMethods() throws Exception {
        String beforeBalFileName = "typesBeforeInterchangingQueryAndSubscriptionMethodsDefault";
        String expectedBalFileName = "typesWithInterchangedQueryAndSubscriptionMethodsDefault";
        String newSchemaFileName = "SchemaWithInterchangedQueryAndSubscriptionMethodsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'SchemaWithInterchangedQueryAndSubscriptionMethodsApi' service object " +
                "'authorNames' method changed from 'subscribe' to 'get'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithInterchangedQueryAndSubscriptionMethodsApi' service object " +
                "'authorNames' method declaration return type has changed from 'stream<string>' to 'string'. This " +
                "can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithInterchangedQueryAndSubscriptionMethodsApi' service object 'book'" +
                " method changed from 'get' to 'subscribe'. This can break existing clients.");
        warningMessages.add("warning: In 'SchemaWithInterchangedQueryAndSubscriptionMethodsApi' service object 'book'" +
                " method declaration return type has changed from 'Book' to 'stream<Book>'. This can break existing " +
                "clients.");
        warningMessages.add("warning: In 'Info' service object 'bookNames' method changed from 'subscribe' to 'get'. " +
                "This can break existing clients.");
        warningMessages.add("warning: In 'Info' service object 'bookNames' method declaration return type has changed" +
                " from 'stream<string>' to 'string'. This can break existing clients.");
        warningMessages.add("warning: In 'Author' class 'bookNames' function definition return type has changed from " +
                "'stream<string>' to 'string'. This can break existing clients.");
        warningMessages.add("warning: In 'Author' service class 'bookNames' method changed from 'subscribe' to 'get'." +
                " This can break existing clients.");
        warningMessages.add("warning: In 'Book' class 'titles' function definition return type has changed from " +
                "'stream<string>' to 'string'. This can break existing clients.");
        warningMessages.add("warning: In 'Book' service class 'titles' method changed from 'subscribe' to 'get'. This" +
                " can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed default values in input and object types")
    public void testCombiningUpdatedSchemaWithRemovedDefaultValuesInInputAndObjectTypes() throws Exception {
        String beforeBalFileName = "typesBeforeRemovingDefaultValuesInInputAndObjectTypesRecordsAllowed";
        String expectedBalFileName = "typesWithRemovedDefaultValuesInInputAndObjectTypesRecordsAllowed";
        String newSchemaFileName = "SchemaWithRemovedDefaultValuesInInputAndObjectTypesApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'CreateBookInput' record type 'title' field assigned '\"No title\"' default " +
                "value has removed. This can break existing clients.");
        warningMessages.add("warning: In 'CreateBookInput' record type 'price' field assigned '100.0' default value " +
                "has removed. This can break existing clients.");
        warningMessages.add("warning: In 'CreateBookInput' record type 'version' field assigned '\"v1.0\"' default " +
                "value has removed. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with changed default values")
    public void testCombiningUpdatedSchemaWithChangedDefaultValues() throws Exception {
        String beforeBalFileName = "typesBeforeChangingDefaultValues";
        String expectedBalFileName = "typesAfterChangingDefaultValues";
        String newSchemaFileName = "SchemaWithChangedDefaultValuesApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with metadata in module members")
    public void testCombiningUpdatedSchemaWithMetadataInModuleMembers() throws Exception {
        String beforeBalFileName = "typesBeforeAddingMetadataInModuleMembers";
        String expectedBalFileName = "typesAfterAddingMetadataInModuleMembers";
        String newSchemaFileName = "SchemaWithMetadataInModuleMembersApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into service object in types with non resolver functions")
    public void testCombiningUpdatedSchemaIntoServiceObjectInTypesWithNonResolverFunctions() throws Exception {
        String beforeBalFileName =
                "typesServiceObjectWithNonResolverFunctionsBeforeAddingQueryFields";
        String expectedBalFileName = "typesServiceObjectWithNonResolverFunctionsAfterAddingQueryFields";
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into service class in types with non resolver functions")
    public void testCombiningUpdatedSchemaIntoServiceClassInTypesWithNonResolverFunctions() throws Exception {
        String beforeBalFileName =
                "typesServiceClassWithNonResolverFunctionsBeforeAddingQueryFields";
        String expectedBalFileName = "typesServiceClassWithNonResolverFunctionsAfterAddingQueryFields";
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into service class in types with attributes")
    public void testCombiningUpdatedSchemaIntoServiceClassInTypesWithAttributes() throws Exception {
        String beforeBalFileName =
                "typesServiceClassWithAttributesBeforeAddingQueryFields";
        String expectedBalFileName = "typesServiceClassWithAttributesAfterAddingQueryFields";
        String newSchemaFileName = "SchemaWithAddedNewQueryFieldsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into types with isolated functions")
    public void testCombiningUpdatedSchemaIntoTypesWithIsolatedFunctions() throws Exception {
        String beforeBalFileName =
                "typesWithIsolatedFunctionsBeforeAddingQueryFields";
        String expectedBalFileName = "typesWithIsolatedFunctionsAfterAddingQueryFields";
        String newSchemaFileName = "SchemaWithAddedQueryFieldsIntoTypesWithIsolatedFunctionsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into types with documentations")
    public void testCombiningUpdatedSchemaIntoTypesWithDocumentations() throws Exception {
        String beforeBalFileName =
                "typesWithDocumentationsBeforeAddingObjectTypeFields";
        String expectedBalFileName = "typesWithDocumentationsAfterAddingObjectTypeFields";
        String newSchemaFileName = "SchemaWithAddedObjectTypeFieldsIntoTypesWithDocumentationsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into types with readonly object and class")
    public void testCombiningUpdatedSchemaIntoTypesWithReadonlyObjectAndClass() throws Exception {
        String beforeBalFileName =
                "typesWithReadonlyObjectAndClassBeforeAddingQueryFields";
        String expectedBalFileName = "typesWithReadonlyObjectAndClassAfterAddingObjectTypeFields";
        String newSchemaFileName = "SchemaWithAddedQueryFieldsIntoTypesWithReadonlyObjectAndClassApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema into input types with readonly")
    public void testCombiningUpdatedSchemaIntoInputTypesWithReadonly() throws Exception {
        String beforeBalFileName =
                "typesWithReadonlyInputTypesBeforeAddingInputFields";
        String expectedBalFileName = "typesWithReadonlyInputTypesAfterAddingInputFields";
        String newSchemaFileName = "SchemaWithAddedInputFieldsIntoTypesWithReadonlyInputTypesApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", beforeBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", expectedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        serviceTypesGenerator.setUseRecordsForObjects(true);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceTypesFileCombiner serviceTypesFileCombiner =
                new ServiceTypesFileCombiner(updateBalFileNode, nextSchemaNode, newGraphqlProject.getGraphQLSchema());
        SyntaxTree mergedSyntaxTree = serviceTypesFileCombiner.generateMergedSyntaxTree();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> breakingChangeWarnings = serviceTypesFileCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 0);
    }

    @Test(description = "Test combining updated schema with new interface fields")
    public void testNodeParser() throws Exception {
        String balFileName = "typesDocsWithEnumDefault";
        Path balFilePath = this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", balFileName + ".bal"));
        String balFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(balFilePath));
        balFileContent = "type Foo object {\n" +
                "    function foo();\n" +
                "    function bar();\n" +
                "};\n" +
                "\n" +
                "type Bar object {\n" +
                "    function bar();\n" +
                "};\n" +
                "\n" +
                "type FooBar readonly & Bar;";
        ModulePartNode balFileNode = NodeParser.parseModulePart(balFileContent);
        Assert.assertTrue(balFileNode != null);
    }
}