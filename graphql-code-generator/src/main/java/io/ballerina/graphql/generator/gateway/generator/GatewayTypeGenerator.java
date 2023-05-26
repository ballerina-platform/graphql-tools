/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package io.ballerina.graphql.generator.gateway.generator;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import io.ballerina.graphql.generator.utils.graphql.SpecReader;
import io.ballerina.graphql.generator.utils.model.FieldType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;

/**
 * Class to generate the types for the gateway.
 */
public class GatewayTypeGenerator {

    private final GraphQLSchema graphQLSchema;

    public GatewayTypeGenerator(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public String generateSrc() throws GatewayGenerationException {
        try {
            SyntaxTree syntaxTree = generateSyntaxTree();
            return Formatter.format(syntaxTree).toString();
        } catch (Exception e) {
            throw new GatewayGenerationException("Error while generating the gateway types");
        }
    }

    private SyntaxTree generateSyntaxTree() throws GatewayGenerationException {
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        NodeList<ImportDeclarationNode> importsList = createEmptyNodeList();

        addCustomDefinedTypes(typeDefinitionNodeList);
        addInputTypes(typeDefinitionNodeList);
        addQueryResponseTypes(typeDefinitionNodeList);

        NodeList<ModuleMemberDeclarationNode> members =
                createNodeList(typeDefinitionNodeList.toArray(new TypeDefinitionNode[typeDefinitionNodeList.size()]));

        ModulePartNode modulePartNode = createModulePartNode(importsList, members, createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);

    }

    private void addCustomDefinedTypes(List<TypeDefinitionNode> typeDefinitionNodeList) {
        List<String> names = CommonUtils.getCustomDefinedObjectTypeNames(graphQLSchema);

        for (String name : names) {
            List<Node> fieldNodes = new ArrayList<>();
            Map<String, FieldType> fields = SpecReader.getObjectTypeFieldsMap(graphQLSchema, name);

            for (Map.Entry<String, FieldType> field : fields.entrySet()) {
                fieldNodes.add(createRecordFieldNode(
                        null,
                        null,
                        createIdentifierToken(field.getValue().getName() + field.getValue().getTokens()),
                        createIdentifierToken(field.getKey()),
                        createToken(QUESTION_MARK_TOKEN),
                        createToken(SEMICOLON_TOKEN)));
            }

            RecordTypeDescriptorNode recordTypeDescriptorNode =
                    createRecordTypeDescriptorNode(
                            createToken(RECORD_KEYWORD),
                            createToken(OPEN_BRACE_PIPE_TOKEN),
                            createNodeList(fieldNodes),
                            null,
                            createToken(CLOSE_BRACE_PIPE_TOKEN));


            typeDefinitionNodeList.add(
                    createTypeDefinitionNode(
                            createMetadataNode(null, createEmptyNodeList()),
                            createToken(PUBLIC_KEYWORD),
                            createToken(TYPE_KEYWORD),
                            createIdentifierToken(name),
                            recordTypeDescriptorNode,
                            createToken(SEMICOLON_TOKEN)));
        }
    }

    private void addInputTypes(List<TypeDefinitionNode> typeDefinitionNodeList) {
        List<String> names = SpecReader.getInputObjectTypeNames(graphQLSchema);
        for (String name : names) {
            List<Node> fieldNodes = new ArrayList<>();
            Map<String, FieldType> fields = SpecReader.getInputTypeFieldsMap(graphQLSchema, name);

            for (Map.Entry<String, FieldType> field : fields.entrySet()) {
                fieldNodes.add(createRecordFieldNode(
                        null,
                        null,
                        createIdentifierToken(field.getValue().getName() + field.getValue().getTokens()),
                        createIdentifierToken(field.getKey()),
                        null,
                        createToken(SEMICOLON_TOKEN)));
            }

            RecordTypeDescriptorNode recordTypeDescriptorNode =
                    createRecordTypeDescriptorNode(
                            createToken(RECORD_KEYWORD),
                            createToken(OPEN_BRACE_PIPE_TOKEN),
                            createNodeList(fieldNodes),
                            null,
                            createToken(CLOSE_BRACE_PIPE_TOKEN));


            typeDefinitionNodeList.add(
                    createTypeDefinitionNode(
                            createMetadataNode(null, createEmptyNodeList()),
                            createToken(PUBLIC_KEYWORD),
                            createToken(TYPE_KEYWORD),
                            createIdentifierToken(name),
                            recordTypeDescriptorNode,
                            createToken(SEMICOLON_TOKEN)));
        }
    }

    private void addQueryResponseTypes(List<TypeDefinitionNode> typeDefinitionNodeList) throws
            GatewayGenerationException {
        List<GraphQLSchemaElement> queryTypes = new ArrayList<>();
        queryTypes.addAll(CommonUtils.getQueryTypes(graphQLSchema));
        queryTypes.addAll(CommonUtils.getMutationTypes(graphQLSchema));

        for (GraphQLSchemaElement queryType : queryTypes) {
            GraphQLFieldDefinition queryDefinition = (GraphQLFieldDefinition) queryType;
            RecordTypeDescriptorNode recordTypeDescriptorNode = getRecordTypeDescriptorNode(queryDefinition);


            typeDefinitionNodeList.add(
                    createTypeDefinitionNode(
                            createMetadataNode(null, createEmptyNodeList()),
                            createToken(PUBLIC_KEYWORD),
                            createToken(TYPE_KEYWORD),
                            createIdentifierToken(queryDefinition.getName() + "Response"),
                            recordTypeDescriptorNode,
                            createToken(SEMICOLON_TOKEN)
                    )
            );
        }
    }

    private RecordTypeDescriptorNode getRecordTypeDescriptorNode(GraphQLFieldDefinition queryDefinition)
            throws GatewayGenerationException {
        String typename = CommonUtils.getTypeNameFromGraphQLType(queryDefinition.getType());
        return createRecordTypeDescriptorNode(
                createToken(RECORD_KEYWORD),
                createToken(OPEN_BRACE_TOKEN),
                createNodeList(createRecordFieldNode(
                        null,
                        null,
                        createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                                createToken(OPEN_BRACE_PIPE_TOKEN),
                                createNodeList(
                                        createRecordFieldNode(null,
                                                null,
                                                createIdentifierToken(typename),
                                                createIdentifierToken(queryDefinition.getName()),
                                                null,
                                                createToken(SEMICOLON_TOKEN))),
                                null,
                                createToken(CLOSE_BRACE_PIPE_TOKEN)),
                        createIdentifierToken("data"), null,
                        createToken(SEMICOLON_TOKEN))),
                null,
                createToken(CLOSE_BRACE_TOKEN));
    }

}
