/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generators.ballerina;

import graphql.language.Document;
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
import io.ballerina.graphql.generators.CodeGeneratorConstants;
import io.ballerina.graphql.generators.CodeGeneratorUtils;
import io.ballerina.graphql.generators.graphql.QueryReader;
import io.ballerina.graphql.generators.graphql.components.ExtendedOperationDefinition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import static io.ballerina.graphql.generators.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.API_KEYS_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL_CLIENT;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.INIT;

/**
 * This class is used to generate ballerina client file according to given SDL and query file.
 */
public class ClientGenerator {
    private static final Log log = LogFactory.getLog(ClientGenerator.class);
    private final AuthConfigGenerator ballerinaAuthConfigGenerator;

    public ClientGenerator(AuthConfigGenerator ballerinaAuthConfigGenerator) {
        this.ballerinaAuthConfigGenerator = ballerinaAuthConfigGenerator;
    }

    /**
     * Generates the client syntax tree.
     *
     * @param queriesDocument   the instance of the queries document
     * @param documentName      the name of the queries document
     * @return                  Syntax tree for the ballerina client code
     */
    public SyntaxTree generateSyntaxTree(Document queriesDocument, String documentName) {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForHttp = CodeGeneratorUtils.getImportDeclarationNode(
                CodeGeneratorConstants.BALLERINA, CodeGeneratorConstants.HTTP);
        imports.add(importForHttp);
        ImportDeclarationNode importForGraphql = CodeGeneratorUtils.getImportDeclarationNode(
                CodeGeneratorConstants.BALLERINAX, CodeGeneratorConstants.GRAPHQL);
        imports.add(importForGraphql);
        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);

        List<ModuleMemberDeclarationNode> members =  new ArrayList<>();
        ballerinaAuthConfigGenerator.addAuthRelatedRecords(members);
        members.add(getClassDefinitionNode(queriesDocument, documentName));

        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(members), createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Returns the class definition node for a given queries document.
     *
     * @param queriesDocument   the instance of the queries document
     * @param documentName      the name of the queries document
     * @return                  the class definition node
     */
    private ClassDefinitionNode getClassDefinitionNode(Document queriesDocument, String documentName) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        IdentifierToken className = createIdentifierToken(CodeGeneratorUtils.getClientClassName(documentName));
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));

        // Collect members for class definition node
        List<Node> memberNodeList =  new ArrayList<>();
        // Add instance variable to class definition node
        memberNodeList.addAll(createClassInstanceVariables());
        // Add init function to class definition node
        memberNodeList.add(createInitFunction());
        // Generate remote function Nodes
        memberNodeList.addAll(createRemoteFunctions(queriesDocument));

        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Creates the client class instance variables.
     *
     * @return                  the list class instance variable nodes
     */
    private List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();

        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);

        QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(GRAPHQL),
                createToken(COLON_TOKEN), createIdentifierToken(CodeGeneratorConstants.CLIENT));

        IdentifierToken fieldName = createIdentifierToken(GRAPHQL_CLIENT);

        ObjectFieldNode graphqlClientField = createObjectFieldNode(metadataNode, null,
                qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN));
        fieldNodeList.add(graphqlClientField);

        if (ballerinaAuthConfigGenerator.isApiKeysConfig()) {
            fieldNodeList.add(getApiKeysFieldNode());
        }

        return fieldNodeList;
    }

    /**
     * Returns the API keys field node.
     *
     * @return                  the API keys field node
     */
    private ObjectFieldNode getApiKeysFieldNode() {
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

    /**
     * Creates the client class init function.
     *
     * @return                  the init function node
     */
    private FunctionDefinitionNode createInitFunction() {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));

        IdentifierToken functionName = createIdentifierToken(INIT);

        FunctionSignatureGenerator functionSignatureGenerator =
                new FunctionSignatureGenerator(ballerinaAuthConfigGenerator);
        FunctionSignatureNode functionSignatureNode = functionSignatureGenerator.getInitFunctionSignatureNode();
        FunctionBodyGenerator functionBodyGenerator =
                new FunctionBodyGenerator(ballerinaAuthConfigGenerator);
        FunctionBodyNode functionBodyNode = functionBodyGenerator.getInitFunctionBodyNode();

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Creates the client class remote functions.
     *
     * @return                  the list of remote function nodes
     */
    private List<FunctionDefinitionNode> createRemoteFunctions(Document queriesDocument) {
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();

        QueryReader queryReader = new QueryReader(queriesDocument);

        for (ExtendedOperationDefinition def: queryReader.getExtendedOperationDefinitions()) {
            // Generate remote function
            FunctionDefinitionNode functionDefinitionNode = createRemoteFunction(def);
            functionDefinitionNodeList.add(functionDefinitionNode);
        }
        return functionDefinitionNodeList;
    }

    /**
     * Creates a client class remote function.
     *
     * @param def               the instance of the `ExtendedOperationDefinition` from the query reader
     * @return                  the remote function definition node
     */
    private FunctionDefinitionNode createRemoteFunction(ExtendedOperationDefinition def) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));

        // Obtain functionName from queryName
        IdentifierToken functionName = createIdentifierToken(def.getName());

        FunctionSignatureGenerator functionSignatureGenerator =
                new FunctionSignatureGenerator(ballerinaAuthConfigGenerator);
        FunctionSignatureNode functionSignatureNode = functionSignatureGenerator.getRemoteFunctionSignatureNode(def);
        FunctionBodyGenerator functionBodyGenerator =
                new FunctionBodyGenerator(ballerinaAuthConfigGenerator);
        FunctionBodyNode functionBodyNode = functionBodyGenerator.getRemoteFunctionBodyNode(def);

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }
}
