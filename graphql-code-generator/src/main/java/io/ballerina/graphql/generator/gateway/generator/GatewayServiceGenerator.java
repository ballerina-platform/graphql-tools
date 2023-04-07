package io.ballerina.graphql.generator.gateway.generator;

import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayDimensionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBinaryExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBlockStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBracedExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createElseBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createErrorConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIfElseStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImportDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImportOrgNameNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createListConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModuleVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPanicStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParameterizedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRemoteMethodCallActionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BINARY_EXPRESSION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BRACED_EXPRESSION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_ACTION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_EXPRESSION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CONFIGURABLE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOUBLE_EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELSE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.INT_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NUMERIC_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PANIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RIGHT_ARROW_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_TYPE_DESC;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getJoinGraphs;

/**
 * Class to generate service code for the gateway.
 */
public class GatewayServiceGenerator {
    private final GraphQLSchema graphQLSchema;

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

    private SyntaxTree generateSyntaxTree() throws GatewayGenerationException {
        NodeList<ImportDeclarationNode> importsList = createNodeList(
                createImportDeclarationNode(
                        createToken(SyntaxKind.IMPORT_KEYWORD),
                        createImportOrgNameNode(
                                createIdentifierToken("ballerina"),
                                createToken(SyntaxKind.SLASH_TOKEN)
                        ),
                        createSeparatedNodeList(
                                createIdentifierToken("graphql")
                        ),
                        null,
                        createToken(SyntaxKind.SEMICOLON_TOKEN)

                )
        );

        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();

        nodes.addAll(getClientDeclarations());

        FunctionDefinitionNode getClientFunction
                = createFunctionDefinitionNode(
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
        nodes.add(getClientFunction);

        nodes.add(
                createModuleVariableDeclarationNode(
                        null,
                        null,
                        createSeparatedNodeList(
                                createToken(CONFIGURABLE_KEYWORD)
                        ),
                        createTypedBindingPatternNode(
                                createBuiltinSimpleNameReferenceNode(
                                        INT_TYPE_DESC,
                                        createIdentifierToken("int")
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("PORT")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createBasicLiteralNode(
                                NUMERIC_LITERAL,
                                createLiteralValueToken(
                                        NUMERIC_LITERAL,
                                        "9000",
                                        createEmptyMinutiaeList(),
                                        createEmptyMinutiaeList()
                                )
                        ),
                        createToken(SEMICOLON_TOKEN)
                )
        );

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
                                        new FunctionDefinitionNode[getResourceFunctions().size()])
                        ),
                        createToken(CLOSE_BRACE_TOKEN),
                        null
                )
        );

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(
                nodes.toArray(
                        new ModuleMemberDeclarationNode[nodes.size()])
        );

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private List<FunctionDefinitionNode> getResourceFunctions() throws GatewayGenerationException {
        List<FunctionDefinitionNode> resourceFunctions = new ArrayList<>();
        for (GraphQLSchemaElement graphQLObjectType : graphQLSchema.getQueryType().getChildren().stream().filter(
                child -> child instanceof GraphQLFieldDefinition).collect(Collectors.toList())) {
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
                                            getArguments(graphQLObjectType).toArray(
                                                    new Node[getArguments(graphQLObjectType).size()])
                                    ),
                                    createToken(CLOSE_PAREN_TOKEN),
                                    createReturnTypeDescriptorNode(
                                            createToken(RETURNS_KEYWORD),
                                            createNodeList(),
                                            createUnionTypeDescriptorNode(
                                                    createSimpleNameReferenceNode(
                                                            createIdentifierToken(CommonUtils
                                                                    .getTypeNameFromGraphQLType(
                                                                            ((GraphQLFieldDefinition) graphQLObjectType).getType()))),
                                                    createToken(PIPE_TOKEN),
                                                    createParameterizedTypeDescriptorNode(
                                                            ERROR_TYPE_DESC,
                                                            createToken(ERROR_KEYWORD),
                                                            null
                                                    )
                                            )
                                    )
                            ),
                            createFunctionBodyBlockNode(
                                    createToken(OPEN_BRACE_TOKEN),
                                    null,
                                    getResourceFunctionBody(graphQLObjectType),
                                    createToken(CLOSE_BRACE_TOKEN),
                                    createToken(SEMICOLON_TOKEN)
                            )
                    )
            );
        }
        return resourceFunctions;
    }

    private SeparatedNodeList getResourceFunctionBody(GraphQLSchemaElement graphQLObjectType)
            throws GatewayGenerationException {
        return createSeparatedNodeList(
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("QueryFieldClassifier")
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("classifier")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createImplicitNewExpressionNode(
                                createToken(NEW_KEYWORD),
                                createParenthesizedArgList(
                                        createToken(OPEN_PAREN_TOKEN),
                                        createSeparatedNodeList(
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken
                                                                        ("'field")
                                                        )
                                                ),
                                                createToken(COMMA_TOKEN),
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken
                                                                        ("queryPlan")
                                                        )
                                                ),
                                                createToken(COMMA_TOKEN),
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken
                                                                        (getClientNameFromFieldDefinition(
                                                                                (GraphQLFieldDefinition)
                                                                                        graphQLObjectType))
                                                        )
                                                )
                                        ),
                                        createToken(CLOSE_PAREN_TOKEN)
                                )
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("string")
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("fieldString")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createMethodCallExpressionNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("classifier")
                                ),
                                createToken(DOT_TOKEN),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("getFieldString")
                                ),
                                createToken(OPEN_PAREN_TOKEN),
                                createSeparatedNodeList(),
                                createToken(CLOSE_PAREN_TOKEN)
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createArrayTypeDescriptorNode(
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken("UnResolvableField")
                                        ),
                                        createSeparatedNodeList(
                                                createArrayDimensionNode(
                                                        createToken(OPEN_BRACKET_TOKEN),
                                                        null,
                                                        createToken(CLOSE_BRACKET_TOKEN)
                                                )
                                        )
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("propertiesNotResolved")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createMethodCallExpressionNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("classifier")
                                ),
                                createToken(DOT_TOKEN),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("getUnresolvableFields")
                                ),
                                createToken(OPEN_PAREN_TOKEN),
                                createSeparatedNodeList(),
                                createToken(CLOSE_PAREN_TOKEN)
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createBuiltinSimpleNameReferenceNode(
                                        STRING_TYPE_DESC,
                                        createToken(STRING_KEYWORD)
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("queryString")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createFunctionCallExpressionNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("wrapwithQuery")
                                ),
                                createToken(OPEN_PAREN_TOKEN),
                                getWarpWithQueryArguments(graphQLObjectType),
                                createToken(CLOSE_PAREN_TOKEN)
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken(
                                                ((GraphQLFieldDefinition) graphQLObjectType).getName()
                                                        + "Response")
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("response")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createCheckExpressionNode(
                                CHECK_ACTION,
                                createToken(CHECK_KEYWORD),
                                createRemoteMethodCallActionNode(
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken(
                                                        getClientNameFromFieldDefinition(
                                                                (GraphQLFieldDefinition) graphQLObjectType)
                                                                + "_CLIENT")
                                        ),
                                        createToken(RIGHT_ARROW_TOKEN),
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken("execute")
                                        ),
                                        createToken(OPEN_PAREN_TOKEN),
                                        createSeparatedNodeList(
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken("queryString")
                                                        )
                                                )
                                        ),
                                        createToken(CLOSE_PAREN_TOKEN)
                                )
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken(CommonUtils.getTypeNameFromGraphQLType(
                                                ((GraphQLFieldDefinition) graphQLObjectType).getType()))
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("result")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createFieldAccessExpressionNode(
                                createFieldAccessExpressionNode(
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken("response")
                                        ),
                                        createToken(DOT_TOKEN),
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken("data")
                                        )
                                ),
                                createToken(DOT_TOKEN),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken(((GraphQLFieldDefinition) graphQLObjectType).getName())
                                )
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createVariableDeclarationNode(
                        createSeparatedNodeList(),
                        null,
                        createTypedBindingPatternNode(
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("Resolver")
                                ),
                                createCaptureBindingPatternNode(
                                        createIdentifierToken("resolver")
                                )
                        ),
                        createToken(EQUAL_TOKEN),
                        createImplicitNewExpressionNode(
                                createToken(NEW_KEYWORD),
                                createParenthesizedArgList(
                                        createToken(OPEN_PAREN_TOKEN),
                                        createSeparatedNodeList(
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken("queryPlan")
                                                        )
                                                ),
                                                createToken(COMMA_TOKEN),
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken("result")
                                                        )
                                                ),
                                                createToken(COMMA_TOKEN),
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken("\"" +
                                                                        CommonUtils.getBasicTypeNameFromGraphQLType(
                                                                            ((GraphQLFieldDefinition) graphQLObjectType)
                                                                                    .getType())
                                                                        + "\"")
                                                        )
                                                ),
                                                createToken(COMMA_TOKEN),
                                                createPositionalArgumentNode(
                                                        createSimpleNameReferenceNode(
                                                                createIdentifierToken("propertiesNotResolved")
                                                        )
                                                ),
                                                createToken(COMMA_TOKEN),
                                                createPositionalArgumentNode(
                                                        createListConstructorExpressionNode(
                                                                createToken(OPEN_BRACKET_TOKEN),
                                                                createSeparatedNodeList(
                                                                        createBasicLiteralNode(
                                                                                STRING_LITERAL,
                                                                                createLiteralValueToken(
                                                                                        STRING_LITERAL_TOKEN,
                                                                                    "\"" +
                                                                                            ((GraphQLFieldDefinition)
                                                                                                    graphQLObjectType)
                                                                                                    .getName() +
                                                                                            "\"",
                                                                                        createEmptyMinutiaeList(),
                                                                                        createEmptyMinutiaeList()
                                                                                )
                                                                        )
                                                                ),
                                                                createToken(CLOSE_BRACKET_TOKEN)
                                                        )
                                                )
                                        ),
                                        createToken(CLOSE_PAREN_TOKEN)
                                )
                        ),
                        createToken(SEMICOLON_TOKEN)
                ),
                createReturnStatementNode(
                        createToken(RETURN_KEYWORD),
                        createMethodCallExpressionNode(
                                createMethodCallExpressionNode(
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken("resolver")
                                        ),
                                        createToken(DOT_TOKEN),
                                        createSimpleNameReferenceNode(
                                                createIdentifierToken("getResult")
                                        ),
                                        createToken(OPEN_PAREN_TOKEN),
                                        createSeparatedNodeList(),
                                        createToken(CLOSE_PAREN_TOKEN)
                                ),
                                createToken(DOT_TOKEN),
                                createSimpleNameReferenceNode(
                                        createIdentifierToken("ensureType")
                                ),
                                createToken(OPEN_PAREN_TOKEN),
                                createSeparatedNodeList(),
                                createToken(CLOSE_PAREN_TOKEN)

                        ),
                        createToken(SEMICOLON_TOKEN)
                )
        );
    }

    private List<Node> getArguments(GraphQLSchemaElement graphQLObjectType) {
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
            FieldType fieldType = Utils.getFieldType(graphQLSchema, argument.getDefinition().getType());
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

    private StatementNode getClientFunctionBody(Map<String, JoinGraph> joinGraphs) {
        int size = joinGraphs.size();
        if (size == 0) {
            return createBlockStatementNode(
                    createToken(OPEN_BRACE_TOKEN),
                    createNodeList(
                            createPanicStatementNode(
                                    createToken(PANIC_KEYWORD),
                                    createErrorConstructorExpressionNode(
                                            createToken(ERROR_KEYWORD),
                                            null,
                                            createToken(OPEN_PAREN_TOKEN),
                                            createSeparatedNodeList(
                                                    createPositionalArgumentNode(
                                                            createBasicLiteralNode(
                                                                    STRING_LITERAL,
                                                                    createLiteralValueToken(
                                                                            STRING_LITERAL_TOKEN,
                                                                            "\"Client not found\"",
                                                                            createEmptyMinutiaeList(),
                                                                            createEmptyMinutiaeList()
                                                                    )
                                                            )
                                                    )
                                            ),
                                            createToken(CLOSE_PAREN_TOKEN)
                                    ),
                                    createToken(SEMICOLON_TOKEN)
                            )
                    ),
                    createToken(CLOSE_BRACE_TOKEN)
            );
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
                    createModuleVariableDeclarationNode(
                            null,
                            null,
                            createSeparatedNodeList(
                                    createToken(FINAL_KEYWORD)
                            ),
                            createTypedBindingPatternNode(
                                    createQualifiedNameReferenceNode(
                                            createIdentifierToken("graphql"),
                                            createToken(COLON_TOKEN),
                                            createIdentifierToken("Client")
                                    ),
                                    createCaptureBindingPatternNode(
                                            createIdentifierToken(key + "_CLIENT")
                                    )
                            ),
                            createToken(EQUAL_TOKEN),
                            createCheckExpressionNode(
                                    CHECK_EXPRESSION,
                                    createToken(CHECK_KEYWORD),
                                    createExplicitNewExpressionNode(
                                            createToken(NEW_KEYWORD),
                                            createQualifiedNameReferenceNode(
                                                    createIdentifierToken("graphql"),
                                                    createToken(COLON_TOKEN),
                                                    createIdentifierToken("Client")
                                            ),
                                            createParenthesizedArgList(
                                                    createToken(OPEN_PAREN_TOKEN),
                                                    createSeparatedNodeList(
                                                            createIdentifierToken("\"" + value.getUrl() + "\"")
                                                    ),
                                                    createToken(CLOSE_PAREN_TOKEN)
                                            )
                                    )
                            ),
                            createToken(SEMICOLON_TOKEN)
                    )
            );
        }
        return nodes;
    }

    private SeparatedNodeList getWarpWithQueryArguments(GraphQLSchemaElement graphQLObjectType) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(
                createSimpleNameReferenceNode(
                        createIdentifierToken(
                                "\"" + ((GraphQLFieldDefinition) graphQLObjectType).getName() + "\""
                        )
                )
        );
        nodes.add(createToken(COMMA_TOKEN));
        nodes.add(
                createSimpleNameReferenceNode(
                        createIdentifierToken("fieldString")
                )
        );

        List<GraphQLArgument> arguments = ((GraphQLFieldDefinition) graphQLObjectType).getArguments();
        if (arguments.size() > 0) {
            nodes.add(createToken(COMMA_TOKEN));
            nodes.add(createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN),
                    getFieldArguments(arguments),
                    createToken(CLOSE_BRACE_TOKEN)
            ));
        }

        return createSeparatedNodeList(nodes);
    }

    private SeparatedNodeList getFieldArguments(List<GraphQLArgument> arguments) {
        List<Node> nodes = new ArrayList<>();
        int size = arguments.size();
        int count = 0;
        for (GraphQLArgument argument : arguments) {
            nodes.add(
                    createSpecificFieldNode(
                            null,
                            createBasicLiteralNode(
                                    STRING_LITERAL,
                                    createLiteralValueToken(
                                            STRING_LITERAL_TOKEN,
                                            "\"" + argument.getName() + "\"",
                                            createEmptyMinutiaeList(),
                                            createEmptyMinutiaeList()
                                    )
                            ),
                            createToken(COLON_TOKEN),
                            createMethodCallExpressionNode(
                                    createSimpleNameReferenceNode(
                                            createIdentifierToken(argument.getName())
                                    )
                                    ,
                                    createToken(DOT_TOKEN),
                                    createSimpleNameReferenceNode(
                                            createIdentifierToken("toString")
                                    ),
                                    createToken(OPEN_PAREN_TOKEN),
                                    createSeparatedNodeList(),
                                    createToken(CLOSE_PAREN_TOKEN)
                            )
                    )
            );

            if (size < ++count) {
                nodes.add(createToken(COMMA_TOKEN));
            }
        }

        return createSeparatedNodeList(nodes);
    }

    private String getClientNameFromFieldDefinition(GraphQLFieldDefinition graphQLFieldDefinition) {
        for (GraphQLDirective directive : graphQLFieldDefinition.getDirectives()) {
            if (directive.getName().equals("join__field")) {
                return ((EnumValue) directive.getArgument("graph").getArgumentValue().getValue()).getName();
            }
        }
        return null;
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
}
