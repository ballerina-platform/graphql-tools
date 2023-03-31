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

package io.ballerina.graphql.generator.client.generator.ballerina;

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
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.client.Utils;
import io.ballerina.graphql.generator.client.exception.ClientGenerationException;
import io.ballerina.graphql.generator.client.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.client.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.client.generator.model.AuthConfig;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.graphql.generator.utils.GeneratorContext;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
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

/**
 * This class is used to generate ballerina client file according to given SDL and query file.
 */
public class ClientGenerator {
    private static ClientGenerator clientGenerator = null;

    // TODO: stop using singleton
    public static ClientGenerator getInstance() {
        if (clientGenerator == null) {
            clientGenerator = new ClientGenerator();
        }
        return clientGenerator;
    }

    /**
     * Generates the client file content.
     *
     * @param queryDocuments the list of names of the query documents
     * @param graphQLSchema  the object instance of the GraphQL schema (SDL)
     * @param authConfig     the object instance representing authentication config information
     * @return the client file content
     * @throws ClientGenerationException when a client code generation error occurs
     */
    public String generateSrc(List<String> queryDocuments, GraphQLSchema graphQLSchema, AuthConfig authConfig,
                              GeneratorContext generatorContext) throws ClientGenerationException {
        try {
            return Formatter.format(generateSyntaxTree(queryDocuments, graphQLSchema, authConfig, generatorContext))
                    .toString();
        } catch (FormatterException | IOException e) {
            throw new ClientGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the client syntax tree.
     *
     * @param queryDocuments the list of names of the query documents
     * @param graphQLSchema  the object instance of the GraphQL schema (SDL)
     * @param authConfig     the object instance representing authentication configuration information
     * @return Syntax tree for the ballerina client code
     */
    private SyntaxTree generateSyntaxTree(List<String> queryDocuments, GraphQLSchema graphQLSchema,
                                          AuthConfig authConfig, GeneratorContext generatorContext) throws IOException {
        // Generate imports
        NodeList<ImportDeclarationNode> imports = generateImports();
        // Generate auth config records & client class
        NodeList<ModuleMemberDeclarationNode> members =
                generateMembers(queryDocuments, graphQLSchema, authConfig, generatorContext);

        ModulePartNode modulePartNode = createModulePartNode(imports, members, createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generates the imports in the client file.
     *
     * @return the node list which represent imports in the client file
     */
    private NodeList<ImportDeclarationNode> generateImports() {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForGraphql =
                CodeGeneratorUtils.getImportDeclarationNode(CodeGeneratorConstants.BALLERINA,
                        CodeGeneratorConstants.GRAPHQL);
        imports.add(importForGraphql);
        return createNodeList(imports);
    }

    /**
     * Generates the members in the client file. The members include auth config record types & client class nodes.
     *
     * @param queryDocuments the list of names of the query documents
     * @param graphQLSchema  the object instance of the GraphQL schema (SDL)
     * @param authConfig     the object instance representing authentication configuration information
     * @return the node list which represent members in the client file
     */
    private NodeList<ModuleMemberDeclarationNode> generateMembers(List<String> queryDocuments,
                                                                  GraphQLSchema graphQLSchema, AuthConfig authConfig,
                                                                  GeneratorContext generatorContext)
            throws IOException {
        List<ModuleMemberDeclarationNode> members = new ArrayList<>();
        // Generate client class
        ClassDefinitionNode classDefinitionNode =
                generateClientClass(queryDocuments, graphQLSchema, authConfig, generatorContext);
        members.add(classDefinitionNode);
        return createNodeList(members);
    }

    /**
     * Generates the client class in the client file.
     *
     * @param queryDocuments the list of names of the query documents
     * @param graphQLSchema  the object instance of the GraphQL schema (SDL)
     * @param authConfig     the object instance representing authentication configuration information
     * @return the node which represent the client class in the client file
     */
    private ClassDefinitionNode generateClientClass(List<String> queryDocuments, GraphQLSchema graphQLSchema,
                                                    AuthConfig authConfig, GeneratorContext generatorContext)
            throws IOException {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        NodeList<Token> classTypeQualifiers =
                createNodeList(createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        IdentifierToken className = createIdentifierToken(CodeGeneratorUtils.getClientClassName(generatorContext));

        // Collect members for class definition node
        List<Node> members = new ArrayList<>();
        // Generate class instance variables
        members.addAll(generateClassInstanceVariables(authConfig));
        // Generate init function
        members.add(generateInitFunction(authConfig));
        // Generate remote functions
        members.addAll(generateRemoteFunctions(queryDocuments, graphQLSchema, authConfig));

        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN), createNodeList(members),
                createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generates the client class instance variables.
     *
     * @param authConfig the object instance representing authentication configuration information
     * @return the list of nodes which represent the client class instance variables
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
     * @param authConfig the object instance representing authentication configuration information
     * @return the node which represent the init function
     */
    private FunctionDefinitionNode generateInitFunction(AuthConfig authConfig) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));

        IdentifierToken functionName = createIdentifierToken(CodeGeneratorConstants.INIT);

        FunctionSignatureNode functionSignatureNode =
                FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
        FunctionBodyNode functionBodyNode = FunctionBodyGenerator.getInstance().generateInitFunctionBody(authConfig);

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Generates the client class remote functions.
     *
     * @param queryDocuments the list of names of the query documents
     * @param graphQLSchema  the object instance of the GraphQL schema (SDL)
     * @param authConfig     the object instance representing authentication configuration information
     * @return the list of nodes which represent the remote functions
     */
    private List<FunctionDefinitionNode> generateRemoteFunctions(List<String> queryDocuments,
                                                                 GraphQLSchema graphQLSchema, AuthConfig authConfig)
            throws IOException {
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();

        for (String document : queryDocuments) {
            Document queryDocument = Utils.getGraphQLQueryDocument(document);
            QueryReader queryReader = new QueryReader(queryDocument);

            for (ExtendedOperationDefinition queryDefinition : queryReader.getExtendedOperationDefinitions()) {
                // Generate remote function
                FunctionDefinitionNode functionDefinitionNode =
                        generateRemoteFunction(queryDefinition, graphQLSchema, authConfig);
                functionDefinitionNodeList.add(functionDefinitionNode);
            }
        }
        return functionDefinitionNodeList;
    }

    /**
     * Generates a client class remote function.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @param authConfig      the object instance representing authentication configuration information
     * @return the node which represent the remote function
     */
    private FunctionDefinitionNode generateRemoteFunction(ExtendedOperationDefinition queryDefinition,
                                                          GraphQLSchema graphQLSchema, AuthConfig authConfig) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));

        // Obtain functionName from queryName
        IdentifierToken functionName = createIdentifierToken(queryDefinition.getName());

        FunctionSignatureNode functionSignatureNode = FunctionSignatureGenerator.getInstance()
                .generateRemoteFunctionSignature(queryDefinition, graphQLSchema);
        FunctionBodyNode functionBodyNode = FunctionBodyGenerator.getInstance()
                .generateRemoteFunctionBody(queryDefinition, graphQLSchema, authConfig);

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Generates the GraphQL client {@code final graphql:Client graphqlClient;} instance variable.
     *
     * @return the node which represent the {@code graphqlClient} instance variable
     */
    private ObjectFieldNode generateGraphqlClientField() {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);

        QualifiedNameReferenceNode typeName =
                createQualifiedNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.GRAPHQL),
                        createToken(COLON_TOKEN), createIdentifierToken(CodeGeneratorConstants.CLIENT));

        IdentifierToken fieldName = createIdentifierToken(CodeGeneratorConstants.GRAPHQL_CLIENT);

        return createObjectFieldNode(metadataNode, null, qualifierList, typeName, fieldName, null, null,
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the API keys config {@code final readonly & ApiKeysConfig apiKeysConfig;} instance variable.
     *
     * @return the node which represent the {@code apiKeysConfig} instance variable
     */
    private ObjectFieldNode generateApiKeysConfigField() {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(FINAL_KEYWORD));

        TypeDescriptorNode readOnlyNode =
                createTypeReferenceTypeDescNode(createSimpleNameReferenceNode(createToken(READONLY_KEYWORD)));
        TypeDescriptorNode apiKeysConfigNode =
                createSimpleNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.API_KEYS_CONFIG_TYPE_NAME));
        TypeDescriptorNode typeName =
                createIntersectionTypeDescriptorNode(readOnlyNode, createToken(BITWISE_AND_TOKEN), apiKeysConfigNode);

        IdentifierToken fieldName = createIdentifierToken(CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME);

        return createObjectFieldNode(metadataNode, null, qualifierList, typeName, fieldName, null, null,
                createToken(SEMICOLON_TOKEN));
    }
}
