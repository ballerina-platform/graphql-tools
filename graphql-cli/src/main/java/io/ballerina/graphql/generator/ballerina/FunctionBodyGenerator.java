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

import graphql.schema.GraphQLSchema;
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
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.model.AuthConfig;

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
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLIENT_EP;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLONE_READ_ONLY;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.COMMA;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HEADER_VALUES_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HEADER_VALUES_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HTTP_HEADERS_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HTTP_HEADERS_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.QUERY_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.SELF;

/**
 * This class is used to generate function body's in the ballerina client file.
 */
public class FunctionBodyGenerator {
    private static FunctionBodyGenerator functionBodyGenerator = null;

    public static FunctionBodyGenerator getInstance() {
        if (functionBodyGenerator == null) {
            functionBodyGenerator = new FunctionBodyGenerator();
        }
        return functionBodyGenerator;
    }

    /**
     * Generates the client class init function body.
     *
     * @param authConfig        the object instance representing authentication configuration information
     * @return                  the node which represent the init function body
     */
    public FunctionBodyNode generateInitFunctionBody(AuthConfig authConfig) {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        // Generate initialization statement of {@code graphql:Client} class instance
        VariableDeclarationNode clientInitializationNode = generateClientInitializationNode();

        // Generate {@code self.graphqlClient = clientEp;} assignment node
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(GRAPHQL_CLIENT)));
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken(CLIENT_EP));
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));

        // Generate {@code self.apiKeyConfig = apiKeyConfig.cloneReadOnly();} assignment node
        AssignmentStatementNode apiKeyConfigAssignmentStatementNode = generateApiKeyConfigAssignmentStatementNode();

        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));

        assignmentNodes.add(clientInitializationNode);
        assignmentNodes.add(httpClientAssignmentStatementNode);
        if (authConfig.isApiKeysConfig()) {
            assignmentNodes.add(apiKeyConfigAssignmentStatementNode);
        }
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generates the client class remote function body.
     *
     * @param queryDefinition       the object instance of a single query definition in a query document
     * @param graphQLSchema         the object instance of the GraphQL schema (SDL)
     * @param authConfig            the object instance representing authentication configuration information
     * @return                      the node which represent the remote function body
     */
    public FunctionBodyNode generateRemoteFunctionBody(ExtendedOperationDefinition queryDefinition,
                                                       GraphQLSchema graphQLSchema, AuthConfig authConfig) {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        VariableDeclarationNode queryVariableDeclarationNode = generateQueryVariableDeclarationNode(queryDefinition);
        VariableDeclarationNode graphqlVariablesDeclarationNode =
                getGraphqlVariablesDeclarationNode(queryDefinition, graphQLSchema);

        VariableDeclarationNode headerValuesVariableDeclarationNode =
                generateHeaderValuesVariableDeclarationNode(authConfig);
        VariableDeclarationNode httpHeadersVariableDeclarationNode = generateHttpHeadersVariableDeclarationNode();

        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(
                createIdentifierToken(CodeGeneratorUtils.getRemoteFunctionBodyReturnTypeName(
                        queryDefinition.getName())));
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), expr, createToken(SEMICOLON_TOKEN));

        assignmentNodes.add(queryVariableDeclarationNode);
        assignmentNodes.add(graphqlVariablesDeclarationNode);

        if (authConfig.isApiKeysConfig()) {
            assignmentNodes.add(headerValuesVariableDeclarationNode);
            assignmentNodes.add(httpHeadersVariableDeclarationNode);
            assignmentNodes.add(generateReturnStatementNodeWithHttpHeaders(queryDefinition));
        } else {
            assignmentNodes.add(returnStatementNode);
        }
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generates the initialization statement of {@code graphql:Client} class instance in the init function.
     *
     * @return                  the node which represent the client initialization
     */
    private VariableDeclarationNode generateClientInitializationNode() {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // {@code graphql:Client} variable declaration
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
     * Generates the {@code self.apiKeyConfig = apiKeyConfig.cloneReadOnly();} assignment node in the init function.
     *
     * @return                  the node which represent the API key config assignment statement
     */
    private AssignmentStatementNode generateApiKeyConfigAssignmentStatementNode() {
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
     * Generates the {@code query} variable declaration node in the remote function.
     *
     * @param queryDefinition       the object instance of a single query definition in a query document
     * @return                      the node which represent the {@code query} variable declaration
     */
    private VariableDeclarationNode generateQueryVariableDeclarationNode(ExtendedOperationDefinition queryDefinition) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // {@code query} variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createToken(STRING_KEYWORD));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(QUERY_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        NodeList<Node> content = createNodeList(createIdentifierToken(queryDefinition.getQueryString()));
        TemplateExpressionNode initializer = createTemplateExpressionNode(null, createToken(STRING_KEYWORD),
                createToken(BACKTICK_TOKEN), content, createToken(BACKTICK_TOKEN));

        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the GraphQL {@code variables} variable declaration node in the remote function.
     *
     * @param queryDefinition       the object instance of a single query definition in a query document
     * @param graphQLSchema         the object instance of the GraphQL schema (SDL)
     * @return                      the node which represent the GraphQL {@code variables} declaration
     */
    private VariableDeclarationNode getGraphqlVariablesDeclarationNode(ExtendedOperationDefinition queryDefinition,
                                                                       GraphQLSchema graphQLSchema) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // GraphQL {@code variables} declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GRAPHQL_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> specificFields = new ArrayList<>();

        int count = 0;
        for (String variableName: queryDefinition.getVariableDefinitionsMap(graphQLSchema).keySet()) {
            BuiltinSimpleNameReferenceNode valueExpr = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(variableName));
            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    createIdentifierToken("\"" + variableName + "\""), createToken(COLON_TOKEN), valueExpr);
            specificFields.add(specificFieldNode);
            count++;
            if (count < queryDefinition.getVariableDefinitionsMap(graphQLSchema).size()) {
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
     * Generates the {@code headerValues} variable declaration node in the remote function.
     *
     * @param authConfig            the object instance representing authentication configuration information
     * @return                      the node which represent the {@code headerValues} variable declaration
     */
    private VariableDeclarationNode generateHeaderValuesVariableDeclarationNode(AuthConfig authConfig) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // {@code headerValues} variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HEADER_VALUES_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(HEADER_VALUES_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        Set<String> apiHeaders = authConfig.getApiHeaders();

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
     * Generates the {@code httpHeaders} variable declaration node in the remote function.
     *
     * @return                  the node which represent the {@code httpHeaders} variable declaration
     */
    private VariableDeclarationNode generateHttpHeadersVariableDeclarationNode() {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        // {@code httpHeaders} variable declaration
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
     * Generate the return statement for a remote function with Http headers.
     *
     * @param queryDefinition   the object instance of a single query definition in a query document
     * @return                  the node which represent the return statement for a remote function with Http headers
     */
    private ReturnStatementNode generateReturnStatementNodeWithHttpHeaders(
            ExtendedOperationDefinition queryDefinition) {
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(
                createIdentifierToken(
                        CodeGeneratorUtils.getRemoteFunctionBodyReturnTypeNameWithHeaders(queryDefinition.getName())));
        return createReturnStatementNode(createToken(RETURN_KEYWORD), expr, createToken(SEMICOLON_TOKEN));
    }
}
