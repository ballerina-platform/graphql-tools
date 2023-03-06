package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
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
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
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
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.graphql.Constants;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEnumDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEnumMemberNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createStreamTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createStreamTypeParamsNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;

/**
 * Generates the Ballerina syntax tree for the service types.
 */
public class ServiceTypesGenerator extends TypesGenerator {

    public static ServiceTypesGenerator serviceTypesGenerator = null;

    private String fileName;
    private boolean recordForced;

    public void setRecordForced(boolean recordForced) {
        this.recordForced = recordForced;
    }

    // TODO: stop using singleton
    public static ServiceTypesGenerator getInstance() {
        if (serviceTypesGenerator == null) {
            serviceTypesGenerator = new ServiceTypesGenerator();
        }
        return serviceTypesGenerator;
    }

    public String generateSrc(String fileName, GraphQLSchema schema) throws TypesGenerationException {
        try {
            this.fileName = fileName;
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
        addInputTypeDefinitions(schema, typeDefinitionNodes);

        if (recordForced) {
            addTypesRecordForced(schema, typeDefinitionNodes);
        } else {
            addServiceClassTypes(schema, typeDefinitionNodes);
        }

        addEnumTypeDefinitions(schema, typeDefinitionNodes);
        NodeList<ModuleMemberDeclarationNode> members = createNodeList(typeDefinitionNodes);
        ModulePartNode modulePartNode = createModulePartNode(imports, members, createToken(SyntaxKind.EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        SyntaxTree modifiedSyntaxTree = syntaxTree.modifyWith(modulePartNode);
        return modifiedSyntaxTree;
    }

    private void addEnumTypeDefinitions(GraphQLSchema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodes) {
        Iterator<Map.Entry<String, GraphQLNamedType>> typesIterator = schema.getTypeMap().entrySet().iterator();

        while (typesIterator.hasNext()) {
            Map.Entry<String, GraphQLNamedType> typeEntry = typesIterator.next();
            String key = typeEntry.getKey();
            GraphQLNamedType type = typeEntry.getValue();
            if (!key.startsWith("__") && type instanceof GraphQLEnumType) {
                GraphQLEnumType enumType = (GraphQLEnumType) type;
                typeDefinitionNodes.add(generateEnumType(enumType));
            }
        }
    }

    private ModuleMemberDeclarationNode generateEnumType(GraphQLEnumType enumType) {
        List<Node> enumMembers = new ArrayList<>();
        List<GraphQLEnumValueDefinition> enumValues = enumType.getValues();
        for (int i = 0; i < enumValues.size(); i++) {
            GraphQLEnumValueDefinition enumValue = enumValues.get(i);
            EnumMemberNode enumMember =
                    createEnumMemberNode(null, createIdentifierToken(enumValue.getName()), null, null);
            if (i == enumValues.size() - 1) {
                enumMembers.add(enumMember);
            } else {
                enumMembers.add(enumMember);
                enumMembers.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }
        SeparatedNodeList<Node> enumMemberNodes = createSeparatedNodeList(enumMembers);
        return createEnumDeclarationNode(null, null, createToken(SyntaxKind.ENUM_KEYWORD),
                createIdentifierToken(enumType.getName()), createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                enumMemberNodes, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }


    private void addInputTypeDefinitions(GraphQLSchema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodes) {
        Iterator<Map.Entry<String, GraphQLNamedType>> typesIterator = schema.getTypeMap().entrySet().iterator();

        while (typesIterator.hasNext()) {
            Map.Entry<String, GraphQLNamedType> typeEntry = typesIterator.next();
            String key = typeEntry.getKey();
            GraphQLNamedType type = typeEntry.getValue();
            if (type instanceof GraphQLInputObjectType) {
                GraphQLInputObjectType inputObjectType = (GraphQLInputObjectType) type;
                typeDefinitionNodes.add(generateRecordType(inputObjectType));
            }
        }

    }

    private ModuleMemberDeclarationNode generateRecordType(GraphQLInputObjectType type) {
        List<GraphQLInputObjectField> typeFields = type.getFields();

        NodeList<Node> recordTypeFields = generateRecordTypeFieldsForGraphQLInputObjectFields(typeFields);
        RecordTypeDescriptorNode recordTypeDescriptor =
                createRecordTypeDescriptorNode(createToken(SyntaxKind.RECORD_KEYWORD),
                        createToken(SyntaxKind.OPEN_BRACE_PIPE_TOKEN), recordTypeFields, null,
                        createToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN));
        TypeDefinitionNode typeDefinition = createTypeDefinitionNode(null, null, createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(type.getName()), recordTypeDescriptor, createToken(SyntaxKind.SEMICOLON_TOKEN));
        return typeDefinition;
    }

    private ModuleMemberDeclarationNode generateRecordType(GraphQLObjectType type) {
        List<GraphQLFieldDefinition> typeFields = type.getFields();
        NodeList<Node> recordTypeFields = generateRecordTypeFieldsForGraphQLFieldDefinitions(typeFields);

        RecordTypeDescriptorNode recordTypeDescriptor =
                createRecordTypeDescriptorNode(createToken(SyntaxKind.RECORD_KEYWORD),
                        createToken(SyntaxKind.OPEN_BRACE_TOKEN), recordTypeFields, null,
                        createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        TypeDefinitionNode typeDefinition = createTypeDefinitionNode(null, null, createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(type.getName()), recordTypeDescriptor, createToken(SyntaxKind.SEMICOLON_TOKEN));
        return typeDefinition;
    }

    private List<GraphQLInputObjectField> convertToGraphQLInputObjectFields(
            List<GraphQLSchemaElement> fieldDefinitions) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        for (GraphQLSchemaElement fieldDefinition : fieldDefinitions) {
            if (fieldDefinition instanceof GraphQLInputObjectField) {
                inputObjectFields.add((GraphQLInputObjectField) fieldDefinition);
            }
        }
        return inputObjectFields;
    }

    private List<GraphQLFieldDefinition> convertToGraphQLFieldDefinitions(List<GraphQLSchemaElement> fieldDefinitions) {
        List<GraphQLFieldDefinition> inputObjectFields = new ArrayList<>();
        for (GraphQLSchemaElement fieldDefinition : fieldDefinitions) {
            if (fieldDefinition instanceof GraphQLFieldDefinition) {
                inputObjectFields.add((GraphQLFieldDefinition) fieldDefinition);
            }
        }
        return inputObjectFields;
    }

    private NodeList<Node> generateRecordTypeFieldsForGraphQLInputObjectFields(
            List<GraphQLInputObjectField> inputTypeFields) {
        List<Node> fields = new ArrayList<>();
        for (GraphQLInputObjectField field : inputTypeFields) {
            fields.add(createRecordFieldNode(null, null, generateTypeDescriptor(field.getType()),
                    createIdentifierToken(field.getName()), null, createToken(SyntaxKind.SEMICOLON_TOKEN)));
        }
        return createNodeList(fields);
    }

    private NodeList<Node> generateRecordTypeFieldsForGraphQLFieldDefinitions(
            List<GraphQLFieldDefinition> typeInputFields) {
        List<Node> fields = new ArrayList<>();
        for (GraphQLFieldDefinition field : typeInputFields) {
            fields.add(createRecordFieldNode(null, null, generateTypeDescriptor(field.getType()),
                    createIdentifierToken(field.getName()), null, createToken(SyntaxKind.SEMICOLON_TOKEN)));
        }
        return createNodeList(fields);
    }


    private NodeList<Node> generateRecordTypeFields(List<GraphQLFieldDefinition> typeFields) {
        List<Node> fields = new ArrayList<>();
//        for (GraphQLFieldDefinition field : typeInputFields) {
//            fields.add(createRecordFieldNode(null, null, generateTypeDescriptor(field.getType()),
//                    createIdentifierToken(field.getName()), null,
//                    createToken(SyntaxKind.SEMICOLON_TOKEN)));
//        }
        return createNodeList(fields);
    }

//    private NodeList<Node> generateRecordTypeFields(List<GraphQLSchemaElement> typeInputFields) {
//        List<Node> fields = new ArrayList<>();
//        for (GraphQLSchemaElement field : typeInputFields) {
//            if (field instanceof GraphQLNamedSchemaElement) {
//
//                GraphQLNamedSchemaElement fieldNamedSchemaElement = (GraphQLNamedSchemaElement) field;
////                fieldNamedSchemaElement.getName()
//                createRecordFieldNode(null, null, generateTypeDescriptor(fieldNamedSchemaElement.getType()),
//                        createIdentifierToken(fieldNamedSchemaElement.getName()), null,
//                        createToken(SyntaxKind.SEMICOLON_TOKEN));
//            } else {
//                // TODO: handle this by exception
//            }
//        }
//        return null;
//    }

    private void addTypes(GraphQLSchema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodes) {


    }

    private void addTypesRecordForced(GraphQLSchema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodes) {
        Iterator<Map.Entry<String, GraphQLNamedType>> typesIterator = schema.getTypeMap().entrySet().iterator();

        while (typesIterator.hasNext()) {
            Map.Entry<String, GraphQLNamedType> typeEntry = typesIterator.next();
            String key = typeEntry.getKey();
            GraphQLNamedType type = typeEntry.getValue();

            if (!key.startsWith("__") && !CodeGeneratorConstants.QUERY.equals(key) &&
                    !CodeGeneratorConstants.MUTATION.equals(key) && !CodeGeneratorConstants.SUBSCRIPTION.equals(key) &&
                    type instanceof GraphQLObjectType) {
                GraphQLObjectType objectType = (GraphQLObjectType) type;
                if (canRepresentWithRecord(objectType)) {
                    typeDefinitionNodes.add(generateRecordType(objectType));
                } else {
                    typeDefinitionNodes.add(generateServiceClassType(type));
                }
            }
        }
    }

    private boolean canRepresentWithRecord(GraphQLObjectType type) {
        List<GraphQLFieldDefinition> fields = type.getFields();
        for (GraphQLFieldDefinition field : fields) {
            if (field.getArguments().size() > 0) {
                return false;
            }
        }
        return true;
    }

    private void addServiceClassTypes(GraphQLSchema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodes) {
        Iterator<Map.Entry<String, GraphQLNamedType>> typesIterator = schema.getTypeMap().entrySet().iterator();

        while (typesIterator.hasNext()) {
            Map.Entry<String, GraphQLNamedType> typeEntry = typesIterator.next();
            String key = typeEntry.getKey();
            GraphQLNamedType type = typeEntry.getValue();

            // TODO: generate record type for input type
            if (!key.startsWith("__") && !CodeGeneratorConstants.QUERY.equals(key) &&
                    !CodeGeneratorConstants.MUTATION.equals(key) && !CodeGeneratorConstants.SUBSCRIPTION.equals(key) &&
                    type instanceof GraphQLObjectType) {
                typeDefinitionNodes.add(generateServiceClassType(type));
            }
        }
    }

    private ClassDefinitionNode generateServiceClassType(GraphQLNamedType type) {
        NodeList<Token> classTypeQualifiers = createNodeList(createToken(SyntaxKind.SERVICE_KEYWORD));
        IdentifierToken className = createIdentifierToken(type.getName());
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();

        for (GraphQLSchemaElement fieldDefinition : type.getChildren()) {
            if (fieldDefinition instanceof GraphQLFieldDefinition) {
                fieldDefinitions.add((GraphQLFieldDefinition) fieldDefinition);
            }
        }

        NodeList<Node> serviceClassTypeMembers = generateServiceClassTypeMembers(fieldDefinitions);

        ClassDefinitionNode classDefinition =
                createClassDefinitionNode(null, null, classTypeQualifiers, createToken(SyntaxKind.CLASS_KEYWORD),
                        className, createToken(SyntaxKind.OPEN_BRACE_TOKEN), serviceClassTypeMembers,
                        createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

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
        NodeList<Token> memberQualifiers = createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD));

        NodeList<Node> memberRelativeResourcePaths = createNodeList(createIdentifierToken(fieldDefinition.getName()));

        SeparatedNodeList<ParameterNode> methodSignatureParameters =
                generateMethodSignatureRequiredParams(fieldDefinition.getArguments());

        ReturnTypeDescriptorNode methodSignatureReturnTypeDescriptor =
                generateMethodSignatureReturnTypeDescriptor(fieldDefinition.getType());

        FunctionSignatureNode functionSignature =
                createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN), methodSignatureParameters,
                        createToken(SyntaxKind.CLOSE_PAREN_TOKEN), methodSignatureReturnTypeDescriptor);

        FunctionBodyBlockNode functionBody =
                createFunctionBodyBlockNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN), null, createEmptyNodeList(),
                        createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        FunctionDefinitionNode functionDefinition =
                createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null, memberQualifiers,
                        createToken(SyntaxKind.FUNCTION_KEYWORD), createIdentifierToken(CodeGeneratorConstants.GET),
                        memberRelativeResourcePaths, functionSignature, functionBody);

        return functionDefinition;
    }

    private void addServiceObjectTypeDefinitionNode(GraphQLSchema schema,
                                                    List<ModuleMemberDeclarationNode> typeDefinitionNodes) {
        IdentifierToken tokenTypeName = createIdentifierToken(this.fileName);

        NodeList<Token> objectTypeQualifiers = createNodeList(createToken(SyntaxKind.SERVICE_KEYWORD));

        NodeList<Node> serviceObjectTypeMembers = generateServiceObjectTypeMembers(schema);

        ObjectTypeDescriptorNode serviceObjectTypeDescriptor =
                createObjectTypeDescriptorNode(objectTypeQualifiers, createToken(SyntaxKind.OBJECT_KEYWORD),
                        createToken(SyntaxKind.OPEN_BRACE_TOKEN), serviceObjectTypeMembers,
                        createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        TypeDefinitionNode serviceObjectTypeDefinition =
                createTypeDefinitionNode(null, null, createToken(SyntaxKind.TYPE_KEYWORD), tokenTypeName,
                        serviceObjectTypeDescriptor, createToken(SyntaxKind.SEMICOLON_TOKEN));

        typeDefinitionNodes.add(serviceObjectTypeDefinition);
    }

    private NodeList<Node> generateServiceObjectTypeMembers(GraphQLSchema schema) {
        List<Node> members = new ArrayList<>();

        TypeReferenceNode typeReferenceNode = createTypeReferenceNode(createToken(SyntaxKind.ASTERISK_TOKEN),
                createIdentifierToken(CodeGeneratorConstants.GRAPHQL_SERVICE_TYPE_NAME),
                createToken(SyntaxKind.SEMICOLON_TOKEN));

        members.add(typeReferenceNode);

        List<Node> serviceTypeMethodDeclarations =
                generateServiceTypeMethodDeclarations(schema.getQueryType(), schema.getMutationType(),
                        schema.getSubscriptionType());
        members.addAll(serviceTypeMethodDeclarations);

        return createNodeList(members);

    }

    private List<Node> generateServiceTypeMethodDeclarations(GraphQLObjectType queryType,
                                                             GraphQLObjectType mutationType,
                                                             GraphQLObjectType subscriptionType) {
        List<Node> methodDeclarations = new ArrayList<>();

        for (GraphQLFieldDefinition fieldDefinition : queryType.getFieldDefinitions()) {
            NodeList<Token> methodQualifiers = createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD));

            NodeList<Node> methodRelativeResourcePaths =
                    createNodeList(createIdentifierToken(fieldDefinition.getDefinition().getName()));

            SeparatedNodeList<ParameterNode> methodSignatureRequiredParams =
                    generateMethodSignatureRequiredParams(fieldDefinition.getArguments());

            ReturnTypeDescriptorNode methodSignatureReturnTypeDescriptor =
                    generateMethodSignatureReturnTypeDescriptor(fieldDefinition.getType());

            FunctionSignatureNode methodSignatureNode =
                    createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN), methodSignatureRequiredParams,
                            createToken(SyntaxKind.CLOSE_PAREN_TOKEN), methodSignatureReturnTypeDescriptor);

            MethodDeclarationNode methodDeclaration =
                    createMethodDeclarationNode(SyntaxKind.METHOD_DECLARATION, null, methodQualifiers,
                            createToken(SyntaxKind.FUNCTION_KEYWORD), createIdentifierToken(CodeGeneratorConstants.GET),
                            methodRelativeResourcePaths, methodSignatureNode, createToken(SyntaxKind.SEMICOLON_TOKEN));

            methodDeclarations.add(methodDeclaration);
        }

        if (mutationType != null) {
            for (GraphQLFieldDefinition fieldDefinition : mutationType.getFieldDefinitions()) {
                NodeList<Token> methodQualifiers = createNodeList(createToken(SyntaxKind.REMOTE_KEYWORD));
                NodeList<Node> methodRelativeResourcePaths =
                        createNodeList(createIdentifierToken(fieldDefinition.getDefinition().getName()));

                SeparatedNodeList<ParameterNode> methodSignatureRequiredParams =
                        generateMethodSignatureRequiredParams(fieldDefinition.getArguments());
                ReturnTypeDescriptorNode methodSignatureReturnTypeDescriptor =
                        generateMethodSignatureReturnTypeDescriptor(fieldDefinition.getType());
                FunctionSignatureNode methodSignatureNode =
                        createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                                methodSignatureRequiredParams, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                                methodSignatureReturnTypeDescriptor);

                MethodDeclarationNode methodDeclaration =
                        createMethodDeclarationNode(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION, null, methodQualifiers,
                                createToken(SyntaxKind.FUNCTION_KEYWORD), createIdentifierToken(""),
                                methodRelativeResourcePaths, methodSignatureNode,
                                createToken(SyntaxKind.SEMICOLON_TOKEN));

                methodDeclarations.add(methodDeclaration);
            }
        }

        if (subscriptionType != null) {
            for (GraphQLFieldDefinition fieldDefinition : subscriptionType.getFieldDefinitions()) {
                NodeList<Token> methodQualifiers = createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD));
                NodeList<Node> methodRelativeResourcePaths =
                        createNodeList(createIdentifierToken(fieldDefinition.getDefinition().getName()));

                SeparatedNodeList<ParameterNode> methodSignatureRequiredParams =
                        generateMethodSignatureRequiredParams(fieldDefinition.getArguments());
                ReturnTypeDescriptorNode methodSignatureReturnTypeDescriptor =
                        generateMethodSignatureReturnTypeDescriptor(fieldDefinition.getType(), true);
                FunctionSignatureNode methodSignatureNode =
                        createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                                methodSignatureRequiredParams, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                                methodSignatureReturnTypeDescriptor);

                MethodDeclarationNode methodDeclaration =
                        createMethodDeclarationNode(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION, null, methodQualifiers,
                                createToken(SyntaxKind.FUNCTION_KEYWORD),
                                createIdentifierToken(CodeGeneratorConstants.SUBSCRIBE), methodRelativeResourcePaths,
                                methodSignatureNode, createToken(SyntaxKind.SEMICOLON_TOKEN));

                methodDeclarations.add(methodDeclaration);
            }
        }

        return methodDeclarations;
    }

    private ReturnTypeDescriptorNode generateMethodSignatureReturnTypeDescriptor(GraphQLOutputType type,
                                                                                 boolean isStream) {
        TypeDescriptorNode typeDescriptor = generateTypeDescriptor(type, false);
        if (isStream) {
            return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createStreamTypeDescriptorNode(createToken(SyntaxKind.STREAM_KEYWORD),
                            createStreamTypeParamsNode(createToken(SyntaxKind.LT_TOKEN), typeDescriptor, null, null,
                                    createToken(SyntaxKind.GT_TOKEN))));
        } else {

            return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    typeDescriptor);
        }
    }

    private ReturnTypeDescriptorNode generateMethodSignatureReturnTypeDescriptor(GraphQLOutputType type) {
        return generateMethodSignatureReturnTypeDescriptor(type, false);
    }

    private SeparatedNodeList<ParameterNode> generateMethodSignatureRequiredParams(List<GraphQLArgument> arguments) {
        List<Node> requiredParams = new ArrayList<>();

        for (int i = 0; i < arguments.size(); i++) {
            GraphQLArgument argument = arguments.get(i);

            GraphQLInputType argumentType = argument.getType();

            TypeDescriptorNode argumentTypeDescriptor = generateTypeDescriptor(argumentType, false);

            RequiredParameterNode requiredParameterNode =
                    createRequiredParameterNode(createEmptyNodeList(), argumentTypeDescriptor,
                            createIdentifierToken(argument.getName()));

            requiredParams.add(requiredParameterNode);

            if (i != arguments.size() - 1) {
                requiredParams.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }

        return createSeparatedNodeList(requiredParams);
    }


    private TypeDescriptorNode generateTypeDescriptor(GraphQLSchemaElement argumentType, boolean nonNull) {
        if (argumentType instanceof GraphQLScalarType) {
            String argumentTypeName = ((GraphQLScalarType) argumentType).getName();

            if (nonNull) {
                return createBuiltinSimpleNameReferenceNode(getTypeDescFor(argumentTypeName),
                        createToken(getTypeKeywordFor(argumentTypeName)));
            } else {
                return createOptionalTypeDescriptorNode(
                        createBuiltinSimpleNameReferenceNode(getTypeDescFor(argumentTypeName),
                                createToken(getTypeKeywordFor(argumentTypeName))),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        }
        if (argumentType instanceof GraphQLObjectType) {
            if (nonNull) {
                return createSimpleNameReferenceNode(
                        createIdentifierToken(((GraphQLObjectType) argumentType).getName()));
            } else {
                return createOptionalTypeDescriptorNode(createSimpleNameReferenceNode(
                                createIdentifierToken(((GraphQLObjectType) argumentType).getName())),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        }
        if (argumentType instanceof GraphQLInputObjectType) {
            if (nonNull) {
                return createSimpleNameReferenceNode(
                        createIdentifierToken(((GraphQLInputObjectType) argumentType).getName()));
            } else {
                return createOptionalTypeDescriptorNode(createSimpleNameReferenceNode(
                                createIdentifierToken(((GraphQLInputObjectType) argumentType).getName())),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        }

        if (argumentType instanceof GraphQLNonNull) {
            nonNull = true;
            List<GraphQLSchemaElement> argumentChildren = argumentType.getChildren();

            GraphQLSchemaElement insideArgumentType = argumentChildren.get(0);
            return generateTypeDescriptor(insideArgumentType, nonNull);
        }

        if (argumentType instanceof GraphQLList) {
            ArrayDimensionNode arrayDimensionNode =
                    createArrayDimensionNode(createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                            createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            NodeList<ArrayDimensionNode> arrayDimensionNodes = createNodeList(arrayDimensionNode);

            List<GraphQLSchemaElement> argumentChildren = argumentType.getChildren();
            GraphQLSchemaElement insideArgumentType = argumentChildren.get(0);

            TypeDescriptorNode insideArgumentTypeDescriptor = generateTypeDescriptor(insideArgumentType, nonNull);

            ArrayTypeDescriptorNode arrayTypeDescriptorNode =
                    createArrayTypeDescriptorNode(insideArgumentTypeDescriptor, arrayDimensionNodes);

            if (nonNull) {
                return arrayTypeDescriptorNode;
            } else {
                return createOptionalTypeDescriptorNode(arrayTypeDescriptorNode,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        }
        return null;
    }

    private TypeDescriptorNode generateTypeDescriptor(GraphQLSchemaElement argumentType) {
        return generateTypeDescriptor(argumentType, false);
    }

    private SyntaxKind getTypeKeywordFor(String argumentTypeName) {
        // TODO: Handle exceptions
        if (Constants.GRAPHQL_STRING_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.STRING_KEYWORD;
        } else if (Constants.GRAPHQL_INT_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.INT_KEYWORD;
        } else if (Constants.GRAPHQL_FLOAT_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.FLOAT_KEYWORD;
        } else if (Constants.GRAPHQL_BOOLEAN_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.BOOLEAN_KEYWORD;
        } else if (Constants.GRAPHQL_ID_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.STRING_KEYWORD;
        } else {
            return SyntaxKind.STREAM_KEYWORD;
        }
    }

    private SyntaxKind getTypeDescFor(String argumentTypeName) {
        // TODO: Handle exceptions
        if (Constants.GRAPHQL_STRING_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.STRING_TYPE_DESC;
        } else if (Constants.GRAPHQL_INT_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.INT_TYPE_DESC;
        } else if (Constants.GRAPHQL_FLOAT_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.FLOAT_TYPE_DESC;
        } else if (Constants.GRAPHQL_BOOLEAN_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.BOOLEAN_TYPE_DESC;
        } else if (Constants.GRAPHQL_ID_TYPE.equals(argumentTypeName)) {
            return SyntaxKind.STRING_TYPE_DESC;
        } else {
            return SyntaxKind.STRING_TYPE_DESC;
        }
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
