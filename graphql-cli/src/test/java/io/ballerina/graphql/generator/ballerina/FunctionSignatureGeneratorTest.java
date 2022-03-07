package io.ballerina.graphql.generator.ballerina;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.model.AuthConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL function signature generator.
 */
public class FunctionSignatureGeneratorTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(FunctionSignatureGeneratorTest.class);

    @Test
    public void testGenerateInitFunctionSignature()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", "graphql.config.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        FunctionSignatureNode initFunctionSignature =
                FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
        String generatedInitFunctionSignature = initFunctionSignature.toString();

        Path expectedInitFunctionSignatureFile =
                resourceDir.resolve(Paths.get("expectedGenCode", "functionSignature",
                        "initFunctionSignature.bal"));
        String expectedInitFunctionSignature = readContent(expectedInitFunctionSignatureFile);

        Assert.assertEquals(expectedInitFunctionSignature, generatedInitFunctionSignature);
    }

    @Test
    public void testGenerateInitFunctionSignatureWithApiKeysConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-auth-apikeys-config.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        FunctionSignatureNode initFunctionSignature =
                FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
        String generatedInitFunctionSignature = initFunctionSignature.toString();

        Path expectedInitFunctionSignatureFile =
                resourceDir.resolve(Paths.get("expectedGenCode", "functionSignature",
                        "initFunctionSignatureWithApiKeysConfig.bal"));
        String expectedInitFunctionSignature = readContent(expectedInitFunctionSignatureFile);

        Assert.assertEquals(expectedInitFunctionSignature, generatedInitFunctionSignature);
    }

    @Test
    public void testGenerateInitFunctionSignatureWithClientConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-auth-client-config.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        FunctionSignatureNode initFunctionSignature =
                FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
        String generatedInitFunctionSignature = initFunctionSignature.toString();

        Path expectedInitFunctionSignatureFile =
                resourceDir.resolve(Paths.get("expectedGenCode", "functionSignature",
                        "initFunctionSignatureWithClientConfig.bal"));
        String expectedInitFunctionSignature = readContent(expectedInitFunctionSignatureFile);

        Assert.assertEquals(expectedInitFunctionSignature, generatedInitFunctionSignature);
    }

    @Test
    public void testGenerateInitFunctionSignatureWithBothApiKeysAndClientConfig()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-with-auth-apikeys-and-client-config.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        FunctionSignatureNode initFunctionSignature =
                FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
        String generatedInitFunctionSignature = initFunctionSignature.toString();

        Path expectedInitFunctionSignatureFile =
                resourceDir.resolve(Paths.get("expectedGenCode", "functionSignature",
                        "initFunctionSignatureWithApiKeysAndClientConfig.bal"));
        String expectedInitFunctionSignature = readContent(expectedInitFunctionSignatureFile);

        Assert.assertEquals(expectedInitFunctionSignature, generatedInitFunctionSignature);
    }

    @Test
    public void testGenerateRemoteFunctionSignatureWithRequiredParameters()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation1Definition = queryReader.getExtendedOperationDefinitions().get(0);
        FunctionSignatureNode operation1FunctionSignatureNode = FunctionSignatureGenerator.getInstance()
                        .generateRemoteFunctionSignature(queryOperation1Definition, schema);
        SeparatedNodeList<ParameterNode> parameters = operation1FunctionSignatureNode.parameters();

        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        RequiredParameterNode param02 = (RequiredParameterNode) parameters.get(1);
        RequiredParameterNode param03 = (RequiredParameterNode) parameters.get(2);
        RequiredParameterNode param04 = (RequiredParameterNode) parameters.get(3);
        RequiredParameterNode param05 = (RequiredParameterNode) parameters.get(4);
        RequiredParameterNode param06 = (RequiredParameterNode) parameters.get(5);
        RequiredParameterNode param07 = (RequiredParameterNode) parameters.get(6);
        RequiredParameterNode param08 = (RequiredParameterNode) parameters.get(7);
        RequiredParameterNode param09 = (RequiredParameterNode) parameters.get(8);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "argument9");
        Assert.assertEquals(param01.typeName().toString(), "CustomInput[]");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "argument5");
        Assert.assertEquals(param02.typeName().toString(), "string");

        Assert.assertEquals(param03.paramName().orElseThrow().text(), "argument6");
        Assert.assertEquals(param03.typeName().toString(), "anydata");

        Assert.assertEquals(param04.paramName().orElseThrow().text(), "argument7");
        Assert.assertEquals(param04.typeName().toString(), "CustomInput");

        Assert.assertEquals(param05.paramName().orElseThrow().text(), "argument8");
        Assert.assertEquals(param05.typeName().toString(), "CustomInput?[]");

        Assert.assertEquals(param06.paramName().orElseThrow().text(), "argument1");
        Assert.assertEquals(param06.typeName().toString(), "boolean");

        Assert.assertEquals(param07.paramName().orElseThrow().text(), "argument2");
        Assert.assertEquals(param07.typeName().toString(), "string");

        Assert.assertEquals(param08.paramName().orElseThrow().text(), "argument3");
        Assert.assertEquals(param08.typeName().toString(), "int");

        Assert.assertEquals(param09.paramName().orElseThrow().text(), "argument4");
        Assert.assertEquals(param09.typeName().toString(), "float");
    }

    @Test
    public void testGenerateRemoteFunctionSignatureWithOptionalParameters()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation2Definition = queryReader.getExtendedOperationDefinitions().get(1);
        FunctionSignatureNode operation2FunctionSignatureNode = FunctionSignatureGenerator.getInstance()
                .generateRemoteFunctionSignature(queryOperation2Definition, schema);
        SeparatedNodeList<ParameterNode> parameters = operation2FunctionSignatureNode.parameters();
        DefaultableParameterNode param01 = (DefaultableParameterNode) parameters.get(0);
        DefaultableParameterNode param02 = (DefaultableParameterNode) parameters.get(1);
        DefaultableParameterNode param03 = (DefaultableParameterNode) parameters.get(2);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "argument1");
        Assert.assertEquals(param01.typeName().toString(), "CustomInput?");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "argument2");
        Assert.assertEquals(param02.typeName().toString(), "CustomInput?[]?");

        Assert.assertEquals(param03.paramName().orElseThrow().text(), "argument3");
        Assert.assertEquals(param03.typeName().toString(), "CustomInput[]?");

    }

    @Test
    public void testGenerateRemoteFunctionSignatureWithRequiredAndOptionalParameters()
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs",
                        "graphql-config-to-test-arguments.yaml")).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();
        List<String> documents = projects.get(0).getDocuments();
        GraphQLSchema schema = projects.get(0).getGraphQLSchema();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        Document queryDocument = Utils.getGraphQLQueryDocument(documents.get(0));
        QueryReader queryReader = new QueryReader(queryDocument);

        ExtendedOperationDefinition queryOperation3Definition = queryReader.getExtendedOperationDefinitions().get(2);
        FunctionSignatureNode operation3FunctionSignatureNode = FunctionSignatureGenerator.getInstance()
                .generateRemoteFunctionSignature(queryOperation3Definition, schema);
        SeparatedNodeList<ParameterNode> parameters = operation3FunctionSignatureNode.parameters();
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        DefaultableParameterNode param02 = (DefaultableParameterNode) parameters.get(1);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "argument1");
        Assert.assertEquals(param01.typeName().toString(), "CustomInput");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "argument2");
        Assert.assertEquals(param02.typeName().toString(), "CustomInput?");
    }
}
