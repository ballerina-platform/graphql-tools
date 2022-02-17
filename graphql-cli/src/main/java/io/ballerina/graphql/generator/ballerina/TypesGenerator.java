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

import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.graphql.SpecReader;
import io.ballerina.graphql.generator.graphql.components.ExtendedFieldDefinition;
import io.ballerina.graphql.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.model.FieldType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
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
import static io.ballerina.graphql.generator.CodeGeneratorConstants.NEW_LINE;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.QUERY;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.RESPONSE;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.WHITESPACE;

/**
 * This class is used to generate ballerina types file according to given SDL and query files.
 */
public class TypesGenerator {
    private static TypesGenerator typesGenerator = null;

    public static TypesGenerator getInstance() {
        if (typesGenerator == null) {
            typesGenerator = new TypesGenerator();
        }
        return typesGenerator;
    }

    /**
     * Generates the types file content.
     *
     * @param schema                        the object instance of the GraphQL schema (SDL)
     * @param documents                     the list of documents of a given GraphQL project
     * @return                              the types file content
     * @throws TypesGenerationException     when an error occurs during type generation
     */
    public String generateSrc(GraphQLSchema schema, List<String> documents) throws TypesGenerationException {
        try {
            String generatedSyntaxTree = Formatter.format(generateSyntaxTree(schema, documents)).toString();
            return Formatter.format(generatedSyntaxTree);
        } catch (FormatterException | IOException e) {
            throw new TypesGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the types syntax tree.
     *
     * @param schema            the object instance of the GraphQL schema (SDL)
     * @param documents         the list of documents of a given GraphQL project
     * @return                  Syntax tree for the types.bal
     * @throws IOException      If an I/O error occurs
     */
    private SyntaxTree generateSyntaxTree(GraphQLSchema schema, List<String> documents) throws IOException {
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        NodeList<ImportDeclarationNode> importsList = createEmptyNodeList();

        addInputRecords(schema, typeDefinitionNodeList);
        addQueryResponseRecords(schema, documents, typeDefinitionNodeList);

        NodeList<ModuleMemberDeclarationNode> members =  createNodeList(typeDefinitionNodeList.toArray(
                new TypeDefinitionNode[typeDefinitionNodeList.size()]));
        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Create query response records.
     *
     * @param schema                    the object instance of the GraphQL schema (SDL)
     * @param documents                 the list of documents of a given GraphQL project
     * @param typeDefinitionNodeList    the list of TypeDefinitionNodes
     * @throws IOException              If an I/O error occurs
     */
    private void addQueryResponseRecords(GraphQLSchema schema, List<String> documents, List<TypeDefinitionNode>
            typeDefinitionNodeList) throws IOException {
        Map<String, FieldType> queryFieldsMap = SpecReader.getObjectTypeFieldsMap(schema, QUERY);

        for (String document: documents) {
            QueryReader queryReader = new QueryReader(Utils.getGraphQLQueryDocument(document));
            for (ExtendedOperationDefinition definition: queryReader.getExtendedOperationDefinitions()) {
                String queryName = definition.getName();
                List<Node> queryRecordFieldList = new ArrayList<>();

                // Create record field for __extensions
                RecordFieldNode extensionsFieldNode = createRecordFieldNode(null, null,
                        createIdentifierToken("map<json?>"),
                        createIdentifierToken("__extensions"),
                        createToken(QUESTION_MARK_TOKEN),
                        createToken(SEMICOLON_TOKEN));
                queryRecordFieldList.add(extensionsFieldNode);

                for (ExtendedFieldDefinition extendedFieldDefinition: definition.getExtendedFieldDefinitions()) {
                    String fieldName = extendedFieldDefinition.getName(); // countries
                    String selectionType = queryFieldsMap.get(fieldName).getName(); // Country
                    Map<String, FieldType> fieldsOfSelectionType =
                            SpecReader.getObjectTypeFieldsMap(schema, selectionType);
                    List<Node> inlineRecordFieldList = new ArrayList<>();

                    for (Selection selection: extendedFieldDefinition.getSelectionSet().getSelections()) {
                        Field field = (Field) selection;

                        if (field.getSelectionSet() != null) {
                            inlineRecordFieldList.add(
                                    createInlineRecordField(schema, field, selectionType)
                            );
                        } else {
                            String typeOfField = fieldsOfSelectionType.get(field.getName()).getFieldTypeAsString();
                            inlineRecordFieldList.add(createRecordFieldNode(null, null,
                                    createIdentifierToken(typeOfField + WHITESPACE),
                                    createIdentifierToken(field.getName()),
                                    null,
                                    createIdentifierToken(createToken(SEMICOLON_TOKEN) + NEW_LINE)
                            ));
                        }
                    }

                    NodeList<Node> inlineRecordFields = createNodeList(inlineRecordFieldList);
                    TypeDescriptorNode typeDescriptorNode = createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                            createToken(OPEN_BRACE_PIPE_TOKEN), inlineRecordFields, null,
                            createToken(CLOSE_BRACE_PIPE_TOKEN));

                    RecordFieldNode queryRecordFieldNode = createRecordFieldNode(null, null,
                            createIdentifierToken(typeDescriptorNode + queryFieldsMap.get(fieldName).getTokens()),
                            createIdentifierToken(fieldName),
                            null,
                            createToken(SEMICOLON_TOKEN));
                    queryRecordFieldList.add(queryRecordFieldNode);
                }

                NodeList<Node> queryResponsefields = createNodeList(queryRecordFieldList);

                RecordTypeDescriptorNode queryResponseRecord = createRecordTypeDescriptorNode(
                        createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), queryResponsefields, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
                MetadataNode metadataNode = createMetadataNode(createIdentifierToken("# Represents " +
                                getQueryResponseTypeName(queryName)),
                        createEmptyNodeList());
                TypeDefinitionNode typeDefNode = createTypeDefinitionNode(metadataNode, null,
                        createToken(TYPE_KEYWORD),
                        createIdentifierToken(getQueryResponseTypeName(queryName)),
                        queryResponseRecord,
                        createToken(SEMICOLON_TOKEN));
                typeDefinitionNodeList.add(typeDefNode);
            }
        }
    }

    /**
     * Create inline record field node.
     *
     * @param schema                the object instance of the GraphQL schema (SDL)
     * @param inlineRecordField     the object instance of the inline record field
     * @param sourceSelectionType   type of the Selection
     * @return                      generated inline record field node
     */
    private RecordFieldNode createInlineRecordField(GraphQLSchema schema, Field inlineRecordField,
                                                    String sourceSelectionType) {
        String inlineRecordFieldName = inlineRecordField.getName(); // continent
        Map<String, FieldType> objectFieldsMap = SpecReader.getObjectTypeFieldsMap(schema, sourceSelectionType);
        String selectionType = objectFieldsMap.get(inlineRecordFieldName).getName(); // Continent
        Map<String, FieldType> fieldsOfSelectionType = SpecReader.getObjectTypeFieldsMap(schema, selectionType);

        SelectionSet selectionSet = inlineRecordField.getSelectionSet();
        List<Node> fieldList = new ArrayList<>();

        for (Selection selection: selectionSet.getSelections()) {
            Field field = (Field) selection;
            String fieldName = field.getName();
            if (field.getSelectionSet() != null) {
                fieldList.add(createInlineRecordField(schema, field, selectionType));

            } else {
                String typeOfField = fieldsOfSelectionType.get(fieldName).getFieldTypeAsString();
                fieldList.add(createRecordFieldNode(null, null,
                        createIdentifierToken(typeOfField + WHITESPACE),
                        createIdentifierToken(fieldName),
                        null,
                        createIdentifierToken(createToken(SEMICOLON_TOKEN) + NEW_LINE)));
            }
        }

        NodeList<Node> fields = createNodeList(fieldList);
        TypeDescriptorNode typeDescriptorNode = createRecordTypeDescriptorNode(
                createToken(RECORD_KEYWORD),
                createToken(OPEN_BRACE_PIPE_TOKEN),
                fields,
                null,
                createToken(CLOSE_BRACE_PIPE_TOKEN)
        );

        return createRecordFieldNode(null, null,
                createIdentifierToken(typeDescriptorNode + objectFieldsMap.get(inlineRecordFieldName).getTokens()),
                createIdentifierToken(inlineRecordFieldName),
                null,
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Create Input records and add it to the typeDefinitionNodeList.
     *
     * @param schema                        the object instance of the GraphQL schema (SDL)
     * @param typeDefinitionNodeList        the list of typeDefinitionNodes
     */
    private void addInputRecords(GraphQLSchema schema, List<TypeDefinitionNode> typeDefinitionNodeList) {
        List<String> inputObjectTypes = SpecReader.getInputObjectTypeNames(schema);
        for (String inputObjectType: inputObjectTypes) {
            List<Node> recordFieldList = new ArrayList<>();
            Map<String, FieldType> inputTypeFieldsMap = SpecReader.getInputTypeFieldsMap(schema, inputObjectType);

            for (Map.Entry<String, FieldType> inputTypeFields: inputTypeFieldsMap.entrySet()) {
                String typeName = inputTypeFields.getValue().getFieldTypeAsString();
                String fieldName = inputTypeFields.getKey();

                RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                        createIdentifierToken(typeName),
                        createIdentifierToken(fieldName), createToken(QUESTION_MARK_TOKEN),
                        createToken(SEMICOLON_TOKEN));
                recordFieldList.add(recordFieldNode);
            }
            NodeList<Node> fieldNodes = createNodeList(recordFieldList);

            RecordTypeDescriptorNode typeDescriptorNode = createRecordTypeDescriptorNode(
                    createToken(RECORD_KEYWORD),
                    createToken(OPEN_BRACE_TOKEN),
                    fieldNodes,
                    null,
                    createToken(CLOSE_BRACE_TOKEN));

            MetadataNode metadataNode = createMetadataNode(createIdentifierToken("# Represents " +
                    inputObjectType), createEmptyNodeList());
            TypeDefinitionNode typeDefNode = createTypeDefinitionNode(metadataNode,
                    createToken(PUBLIC_KEYWORD),
                    createToken(TYPE_KEYWORD),
                    createIdentifierToken(inputObjectType),
                    typeDescriptorNode,
                    createToken(SEMICOLON_TOKEN));
            typeDefinitionNodeList.add(typeDefNode);
        }
    }

    /**
     * Get name for query response type from the query name.
     * -- ex: If the query name is `countries`, the type name will be `CountriesResponse`
     *
     * @return  name for the query response type
     */
    private String getQueryResponseTypeName(String queryName) {
        return queryName.substring(0, 1).toUpperCase() +
                queryName.substring(1).concat(RESPONSE);
    }
}
