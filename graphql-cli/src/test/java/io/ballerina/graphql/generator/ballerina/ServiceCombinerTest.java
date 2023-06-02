package io.ballerina.graphql.generator.ballerina;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.ServiceCombiner;
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
public class ServiceCombinerTest extends GraphqlTest {
    @Test(description = "Test combining updated schema with object type")
    public void testCombiningUpdatedSchemaWithObjectType() throws Exception {
        String updatedBalFileName = "typesWithSingleObjectDefault";
        String newSchemaFileName = "SchemaWithSingleObjectApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", updatedBalFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addType", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addType", updatedBalFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with enum")
    public void testCombiningUpdatedSchemaWithEnum() throws Exception {
        String balFileName = "typesWithEnumDefault";
        String newSchemaFileName = "SchemaWithEnumApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addType", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addType", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with interface")
    public void testCombiningUpdatedSchemaWithInterface() throws Exception {
        String balFileName = "typesWithInterfaceDefault";
        String newSchemaFileName = "SchemaWithInterfaceApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addType", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addType", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with input object")
    public void testCombiningUpdatedSchemaWithInputObject() throws Exception {
        String balFileName = "typesWithInputsDefault";
        String newSchemaFileName = "SchemaWithInputsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addType", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addType", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with union")
    public void testCombiningUpdatedSchemaWithUnion() throws Exception {
        String balFileName = "typesWithUnionDefault";
        String newSchemaFileName = "SchemaWithUnionApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addType", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addType", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with object use records if possible")
    public void testCombiningUpdatedSchemaWithObjectUseRecordsIfPossible() throws Exception {
        String balFileName = "typesWithObjectTakingInputArgumentRecordsAllowed";
        String newSchemaFileName = "SchemaWithObjectTakingInputArgumentApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addType", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addType", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setUseRecordsForObjects(true);
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with new query fields")
    public void testCombiningUpdatedSchemaWithNewQueryFields() throws Exception {
        String balFileName = "typesWithQueryDefault";
        String newSchemaFileName = "SchemaWithQueryApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with new mutation fields")
    public void testCombiningUpdatedSchemaWithNewMutationFields() throws Exception {
        String balFileName = "typesWithMutationDefault";
        String newSchemaFileName = "SchemaWithMutationApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with new subscription fields")
    public void testCombiningUpdatedSchemaWithNewSubscriptionFields() throws Exception {
        String balFileName = "typesWithSubscriptionDefault";
        String newSchemaFileName = "SchemaWithSubscriptionApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with new enum fields")
    public void testCombiningUpdatedSchemaWithNewEnumFields() throws Exception {
        String balFileName = "typesWithEnumDefault";
        String newSchemaFileName = "SchemaWithEnumApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);
    }

    @Test(description = "Test combining updated schema with new input type fields")
    public void testCombiningUpdatedSchemaWithNewInputTypeFields() throws Exception {
        String balFileName = "typesWithInputsDefault";
        String newSchemaFileName = "SchemaWithInputsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(mergedSyntaxTree).toString().trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(result, expectedServiceTypesContent);

        List<String> warnings = new ArrayList<>();
        warnings.add("warning: In 'CreateAuthorInput' input type 'address' field is introduced without " +
                "a default value. This can brake available clients");
        warnings.add("warning: In 'CreateBookInput' input type 'version' field is introduced without a " +
                "default value. This can brake available clients");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 2);
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(warnings.get(i), breakingChangeWarnings.get(i));
        }
    }

    @Test(description = "Test combining updated schema with new union sub types")
    public void testCombiningUpdatedSchemaWithNewUnionSubTypes() throws Exception {
        String balFileName = "typesWithUnionDefault";
        String newSchemaFileName = "SchemaWithUnionApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);
    }

    @Test(description = "Test combining updated schema with new object fields")
    public void testCombiningUpdatedSchemaWithNewObjectFields() throws Exception {
        String balFileName = "typesWithSingleObjectDefault";
        String newSchemaFileName = "SchemaWithSingleObjectApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);
    }


    @Test(description = "Test combining updated schema with new object fields in multiple objects")
    public void testCombiningUpdatedSchemaWithNewObjectFieldsInMultipleObjects() throws Exception {
        String balFileName = "typesWithMultipleObjectsDefault";
        String newSchemaFileName = "SchemaWithMultipleObjectsApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);
    }

    @Test(description = "Test combining updated schema with new interface fields")
    public void testCombiningUpdatedSchemaWithNewInterfaceFields() throws Exception {
        String balFileName = "typesWithInterfaceDefault";
        String newSchemaFileName = "SchemaWithInterfaceApi";
        Path updatedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "updatedServices", "onlyLogicImplementation", balFileName + ".bal"));
        Path newSchemaPath = this.resourceDir.resolve(
                Paths.get("serviceGen", "graphqlSchemas", "updated", "addField", newSchemaFileName + ".graphql"));
        Path mergedBalFilePath = this.resourceDir.resolve(
                Paths.get("serviceGen", "expectedServices", "updated", "addField", balFileName + ".bal"));

        GraphqlServiceProject newGraphqlProject =
                new GraphqlServiceProject(ROOT_PROJECT_NAME, newSchemaPath.toString(), "./");
        Utils.validateGraphqlProject(newGraphqlProject);

        String updatedBalFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(updatedBalFilePath));
        ModulePartNode updateBalFileNode = NodeParser.parseModulePart(updatedBalFileContent);
        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setFileName(newSchemaFileName);
        ModulePartNode nextSchemaNode = serviceTypesGenerator.generateContentNode(newGraphqlProject.getGraphQLSchema());

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(
                "warning: In 'CreateAuthorInput' input type 'email' field has removed. This can brake clients");
        warningMessages.add(
                "warning: In 'CreateBookInput' input type 'authorId' field has removed. This can brake clients");
        warningMessages.add(
                "warning: In 'CreateBookInput' input type 'price' field has removed. This can brake clients");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Gender' enum 'FEMALE' member has removed. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 1);
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Book' service class 'title' function definition has removed. " +
                "This can break available clients");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 1);
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(
                "warning: In 'Book' class 'price' function definition return type has changed from 'int' to 'float'. " +
                        "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Author' class 'id' function definition return type has changed from 'int' " +
                "to 'string'. This can break existing clients.");
        warningMessages.add(
                "warning: In 'Book' class 'id' function definition return type has changed from 'int' to 'string'. " +
                        "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
        String result = Formatter.format(Formatter.format(mergedSyntaxTree).toString().trim()).trim();
        String expectedServiceTypesContent = readContentWithFormat(mergedBalFilePath);
        Assert.assertEquals(expectedServiceTypesContent, result);

        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning: In 'Adult' class 'age' function definition 'nic' parameter removed. " +
                "This can break existing clients.");
        warningMessages.add(
                "warning: In 'Child' class 'pass' function definition 'score3' parameter removed. " +
                        "This can break existing clients.");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 2);
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
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
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == 3);
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed fields in query, mutation and subscription")
    public void testCombiningUpdatedSchemaWithRemovedFieldsInQueryMutationAndSubscription()
            throws Exception {
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
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
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with removed parameters in query, mutation and subscription " +
            "fields")
    public void testCombiningUpdatedSchemaWithRemovedParametersInQueryMutationAndSubscriptionFields()
            throws Exception {
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

        ServiceCombiner serviceCombiner = new ServiceCombiner(updateBalFileNode, nextSchemaNode);
        SyntaxTree mergedSyntaxTree = serviceCombiner.mergeRootNodes();
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
                        "'bookTitles' method declaration 'ids' parameter has removed. This can break existing clients.");
        List<String> breakingChangeWarnings = serviceCombiner.getBreakingChangeWarnings();
        Assert.assertTrue(breakingChangeWarnings.size() == warningMessages.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warningMessages.get(i));
        }
    }

    @Test(description = "Test combining updated schema with new interface fields")
    public void testNodeParser() throws Exception {
        String balFileName = "typesDocsWithEnumDefault";
        Path balFilePath = this.resourceDir.resolve(Paths.get("serviceGen", "expectedServices", balFileName + ".bal"));
        String balFileContent = String.join(Constants.NEW_LINE, Files.readAllLines(balFilePath));
        ModulePartNode balFileNode = NodeParser.parseModulePart(balFileContent);
        Assert.assertTrue(balFileNode != null);
    }
}
