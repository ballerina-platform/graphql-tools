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
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.client.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.client.generator.model.AuthConfig;
import io.ballerina.graphql.generator.client.generator.model.FieldType;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;

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


/**
 * This class is used to generate function signatures in the ballerina client file.
 */
public class FunctionSignatureGenerator {
    private static FunctionSignatureGenerator functionSignatureGenerator = null;

    public static FunctionSignatureGenerator getInstance() {
        if (functionSignatureGenerator == null) {
            functionSignatureGenerator = new FunctionSignatureGenerator();
        }
        return functionSignatureGenerator;
    }

    /**
     * Generates the client class init function signature.
     *
     * @param authConfig        the object instance representing authentication configuration information
     * @return                  the node which represent the init function signature
     */
    public FunctionSignatureNode generateInitFunctionSignature(AuthConfig authConfig) {
        SeparatedNodeList<ParameterNode> parameterList =
                createSeparatedNodeList(generateInitFunctionParams(authConfig));

        OptionalTypeDescriptorNode returnType =
                createOptionalTypeDescriptorNode(createIdentifierToken(CodeGeneratorConstants.INIT_RETURN_TYPE),
                        createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode =
                createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
    }

    /**
     * Generates the client class remote function signature.
     *
     * @param queryDefinition       the object instance of a single query definition in a query document
     * @param graphQLSchema         the object instance of the GraphQL schema (SDL)
     * @return                      the node which represent the remote function signature
     */
    public FunctionSignatureNode generateRemoteFunctionSignature(ExtendedOperationDefinition queryDefinition,
                                                                 GraphQLSchema graphQLSchema) {
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                generateRemoteFunctionParams(queryDefinition.getVariableDefinitionsMap(graphQLSchema)));

        BuiltinSimpleNameReferenceNode returnType = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                CodeGeneratorUtils.getRemoteFunctionSignatureReturnTypeName(queryDefinition.getName())));
        ReturnTypeDescriptorNode returnTypeDescriptorNode =
                createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
    }

    /**
     * Generates the client class init function parameters.
     *
     * @param authConfig        the object instance representing authentication configuration information
     * @return                  the list of nodes which represent the init function parameters
     */
    private List<Node> generateInitFunctionParams(AuthConfig authConfig) {
        List<Node> parameters = new ArrayList<>();

        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode httpClientConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(CodeGeneratorConstants.CONNECTION_CONFIG_TYPE_NAME));
        IdentifierToken httpClientConfigParamName = createIdentifierToken(CodeGeneratorConstants.CONFIG_PARAM_NAME);
        IdentifierToken equalToken = createIdentifierToken(CodeGeneratorConstants.EQUAL);
        BasicLiteralNode emptyExpression =
                createBasicLiteralNode(null, createIdentifierToken(CodeGeneratorConstants.EMPTY_EXPRESSION));
        DefaultableParameterNode defaultConnectionConfig =
                createDefaultableParameterNode(annotationNodes, httpClientConfigTypeName, httpClientConfigParamName,
                        equalToken, emptyExpression);

        Node serviceURLNode = generateServiceURLNode();

        if (authConfig.isApiKeysConfig() && authConfig.isClientConfig()) {
            parameters.add(generateClientConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(generateApiKeysConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
        } else if (authConfig.isApiKeysConfig() && !authConfig.isClientConfig()) {
            parameters.add(generateApiKeysConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(defaultConnectionConfig);
        } else if (authConfig.isClientConfig() && !authConfig.isApiKeysConfig()) {
            parameters.add(generateClientConfigNode());
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
        } else {
            parameters.add(serviceURLNode);
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(defaultConnectionConfig);
        }
        return parameters;
    }

    /**
     * Generates the service URL {@code string serviceUrl} node.
     *
     * @return                  the service URL {@code string serviceUrl} node
     */
    private Node generateServiceURLNode() {
        Node serviceURLNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(CodeGeneratorConstants.SERVICE_URL_TYPE_NAME));
        IdentifierToken serviceURLParamName = createIdentifierToken(CodeGeneratorConstants.SERVICE_URL_PARAM_NAME);
        serviceURLNode = createRequiredParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName);
        return serviceURLNode;
    }

    /**
     * Generates the client config {@code ClientConfig clientConfig} node.
     *
     * @return                  the client config {@code ClientConfig clientConfig} node
     */
    private Node generateClientConfigNode() {
        Node clientConfigNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(CodeGeneratorConstants.CONNECTION_CONFIG_TYPE_NAME));
        IdentifierToken serviceURLParamName = createIdentifierToken(CodeGeneratorConstants.CONFIG_PARAM_NAME);
        clientConfigNode = createRequiredParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName);
        return clientConfigNode;
    }

    /**
     * Generates the API key config {@code ApiKeysConfig apiKeysConfig} node.
     *
     * @return the API key config {@code ApiKeysConfig apiKeysConfig} node
     */
    private Node generateApiKeysConfigNode() {
        Node apiKeyConfigNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(CodeGeneratorConstants.API_KEYS_CONFIG_TYPE_NAME));
        IdentifierToken serviceURLParamName = createIdentifierToken(CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME);
        apiKeyConfigNode = createRequiredParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName);
        return apiKeyConfigNode;
    }

    /**
     * Generates the client class remote function parameters.
     *
     * @param variableDefinitionsMap the variable definition map from the query definition
     * @return the list of nodes which represent remote function parameters
     */
    private List<Node> generateRemoteFunctionParams(Map<String, FieldType> variableDefinitionsMap) {
        List<Node> parameters = new ArrayList<>();
        List<Node> requiredParameters = new ArrayList<>();
        List<Node> optionalParameters = new ArrayList<>();

        for (String variableName : variableDefinitionsMap.keySet()) {
            if (variableDefinitionsMap.get(variableName).getFieldTypeAsString()
                    .endsWith(CodeGeneratorConstants.QUESTION_MARK)) {
                BuiltinSimpleNameReferenceNode optionalFieldTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(variableDefinitionsMap.get(variableName).getFieldTypeAsString()));
                IdentifierToken optionalFieldParamName =
                        createIdentifierToken(CodeGeneratorUtils.escapeIdentifier(variableName));
                IdentifierToken equalToken = createIdentifierToken(CodeGeneratorConstants.EQUAL);
                BasicLiteralNode nullableExpression =
                        createBasicLiteralNode(null, createIdentifierToken(CodeGeneratorConstants.NULLABLE_EXPRESSION));
                DefaultableParameterNode optionalFieldNode =
                        createDefaultableParameterNode(createEmptyNodeList(), optionalFieldTypeName,
                                optionalFieldParamName, equalToken, nullableExpression);
                optionalParameters.add(optionalFieldNode);
                optionalParameters.add(createToken(COMMA_TOKEN));
            } else {
                BuiltinSimpleNameReferenceNode requiredFieldTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(variableDefinitionsMap.get(variableName).getFieldTypeAsString()));
                IdentifierToken requiredFieldParamName =
                        createIdentifierToken(CodeGeneratorUtils.escapeIdentifier(variableName));
                RequiredParameterNode requiredFieldNode =
                        createRequiredParameterNode(createEmptyNodeList(), requiredFieldTypeName,
                                requiredFieldParamName);
                requiredParameters.add(requiredFieldNode);
                requiredParameters.add(createToken(COMMA_TOKEN));
            }
        }
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        if (parameters.size() >= 1) {
            parameters.remove(parameters.size() - 1);
        }
        return parameters;
    }
}
