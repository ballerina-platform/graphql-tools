package io.ballerina.graphql.generator.ballerina;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is used to test the functionality of the GraphQL function body generator.
 */
public class FunctionBodyGeneratorTest extends GraphqlTest {
    private static final Log log = LogFactory.getLog(FunctionSignatureGeneratorTest.class);

    @Test(description = "Test the successful generation of init function body",
            dataProvider = "dataProviderForInitFunctionBody")
    public void testGenerateInitFunctionBody(String configFile, String expectedInitFunctionBody)
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", configFile)).toString(),
                this.tmpDir);

        Extension extensions = projects.get(0).getExtensions();

        AuthConfig authConfig = new AuthConfig();
        AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
        AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

        FunctionBodyNode initFunctionBodyNode =
                FunctionBodyGenerator.getInstance().generateInitFunctionBody(authConfig);
        String generatedInitFunctionBody = initFunctionBodyNode.toString();
        Assert.assertEquals(expectedInitFunctionBody, generatedInitFunctionBody);
    }

    @Test(description = "Test the successful generation of remote function body",
            dataProvider = "dataProviderForRemoteFunctionBody")
    public void testGenerateRemoteFunctionBody(String configFile, String expectedRemoteFunctionBody)
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", configFile)).toString(),
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

        FunctionBodyNode remoteFunctionBodyNode = FunctionBodyGenerator.getInstance().
                generateRemoteFunctionBody(queryOperation1Definition, schema, authConfig);
        String generatedRemoteFunctionBody = remoteFunctionBodyNode.toString();
        Assert.assertEquals(expectedRemoteFunctionBody, generatedRemoteFunctionBody);
    }

    @Test(description = "Test the successful generation of remote function body with required parameters",
            dataProvider = "dataProviderForRemoteFunctionBodyWithRequiredParameters")
    public void testGenerateRemoteFunctionBodyWithRequiredParameters(String configFile,
                                                                     String expectedRemoteFunctionBody)
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", configFile)).toString(),
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

        FunctionBodyNode remoteFunctionBodyNode = FunctionBodyGenerator.getInstance().
                generateRemoteFunctionBody(queryOperation1Definition, schema, authConfig);
        String generatedRemoteFunctionBody = remoteFunctionBodyNode.toString();
        Assert.assertEquals(expectedRemoteFunctionBody, generatedRemoteFunctionBody);
    }

    @Test(description = "Test the successful generation of remote function body with optional parameters",
            dataProvider = "dataProviderForRemoteFunctionBodyWithOptionalParameters")
    public void testGenerateRemoteFunctionBodyWithOptionalParameters(String configFile,
                                                                     String expectedRemoteFunctionBody)
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", configFile)).toString(),
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

        FunctionBodyNode remoteFunctionBodyNode = FunctionBodyGenerator.getInstance().
                generateRemoteFunctionBody(queryOperation2Definition, schema, authConfig);
        String generatedRemoteFunctionBody = remoteFunctionBodyNode.toString();
        Assert.assertEquals(expectedRemoteFunctionBody, generatedRemoteFunctionBody);
    }

    @Test(description = "Test the successful generation of remote function body with required & optional parameters",
            dataProvider = "dataProviderForRemoteFunctionBodyWithRequiredAndOptionalParameters")
    public void testGenerateRemoteFunctionBodyWithRequiredAndOptionalParameters(String configFile,
                                                                                String expectedRemoteFunctionBody)
            throws ValidationException, CmdException, IOException, ParseException {
        List<GraphqlProject> projects = TestUtils.getValidatedMockProjects(
                this.resourceDir.resolve(Paths.get("specs", configFile)).toString(),
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

        FunctionBodyNode remoteFunctionBodyNode = FunctionBodyGenerator.getInstance().
                generateRemoteFunctionBody(queryOperation3Definition, schema, authConfig);
        String generatedRemoteFunctionBody = remoteFunctionBodyNode.toString();
        Assert.assertEquals(expectedRemoteFunctionBody, generatedRemoteFunctionBody);
    }

    @DataProvider(name = "dataProviderForInitFunctionBody")
    public Object[][] dataProviderForInitFunctionBody() {
        return new Object[][]{
                {"graphql.config.yaml", "{graphql:ClientclientEp=checknew(serviceUrl,clientConfig);" +
                        "self.graphqlClient=clientEp;return;}"},
                {"graphql-config-with-auth-apikeys-config.yaml", "{graphql:ClientclientEp=checknew(serviceUrl," +
                        "clientConfig);self.graphqlClient=clientEp;self.apiKeysConfig=apiKeysConfig.cloneReadOnly();" +
                        "return;}"},
                {"graphql-config-with-auth-client-config.yaml", "{graphql:ClientclientEp=checknew(serviceUrl," +
                        "clientConfig);self.graphqlClient=clientEp;return;}"},
                {"graphql-config-with-auth-apikeys-and-client-config.yaml", "{graphql:ClientclientEp=" +
                        "checknew(serviceUrl,clientConfig);self.graphqlClient=clientEp;" +
                        "self.apiKeysConfig=apiKeysConfig.cloneReadOnly();return;}"}
        };
    }

    @DataProvider(name = "dataProviderForRemoteFunctionBody")
    public Object[][] dataProviderForRemoteFunctionBody() {
        return new Object[][]{
                {"graphql.config.yaml", "{stringquery=string`query country($code:ID!) {country(code:$code) " +
                        "{capital name}}`;map<anydata>variables={\"code\":code};return<CountryResponse> " +
                        "check self.graphqlClient->execute(CountryResponse, query, variables);}"},
                {"graphql-config-with-auth-apikeys-config.yaml", "{stringquery=string`query country($code:ID!) " +
                        "{country(code:$code) {capital name}}`;map<anydata>variables={\"code\":code};" +
                        "map<any>headerValues={\"Header1\":self.apiKeysConfig.header1,\"Header2\":" +
                        "self.apiKeysConfig.header2};map<string|string[]>httpHeaders=getMapForHeaders(headerValues);" +
                        "return<CountryResponse> check self.graphqlClient->execute(CountryResponse, query, " +
                        "variables, httpHeaders);}"},
                {"graphql-config-with-auth-client-config.yaml", "{stringquery=string`query country($code:ID!) " +
                        "{country(code:$code) {capital name}}`;map<anydata>variables={\"code\":code};" +
                        "return<CountryResponse> check self.graphqlClient->execute(CountryResponse, query, " +
                        "variables);}"},
                {"graphql-config-with-auth-apikeys-and-client-config.yaml", "{stringquery=string`query " +
                        "country($code:ID!) {country(code:$code) {capital name}}`;map<anydata>variables=" +
                        "{\"code\":code};map<any>headerValues={\"Header1\":self.apiKeysConfig.header1," +
                        "\"Header2\":self.apiKeysConfig.header2};map<string|string[]>httpHeaders=" +
                        "getMapForHeaders(headerValues);return<CountryResponse> check self.graphqlClient->" +
                        "execute(CountryResponse, query, variables, httpHeaders);}"}
        };
    }

    @DataProvider(name = "dataProviderForRemoteFunctionBodyWithRequiredParameters")
    public Object[][] dataProviderForRemoteFunctionBodyWithRequiredParameters() {
        return new Object[][]{
                {"graphql-config-to-test-arguments.yaml", "{stringquery=string`query operation1($argument1:Boolean!," +
                        "$argument2:String!,$argument3:Int!,$argument4:Float!,$argument5:ID!," +
                        "$argument6:CustomScalar!,$argument7:CustomInput!,$argument8:[CustomInput]!," +
                        "$argument9:[CustomInput!]!) {operation1(argument1:$argument1,argument2:$argument2," +
                        "argument3:$argument3,argument4:$argument4,argument5:$argument5,argument6:$argument6," +
                        "argument7:$argument7,argument8:$argument8,argument9:$argument9) {field1 field2}}`;" +
                        "map<anydata>variables={\"argument9\":argument9,\"argument5\":argument5," +
                        "\"argument6\":argument6,\"argument7\":argument7,\"argument8\":argument8," +
                        "\"argument1\":argument1,\"argument2\":argument2,\"argument3\":argument3," +
                        "\"argument4\":argument4};return<Operation1Response> check self.graphqlClient->" +
                        "execute(Operation1Response, query, variables);}"}
        };
    }

    @DataProvider(name = "dataProviderForRemoteFunctionBodyWithOptionalParameters")
    public Object[][] dataProviderForRemoteFunctionBodyWithOptionalParameters() {
        return new Object[][]{
                {"graphql-config-to-test-arguments.yaml", "{stringquery=string`query operation2(" +
                        "$argument1:CustomInput,$argument2:[CustomInput],$argument3:[CustomInput!]) " +
                        "{operation2(argument1:$argument1,argument2:$argument2,argument3:$argument3) " +
                        "{field1 field2}}`;map<anydata>variables={\"argument1\":argument1,\"argument2\":argument2," +
                        "\"argument3\":argument3};return<Operation2Response> check self.graphqlClient->" +
                        "execute(Operation2Response, query, variables);}"}
        };
    }

    @DataProvider(name = "dataProviderForRemoteFunctionBodyWithRequiredAndOptionalParameters")
    public Object[][] dataProviderForRemoteFunctionBodyWithRequiredAndOptionalParameters() {
        return new Object[][]{
                {"graphql-config-to-test-arguments.yaml", "{stringquery=string`query operation3(" +
                        "$argument1:CustomInput!,$argument2:CustomInput) {operation3(argument1:$argument1," +
                        "argument2:$argument2) {field1 field2}}`;map<anydata>variables={\"argument1\":argument1," +
                        "\"argument2\":argument2};return<Operation3Response> check self.graphqlClient->" +
                        "execute(Operation3Response, query, variables);}"}
        };
    }
}
