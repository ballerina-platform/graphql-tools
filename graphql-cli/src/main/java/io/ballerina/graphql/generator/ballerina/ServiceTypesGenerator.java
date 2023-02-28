package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayDimensionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.INT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.INT_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OBJECT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DECLARATION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GET;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.MUTATION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.QUERY;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.SUBSCRIPTION;

/**
 * Generates the Ballerina syntax tree for the service types.
 */
public class ServiceTypesGenerator extends TypesGenerator {

    public static ServiceTypesGenerator serviceTypesGenerator = null;

    public static ServiceTypesGenerator getInstance() {
        if (serviceTypesGenerator == null) {
            serviceTypesGenerator = new ServiceTypesGenerator();
        }
        return serviceTypesGenerator;
    }

    public String generateSrc(GraphQLSchema schema) throws TypesGenerationException {
        try {
            String generatedSyntaxTree = Formatter.format(this.generateSyntaxTree(schema)).toString();
            return Formatter.format(generatedSyntaxTree);
        } catch (FormatterException | IOException e) {
            throw new TypesGenerationException(e.getMessage());
        }
    }

    public SyntaxTree generateSyntaxTree(GraphQLSchema schema) throws IOException {
        NodeList<ImportDeclarationNode> imports = generateImports();

        List<ModuleMemberDeclarationNode> typeDefinitionNodes = new LinkedList<>();
        addServiceObjectTypeDefinitionNode(schema, typeDefinitionNodes);

        addServiceClassTypes(schema, typeDefinitionNodes);

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(typeDefinitionNodes);

        ModulePartNode modulePartNode = createModulePartNode(imports, members, createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private void addServiceClassTypes(GraphQLSchema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodes) {

        Iterator<Map.Entry<String, GraphQLNamedType>> typesIterator = schema.getTypeMap().entrySet().iterator();

        while (typesIterator.hasNext()) {
            Map.Entry<String, GraphQLNamedType> typeEntry = typesIterator.next();
            String key = typeEntry.getKey();
            GraphQLNamedType type = typeEntry.getValue();

            if (!key.startsWith("__") && !QUERY.equals(key) && !MUTATION.equals(key) && !SUBSCRIPTION.equals(key) && !(type instanceof GraphQLScalarType)) {
                typeDefinitionNodes.add(generateServiceClassType(type));
            }
        }
    }

    private ClassDefinitionNode generateServiceClassType(GraphQLNamedType type) {
        NodeList<Token> classTypeQualifiers = createNodeList(createToken(SERVICE_KEYWORD));

        IdentifierToken className = createIdentifierToken(type.getName());

        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();

        for (GraphQLSchemaElement fieldDefinition : type.getChildren()) {
            if (fieldDefinition instanceof GraphQLFieldDefinition) {
                fieldDefinitions.add((GraphQLFieldDefinition) fieldDefinition);
            }
        }

        NodeList<Node> serviceClassTypeMembers = generateServiceClassTypeMembers(fieldDefinitions);

        ClassDefinitionNode classDefinition =
                createClassDefinitionNode(null, null, classTypeQualifiers, createToken(CLASS_KEYWORD), className,
                        createToken(OPEN_BRACE_TOKEN), serviceClassTypeMembers, createToken(CLOSE_BRACE_TOKEN),
                        createToken(SEMICOLON_TOKEN));

        return classDefinition;
    }

    private NodeList<Node> generateServiceClassTypeMembers(List<GraphQLFieldDefinition> fieldDefinitions) {
        List<Node> members = new ArrayList<>();

        for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
            FunctionDefinitionNode functionDefinition = generateServiceClassTypeMember(fieldDefinition);

            members.add(functionDefinition);
        }

        return createNodeList(members);
    }

    private FunctionDefinitionNode generateServiceClassTypeMember(GraphQLFieldDefinition fieldDefinition) {
        NodeList<Token> memberQualifiers = createNodeList(createToken(RESOURCE_KEYWORD));

        NodeList<Node> memberRelativeResourcePaths =
                createNodeList(createIdentifierToken(fieldDefinition.getName()));

        SeparatedNodeList<ParameterNode> methodSignatureParameters =
                generateMethodSignatureRequiredParams(fieldDefinition.getArguments());

        ReturnTypeDescriptorNode methodSignatureReturnTypeDescriptor =
                generateMethodSignatureReturnTypeDescriptor(fieldDefinition.getType());

        FunctionSignatureNode functionSignature =
                createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), methodSignatureParameters,
                        createToken(CLOSE_PAREN_TOKEN),
                        methodSignatureReturnTypeDescriptor);

        FunctionBodyBlockNode functionBody =
                createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null, createEmptyNodeList(),
                        createToken(CLOSE_BRACE_TOKEN), createToken(SEMICOLON_TOKEN));

        FunctionDefinitionNode functionDefinition =
                createFunctionDefinitionNode(RESOURCE_ACCESSOR_DEFINITION, null, memberQualifiers,
                        createToken(FUNCTION_KEYWORD), createIdentifierToken(GET), memberRelativeResourcePaths,
                        functionSignature, functionBody);

        return functionDefinition;
    }

    private void addServiceObjectTypeDefinitionNode(GraphQLSchema schema,
                                                   List<ModuleMemberDeclarationNode> typeDefinitionNodes) {
        
        IdentifierToken tokenTypeName = createIdentifierToken("CustomerApi");

        NodeList<Token> objectTypeQualifiers = createNodeList(createToken(SERVICE_KEYWORD));

        NodeList<Node> serviceObjectTypeMembers = generateServiceObjectTypeMembers(schema);

        ObjectTypeDescriptorNode serviceObjectTypeDescriptor =
                createObjectTypeDescriptorNode(objectTypeQualifiers, createToken(OBJECT_KEYWORD),
                        createToken(OPEN_BRACE_TOKEN), serviceObjectTypeMembers, createToken(CLOSE_BRACE_TOKEN));

        TypeDefinitionNode serviceObjectTypeDefinition =
                createTypeDefinitionNode(null, null, createToken(TYPE_KEYWORD), tokenTypeName,
                        serviceObjectTypeDescriptor,
                        createToken(SEMICOLON_TOKEN));

        typeDefinitionNodes.add(serviceObjectTypeDefinition);
    }

    private NodeList<Node> generateServiceObjectTypeMembers(GraphQLSchema schema) {
        List<Node> members = new ArrayList<>();

        TypeReferenceNode typeReferenceNode = createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                createIdentifierToken(CodeGeneratorConstants.GRAPHQL_SERVICE_TYPE_NAME), createToken(SEMICOLON_TOKEN));

        members.add(typeReferenceNode);

        List<Node> serviceTypeMethodDeclarations = generateServiceTypeMethodDeclarations(schema.getQueryType(), schema.getMutationType(),
                schema.getSubscriptionType());
        members.addAll(serviceTypeMethodDeclarations);

//        // manually written for checking
//        // method declarations
//        // declaration 1
//        NodeList<Token> methodQualifierList = createNodeList(createToken(RESOURCE_KEYWORD));
//        // "book" need to be fetched from the schema
//        NodeList<Node> relativeResourcePath = createNodeList(createIdentifierToken("book"));
//        // param 1
//        RequiredParameterNode methodSignatureRequiredParam1 = createRequiredParameterNode(createEmptyNodeList(),
//                createBuiltinSimpleNameReferenceNode(STRING_TYPE_DESC, createToken(STRING_KEYWORD)),
//                createIdentifierToken(CodeGeneratorConstants.ID));
//
//        // param 2
//        OptionalTypeDescriptorNode optionalParamTypeDescriptor2 = createOptionalTypeDescriptorNode(
//                createBuiltinSimpleNameReferenceNode(INT_TYPE_DESC, createToken(INT_KEYWORD)),
//                createToken(QUESTION_MARK_TOKEN));
//
//        RequiredParameterNode methodSignatureRequiredParam2 =
//                createRequiredParameterNode(createEmptyNodeList(), optionalParamTypeDescriptor2,
//                        createIdentifierToken("page"));
//
//        // concat 2 params
//        SeparatedNodeList<ParameterNode> methodSignatureRequiredParams =
//                createSeparatedNodeList(methodSignatureRequiredParam1, methodSignatureRequiredParam2);
//
//        // return type
//        ReturnTypeDescriptorNode methodSignatureReturnTypeDesc =
//                createReturnTypeDescriptorNode(createToken(RETURN_KEYWORD), createEmptyNodeList(),
//                        createOptionalTypeDescriptorNode(createSimpleNameReferenceNode(createIdentifierToken("Book")),
//                                createToken(QUESTION_MARK_TOKEN)));
//
//        // whole func signature
//        FunctionSignatureNode methodSignatureNode =
//                createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), methodSignatureRequiredParams,
//                        createToken(CLOSE_PAREN_TOKEN), methodSignatureReturnTypeDesc);
//
//        // whole func declaration
//        MethodDeclarationNode methodDeclarationNode =
//                createMethodDeclarationNode(RESOURCE_ACCESSOR_DECLARATION, null, methodQualifierList,
//                        createToken(FUNCTION_KEYWORD), createIdentifierToken(CodeGeneratorConstants.GET),
//                        relativeResourcePath, methodSignatureNode, createToken(SEMICOLON_TOKEN));
//
//        members.add(methodDeclarationNode);
//
//        // declaration 2
//        NodeList<Token> methodQualifierList2 = createNodeList(createToken(RESOURCE_KEYWORD));
//        NodeList<Node> relativeResourcePath2 = createNodeList(createIdentifierToken("books"));
//
//        ArrayDimensionNode arrayDimensionNode =
//                createArrayDimensionNode(createToken(OPEN_BRACKET_TOKEN), null, createToken(CLOSE_BRACKET_TOKEN));
//
//        NodeList<ArrayDimensionNode> arrayDimensionNodes = createNodeList(arrayDimensionNode);
//
//        ArrayTypeDescriptorNode arrayTypeDescriptorNode =
//                createArrayTypeDescriptorNode(createSimpleNameReferenceNode(createIdentifierToken("Book")),
//                        arrayDimensionNodes);
//
//        // return type
//        ReturnTypeDescriptorNode methodSignatureReturnTypeDesc2 =
//                createReturnTypeDescriptorNode(createToken(RETURN_KEYWORD), createEmptyNodeList(),
//                        createOptionalTypeDescriptorNode(arrayTypeDescriptorNode, createToken(QUESTION_MARK_TOKEN)));
//        // manually written part finished

        return createNodeList(members);

    }

    private List<Node> generateServiceTypeMethodDeclarations(GraphQLObjectType queryType,
                                                             GraphQLObjectType mutationType,
                                                             GraphQLObjectType subscriptionType) {
        List<Node> methodDeclarations = new ArrayList<>();

        for (GraphQLFieldDefinition fieldDefinition : queryType.getFieldDefinitions()) {
            NodeList<Token> methodQualifiers = createNodeList(createToken(RESOURCE_KEYWORD));

            NodeList<Node> methodRelativeResourcePaths =
                    createNodeList(createIdentifierToken(fieldDefinition.getDefinition().getName()));

            SeparatedNodeList<ParameterNode> methodSignatureRequiredParams =
                    generateMethodSignatureRequiredParams(fieldDefinition.getArguments());

            ReturnTypeDescriptorNode methodSignatureReturnTypeDescriptor =
                    generateMethodSignatureReturnTypeDescriptor(fieldDefinition.getType());

            // whole func signature
            FunctionSignatureNode methodSignatureNode =
                    createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), methodSignatureRequiredParams,
                            createToken(CLOSE_PAREN_TOKEN), methodSignatureReturnTypeDescriptor);

            // whole func declaration
            MethodDeclarationNode methodDeclaration =
                    createMethodDeclarationNode(RESOURCE_ACCESSOR_DECLARATION, null, methodQualifiers,
                            createToken(FUNCTION_KEYWORD), createIdentifierToken(CodeGeneratorConstants.GET),
                            methodRelativeResourcePaths, methodSignatureNode, createToken(SEMICOLON_TOKEN));

            methodDeclarations.add(methodDeclaration);
        }

        return methodDeclarations;
    }

    private ReturnTypeDescriptorNode generateMethodSignatureReturnTypeDescriptor(GraphQLOutputType type) {
        return createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                generateTypeDescriptor(type, false));
    }

    private SeparatedNodeList<ParameterNode> generateMethodSignatureRequiredParams(List<GraphQLArgument> arguments) {
        List<Node> requiredParams = new ArrayList<>();

        for (GraphQLArgument argument : arguments) {
            GraphQLInputType argumentType = argument.getType();

            TypeDescriptorNode argumentTypeDescriptor = generateTypeDescriptor(argumentType, false);

            RequiredParameterNode requiredParameterNode =
                    createRequiredParameterNode(createEmptyNodeList(), argumentTypeDescriptor,
                            createIdentifierToken(argument.getName()));
            requiredParams.add(requiredParameterNode);
        }

        return createSeparatedNodeList(requiredParams);
    }


    private TypeDescriptorNode generateTypeDescriptor(GraphQLSchemaElement argumentType, boolean nonNull) {
        if (argumentType instanceof GraphQLScalarType) {
            if (nonNull) {
                return createBuiltinSimpleNameReferenceNode(INT_TYPE_DESC, createToken(INT_KEYWORD));
            } else {
                return createOptionalTypeDescriptorNode(
                        createBuiltinSimpleNameReferenceNode(INT_TYPE_DESC, createToken(INT_KEYWORD)),
                        createToken(QUESTION_MARK_TOKEN));
            }
        }
        if (argumentType instanceof GraphQLObjectType) {
            if (nonNull) {
                return createSimpleNameReferenceNode(
                        createIdentifierToken(((GraphQLObjectType) argumentType).getName()));
            } else {
                return createOptionalTypeDescriptorNode(createSimpleNameReferenceNode(
                                createIdentifierToken(((GraphQLObjectType) argumentType).getName())),
                        createToken(QUESTION_MARK_TOKEN));
            }
        }

        if (argumentType instanceof GraphQLNonNull) {
            nonNull = true;
            List<GraphQLSchemaElement> argumentChildren = argumentType.getChildren();

            GraphQLSchemaElement insideArgumentType = argumentChildren.get(0);
            return generateTypeDescriptor(insideArgumentType, nonNull);

//            for (GraphQLSchemaElement argumentChild : argumentChildren) {
//                if (argumentChild instanceof GraphQLScalarType) {
//                    String argumentTypeName = ((GraphQLScalarType) argumentChild).getName();
//                    if (CodeGeneratorConstants.STRING_TYPE.equals(argumentTypeName)) {
//                        return createBuiltinSimpleNameReferenceNode(STRING_TYPE_DESC, createToken(STRING_KEYWORD));
//                    } else if (CodeGeneratorConstants.INT_TYPE.equals(argumentTypeName)) {
//                        return createBuiltinSimpleNameReferenceNode(INT_TYPE_DESC, createToken(INT_KEYWORD));
//                    } else if (CodeGeneratorConstants.FLOAT_TYPE.equals(argumentTypeName)) {
//                        return createBuiltinSimpleNameReferenceNode(FLOAT_TYPE_DESC, createToken(FLOAT_KEYWORD));
//                    } else if (CodeGeneratorConstants.BOOLEAN_TYPE.equals(argumentTypeName)) {
//                        return createBuiltinSimpleNameReferenceNode(BOOLEAN_TYPE_DESC, createToken(BOOLEAN_KEYWORD));
//                    }
//                }
//            }
//        } else if (argumentType instanceof GraphQLScalarType) {
//            String argumentTypeName = ((GraphQLScalarType) argumentType).getName();
//            if (CodeGeneratorConstants.STRING_TYPE.equals(argumentTypeName)) {
//                return createBuiltinSimpleNameReferenceNode(STRING_TYPE_DESC, createToken(STRING_KEYWORD));
//            } else if (CodeGeneratorConstants.INT_TYPE.equals(argumentTypeName)) {
//                return createBuiltinSimpleNameReferenceNode(INT_TYPE_DESC, createToken(INT_KEYWORD));
//            } else if (CodeGeneratorConstants.FLOAT_TYPE.equals(argumentTypeName)) {
//                return createBuiltinSimpleNameReferenceNode(FLOAT_TYPE_DESC, createToken(FLOAT_KEYWORD));
//            } else if (CodeGeneratorConstants.BOOLEAN_TYPE.equals(argumentTypeName)) {
//                return createBuiltinSimpleNameReferenceNode(BOOLEAN_TYPE_DESC, createToken(BOOLEAN_KEYWORD));
//            }
        }

        if (argumentType instanceof GraphQLList) {
            ArrayDimensionNode arrayDimensionNode =
                    createArrayDimensionNode(createToken(OPEN_BRACKET_TOKEN), null, createToken(CLOSE_BRACKET_TOKEN));

            NodeList<ArrayDimensionNode> arrayDimensionNodes = createNodeList(arrayDimensionNode);


            List<GraphQLSchemaElement> argumentChildren = argumentType.getChildren();
            GraphQLSchemaElement insideArgumentType = argumentChildren.get(0);

            TypeDescriptorNode insideArgumentTypeDescriptor = generateTypeDescriptor(insideArgumentType, nonNull);

            ArrayTypeDescriptorNode arrayTypeDescriptorNode =
                    createArrayTypeDescriptorNode(insideArgumentTypeDescriptor, arrayDimensionNodes);

            if (nonNull) {
                return arrayTypeDescriptorNode;
            } else {
                return createOptionalTypeDescriptorNode(arrayTypeDescriptorNode, createToken(QUESTION_MARK_TOKEN));
            }
        }
        return null;
    }

    private NodeList<ImportDeclarationNode> generateImports() {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForGraphQL =
                CodeGeneratorUtils.getImportDeclarationNode(CodeGeneratorConstants.BALLERINA,
                        CodeGeneratorConstants.GRAPHQL);
        imports.add(importForGraphQL);
        return createNodeList(imports);
    }

}
