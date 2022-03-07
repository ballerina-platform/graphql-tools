/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.graphql.generator.ballerina;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.model.AuthConfig;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIntersectionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceTypeDescNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BITWISE_AND_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEYS_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.INIT;

/**
 * This class is used to generate ballerina client file according to given SDL and query file.
 */
public class ClientGenerator {
    private static ClientGenerator clientGenerator = null;

    public static ClientGenerator getInstance() {
        if (clientGenerator == null) {
            clientGenerator = new ClientGenerator();
        }
        return clientGenerator;
    }

    /**
     * Generates the client file content.
     *
     * @param queryDocument                     the object instance of the queries document
     * @param queryDocumentName                 the name of the queries document
     * @param graphQLSchema                     the object instance of the GraphQL schema (SDL)
     * @param authConfig                        the object instance representing authentication config information
     * @return                                  the client file content
     * @throws ClientGenerationException        when a client code generation error occurs
     */
    public String generateSrc(Document queryDocument, String queryDocumentName, GraphQLSchema graphQLSchema,
                              AuthConfig authConfig) throws ClientGenerationException {
        try {
            return Formatter.format(generateSyntaxTree(queryDocument, queryDocumentName,
                    graphQLSchema, authConfig)).toString();
        } catch (FormatterException e) {
            throw new ClientGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the client syntax tree.
     *
     * @param queryDocument             the object instance of the queries document
     * @param queryDocumentName         the name of the queries document
     * @param graphQLSchema             the object instance of the GraphQL schema (SDL)
     * @param authConfig                the object instance representing authentication configuration information
     * @return                          Syntax tree for the ballerina client code
     */
    private SyntaxTree generateSyntaxTree(Document queryDocument, String queryDocumentName,
                                          GraphQLSchema graphQLSchema, AuthConfig authConfig) {
        // Generate imports
        NodeList<ImportDeclarationNode> imports = generateImports();
        // Generate auth config records & client class
        NodeList<ModuleMemberDeclarationNode> members =
                generateMembers(queryDocument, queryDocumentName, graphQLSchema, authConfig);

        ModulePartNode modulePartNode = createModulePartNode(imports, members, createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generates the imports in the client file.
     *
     * @return                          the node list which represent imports in the client file
     */
    private NodeList<ImportDeclarationNode> generateImports() {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForHttp = CodeGeneratorUtils.getImportDeclarationNode(
                CodeGeneratorConstants.BALLERINA, CodeGeneratorConstants.HTTP);
        ImportDeclarationNode importForGraphql = CodeGeneratorUtils.getImportDeclarationNode(
                CodeGeneratorConstants.BALLERINAX, CodeGeneratorConstants.GRAPHQL);
        imports.add(importForHttp);
        imports.add(importForGraphql);
        return createNodeList(imports);
    }

    /**
     * Generates the members in the client file. The members include auth config record types & client class nodes.
     *
     * @param queryDocument             the object instance of the queries document
     * @param queryDocumentName         the name of the queries document
     * @param graphQLSchema             the object instance of the GraphQL schema (SDL)
     * @param authConfig                the object instance representing authentication configuration information
     * @return                          the node list which represent members in the client file
     */
    private NodeList<ModuleMemberDeclarationNode> generateMembers(Document queryDocument, String queryDocumentName,
                                                                  GraphQLSchema graphQLSchema, AuthConfig authConfig) {
        List<ModuleMemberDeclarationNode> members =  new ArrayList<>();
        // Generate auth config records
        if (authConfig.getAuthConfigTypes().size() != 0) {
            members.addAll(generateAuthConfigRecords(authConfig));
        }
        // Generate client class
        members.add(generateClientClass(queryDocument, queryDocumentName, graphQLSchema, authConfig));
        return createNodeList(members);
    }

    /**
     * Generates the auth config records in the client file.
     *
     * @param authConfig                the object instance representing authentication configuration information
     * @return                          the node list which represent the auth config records in the client file
     */
    private List<ModuleMemberDeclarationNode> generateAuthConfigRecords(AuthConfig authConfig) {
        List<ModuleMemberDeclarationNode> members =  new ArrayList<>();
        if (authConfig.isApiKeysConfig()) {
            members.add(AuthConfigGenerator.getInstance().
                    generateAuthConfigRecord("ApiKeysConfig", authConfig));
        }
        if (authConfig.isClientConfig()) {
            members.add(AuthConfigGenerator.getInstance().
                    generateAuthConfigRecord("ClientConfig", authConfig));
        }
        return members;
    }

    /**
     * Generates the client class in the client file.
     *
     * @param queryDocument             the object instance of the queries document
     * @param queryDocumentName         the name of the queries document
     * @param graphQLSchema             the object instance of the GraphQL schema (SDL)
     * @param authConfig                the object instance representing authentication configuration information
     * @return                          the node which represent the client class in the client file
     */
    private ClassDefinitionNode generateClientClass(Document queryDocument, String queryDocumentName,
                                                    GraphQLSchema graphQLSchema, AuthConfig authConfig) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        IdentifierToken className = createIdentifierToken(CodeGeneratorUtils.getClientClassName(queryDocumentName));

        // Collect members for class definition node
        List<Node> members =  new ArrayList<>();
        // Generate class instance variables
        members.addAll(generateClassInstanceVariables(authConfig));
        // Generate init function
        members.add(generateInitFunction(authConfig));
        // Generate remote functions
        members.addAll(generateRemoteFunctions(queryDocument, graphQLSchema, authConfig));

        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(members), createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generates the client class instance variables.
     *
     * @param authConfig        the object instance representing authentication configuration information
     * @return                  the list of nodes which represent the client class instance variables
     */
    private List<ObjectFieldNode> generateClassInstanceVariables(AuthConfig authConfig) {
        List<ObjectFieldNode> objectFields = new ArrayList<>();
        objectFields.add(generateGraphqlClientField());

        if (authConfig.isApiKeysConfig()) {
            objectFields.add(generateApiKeysConfigField());
        }
        return objectFields;
    }

    /**
     * Generates the client class init function.
     *
     * @param authConfig        the object instance representing authentication configuration information
     * @return                  the node which represent the init function
     */
    private FunctionDefinitionNode generateInitFunction(AuthConfig authConfig) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));

        IdentifierToken functionName = createIdentifierToken(INIT);

        FunctionSignatureNode functionSignatureNode =
                FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
        FunctionBodyNode functionBodyNode = FunctionBodyGenerator.getInstance().generateInitFunctionBody(authConfig);

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Generates the client class remote functions.
     *
     * @param queryDocument     the object instance of the queries document
     * @param graphQLSchema     the object instance of the GraphQL schema (SDL)
     * @param authConfig        the object instance representing authentication configuration information
     * @return                  the list of nodes which represent the remote functions
     */
    private List<FunctionDefinitionNode> generateRemoteFunctions(Document queryDocument, GraphQLSchema graphQLSchema,
                                                                 AuthConfig authConfig) {
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();

        QueryReader queryReader = new QueryReader(queryDocument);

        for (ExtendedOperationDefinition queryDefinition: queryReader.getExtendedOperationDefinitions()) {
            // Generate remote function
            FunctionDefinitionNode functionDefinitionNode =
                    generateRemoteFunction(queryDefinition, graphQLSchema, authConfig);
            functionDefinitionNodeList.add(functionDefinitionNode);
        }
        return functionDefinitionNodeList;
    }

    /**
     * Generates a client class remote function.
     *
     * @param queryDefinition       the object instance of a single query definition in a query document
     * @param graphQLSchema         the object instance of the GraphQL schema (SDL)
     * @param authConfig            the object instance representing authentication configuration information
     * @return                      the node which represent the remote function
     */
    private FunctionDefinitionNode generateRemoteFunction(ExtendedOperationDefinition queryDefinition,
                                                          GraphQLSchema graphQLSchema, AuthConfig authConfig) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));

        // Obtain functionName from queryName
        IdentifierToken functionName = createIdentifierToken(queryDefinition.getName());

        FunctionSignatureNode functionSignatureNode =
                FunctionSignatureGenerator.getInstance()
                        .generateRemoteFunctionSignature(queryDefinition, graphQLSchema);
        FunctionBodyNode functionBodyNode =
                FunctionBodyGenerator.getInstance()
                        .generateRemoteFunctionBody(queryDefinition, graphQLSchema, authConfig);

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Generates the GraphQL client {@code final graphql:Client graphqlClient;} instance variable.
     *
     * @return                  the node which represent the {@code graphqlClient} instance variable
     */
    private ObjectFieldNode generateGraphqlClientField() {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);

        QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(GRAPHQL),
                createToken(COLON_TOKEN), createIdentifierToken(CodeGeneratorConstants.CLIENT));

        IdentifierToken fieldName = createIdentifierToken(GRAPHQL_CLIENT);

        return createObjectFieldNode(metadataNode, null,
                qualifierList, typeName, fieldName, null, null,
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the API keys config {@code final readonly & ApiKeysConfig apiKeysConfig;} instance variable.
     *
     * @return                  the node which represent the {@code apiKeysConfig} instance variable
     */
    private ObjectFieldNode generateApiKeysConfigField() {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(FINAL_KEYWORD));

        TypeDescriptorNode readOnlyNode =
                createTypeReferenceTypeDescNode(createSimpleNameReferenceNode(createToken(READONLY_KEYWORD)));
        TypeDescriptorNode apiKeysConfigNode =
                createSimpleNameReferenceNode(createIdentifierToken(API_KEYS_CONFIG_TYPE_NAME));
        TypeDescriptorNode typeName = createIntersectionTypeDescriptorNode(readOnlyNode,
                createToken(BITWISE_AND_TOKEN), apiKeysConfigNode);

        IdentifierToken fieldName = createIdentifierToken(API_KEYS_CONFIG_PARAM_NAME);

        return createObjectFieldNode(metadataNode, null,
                qualifierList, typeName, fieldName, null, null,
                createToken(SEMICOLON_TOKEN));
    }
}
