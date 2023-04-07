package io.ballerina.graphql.generator.gateway.generator;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.StringValue;
import graphql.language.TypeName;
import graphql.parser.Parser;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.generator.gateway.exception.GatewayQueryPlanGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import io.ballerina.graphql.generator.gateway.generator.common.JoinGraph;
import io.ballerina.graphql.generator.utils.graphql.SpecReader;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createConstantDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIntersectionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createKeySpecifierNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createListConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModuleVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTableConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTableTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BITWISE_AND_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CONST_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.GT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.KEY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TABLE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getJoinGraphs;

/**
 * Class to generate the query plan for the gateway.
 */
public class GatewayQueryPlanGenerator {

    private final GraphQLSchema graphQLSchema;
    private final Map<String, JoinGraph> joinGraphs;
    private final Map<String, List<FieldData>> fieldDataMap;

    public GatewayQueryPlanGenerator(GraphQLSchema graphQLSchema) throws GatewayQueryPlanGenerationException {
        this.graphQLSchema = graphQLSchema;
        this.joinGraphs = getJoinGraphs(graphQLSchema);
        this.fieldDataMap = new HashMap<>();
        List<String> names = CommonUtils.getCustomDefinedObjectTypeNames(graphQLSchema);

        for (String name : names) {
            fieldDataMap.put(name, getFieldsOfType(name));
        }

    }

    public String generateSrc() throws GatewayQueryPlanGenerationException {
        try {
            SyntaxTree syntaxTree = generateSyntaxTree();
            return Formatter.format(syntaxTree).toString();
        } catch (Exception e) {
            throw new GatewayQueryPlanGenerationException("Error while generating the gateway types", e);
        }
    }

    private SyntaxTree generateSyntaxTree() throws GatewayQueryPlanGenerationException {
        List<Node> nodeList = new LinkedList<>();
        NodeList<ImportDeclarationNode> importsList = createEmptyNodeList();

        addClientConstantDeclarations(nodeList);
        addQueryPlanTableNode(nodeList);

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(nodeList.toArray(
                new ModuleMemberDeclarationNode[nodeList.size()]));

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private void addQueryPlanTableNode(List<Node> nodeList) throws GatewayQueryPlanGenerationException {


        Node tableNode = createModuleVariableDeclarationNode(
                null,
                createToken(PUBLIC_KEYWORD),
                createNodeList(createToken(FINAL_KEYWORD)),
                createTypedBindingPatternNode(
                        createIntersectionTypeDescriptorNode(
                                createBuiltinSimpleNameReferenceNode(READONLY_TYPE_DESC, createToken(READONLY_KEYWORD)),
                                createToken(BITWISE_AND_TOKEN),
                                createTableTypeDescriptorNode(
                                        createToken(TABLE_KEYWORD),
                                        createTypeParameterNode(
                                                createToken(LT_TOKEN),
                                                createSimpleNameReferenceNode(
                                                        createIdentifierToken("QueryPlanEntry",
                                                                createEmptyMinutiaeList(),
                                                                createEmptyMinutiaeList()
                                                        )

                                                ),
                                                createToken(GT_TOKEN)
                                        ),
                                        createKeySpecifierNode(
                                                createToken(KEY_KEYWORD),
                                                createToken(OPEN_PAREN_TOKEN),
                                                createSeparatedNodeList(createIdentifierToken("typename")),
                                                createToken(CLOSE_PAREN_TOKEN)
                                        )
                                )
                        ),
                        createCaptureBindingPatternNode(createIdentifierToken("queryPlan"))
                ),
                createToken(EQUAL_TOKEN),
                createTableConstructorExpressionNode(
                        createToken(TABLE_KEYWORD),
                        null,
                        createToken(OPEN_BRACKET_TOKEN),
                        getTableRows(),
                        createToken(CLOSE_BRACKET_TOKEN)
                ),
                createToken(SEMICOLON_TOKEN)
        );

        nodeList.add(tableNode);
    }

    private SeparatedNodeList<Node> getTableRows() throws GatewayQueryPlanGenerationException {
        List<Node> nodeList = new ArrayList<>();
        List<String> names = CommonUtils.getCustomDefinedObjectTypeNames(graphQLSchema);

        int namesLength = names.size();
        int i = 0;
        for (String name : names) {
            MappingConstructorExpressionNode entry = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN),
                    getTableEntry(name),
                    createToken(CLOSE_BRACE_TOKEN)
            );

            nodeList.add(entry);
            i += 1;
            if (i < namesLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }
        return createSeparatedNodeList(nodeList);
    }

    private SeparatedNodeList<MappingFieldNode> getTableEntry(String name) throws GatewayQueryPlanGenerationException {
        List<Node> nodeList = new ArrayList<>();

        nodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("typename"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        "\"" + name + "\"",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );

        nodeList.add(createToken(COMMA_TOKEN));

        Map<String, String> keys = getKeys(name);
        Node keyNode = createSpecificFieldNode(
                null,
                createIdentifierToken("keys"),
                createToken(COLON_TOKEN),
                createMappingConstructorExpressionNode(
                        createToken(OPEN_BRACE_TOKEN),
                        getKeysNodeList(keys),
                        createToken(CLOSE_BRACE_TOKEN)
                )
        );

        nodeList.add(keyNode);

        nodeList.add(createToken(COMMA_TOKEN));

        Node fieldNode = createSpecificFieldNode(
                null,
                createIdentifierToken("fields"),
                createToken(COLON_TOKEN),
                createTableConstructorExpressionNode(
                        createToken(TABLE_KEYWORD),
                        null,
                        createToken(OPEN_BRACKET_TOKEN),
                        getFieldTableRows(name),
                        createToken(CLOSE_BRACKET_TOKEN)
                )
        );

        nodeList.add(fieldNode);

        return createSeparatedNodeList(nodeList);
    }

    private SeparatedNodeList<MappingFieldNode> getKeysNodeList(Map<String, String> keys) {
        List<Node> nodeList = new ArrayList<>();

        int keysLength = keys.size();
        int i = 0;

        for (Map.Entry<String, String> entry : keys.entrySet()) {
            nodeList.add(
                    createSpecificFieldNode(
                            null,
                            createIdentifierToken("\"" + entry.getKey() + "\""),
                            createToken(COLON_TOKEN),
                            createBasicLiteralNode(
                                    STRING_LITERAL,
                                    createLiteralValueToken(
                                            STRING_LITERAL_TOKEN,
                                            "\"" + entry.getValue() + "\"",
                                            createEmptyMinutiaeList(),
                                            createEmptyMinutiaeList()
                                    )
                            )
                    )
            );
            i += 1;
            if (i < keysLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }

        return createSeparatedNodeList(nodeList);
    }

    private Map<String, String> getKeys(String name)
            throws GatewayQueryPlanGenerationException {
        Map<String, String> keys = new HashMap<>();

        List<GraphQLDirective> directives = SpecReader.getObjectTypeDirectives(this.graphQLSchema, name);

        for (GraphQLDirective directive : directives) {
            if (directive.getName().equals("join__type")) {
                String graph = getGraphOfJoinTypeArgument(directive);
                String key = getKeyOfJoinTypeArgument(name, directive);
                keys.put(graph, key);
            }
        }

        return keys;
    }

    private String getGraphOfJoinTypeArgument(GraphQLDirective directive) throws GatewayQueryPlanGenerationException {
        for (GraphQLArgument argument : directive.getArguments()) {
            if (argument.getName().equals("graph")) {
                String graphEnumName =
                        ((EnumValue) Objects.requireNonNull(argument.getArgumentValue().getValue())).getName();
                return this.joinGraphs.get(graphEnumName).getName();
            }
        }
        throw new GatewayQueryPlanGenerationException("No graph argument found in @join__type directive");
    }

    private String getKeyOfJoinTypeArgument(String name, GraphQLDirective directive)
            throws GatewayQueryPlanGenerationException {

        try {
            for (GraphQLArgument argument : directive.getArguments()) {
                if (argument.getName().equals("key")) {
                    return ((StringValue) Objects.requireNonNull(argument.getArgumentValue().getValue())).getValue();
                }
            }
        } catch (NullPointerException e) {
            for (FieldData field :
                    this.fieldDataMap.get(name)) {
                if (field.isID()) {
                    return field.getFieldName();
                }
            }
        }
        throw new GatewayQueryPlanGenerationException("No key argument found in @join__type directive");
    }

    private SeparatedNodeList<Node> getFieldTableRows(String name) throws GatewayQueryPlanGenerationException {
        List<Node> nodeList = new ArrayList<>();

        List<FieldData> fields = this.fieldDataMap.get(name);

        int fieldsLength = fields.size();
        int i = 0;

        for (FieldData field : fields) {
            if (field.isID()) {
                continue;
            }

            MappingConstructorExpressionNode entryNode = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN),
                    getFieldTableEntry(field),
                    createToken(CLOSE_BRACE_TOKEN)
            );

            nodeList.add(entryNode);
            i += 1;
            if (i < fieldsLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }

        return createSeparatedNodeList(nodeList);
    }

    private SeparatedNodeList<MappingFieldNode> getFieldTableEntry(FieldData data)
            throws GatewayQueryPlanGenerationException {
        List<Node> fieldNodeList = new ArrayList<>();
        fieldNodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("name"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        "\"" + data.getFieldName() + "\"",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );

        fieldNodeList.add(createToken(COMMA_TOKEN));

        fieldNodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("'type"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        "\"" + data.getType() + "\"",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );

        fieldNodeList.add(createToken(COMMA_TOKEN));

        fieldNodeList.add(
                createSpecificFieldNode(
                        null,
                        createIdentifierToken("'client"),
                        createToken(COLON_TOKEN),
                        createBasicLiteralNode(
                                STRING_LITERAL,
                                createLiteralValueToken(
                                        STRING_LITERAL_TOKEN,
                                        data.getClient(),
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        )
                )
        );

        if (data.getRequires() != null) {
            fieldNodeList.add(createToken(COMMA_TOKEN));
            fieldNodeList.add(
                    createSpecificFieldNode(
                            null,
                            createIdentifierToken("requires"),
                            createToken(COLON_TOKEN),
                            createListConstructorExpressionNode(
                                    createToken(OPEN_BRACKET_TOKEN),
                                    getRequiresEntries(data.getRequires()),
                                    createToken(CLOSE_BRACKET_TOKEN)
                            )
                    )
            );
        }

        return createSeparatedNodeList(fieldNodeList);
    }

    private SeparatedNodeList<Node> getRequiresEntries(Map<String, String> requires) {
        List<Node> nodeList = new ArrayList<>();

        int requiresLength = requires.size();
        int i = 0;

        for (Map.Entry<String, String> entry : requires.entrySet()) {
            MappingConstructorExpressionNode entryNode = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN),
                    createSeparatedNodeList(
                            createSpecificFieldNode(
                                    null,
                                    createIdentifierToken("clientName"),
                                    createToken(COLON_TOKEN),
                                    createBasicLiteralNode(
                                            STRING_LITERAL,
                                            createLiteralValueToken(
                                                    STRING_LITERAL_TOKEN,
                                                    entry.getKey(),
                                                    createEmptyMinutiaeList(),
                                                    createEmptyMinutiaeList()
                                            )
                                    )
                            ),
                            createToken(COMMA_TOKEN),
                            createSpecificFieldNode(
                                    null,
                                    createIdentifierToken("fieldString"),
                                    createToken(COLON_TOKEN),
                                    createBasicLiteralNode(
                                            STRING_LITERAL,
                                            createLiteralValueToken(
                                                    STRING_LITERAL_TOKEN,
                                                    "\"" + entry.getValue() + "\"",
                                                    createEmptyMinutiaeList(),
                                                    createEmptyMinutiaeList()
                                            )
                                    )
                            )
                    ),
                    createToken(CLOSE_BRACE_TOKEN)
            );

            nodeList.add(entryNode);
            i += 1;
            if (i < requiresLength) {
                nodeList.add(createToken(COMMA_TOKEN));
            }
        }

        return createSeparatedNodeList(nodeList);
    }

    private static String getTypeFromFieldDefinition(FieldDefinition definition) {
        if (definition.getType() instanceof NonNullType) {
            return ((TypeName) ((NonNullType) definition.getType()).getType()).getName();
        } else if (definition.getType() instanceof ListType) {
            return ((TypeName) ((ListType) definition.getType()).getType()).getName();
        } else {
            return ((TypeName) definition.getType()).getName();
        }
    }

    private Map<String, String> getRequiresFromFieldDefinition(FieldDefinition definition,
                                                               String parentType) {
        for (Directive directive : definition.getDirectives()) {
            if (directive.getName().equals("join__field")) {
                for (Argument argument : directive.getArguments()) {
                    if (argument.getName().equals("requires")) {
                        String requiresString = ((StringValue) argument.getValue()).getValue();
                        return getClassifiedRequiresString(requiresString, parentType);
                    }
                }
            }
        }
        return null;
    }

    private Map<String, String> getClassifiedRequiresString(String requiresString,
                                                            String parentType) {
        Map<String, List<Selection>> clientToFieldMap = new HashMap<>();
        Document document = Parser.parse("query{ Document {" + requiresString + "} }");
        List<Selection> requires =
                ((Field) ((OperationDefinition) document.getDefinitions().get(0)).getSelectionSet().
                        getSelections().get(0)).getSelectionSet().getSelections();
        List<FieldData> fields = this.fieldDataMap.get(parentType);
        for (Selection selection : requires) {
            if (selection instanceof Field) {
                String field = ((Field) selection).getName();
                for (FieldData fieldData : fields) {
                    if (fieldData.getFieldName().equals(field)) {
                        clientToFieldMap.computeIfAbsent(fieldData.getClient(), k -> new ArrayList<>()).add(selection);
                    }
                    // TODO: if the field is an selection set handle it. Currently checking only parent field
                    //  resolving client
                }
            }
        }

        Map<String, String> classifiedRequires = new HashMap<>();
        for (Map.Entry<String, List<Selection>> entry : clientToFieldMap.entrySet()) {
            classifiedRequires.put(entry.getKey(), getFieldAsString(entry.getValue()));
        }
        return classifiedRequires;
    }

    private static String getFieldAsString(List<Selection> fields) {
        List<String> fieldStrings = new ArrayList<>();
        for (Selection selection : fields) {

            if (selection instanceof Field) {
                if (((Field) selection).getSelectionSet() != null) {
                    fieldStrings.add(((Field) selection).getName() + " {" +
                            getFieldAsString(((Field) selection).getSelectionSet().getSelections()) + "}");
                } else {
                    fieldStrings.add(((Field) selection).getName());
                }
            }
        }

        return String.join(" ", fieldStrings);
    }


    private static String getClientFromFieldDefinition(FieldDefinition definition,
                                                       List<GraphQLDirective> joinTypeDirectivesOnParent)
            throws GatewayQueryPlanGenerationException {
        for (Directive directive : definition.getDirectives()) {
            if (directive.getName().equals("join__field")) {
                String graph = null;
                Boolean external = null;
                for (Argument argument : directive.getArguments()) {
                    if (argument.getName().equals("graph")) {
                        graph = ((EnumValue) argument.getValue()).getName();
                    } else if (argument.getName().equals("external")) {
                        external = ((BooleanValue) argument.getValue()).isValue();
                    }
                }

                if (graph != null && (external == null || !external)) {
                    return graph;
                }
            }
        }

        if (joinTypeDirectivesOnParent.size() == 1) {
            for (GraphQLArgument argument : joinTypeDirectivesOnParent.get(0).getArguments()) {
                if (argument.getName().equals("graph")) {
                    return ((EnumValue) argument.getArgumentValue().getValue()).getName();
                }
            }
        }

        return null;
    }

    private List<FieldData> getFieldsOfType(String typeName) throws GatewayQueryPlanGenerationException {
        List<FieldData> fields = new ArrayList<>();

        List<GraphQLDirective> joinTypeDirectives =
                SpecReader.getObjectTypeDirectives(this.graphQLSchema, typeName).stream().filter(
                        directive -> directive.getName().equals("join__type")
                ).collect(Collectors.toList());
        for (Map.Entry<String, FieldDefinition> entry :
                SpecReader.getObjectTypeFieldDefinitionMap(this.graphQLSchema, typeName).entrySet()) {
            FieldData field = new FieldData(this,
                    entry.getKey(), entry.getValue(), joinTypeDirectives, typeName);
            if (field.getClient() != null) {
                fields.add(field);
            }
        }

        return fields;
    }

    private void addClientConstantDeclarations(List<Node> nodeList) {
        for (Map.Entry<String, JoinGraph> entry :
                getJoinGraphs(this.graphQLSchema).entrySet()) {
            Node declarationNode = createConstantDeclarationNode(
                    null,
                    createToken(PUBLIC_KEYWORD),
                    createToken(CONST_KEYWORD),
                    createBuiltinSimpleNameReferenceNode(STRING_TYPE_DESC, createToken(STRING_KEYWORD)),
                    createIdentifierToken(entry.getKey()),
                    createToken(EQUAL_TOKEN),
                    createBasicLiteralNode(
                            STRING_LITERAL,
                            createLiteralValueToken(STRING_LITERAL_TOKEN,
                                    "\"" + entry.getValue().getName() + "\"",
                                    createEmptyMinutiaeList(), createEmptyMinutiaeList())
                    ),
                    createToken(SEMICOLON_TOKEN)
            );

            nodeList.add(declarationNode);
        }
    }

    static class FieldData {
        private final String fieldName;
        private final String type;
        private final String client;
        private final String typename;
        private final FieldDefinition fieldDefinition;

        private final GatewayQueryPlanGenerator generator;

        public FieldData(GatewayQueryPlanGenerator generator, String fieldName, FieldDefinition fieldDefinition,
                         List<GraphQLDirective> joinTypeDirectivesOnParent, String parentType)
                throws GatewayQueryPlanGenerationException {
            this.fieldName = fieldName;
            this.type = getTypeFromFieldDefinition(fieldDefinition);
            this.client = getClientFromFieldDefinition(fieldDefinition, joinTypeDirectivesOnParent);
            this.typename = parentType;
            this.fieldDefinition = fieldDefinition;
            this.generator = generator;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getType() {
            return type;
        }

        public String getClient() {
            return client;
        }

        public Map<String, String> getRequires() {
            return generator.getRequiresFromFieldDefinition(fieldDefinition, typename);
        }

        public boolean isID() {
            return this.type.equals("ID");
        }

    }


}
