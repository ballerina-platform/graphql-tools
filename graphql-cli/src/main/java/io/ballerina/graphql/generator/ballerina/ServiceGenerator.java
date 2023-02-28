package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NUMERIC_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ON_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;

/**
 * This class is used to generate ballerina service file according to the GraphQL schema.
 */
public class ServiceGenerator {
    private static ServiceGenerator serviceGenerator = null;

    public static ServiceGenerator getInstance() {
        if (serviceGenerator == null) {
            serviceGenerator = new ServiceGenerator();
        }
        return serviceGenerator;
    }

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
        // Generate imports
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

        NodeList<Token> qualifierList = createNodeList(createToken(SyntaxKind.CONFIGURABLE_KEYWORD));

        // generate ports
        BuiltinSimpleNameReferenceNode typeBindingPattern =
                NodeFactory.createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("int"));
        CaptureBindingPatternNode bindingPattern =
                NodeFactory.createCaptureBindingPatternNode(createIdentifierToken("port"));
        TypedBindingPatternNode typedBindingPatternNode =
                NodeFactory.createTypedBindingPatternNode(typeBindingPattern, bindingPattern);

        // generate literal
        BasicLiteralNode basicLiteralNode =
                NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, createIdentifierToken("9090"));

        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        // generate module variable declaration
        ModuleVariableDeclarationNode moduleVariableDeclarationNode =
                NodeFactory.createModuleVariableDeclarationNode(metadataNode, null, qualifierList,
                        typedBindingPatternNode,
                        createToken(SyntaxKind.EQUAL_TOKEN), basicLiteralNode, createToken(SEMICOLON_TOKEN));

        members.add(moduleVariableDeclarationNode);

        // Generate service file
        ServiceDeclarationNode serviceDeclarationNode = generateServiceDeclaration(graphQLSchema, generatorContext);
        members.add(serviceDeclarationNode);
        return createNodeList(members);
    }

    private ServiceDeclarationNode generateServiceDeclaration(GraphQLSchema graphQLSchema,
                                                              GeneratorContext generatorContext) throws IOException {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        // Generate qualifiers
        NodeList<Token> serviceTypeQualifiers = createNodeList(createIdentifierToken(EMPTY_STRING));

        Token serviceToken = createToken(SERVICE_KEYWORD);

        SimpleNameReferenceNode serviceObjectTypeDescriptor = createSimpleNameReferenceNode(createIdentifierToken(
                "CustomerApi"));

        NodeList<Node> absoluteResourcePath = createNodeList(createIdentifierToken(EMPTY_STRING));

        NodeList<Node> expressions = createNodeList();

        QualifiedNameReferenceNode expressionQualifiedNameReferenceNode =
                createQualifiedNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.GRAPHQL),
                        createToken(COLON_TOKEN),
                        createIdentifierToken(
                                CodeGeneratorConstants.LISTENER));

        ParenthesizedArgList expressionParenthesizedArgList =
                createParenthesizedArgList(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                        createSeparatedNodeList(createPositionalArgumentNode(
                                createBasicLiteralNode(NUMERIC_LITERAL, createIdentifierToken("9090")))),
                        createToken(CLOSE_PAREN_TOKEN));

        ExplicitNewExpressionNode explicitNewExpressionNode =
                createExplicitNewExpressionNode(createToken(NEW_KEYWORD), expressionQualifiedNameReferenceNode,
                        expressionParenthesizedArgList);

        SeparatedNodeList<ExpressionNode> separatedNodeList = createSeparatedNodeList(explicitNewExpressionNode);

        ServiceDeclarationNode serviceDeclarationNode =
                createServiceDeclarationNode(metadataNode, serviceTypeQualifiers, serviceToken,
                        serviceObjectTypeDescriptor,
                        absoluteResourcePath, createToken(ON_KEYWORD), separatedNodeList,
                        createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                        createNodeList(createIdentifierToken(EMPTY_STRING)), createToken(SyntaxKind.CLOSE_BRACE_TOKEN),
                        createToken(SEMICOLON_TOKEN));

        return serviceDeclarationNode;
    }


    private List<ObjectFieldNode> generateServiceVariables() {
        List<ObjectFieldNode> objectFields = new ArrayList<>();
        return objectFields;
    }

    public String generateSrc(GraphQLSchema graphQLSchema, GeneratorContext generatorContext)
            throws ClientGenerationException {
        try {
            TextDocument from = TextDocuments.from(EMPTY_STRING);

            return Formatter.format(generateSyntaxTree(graphQLSchema, generatorContext)).toString();
        } catch (FormatterException | IOException e) {
            throw new ClientGenerationException(e.getMessage());
        }
    }
}
