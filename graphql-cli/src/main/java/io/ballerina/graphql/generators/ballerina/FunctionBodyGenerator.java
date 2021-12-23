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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.graphql.generators.CodeGeneratorConstants;
import io.ballerina.graphql.generators.CodeGeneratorUtils;
import io.ballerina.graphql.generators.graphql.components.ExtendedOperationDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTemplateExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BACKTICK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.CLIENT_EP;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.CLONE_READ_ONLY;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.COMMA;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL_CLIENT;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL_CLIENT_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL_CLIENT_VAR_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.GRAPHQL_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HEADER_VALUES_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HEADER_VALUES_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HTTP_HEADERS_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HTTP_HEADERS_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.QUERY_VAR_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.SELF;

/**
 * This class is used to generate function body's in the ballerina client file.
 */
public class FunctionBodyGenerator {
    private static final Log log = LogFactory.getLog(FunctionBodyGenerator.class);
    private final AuthConfigGenerator ballerinaAuthConfigGenerator;

    public FunctionBodyGenerator(AuthConfigGenerator ballerinaAuthConfigGenerator) {
        this.ballerinaAuthConfigGenerator = ballerinaAuthConfigGenerator;
    }

    /**
     * Returns the client class init function body node.
     *
     * @return                  the init function body node
     */
    public FunctionBodyNode getInitFunctionBodyNode() {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        // create initialization statement of graphql:Client class instance
        VariableDeclarationNode clientInitializationNode = getClientInitializationNode();

        // create {@code self.graphqlClient = clientEp;} assignment node
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(GRAPHQL_CLIENT)));
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken(CLIENT_EP));
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));

        // create {@code self.apiKeyConfig = apiKeyConfig.cloneReadOnly();} assignment node
        AssignmentStatementNode apiKeyConfigAssignmentStatementNode = getApiKeyConfigAssignmentStatementNode();

        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));

        assignmentNodes.add(clientInitializationNode);
        assignmentNodes.add(httpClientAssignmentStatementNode);

        if (ballerinaAuthConfigGenerator.isApiKeysConfig()) {
            assignmentNodes.add(apiKeyConfigAssignmentStatementNode);
        }

        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Returns the client initialization node in the init function.
     *
     * @return                  the client initialization node
     */
    private VariableDeclarationNode getClientInitializationNode() {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // graphql:Client variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GRAPHQL_CLIENT_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_CLIENT_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(CodeGeneratorConstants.SERVICE_URL_PARAM_NAME)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(COMMA);

        PositionalArgumentNode positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(HTTP_CLIENT_CONFIG_PARAM_NAME)));
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(createToken(OPEN_PAREN_TOKEN), arguments,
                createToken(CLOSE_PAREN_TOKEN));
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(createToken(NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);

        return createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer,
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns the API key config assignment statement node.
     *
     * @return                  the API key config assignment statement node
     */
    private AssignmentStatementNode getApiKeyConfigAssignmentStatementNode() {
        FieldAccessExpressionNode varRefApiKey = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));

        ExpressionNode fieldAccessExpressionNode = createRequiredExpressionNode(
                createIdentifierToken(API_KEY_CONFIG_PARAM));
        ExpressionNode methodCallExpressionNode = createMethodCallExpressionNode(
                fieldAccessExpressionNode, createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(CLONE_READ_ONLY)),
                createToken(OPEN_PAREN_TOKEN), createSeparatedNodeList(), createToken(CLOSE_PAREN_TOKEN));

        return createAssignmentStatementNode(varRefApiKey, createToken(EQUAL_TOKEN),
                methodCallExpressionNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns the client class remote function body node.
     *
     * @param def               the instance of the `ExtendedOperationDefinition` from the query reader
     * @return                  the remote function body node
     */
    public FunctionBodyNode getRemoteFunctionBodyNode(ExtendedOperationDefinition def) {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        VariableDeclarationNode queryVariableDeclarationNode = getQueryVariableDeclarationNode(def);
        VariableDeclarationNode graphqlVariablesDeclarationNode = getGraphqlVariablesDeclarationNode(def);

        VariableDeclarationNode headerValuesVariableDeclarationNode = getHeaderValuesVariableDeclarationNode();
        VariableDeclarationNode httpHeadersVariableDeclarationNode = getHttpHeadersVariableDeclarationNode();

        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(
                createIdentifierToken(CodeGeneratorUtils.getRemoteFunctionBodyReturnTypeName(def.getName())));
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), expr, createToken(SEMICOLON_TOKEN));

        assignmentNodes.add(queryVariableDeclarationNode);
        assignmentNodes.add(graphqlVariablesDeclarationNode);

        if (ballerinaAuthConfigGenerator.isApiKeysConfig()) {
            assignmentNodes.add(headerValuesVariableDeclarationNode);
            assignmentNodes.add(httpHeadersVariableDeclarationNode);
            assignmentNodes.add(getReturnStatementNodeWithHttpHeaders(def));
        } else {
            assignmentNodes.add(returnStatementNode);
        }
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Returns the query variable declaration node.
     *
     * @param def               the instance of the `ExtendedOperationDefinition` from the query reader
     * @return                  the query variable declaration node
     */
    private VariableDeclarationNode getQueryVariableDeclarationNode(ExtendedOperationDefinition def) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // query variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createToken(STRING_KEYWORD));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(QUERY_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        NodeList<Node> content = createNodeList(createIdentifierToken(def.getQueryString()));
        TemplateExpressionNode initializer = createTemplateExpressionNode(null, createToken(STRING_KEYWORD),
                createToken(BACKTICK_TOKEN), content, createToken(BACKTICK_TOKEN));

        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns the GraphQL variables declaration node.
     *
     * @param def               the instance of the `ExtendedOperationDefinition` from the query reader
     * @return                  the GraphQL variables declaration node
     */
    private VariableDeclarationNode getGraphqlVariablesDeclarationNode(ExtendedOperationDefinition def) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // GraphQL variables declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GRAPHQL_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> specificFields = new ArrayList<>();

        int count = 0;
        for (String variableName: def.getVariableDefinitionsMap().keySet()) {
            BuiltinSimpleNameReferenceNode valueExpr = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(variableName));
            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    createIdentifierToken("\"" + variableName + "\""), createToken(COLON_TOKEN), valueExpr);
            specificFields.add(specificFieldNode);
            count++;
            if (count < def.getVariableDefinitionsMap().size()) {
                specificFields.add(createToken(COMMA_TOKEN));
            }
        }

        MappingConstructorExpressionNode initializer = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(specificFields),
                createToken(CLOSE_BRACE_TOKEN));

        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns the GraphQL variables declaration node.
     *
     * @return                  the GraphQL variables declaration node
     */
    private VariableDeclarationNode getHeaderValuesVariableDeclarationNode() {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // headerValues variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HEADER_VALUES_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(HEADER_VALUES_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        Set<String> apiHeaders = ballerinaAuthConfigGenerator.getApiHeaders();

        // Expression node
        List<Node> specificFields = new ArrayList<>();

        int count = 0;
        for (String apiHeaderName: apiHeaders) {
            IdentifierToken apiKeyConfigIdentifierToken = createIdentifierToken(API_KEYS_CONFIG_PARAM_NAME);
            SimpleNameReferenceNode apiKeyConfigParamNode = createSimpleNameReferenceNode(
                    apiKeyConfigIdentifierToken);
            FieldAccessExpressionNode fieldExpr = createFieldAccessExpressionNode(
                    createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                    apiKeyConfigParamNode);
            SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(
                    createIdentifierToken(CodeGeneratorUtils.getValidName(apiHeaderName)));
            ExpressionNode apiKeyExpr = createFieldAccessExpressionNode(
                    fieldExpr, createToken(DOT_TOKEN), valueExpr);

            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    createIdentifierToken("\"" + apiHeaderName + "\""), createToken(COLON_TOKEN), apiKeyExpr);
            specificFields.add(specificFieldNode);
            count++;
            if (count < apiHeaders.size()) {
                specificFields.add(createToken(COMMA_TOKEN));
            }
        }

        MappingConstructorExpressionNode initializer = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(specificFields),
                createToken(CLOSE_BRACE_TOKEN));

        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns the GraphQL variables declaration node.
     *
     * @return                  the GraphQL variables declaration node
     */
    private VariableDeclarationNode getHttpHeadersVariableDeclarationNode() {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // httpHeaders variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HTTP_HEADERS_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(HTTP_HEADERS_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        IdentifierToken functionName = createIdentifierToken("getMapForHeaders");
        SimpleNameReferenceNode functionNameNode = createSimpleNameReferenceNode(functionName);
        SimpleNameReferenceNode expressionNode = createSimpleNameReferenceNode(
                createIdentifierToken("headerValues"));
        FunctionArgumentNode node = createPositionalArgumentNode(expressionNode);
        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(node);
        FunctionCallExpressionNode initializer =
                createFunctionCallExpressionNode(functionNameNode, createToken(OPEN_PAREN_TOKEN),
                        arguments, createToken(CLOSE_PAREN_TOKEN));

        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns the return statement node for a remote function with Http headers.
     *
     * @return                  the return statement node for a remote function with Http headers
     */
    private ReturnStatementNode getReturnStatementNodeWithHttpHeaders(ExtendedOperationDefinition def) {
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(
                createIdentifierToken(
                        CodeGeneratorUtils.getRemoteFunctionBodyReturnTypeNameWithHeaders(def.getName())));
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), expr, createToken(SEMICOLON_TOKEN));
        return returnStatementNode;
    }
}
