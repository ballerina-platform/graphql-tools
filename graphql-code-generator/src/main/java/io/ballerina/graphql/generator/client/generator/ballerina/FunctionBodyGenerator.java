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

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.client.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.client.generator.model.AuthConfig;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BACKTICK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DO_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FAIL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.JSON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RIGHT_ARROW_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.VAR_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLIENT_EP;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLONE_READ_ONLY;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.COMMA;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT_CONFIGURATION_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_RESPONSE_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HEADER_VALUES_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HEADER_VALUES_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HTTP_HEADERS_VARIABLES_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.HTTP_HEADERS_VARIABLES_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.QUERY_VAR_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.SELF;
import static io.ballerina.graphql.generator.utils.CodeGeneratorUtils.escapeIdentifier;

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
     * @param authConfig the object instance representing authentication configuration information
     * @return the node which represent the init function body
     */
    public FunctionBodyNode generateInitFunctionBody(AuthConfig authConfig) {
        List<StatementNode> assignmentNodes = new ArrayList<>();
        // Generate initialization statement of httpClientConfig
        List<StatementNode> httpClientConfigNode = generateHttpClientConfigurationNode(authConfig);

        // Generate initialization statement of {@code graphql:Client} class instance
        VariableDeclarationNode clientInitializationNode = generateClientInitializationNode();

        // Generate {@code self.graphqlClient = clientEp;} assignment node
        FieldAccessExpressionNode varRef = NodeFactory.createFieldAccessExpressionNode(
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(GRAPHQL_CLIENT)));
        SimpleNameReferenceNode expr = NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(CLIENT_EP));
        AssignmentStatementNode httpClientAssignmentStatementNode = NodeFactory.createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));

        // Generate {@code self.apiKeyConfig = apiKeyConfig.cloneReadOnly();} assignment node
        AssignmentStatementNode apiKeyConfigAssignmentStatementNode = generateApiKeyConfigAssignmentStatementNode();

        assignmentNodes.addAll(httpClientConfigNode);
        assignmentNodes.add(clientInitializationNode);
        assignmentNodes.add(httpClientAssignmentStatementNode);
        if (authConfig.isApiKeysConfig()) {
            assignmentNodes.add(apiKeyConfigAssignmentStatementNode);
        }
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null,
                statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generates http client config record.
     *
     * @param authConfig the object instance representing authentication configuration information
     * @return           the object instance representing http client config
     * http:ClientConfiguration httpClientConfig = {auth: config.auth, httpVersion: config.httpVersion,
     * timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig,
     * compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig,
     * validation: config.validation};
     * do {
     *     if config.http1Settings is ClientHttp1Settings {
     *         ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
     *         httpClientConfig.http1Settings = {...settings};
     *     }
     *     if config.http2Settings is http:ClientHttp2Settings {
     *         httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
     *     }
     *     if config.cache is http:CacheConfig {
     *         httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
     *     }
     *     if config.responseLimits is http:ResponseLimitConfigs {
     *         httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
     *     }
     *     if config.secureSocket is http:ClientSecureSocket {
     *         httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
     *     }
     *     if config.proxy is http:ProxyConfig {
     *         httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
     *     }
     * } on fail var e {
     *    return <graphql:ClientError>error("GraphQL Client Error", e, body = ());
     * }
     */
    private List<StatementNode>  generateHttpClientConfigurationNode(AuthConfig authConfig) {
        List<StatementNode> assignmentNodes = new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // GraphQL {@code variables} declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HTTP_CLIENT_CONFIG_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken("graphqlClientConfig"));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> defaultFields = new ArrayList<>();

        int count = 0;
        Map<String, String> map = new LinkedHashMap<>();
        if (authConfig.isClientConfig()) {
            map.put("auth", "config.auth");
        }
        map.put("timeout", "config.timeout");
        map.put("forwarded", "config.forwarded");
        map.put("poolConfig", "config.poolConfig");
        map.put("compression", "config.compression");
        map.put("circuitBreaker", "config.circuitBreaker");
        map.put("retryConfig", "config.retryConfig");
        map.put("validation", "config.validation");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            BuiltinSimpleNameReferenceNode valueExpr = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken((entry.getValue())));
            SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null,
                    createIdentifierToken(entry.getKey()), createToken(COLON_TOKEN), valueExpr);
            defaultFields.add(specificFieldNode);
            count++;
            if (count < map.size()) {
                defaultFields.add(createToken(COMMA_TOKEN));
            }
        }
        MappingConstructorExpressionNode initializer = NodeFactory.createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(defaultFields),
                createToken(CLOSE_BRACE_TOKEN));

        assignmentNodes.add(NodeFactory.createVariableDeclarationNode(annotationNodes, null,
                typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN)));
        DoStatementNode doStatementNode = generateHttpClientConfigOptionalFieldsAssignment();
        assignmentNodes.add(doStatementNode);
        return assignmentNodes;
    }

    /**
     * Generates assignment of http optional client config fields.
     *
     * @return the node representing optional field assignment
     */
    private DoStatementNode generateHttpClientConfigOptionalFieldsAssignment() {
        List<StatementNode> nodes = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("cache", "CacheConfig");
        map.put("responseLimits", "ResponseLimitConfigs");
        map.put("secureSocket", "ClientSecureSocket");
        map.put("proxy", "ProxyConfig");

        String http1SettingsStatement = "if config.http1Settings is ClientHttp1Settings {\n" +
                "        ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);\n" +
                "        graphqlClientConfig.http1Settings = {...settings};" +
                "    }";
        nodes.add(NodeParser.parseStatement(http1SettingsStatement));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String node = String.format("if config.%s is graphql:%s {\n" +
                    "        graphqlClientConfig.%s = check config.%s.ensureType(graphql:%s);\n" +
                    "    }\n", entry.getKey(), entry.getValue(), entry.getKey(), entry.getKey(), entry.getValue());
            nodes.add(NodeParser.parseStatement(node));
        }

        BlockStatementNode doBlockStatementNode = NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(nodes), createToken(CLOSE_BRACE_TOKEN));
        TypeDescriptorNode varNode = NodeFactory.createTypeReferenceTypeDescNode(NodeFactory.
                createSimpleNameReferenceNode(createToken(VAR_KEYWORD)));
        // TODO : Revert this change after issue in graphql:HttpError is fixed
        ExpressionNode errorNode = NodeParser.parseExpression("<graphql:ClientError> error(\"GraphQL Client " +
                "Error\", e, body = ())");
        ReturnStatementNode returnStatementNode = NodeFactory.createReturnStatementNode(createToken(RETURN_KEYWORD),
                errorNode, createToken(SEMICOLON_TOKEN));
        NodeList<StatementNode> failNodeList = createNodeList(returnStatementNode);
        BlockStatementNode failBlockStatementNode = NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                failNodeList, createToken(CLOSE_BRACE_TOKEN));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(varNode,
             NodeFactory.createCaptureBindingPatternNode(createIdentifierToken("e")));
        OnFailClauseNode onFailClauseNode = NodeFactory.createOnFailClauseNode(createToken(ON_KEYWORD),
                createToken(FAIL_KEYWORD), typedBindingPatternNode, failBlockStatementNode);
        return NodeFactory.createDoStatementNode(createToken(DO_KEYWORD), doBlockStatementNode, onFailClauseNode);
    }

    /**
     * Generates the client class remote function body.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @param authConfig      the object instance representing authentication configuration information
     * @return the node which represent the remote function body
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

        assignmentNodes.add(queryVariableDeclarationNode);
        assignmentNodes.add(graphqlVariablesDeclarationNode);

        if (authConfig.isApiKeysConfig()) {
            assignmentNodes.add(headerValuesVariableDeclarationNode);
            assignmentNodes.add(httpHeadersVariableDeclarationNode);
            assignmentNodes.add(generateGraphqlResponseVariableDeclarationNodeWithHttpHeaders(queryDefinition));
        } else {
            assignmentNodes.add(generateGraphqlResponseVariableDeclarationNode(queryDefinition));
        }

        assignmentNodes.add(generateReturnStatementNode(queryDefinition));

        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generates the initialization statement of {@code graphql:Client} class instance in the init function.
     *
     * @return the node which represent the client initialization
     */
    private VariableDeclarationNode generateClientInitializationNode() {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // {@code graphql:Client} variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GRAPHQL_CLIENT_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_CLIENT_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = NodeFactory.createPositionalArgumentNode(
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(
                        CodeGeneratorConstants.SERVICE_URL_PARAM_NAME)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(COMMA);

        PositionalArgumentNode positionalArgumentNode02 = NodeFactory.createPositionalArgumentNode(NodeFactory.
                createSimpleNameReferenceNode(createIdentifierToken(GRAPHQL_CLIENT_CONFIGURATION_VAR_NAME)));
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(argumentsList);
        ParenthesizedArgList parenthesizedArgList = NodeFactory.createParenthesizedArgList(
                createToken(OPEN_PAREN_TOKEN), arguments, createToken(CLOSE_PAREN_TOKEN));
        ImplicitNewExpressionNode expressionNode = NodeFactory.createImplicitNewExpressionNode(createToken(NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = NodeFactory.createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the {@code self.apiKeyConfig = apiKeyConfig.cloneReadOnly();} assignment node in the init function.
     *
     * @return the node which represent the API key config assignment statement
     */
    private AssignmentStatementNode generateApiKeyConfigAssignmentStatementNode() {
        FieldAccessExpressionNode varRefApiKey = NodeFactory.createFieldAccessExpressionNode(
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));

        ExpressionNode fieldAccessExpressionNode = NodeFactory.createRequiredExpressionNode(
                createIdentifierToken(API_KEY_CONFIG_PARAM));
        ExpressionNode methodCallExpressionNode = NodeFactory.createMethodCallExpressionNode(
                fieldAccessExpressionNode, createToken(DOT_TOKEN),
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(CLONE_READ_ONLY)),
                createToken(OPEN_PAREN_TOKEN), NodeFactory.createSeparatedNodeList(), createToken(CLOSE_PAREN_TOKEN));

        return NodeFactory.createAssignmentStatementNode(varRefApiKey, createToken(EQUAL_TOKEN),
                methodCallExpressionNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the {@code query} variable declaration node in the remote function.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @return the node which represent the {@code query} variable declaration
     */
    private VariableDeclarationNode generateQueryVariableDeclarationNode(ExtendedOperationDefinition queryDefinition) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // {@code query} variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createToken(STRING_KEYWORD));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(QUERY_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        NodeList<Node> content = createNodeList(createIdentifierToken(queryDefinition.getQueryString()));
        TemplateExpressionNode initializer = NodeFactory.createTemplateExpressionNode(null,
                createToken(STRING_KEYWORD), createToken(BACKTICK_TOKEN), content, createToken(BACKTICK_TOKEN));

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the GraphQL {@code variables} variable declaration node in the remote function.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @return the node which represent the GraphQL {@code variables} declaration
     */
    private VariableDeclarationNode getGraphqlVariablesDeclarationNode(ExtendedOperationDefinition queryDefinition,
                                                                       GraphQLSchema graphQLSchema) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // GraphQL {@code variables} declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GRAPHQL_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> specificFields = new ArrayList<>();

        int count = 0;
        for (String variableName : queryDefinition.getVariableDefinitionsMap(graphQLSchema).keySet()) {
            BuiltinSimpleNameReferenceNode valueExpr = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(escapeIdentifier(variableName)));
            SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null,
                    createIdentifierToken("\"" + variableName + "\""), createToken(COLON_TOKEN), valueExpr);
            specificFields.add(specificFieldNode);
            count++;
            if (count < queryDefinition.getVariableDefinitionsMap(graphQLSchema).size()) {
                specificFields.add(createToken(COMMA_TOKEN));
            }
        }

        MappingConstructorExpressionNode initializer = NodeFactory.createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(specificFields),
                createToken(CLOSE_BRACE_TOKEN));

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the {@code headerValues} variable declaration node in the remote function.
     *
     * @param authConfig the object instance representing authentication configuration information
     * @return the node which represent the {@code headerValues} variable declaration
     */
    private VariableDeclarationNode generateHeaderValuesVariableDeclarationNode(AuthConfig authConfig) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // {@code headerValues} variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HEADER_VALUES_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(HEADER_VALUES_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        Set<String> apiHeaders = authConfig.getApiHeaders();

        // Expression node
        List<Node> specificFields = new ArrayList<>();

        int count = 0;
        for (String apiHeaderName : apiHeaders) {
            IdentifierToken apiKeyConfigIdentifierToken = createIdentifierToken(API_KEYS_CONFIG_PARAM_NAME);
            SimpleNameReferenceNode apiKeyConfigParamNode = NodeFactory.createSimpleNameReferenceNode(
                    apiKeyConfigIdentifierToken);
            FieldAccessExpressionNode fieldExpr = NodeFactory.createFieldAccessExpressionNode(
                    NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                    apiKeyConfigParamNode);
            SimpleNameReferenceNode valueExpr = NodeFactory.createSimpleNameReferenceNode(
                    createIdentifierToken(CodeGeneratorUtils.getValidName(apiHeaderName)));
            ExpressionNode apiKeyExpr = NodeFactory.createFieldAccessExpressionNode(
                    fieldExpr, createToken(DOT_TOKEN), valueExpr);

            SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null,
                    createIdentifierToken("\"" + apiHeaderName + "\""), createToken(COLON_TOKEN), apiKeyExpr);
            specificFields.add(specificFieldNode);
            count++;
            if (count < apiHeaders.size()) {
                specificFields.add(createToken(COMMA_TOKEN));
            }
        }

        MappingConstructorExpressionNode initializer = NodeFactory.createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(specificFields),
                createToken(CLOSE_BRACE_TOKEN));

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates the {@code httpHeaders} variable declaration node in the remote function.
     *
     * @return the node which represent the {@code httpHeaders} variable declaration
     */
    private VariableDeclarationNode generateHttpHeadersVariableDeclarationNode() {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // {@code httpHeaders} variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HTTP_HEADERS_VARIABLES_TYPE_NAME));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(HTTP_HEADERS_VARIABLES_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        IdentifierToken functionName = createIdentifierToken("getMapForHeaders");
        SimpleNameReferenceNode functionNameNode = NodeFactory.createSimpleNameReferenceNode(functionName);
        SimpleNameReferenceNode expressionNode = NodeFactory.createSimpleNameReferenceNode(
                createIdentifierToken("headerValues"));
        FunctionArgumentNode node = NodeFactory.createPositionalArgumentNode(expressionNode);
        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(node);
        FunctionCallExpressionNode initializer =
                NodeFactory.createFunctionCallExpressionNode(functionNameNode, createToken(OPEN_PAREN_TOKEN),
                        arguments, createToken(CLOSE_PAREN_TOKEN));

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate the return statement for a remote function.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @return the node which represent the return statement for a remote function with Http headers
     */
    private ReturnStatementNode generateReturnStatementNode(
            ExtendedOperationDefinition queryDefinition) {
        SimpleNameReferenceNode expr = NodeFactory.createSimpleNameReferenceNode(
                createIdentifierToken(
                        CodeGeneratorUtils.getRemoteFunctionBodyReturnTypeName(queryDefinition.getName())));
        return NodeFactory.createReturnStatementNode(createToken(RETURN_KEYWORD), expr, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate the {@code graphqlResponse} variable declaration node for a remote function.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @return the node which represent the {@code graphqlResponse} variable declaration
     */
    private VariableDeclarationNode generateGraphqlResponseVariableDeclarationNode(
            ExtendedOperationDefinition queryDefinition) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // {@code json graphqlResponse} declaration
        BuiltinSimpleNameReferenceNode typeDescriptor = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createToken(JSON_KEYWORD));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_RESPONSE_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeDescriptor,
                bindingPattern);

        // {@code self.graphqlClient} declaration
        SimpleNameReferenceNode fieldName =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("graphqlClient"));
        FieldAccessExpressionNode graphqlClientFieldAccessExpr = NodeFactory.createFieldAccessExpressionNode(
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                fieldName);

        // {@code self.graphqlClient->executeWithType(query, variables)} declaration
        SimpleNameReferenceNode methodName =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("executeWithType"));
        List<Node> arguments = new ArrayList<>();
        FunctionArgumentNode queryArgument =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("query")));
        FunctionArgumentNode variableSArgument =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("variables")));
        arguments.add(queryArgument);
        arguments.add(createToken(COMMA_TOKEN));
        arguments.add(variableSArgument);
        SeparatedNodeList<FunctionArgumentNode> remoteFunctionArguments = NodeFactory.
                createSeparatedNodeList(arguments);
        RemoteMethodCallActionNode remoteMethodCallExpr = NodeFactory.createRemoteMethodCallActionNode(
                graphqlClientFieldAccessExpr, createToken(RIGHT_ARROW_TOKEN), methodName, createToken(OPEN_PAREN_TOKEN),
                remoteFunctionArguments, createToken(CLOSE_PAREN_TOKEN));

        // {@code check self.graphqlClient->executeWithType(query, variables)} declaration
        CheckExpressionNode initializer = NodeFactory.createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                remoteMethodCallExpr);

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate the {@code graphqlResponse} variable declaration node for a remote function with Http headers.
     *
     * @param queryDefinition the object instance of a single query definition in a query document
     * @return the node which represent the {@code graphqlResponse} variable declaration
     */
    private VariableDeclarationNode generateGraphqlResponseVariableDeclarationNodeWithHttpHeaders(
            ExtendedOperationDefinition queryDefinition) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        // {@code json graphqlResponse} declaration
        BuiltinSimpleNameReferenceNode typeDescriptor = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createToken(JSON_KEYWORD));
        CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                createIdentifierToken(GRAPHQL_RESPONSE_VAR_NAME));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(typeDescriptor,
                bindingPattern);

        // {@code self.graphqlClient} declaration
        SimpleNameReferenceNode fieldName =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("graphqlClient"));
        FieldAccessExpressionNode graphqlClientFieldAccessExpr = NodeFactory.createFieldAccessExpressionNode(
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                fieldName);

        // {@code self.graphqlClient->executeWithType(query, variables, headers = httpHeaders)} declaration
        SimpleNameReferenceNode methodName =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("executeWithType"));
        List<Node> arguments = new ArrayList<>();
        FunctionArgumentNode queryArgument =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("query")));
        FunctionArgumentNode variablesArgument =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("variables")));
        FunctionArgumentNode httpHeadersArgument = NodeFactory.createNamedArgumentNode(
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("headers")),
                createToken(EQUAL_TOKEN), NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("httpHeaders")));
        arguments.add(queryArgument);
        arguments.add(createToken(COMMA_TOKEN));
        arguments.add(variablesArgument);
        arguments.add(createToken(COMMA_TOKEN));
        arguments.add(httpHeadersArgument);
        SeparatedNodeList<FunctionArgumentNode> remoteFunctionArguments =
                NodeFactory.createSeparatedNodeList(arguments);
        RemoteMethodCallActionNode remoteMethodCallExpr = NodeFactory.
                createRemoteMethodCallActionNode(graphqlClientFieldAccessExpr, createToken(RIGHT_ARROW_TOKEN),
                        methodName, createToken(OPEN_PAREN_TOKEN), remoteFunctionArguments,
                        createToken(CLOSE_PAREN_TOKEN));

        // {@code check self.graphqlClient->executeWithType(query, variables, httpHeaders)} declaration
        CheckExpressionNode initializer = NodeFactory.createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                remoteMethodCallExpr);

        return NodeFactory.createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }
}
