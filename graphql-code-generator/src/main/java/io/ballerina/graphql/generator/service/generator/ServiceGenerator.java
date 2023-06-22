/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.generator.service.generator;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createCommentMinutiae;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModuleVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.INT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;

/**
 * This class is used to generate ballerina service file according to the GraphQL schema.
 */
public class ServiceGenerator {
    private String fileName;
    private List<MethodDeclarationNode> methodDeclarations;

    public ModulePartNode generateContentNode() {
        NodeList<ImportDeclarationNode> imports = CodeGeneratorUtils.generateImports();
        NodeList<ModuleMemberDeclarationNode> serviceBody = generateMembers();
        return createModulePartNode(imports, serviceBody, createToken(SyntaxKind.EOF_TOKEN));
    }

    private SyntaxTree generateSyntaxTree() throws IOException {
        ModulePartNode modulePartNode = generateContentNode();
        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private NodeList<ModuleMemberDeclarationNode> generateMembers() {
        List<ModuleMemberDeclarationNode> members = new ArrayList<>();
        ModuleVariableDeclarationNode portVariable =
                generatePortModuleVariableDeclaration(CodeGeneratorConstants.PORT_NUMBER_DEFAULT);
        members.add(portVariable);
        ServiceDeclarationNode serviceDeclaration = generateServiceDeclaration();
        members.add(serviceDeclaration);
        return createNodeList(members);
    }

    private ModuleVariableDeclarationNode generatePortModuleVariableDeclaration(String portNumberStr) {
        NodeList<Token> configurableQualifier = createNodeList(createToken(SyntaxKind.CONFIGURABLE_KEYWORD));
        TypedBindingPatternNode portTypeBinding =
                createTypedBindingPatternNode(createBuiltinSimpleNameReferenceNode(null, createToken(INT_KEYWORD)),
                        createCaptureBindingPatternNode(createIdentifierToken(CodeGeneratorConstants.PORT)));
        BasicLiteralNode portNumber =
                NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, createIdentifierToken(portNumberStr));
        return createModuleVariableDeclarationNode(null, null, configurableQualifier, portTypeBinding,
                createToken(SyntaxKind.EQUAL_TOKEN), portNumber, createToken(SEMICOLON_TOKEN));
    }

    private ServiceDeclarationNode generateServiceDeclaration() {
        NodeList<Token> qualifiers = createEmptyNodeList();
        SimpleNameReferenceNode fileName = createSimpleNameReferenceNode(createIdentifierToken(this.fileName));
        NodeList<Node> absoluteResourcePath = createEmptyNodeList();
        ExplicitNewExpressionNode graphqlListener = generateGraphqlListener();
        NodeList<Node> functionDefinitions = generateServiceFunctionDefinitions();
        return createServiceDeclarationNode(null, qualifiers, createToken(SERVICE_KEYWORD), fileName,
                absoluteResourcePath, createToken(ON_KEYWORD), createSeparatedNodeList(graphqlListener),
                createToken(SyntaxKind.OPEN_BRACE_TOKEN), functionDefinitions,
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }

    private NodeList<Node> generateServiceFunctionDefinitions() {
        List<Node> functionDefinitions = new ArrayList<>();
        for (int methodDeclarationInd = 0; methodDeclarationInd < this.methodDeclarations.size();
             methodDeclarationInd++) {
            MethodDeclarationNode methodDeclaration = this.methodDeclarations.get(methodDeclarationInd);
            MetadataNode methodDeclarationMetadata = methodDeclaration.metadata().orElse(null);
            methodDeclarationMetadata =
                    modifyMetadataForServiceFunctionDefinition(methodDeclarationMetadata, methodDeclarationInd);
            NodeList<Token> finalQualifiers =
                    modifyMethodDeclarationQualifiersForServiceFunctionDefinition(methodDeclaration.qualifierList(),
                            methodDeclarationInd, methodDeclarationMetadata);
            FunctionBodyBlockNode emptyFunctionBody =
                    createFunctionBodyBlockNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN), null, createEmptyNodeList(),
                            createToken(SyntaxKind.CLOSE_BRACE_TOKEN, createEmptyMinutiaeList(),
                                    createEmptyMinutiaeList()), null);
            FunctionDefinitionNode functionDefinition =
                    createFunctionDefinitionNode(getDefinitionKindFromDeclarationKind(methodDeclaration.kind()),
                            methodDeclarationMetadata, finalQualifiers,
                            methodDeclaration.functionKeyword(), methodDeclaration.methodName(),
                            methodDeclaration.relativeResourcePath(), methodDeclaration.methodSignature(),
                            emptyFunctionBody);
            functionDefinitions.add(functionDefinition);
        }
        return createNodeList(functionDefinitions);
    }

    private NodeList<Token> modifyMethodDeclarationQualifiersForServiceFunctionDefinition
            (NodeList<Token> qualifierList, int methodDeclarationInd, MetadataNode methodDeclarationMetadata) {
        if (methodDeclarationInd != 0 && methodDeclarationMetadata == null) {
            MinutiaeList leadingMinutiaeList =
                    createMinutiaeList(createCommentMinutiae(CodeGeneratorConstants.NEW_LINE));
            Token firstQualifier = qualifierList.get(0);
            qualifierList = qualifierList.remove(0);
            Token newLineAddedFirstQualifier =
                    firstQualifier.modify(leadingMinutiaeList, createEmptyMinutiaeList());
            qualifierList = qualifierList.add(0, newLineAddedFirstQualifier);
        }
        return qualifierList;
    }

    private MetadataNode modifyMetadataForServiceFunctionDefinition(MetadataNode methodDeclarationMetadata,
                                                                    int methodDeclarationInd) {
        if (methodDeclarationInd == 0 || methodDeclarationMetadata == null) {
            return methodDeclarationMetadata;
        } else {
            return addNewLineInFrontOfMetadata(methodDeclarationMetadata);
        }
    }

    public static MetadataNode addNewLineInFrontOfMetadata(MetadataNode metadata) {
        Node documentationString = metadata.documentationString().orElse(null);
        if (documentationString instanceof MarkdownDocumentationNode) {
            MarkdownDocumentationNode markdownDocumentation = (MarkdownDocumentationNode) documentationString;
            NodeList<Node> documentationLines = markdownDocumentation.documentationLines();
            Node firstDocumentationLine = documentationLines.get(0);
            if (firstDocumentationLine instanceof MarkdownDocumentationLineNode) {
                MarkdownDocumentationLineNode firstMarkdownDocumentationLine =
                        (MarkdownDocumentationLineNode) firstDocumentationLine;
                Token firstLineHash = firstMarkdownDocumentationLine.hashToken();
                return metadata.replace(firstLineHash, createToken(SyntaxKind.HASH_TOKEN,
                        createMinutiaeList(createCommentMinutiae(CodeGeneratorConstants.NEW_LINE)),
                        createEmptyMinutiaeList()));
            }
        }
        return metadata;
    }

    private SyntaxKind getDefinitionKindFromDeclarationKind(SyntaxKind declarationKind) {
        if (SyntaxKind.RESOURCE_ACCESSOR_DECLARATION.equals(declarationKind)) {
            return SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
        } else if (SyntaxKind.METHOD_DECLARATION.equals(declarationKind)) {
            return SyntaxKind.OBJECT_METHOD_DEFINITION;
        } else {
            return null;
        }
    }

    private ExplicitNewExpressionNode generateGraphqlListener() {
        QualifiedNameReferenceNode graphqlListenerName =
                createQualifiedNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.GRAPHQL),
                        createToken(COLON_TOKEN), createIdentifierToken(CodeGeneratorConstants.LISTENER));

        PositionalArgumentNode argumentPort = createPositionalArgumentNode(
                createSimpleNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.PORT)));
        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentPort);
        ParenthesizedArgList parenthesizedArguments =
                createParenthesizedArgList(createToken(SyntaxKind.OPEN_PAREN_TOKEN), arguments,
                        createToken(SyntaxKind.CLOSE_PAREN_TOKEN));
        return createExplicitNewExpressionNode(createToken(NEW_KEYWORD), graphqlListenerName, parenthesizedArguments);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMethodDeclarations(List<MethodDeclarationNode> methodDeclarations) {
        this.methodDeclarations = methodDeclarations;
    }

    public String generateSrc() throws ServiceGenerationException {
        try {
            return Formatter.format(generateSyntaxTree()).toString();
        } catch (FormatterException | IOException e) {
            throw new ServiceGenerationException(e.getMessage());
        }
    }
}
