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
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.graphql.generators.CodeGeneratorConstants;
import io.ballerina.graphql.generators.CodeGeneratorUtils;
import io.ballerina.graphql.generators.graphql.components.ExtendedOperationDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.API_KEYS_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.CLIENT_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.EMPTY_EXPRESSION;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.INIT_RETURN_TYPE;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.NULLABLE_EXPRESSION;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.QUESTION_MARK;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.SERVICE_URL_TYPE_NAME;

/**
 * This class is used to generate function signatures in the ballerina client file.
 */
public class FunctionSignatureGenerator {
    private static final Log log = LogFactory.getLog(FunctionSignatureGenerator.class);
    private final AuthConfigGenerator ballerinaAuthConfigGenerator;

    public FunctionSignatureGenerator(AuthConfigGenerator ballerinaAuthConfigGenerator) {
        this.ballerinaAuthConfigGenerator = ballerinaAuthConfigGenerator;
    }

    /**
     * Returns the client class init function signature node.
     *
     * @return                  the init function signature node
     */
    public FunctionSignatureNode getInitFunctionSignatureNode() {
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                getInitFunctionParams());

        OptionalTypeDescriptorNode returnType = createOptionalTypeDescriptorNode(
                createIdentifierToken(INIT_RETURN_TYPE),
                createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    /**
     * Returns the client class init function parameters.
     *
     * @return                  the list of init function parameter nodes
     */
    private List<Node> getInitFunctionParams() {
        List<Node> parameters = new ArrayList<>();

        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode httpClientConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HTTP_CLIENT_CONFIG_TYPE_NAME));
        IdentifierToken httpClientConfigParamName = createIdentifierToken(HTTP_CLIENT_CONFIG_PARAM_NAME);
        IdentifierToken equalToken = createIdentifierToken(CodeGeneratorConstants.EQUAL);
        BasicLiteralNode emptyExpression = createBasicLiteralNode(null, createIdentifierToken(EMPTY_EXPRESSION));
        DefaultableParameterNode defaultHTTPConfig = createDefaultableParameterNode(annotationNodes,
                httpClientConfigTypeName, httpClientConfigParamName, equalToken, emptyExpression);

        Node serviceURLNode = getServiceURLNode();

        if (ballerinaAuthConfigGenerator.isApiKeysConfig() && ballerinaAuthConfigGenerator.isClientConfig()) {
            parameters.add(getClientConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(getApiKeysConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
        } else if (ballerinaAuthConfigGenerator.isApiKeysConfig() && !ballerinaAuthConfigGenerator.isClientConfig()) {
            parameters.add(getApiKeysConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(defaultHTTPConfig);
        } else if (ballerinaAuthConfigGenerator.isClientConfig() && !ballerinaAuthConfigGenerator.isApiKeysConfig()) {
            parameters.add(getClientConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
        } else {
            parameters.add(serviceURLNode);
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(defaultHTTPConfig);
        }
        return parameters;
    }

    /**
     * Returns the service URL node.
     *
     * @return                  the service URL node
     */
    private Node getServiceURLNode() {
        Node serviceURLNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(SERVICE_URL_TYPE_NAME));
        IdentifierToken serviceURLParamName = createIdentifierToken(CodeGeneratorConstants.SERVICE_URL_PARAM_NAME);
        serviceURLNode = createRequiredParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName);
        return serviceURLNode;
    }

    /**
     * Returns the client config node.
     *
     * @return                  the client config node
     */
    private Node getClientConfigNode() {
        Node clientConfigNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(CLIENT_CONFIG_TYPE_NAME));
        IdentifierToken serviceURLParamName = createIdentifierToken(CodeGeneratorConstants.CLIENT_CONFIG_PARAM_NAME);
        clientConfigNode = createRequiredParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName);
        return clientConfigNode;
    }

    /**
     * Returns the API key config node.
     *
     * @return                  the client config node
     */
    private Node getApiKeysConfigNode() {
        Node apiKeyConfigNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(API_KEYS_CONFIG_TYPE_NAME));
        IdentifierToken serviceURLParamName =
                createIdentifierToken(API_KEYS_CONFIG_PARAM_NAME);
        apiKeyConfigNode = createRequiredParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName);
        return apiKeyConfigNode;
    }

    /**
     * Returns the client class remote function signature node.
     *
     * @param def               the instance of the `ExtendedOperationDefinition` from the query reader
     * @return                  the remote function signature node
     */
    public FunctionSignatureNode getRemoteFunctionSignatureNode(ExtendedOperationDefinition def) {
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                getRemoteFunctionParams(def.getVariableDefinitionsMap()));

        BuiltinSimpleNameReferenceNode returnType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(
                        CodeGeneratorUtils.getRemoteFunctionSignatureReturnTypeName(def.getName())));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    /**
     * Returns the client class remote function parameters.
     *
     * @param variableDefinitionsMap    the variable definition map from the query reader
     * @return                          the list of remote function parameter nodes
     */
    private List<Node> getRemoteFunctionParams(Map<String, String> variableDefinitionsMap) {
        List<Node> parameters = new ArrayList<>();
        List<Node> requiredParameters = new ArrayList<>();
        List<Node> optionalParameters = new ArrayList<>();

        for (String variableName :variableDefinitionsMap.keySet()) {
            if (variableDefinitionsMap.get(variableName).contains(QUESTION_MARK)) {
                BuiltinSimpleNameReferenceNode optionalFieldTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(variableDefinitionsMap.get(variableName)));
                IdentifierToken optionalFieldParamName = createIdentifierToken(variableName);
                IdentifierToken equalToken = createIdentifierToken(CodeGeneratorConstants.EQUAL);
                BasicLiteralNode nullableExpression =
                        createBasicLiteralNode(null, createIdentifierToken(NULLABLE_EXPRESSION));
                DefaultableParameterNode optionalFieldNode = createDefaultableParameterNode(createEmptyNodeList(),
                        optionalFieldTypeName, optionalFieldParamName, equalToken, nullableExpression);
                optionalParameters.add(optionalFieldNode);
                optionalParameters.add(createToken(COMMA_TOKEN));
            } else {
                BuiltinSimpleNameReferenceNode requiredFieldTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(variableDefinitionsMap.get(variableName)));
                IdentifierToken requiredFieldParamName = createIdentifierToken(variableName);
                RequiredParameterNode requiredFieldNode = createRequiredParameterNode(createEmptyNodeList(),
                        requiredFieldTypeName, requiredFieldParamName);
                requiredParameters.add(requiredFieldNode);
                requiredParameters.add(createToken(COMMA_TOKEN));
            }
        }
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        return parameters;
    }
}
