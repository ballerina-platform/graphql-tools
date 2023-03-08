package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.GeneratorContext;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModuleVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.INT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.PORT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.PORT_NUMBER_DEFAULT;

/**
 * This class is used to generate ballerina service file according to the GraphQL schema.
 */
public class ServiceGenerator {

    private String fileName;

    private NodeList<ImportDeclarationNode> generateImports() {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForGraphql =
                CodeGeneratorUtils.getImportDeclarationNode(CodeGeneratorConstants.BALLERINA,
                        CodeGeneratorConstants.GRAPHQL);
        imports.add(importForGraphql);
        return createNodeList(imports);
    }

    private SyntaxTree generateSyntaxTree(GraphQLSchema graphQLSchema, GeneratorContext generatorContext)
            throws IOException {
        NodeList<ImportDeclarationNode> imports = generateImports();

        NodeList<ModuleMemberDeclarationNode> moduleMemberDeclarationNodes =
                generateMembers(graphQLSchema, generatorContext);

        ModulePartNode modulePartNode =
                createModulePartNode(imports, moduleMemberDeclarationNodes, createToken(SyntaxKind.EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private NodeList<ModuleMemberDeclarationNode> generateMembers(GraphQLSchema graphQLSchema,
                                                                  GeneratorContext generatorContext)
            throws IOException {
        List<ModuleMemberDeclarationNode> members = new ArrayList<>();

        ModuleVariableDeclarationNode portVariable = generatePortModuleVariableDeclaration(PORT_NUMBER_DEFAULT);
        members.add(portVariable);

        ServiceDeclarationNode serviceDeclaration = generateServiceDeclaration(graphQLSchema, generatorContext);
        members.add(serviceDeclaration);

        return createNodeList(members);
    }

    private ModuleVariableDeclarationNode generatePortModuleVariableDeclaration(String portNumberStr) {
        NodeList<Token> qualifierList = createNodeList(createToken(SyntaxKind.CONFIGURABLE_KEYWORD));

        BuiltinSimpleNameReferenceNode portTypeDescriptor =
                createBuiltinSimpleNameReferenceNode(null, createToken(INT_KEYWORD));
        CaptureBindingPatternNode portBindingPattern = createCaptureBindingPatternNode(createIdentifierToken(PORT));
        TypedBindingPatternNode portTypeBinding = createTypedBindingPatternNode(portTypeDescriptor, portBindingPattern);

        BasicLiteralNode portBasicLiteral =
                NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, createIdentifierToken(portNumberStr));

        return createModuleVariableDeclarationNode(null, null, qualifierList, portTypeBinding,
                createToken(SyntaxKind.EQUAL_TOKEN), portBasicLiteral, createToken(SEMICOLON_TOKEN));
    }

    private ServiceDeclarationNode generateServiceDeclaration(GraphQLSchema graphQLSchema,
                                                              GeneratorContext generatorContext) throws IOException {
        NodeList<Token> serviceQualifiers = createEmptyNodeList();

        SimpleNameReferenceNode serviceObjectTypeDescriptor =
                createSimpleNameReferenceNode(createIdentifierToken(this.fileName));

        NodeList<Node> absoluteResourcePath = createEmptyNodeList();

        ExplicitNewExpressionNode serviceExpression = generateServiceExpression();

        return createServiceDeclarationNode(null, serviceQualifiers, createToken(SERVICE_KEYWORD),
                serviceObjectTypeDescriptor, absoluteResourcePath, createToken(ON_KEYWORD),
                createSeparatedNodeList(serviceExpression), createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                createEmptyNodeList(), createToken(SyntaxKind.CLOSE_BRACE_TOKEN),
                null);
    }

    private ExplicitNewExpressionNode generateServiceExpression() {
        QualifiedNameReferenceNode expressionQualifiedNameReference =
                createQualifiedNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.GRAPHQL),
                        createToken(COLON_TOKEN), createIdentifierToken(CodeGeneratorConstants.LISTENER));

        PositionalArgumentNode expressionPositionalArgument =
                createPositionalArgumentNode(createSimpleNameReferenceNode(createIdentifierToken(PORT)));

        SeparatedNodeList<FunctionArgumentNode> expressionArguments =
                createSeparatedNodeList(expressionPositionalArgument);

        ParenthesizedArgList expressionParenthesizedArgList =
                createParenthesizedArgList(createToken(SyntaxKind.OPEN_PAREN_TOKEN), expressionArguments,
                        createToken(CLOSE_PAREN_TOKEN));

        return createExplicitNewExpressionNode(createToken(NEW_KEYWORD), expressionQualifiedNameReference,
                expressionParenthesizedArgList);
    }


    private List<ObjectFieldNode> generateServiceVariables() {
        List<ObjectFieldNode> objectFields = new ArrayList<>();
        return objectFields;
    }

    public String generateSrc(String fileName, GraphQLSchema graphQLSchema, GeneratorContext generatorContext)
            throws ClientGenerationException {
        try {
            this.fileName = fileName;
            return Formatter.format(generateSyntaxTree(graphQLSchema, generatorContext)).toString();
        } catch (FormatterException | IOException e) {
            throw new ClientGenerationException(e.getMessage());
        }
    }
}
