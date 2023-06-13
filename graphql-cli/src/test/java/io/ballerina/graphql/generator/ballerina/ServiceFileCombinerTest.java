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
import java.util.ArrayList;
import java.util.List;

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

        List<String> breakingChangeWarnings = serviceFileCombiner.getBreakingChangeWarnings();
        List<String> warnings = new ArrayList<>();
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'book' function " +
                "definition return type has changed from 'Book?' to 'Book'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'books' function " +
                "definition return type has changed from 'Book?[]?' to 'Book[]?'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'authors' function " +
                "definition return type has changed from 'Author[]' to 'Author[]?'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'createBook' function " +
                "definition return type has changed from 'Book?' to 'Book'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'createAuthor' function" +
                " definition return type has changed from 'Author' to 'Author?'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'createAuthor' function" +
                " definition 'name' parameter type change from 'String' to 'string'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'bookTitles' function " +
                "definition return type has changed from 'stream<string?>' to 'stream<string>'. This can break " +
                "existing clients.");
        warnings.add("warning: In 'SchemaWithChangedReturnTypeInFunctionsApi' GraphQL service 'authorNames' function " +
                "definition return type has changed from 'stream<string>' to 'stream<string?>'. This can break " +
                "existing clients.");
        Assert.assertEquals(breakingChangeWarnings.size(), warnings.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warnings.get(i));
        }
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

        List<String> breakingChangeWarnings = serviceFileCombiner.getBreakingChangeWarnings();
        List<String> warnings = new ArrayList<>();
        warnings.add("warning: In 'SchemaWithChangedParametersInFunctionsApi' GraphQL service 'book' function " +
                "definition 'title' parameter added without default value. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedParametersInFunctionsApi' GraphQL service 'book' function " +
                "definition 'id' parameter type change from 'int?' to 'int'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedParametersInFunctionsApi' GraphQL service 'books' function " +
                "definition 'ids' parameter removed. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedParametersInFunctionsApi' GraphQL service 'authors' function " +
                "definition 'ids' parameter type change from 'int[]' to 'string[]'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedParametersInFunctionsApi' GraphQL service 'addBook' function " +
                "definition 'price' parameter type change from 'int' to 'float'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedParametersInFunctionsApi' GraphQL service 'addBook' function " +
                "'title' parameter assigned '\"No title\"' default value has removed. This can break existing clients" +
                ".");
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warnings.get(i));
        }
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

        List<String> breakingChangeWarnings = serviceFileCombiner.getBreakingChangeWarnings();
        List<String> warnings = new ArrayList<>();
        warnings.add("warning: In 'SchemaWithChangedQualifiersInFunctionsApi' service 'books' function qualifier " +
                "changed from 'remote' to 'resource'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithChangedQualifiersInFunctionsApi' service 'createBook' function qualifier" +
                " changed from 'resource' to 'remote'. This can break existing clients.");
        Assert.assertEquals(breakingChangeWarnings.size(), warnings.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warnings.get(i));
        }
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

        List<String> breakingChangeWarnings = serviceFileCombiner.getBreakingChangeWarnings();
        List<String> warnings = new ArrayList<>();
        warnings.add("warning: In 'SchemaWithInterchangedQueryAndSubscriptionFieldsApi' service class 'book' method " +
                "changed from 'subscribe' to 'get'. This can break existing clients.");
        warnings.add("warning: In 'SchemaWithInterchangedQueryAndSubscriptionFieldsApi' service class 'bookTitles' " +
                "method changed from 'get' to 'subscribe'. This can break existing clients.");
        Assert.assertEquals(breakingChangeWarnings.size(), warnings.size());
        for (int i = 0; i < breakingChangeWarnings.size(); i++) {
            Assert.assertEquals(breakingChangeWarnings.get(i), warnings.get(i));
        }
    }
}
