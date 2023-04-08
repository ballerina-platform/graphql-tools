package io.ballerina.graphql.generator.gateway.generator;

import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import io.ballerina.graphql.generator.gateway.generator.common.JoinGraph;
import io.ballerina.graphql.generator.utils.graphql.Utils;
import io.ballerina.graphql.generator.utils.model.FieldType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBinaryExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBlockStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBracedExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createElseBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIfElseStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParameterizedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BINARY_EXPRESSION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BRACED_EXPRESSION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOUBLE_EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELSE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_TYPE_DESC;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.gateway.generator.Constants.BALLERINA_GRAPHQL_IMPORT_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.Constants.BASIC_RESPONSE_TYPE_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.Constants.CLIENT_NAME_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.Constants.CLIENT_NOT_FOUND_PANIC_BLOCK;
import static io.ballerina.graphql.generator.gateway.generator.Constants.CONFIGURABLE_PORT_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.Constants.GRAPHQL_CLIENT_DECLARATION_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.Constants.QUERY_ARGS_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.Constants.QUERY_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.Constants.RESPONSE_TYPE_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.Constants.URL_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getJoinGraphs;

/**
 * Class to generate service code for the gateway.
 */
public class GatewayServiceGenerator {
    private final GraphQLSchema graphQLSchema;

    private static final Path RESOURCE_PATH = Paths.get("src", "main", "resources", "gateway_templates");

    public GatewayServiceGenerator(GraphQLSchema graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public String generateSrc() throws GatewayServiceGenerationException {
        try {
            SyntaxTree syntaxTree = generateSyntaxTree();
            return Formatter.format(syntaxTree).toString();
        } catch (Exception e) {
            throw new GatewayServiceGenerationException("Error while generating the gateway types", e);
        }
    }

    private SyntaxTree generateSyntaxTree() throws GatewayGenerationException, IOException {
        NodeList<ImportDeclarationNode> importsList = createNodeList(
                NodeParser.parseImportDeclaration(BALLERINA_GRAPHQL_IMPORT_STATEMENT)
        );

        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>(getClientDeclarations());
        nodes.add(getGetClientFunction());
        nodes.add(NodeParser.parseModuleMemberDeclaration(CONFIGURABLE_PORT_STATEMENT));
        nodes.add(
                createServiceDeclarationNode(
                        null,
                        createSeparatedNodeList(
                                createToken(ISOLATED_KEYWORD)
                        ),
                        createToken(SERVICE_KEYWORD),
                        null,
                        createSeparatedNodeList(),
                        createToken(ON_KEYWORD),
                        createSeparatedNodeList(
                                createExplicitNewExpressionNode(
                                        createToken(NEW_KEYWORD),
                                        createQualifiedNameReferenceNode(
                                                createIdentifierToken("graphql"),
                                                createToken(COLON_TOKEN),
                                                createIdentifierToken("Listener")
                                        ),
                                        createParenthesizedArgList(
                                                createToken(OPEN_PAREN_TOKEN),
                                                createSeparatedNodeList(
                                                        createPositionalArgumentNode(
                                                                createSimpleNameReferenceNode(
                                                                        createIdentifierToken("PORT"))
                                                        )
                                                ),
                                                createToken(CLOSE_PAREN_TOKEN)
                                        )
                                )
                        ),
                        createToken(OPEN_BRACE_TOKEN),
                        createSeparatedNodeList(
                                getResourceFunctions().toArray(
                                        new FunctionDefinitionNode[0])
                        ),
                        createToken(CLOSE_BRACE_TOKEN),
                        null
                )
        );

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(
                nodes.toArray(
                        new ModuleMemberDeclarationNode[0])
        );

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private List<FunctionDefinitionNode> getResourceFunctions() throws GatewayGenerationException, IOException {
        List<FunctionDefinitionNode> resourceFunctions = new ArrayList<>();
        for (GraphQLSchemaElement graphQLObjectType : CommonUtils.getQueryTypes(graphQLSchema)) {
            resourceFunctions.add(
                    createFunctionDefinitionNode(
                            RESOURCE_ACCESSOR_DEFINITION,
                            null,
                            createSeparatedNodeList(
                                    createToken(ISOLATED_KEYWORD),
                                    createToken(RESOURCE_KEYWORD)
                            ),
                            createToken(FUNCTION_KEYWORD),
                            createIdentifierToken("get"),
                            createSeparatedNodeList(
                                    createIdentifierToken(((GraphQLFieldDefinition) graphQLObjectType).getName())
                            ),
                            createFunctionSignatureNode(
                                    createToken(OPEN_PAREN_TOKEN),
                                    createSeparatedNodeList(
                                            getResourceFunctionArguments(graphQLObjectType).toArray(
                                                    new Node[0])
                                    ),
                                    createToken(CLOSE_PAREN_TOKEN),
                                    createReturnTypeDescriptorNode(
                                            createToken(RETURNS_KEYWORD),
                                            createNodeList(),
                                            createUnionTypeDescriptorNode(
                                                    createSimpleNameReferenceNode(
                                                            createIdentifierToken(CommonUtils
                                                                    .getTypeNameFromGraphQLType(
                                                                            ((GraphQLFieldDefinition) graphQLObjectType)
                                                                                    .getType()))),
                                                    createToken(PIPE_TOKEN),
                                                    createParameterizedTypeDescriptorNode(
                                                            ERROR_TYPE_DESC,
                                                            createToken(ERROR_KEYWORD),
                                                            null
                                                    )
                                            )
                                    )
                            ),
                            getResourceFunctionBody(graphQLObjectType)
                    )
            );
        }
        return resourceFunctions;
    }

    private List<Node> getResourceFunctionArguments(GraphQLSchemaElement graphQLObjectType) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(
                createRequiredParameterNode(
                        createSeparatedNodeList(),
                        createQualifiedNameReferenceNode(
                                createIdentifierToken("graphql"),
                                createToken(COLON_TOKEN),
                                createIdentifierToken("Field")
                        ),
                        createIdentifierToken("'field")
                )
        );
        for (GraphQLArgument argument : ((GraphQLFieldDefinition) graphQLObjectType).getArguments()) {
            nodes.add(createToken(COMMA_TOKEN));
            FieldType fieldType = Utils.getFieldType(graphQLSchema,
                    Objects.requireNonNull(argument.getDefinition()).getType());
            if (argument.getDefinition().getDefaultValue() != null) {
                nodes.add(
                        createDefaultableParameterNode(
                                createSeparatedNodeList(),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken(fieldType.getName() + fieldType.getTokens())
                                ),
                                createIdentifierToken(argument.getName()),
                                createToken(EQUAL_TOKEN),
                                createBasicLiteralNode(
                                        STRING_LITERAL,
                                        createLiteralValueToken(
                                                STRING_LITERAL_TOKEN,
                                                getValue(argument.getDefinition().getDefaultValue()),
                                                createEmptyMinutiaeList(),
                                                createEmptyMinutiaeList()
                                        )
                                )
                        )
                );
            } else {
                nodes.add(
                        createRequiredParameterNode(
                                createSeparatedNodeList(),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken(fieldType.getName() + fieldType.getTokens())
                                ),
                                createIdentifierToken(argument.getName())
                        )
                );
            }
        }
        return nodes;
    }

    private FunctionBodyBlockNode getResourceFunctionBody(GraphQLSchemaElement graphQLSchemaElement)
            throws IOException, GatewayGenerationException {
        String functionTemplate = Files.readString(
                Path.of(RESOURCE_PATH.toString(), "resource_function_body.bal.partial").toAbsolutePath());
        functionTemplate = functionTemplate.replaceAll(QUERY_PLACEHOLDER,
                ((GraphQLFieldDefinition) graphQLSchemaElement).getName());
        functionTemplate = functionTemplate.replaceAll(RESPONSE_TYPE_PLACEHOLDER,
                CommonUtils.getTypeNameFromGraphQLType(((GraphQLFieldDefinition) graphQLSchemaElement).getType()));
        functionTemplate = functionTemplate.replaceAll(BASIC_RESPONSE_TYPE_PLACEHOLDER,
                CommonUtils.getBasicTypeNameFromGraphQLType(((GraphQLFieldDefinition) graphQLSchemaElement).getType()));
        functionTemplate = functionTemplate.replaceAll(CLIENT_NAME_PLACEHOLDER,
                getClientNameFromFieldDefinition((GraphQLFieldDefinition) graphQLSchemaElement));
        functionTemplate = functionTemplate.replaceAll(QUERY_ARGS_PLACEHOLDER, getQueryArguments(graphQLSchemaElement));

        return NodeParser.parseFunctionBodyBlock(functionTemplate);
    }

    private FunctionDefinitionNode getGetClientFunction() {
        return createFunctionDefinitionNode(
                FUNCTION_DEFINITION,
                null,
                createSeparatedNodeList(createToken(ISOLATED_KEYWORD)),
                createToken(FUNCTION_KEYWORD),
                createIdentifierToken("getClient"),
                createSeparatedNodeList(),
                createFunctionSignatureNode(
                        createToken(OPEN_PAREN_TOKEN),
                        createSeparatedNodeList(
                                createRequiredParameterNode(
                                        createSeparatedNodeList(),
                                        createBuiltinSimpleNameReferenceNode(
                                                STRING_TYPE_DESC,
                                                createIdentifierToken("string")
                                        ),
                                        createIdentifierToken("clientName")
                                )
                        ),
                        createToken(CLOSE_PAREN_TOKEN),
                        createReturnTypeDescriptorNode(
                                createToken(RETURNS_KEYWORD),
                                createNodeList(),
                                createQualifiedNameReferenceNode(
                                        createIdentifierToken("graphql"),
                                        createToken(COLON_TOKEN),
                                        createIdentifierToken("Client")
                                )
                        )
                ),
                createFunctionBodyBlockNode(
                        createToken(OPEN_BRACE_TOKEN),
                        null,
                        createNodeList(
                                getClientFunctionBody(getJoinGraphs(graphQLSchema))
                        ),
                        createToken(CLOSE_BRACE_TOKEN),
                        createToken(SEMICOLON_TOKEN)
                )
        );
    }

    private StatementNode getClientFunctionBody(Map<String, JoinGraph> joinGraphs) {
        int size = joinGraphs.size();
        if (size == 0) {
            return NodeParser.parseBlockStatement(CLIENT_NOT_FOUND_PANIC_BLOCK);
        }

        String clientName = joinGraphs.keySet().iterator().next();
        JoinGraph joinGraph = joinGraphs.remove(clientName);

        return createIfElseStatementNode(
                createToken(IF_KEYWORD),
                createBracedExpressionNode(
                        BRACED_EXPRESSION,
                        createToken(OPEN_PAREN_TOKEN),
                        createBinaryExpressionNode(
                                BINARY_EXPRESSION,
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("clientName")
                                ),
                                createToken(DOUBLE_EQUAL_TOKEN),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("\"" + joinGraph.getName() + "\"")
                                )
                        ),
                        createToken(CLOSE_PAREN_TOKEN)
                ),
                createBlockStatementNode(
                        createToken(OPEN_BRACE_TOKEN),
                        createNodeList(
                                createReturnStatementNode(
                                        createToken(RETURN_KEYWORD),
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken(clientName + "_CLIENT")
                                        ),
                                        createToken(SEMICOLON_TOKEN)
                                )
                        ),
                        createToken(CLOSE_BRACE_TOKEN)
                ),
                createElseBlockNode(
                        createToken(ELSE_KEYWORD),
                        getClientFunctionBody(joinGraphs)
                )
        );
    }

    private List<ModuleMemberDeclarationNode> getClientDeclarations() {
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();

        Map<String, JoinGraph> joinGraphs
                = getJoinGraphs(graphQLSchema);

        for (Map.Entry<String, JoinGraph> entry : joinGraphs.entrySet()) {
            String key = entry.getKey();
            JoinGraph value = entry.getValue();
            nodes.add(
                    NodeParser.parseModuleMemberDeclaration(
                            GRAPHQL_CLIENT_DECLARATION_STATEMENT.replace(CLIENT_NAME_PLACEHOLDER, key)
                                    .replace(URL_PLACEHOLDER, value.getUrl())
                    )
            );
        }
        return nodes;
    }

    private String getClientNameFromFieldDefinition(GraphQLFieldDefinition graphQLFieldDefinition) {
        for (GraphQLAppliedDirective directive : graphQLFieldDefinition.getAppliedDirectives()) {
            if (directive.getName().equals("join__field")) {
                return ((EnumValue) Objects.requireNonNull(
                        directive.getArgument("graph").getArgumentValue().getValue())).getName();
            }
        }
        throw new GatewayServiceGenerationException("No client name found: " + graphQLFieldDefinition.getName());
    }

    private String getValue(Value value) {
        if (value instanceof IntValue) {
            return ((IntValue) value).getValue().toString();
        } else if (value instanceof StringValue) {
            return "\"" + ((StringValue) value).getValue() + "\"";
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue() ? "true" : "false";
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue().toString();
        } else {
            throw new GatewayServiceGenerationException("Unsupported value: " + value);
        }
    }

    /**
     * Can be used Node parser supports function Declaration to be parsed.
     */
    private String getArgumentString(GraphQLSchemaElement graphQLObjectType) {
        StringBuilder arguments = new StringBuilder();
        for (GraphQLArgument argument : ((GraphQLFieldDefinition) graphQLObjectType).getArguments()) {
            arguments.append(", ");
            FieldType fieldType = Utils.getFieldType(graphQLSchema,
                    Objects.requireNonNull(argument.getDefinition()).getType());
            if (argument.getDefinition().getDefaultValue() != null) {
                arguments.append(fieldType.getName()).append(fieldType.getTokens()).append(" ")
                        .append(argument.getName()).append(" = ")
                        .append(getValue(argument.getDefinition().getDefaultValue()));
            } else {
                arguments.append(fieldType.getName()).append(fieldType.getTokens()).append(" ")
                        .append(argument.getName());
            }
        }
        return arguments.toString();
    }

    private String getQueryArguments(GraphQLSchemaElement graphQLObjectType) {
        StringBuilder argumentString = new StringBuilder();
        List<GraphQLArgument> arguments = ((GraphQLFieldDefinition) graphQLObjectType).getArguments();
        if (arguments.size() > 0) {
            argumentString.append(", ");
            argumentString.append("{");
            argumentString.append(getQueryArgumentList(arguments));
            argumentString.append("}");
        }
        return argumentString.toString();
    }

    private String getQueryArgumentList(List<GraphQLArgument> arguments) {
        StringBuilder argumentList = new StringBuilder();
        int size = arguments.size();
        int count = 0;
        for (GraphQLArgument argument : arguments) {
            argumentList.append("\"").append(argument.getName()).append("\": ").append(argument.getName())
                    .append(".").append("toString()");

            if (size < ++count) {
                argumentList.append(", ");
            }
        }
        return argumentList.toString();
    }
}
