package io.ballerina.graphql.generator.service.generator;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLUnionType;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
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
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.client.generator.ballerina.TypesGenerator;
import io.ballerina.graphql.generator.client.generator.graphql.Constants;
import io.ballerina.graphql.generator.service.exception.ServiceTypesGenerationException;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEndOfLineMinutiae;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createWhitespaceMinutiae;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayDimensionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDistinctTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEnumDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEnumMemberNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createListConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownParameterDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createStreamTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createStreamTypeParamsNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;

/**
 * Generates the Ballerina syntax tree for the service types.
 */
public class ServiceTypesGenerator extends TypesGenerator {

    private String fileName;
    private boolean useRecordsForObjects;
    private HashMap<GraphQLObjectType, Boolean> canRecordFromObject;

    private List<ModuleMemberDeclarationNode> moduleMembers;
    private List<ModuleMemberDeclarationNode> inputObjectTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> interfaceTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> enumTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> unionTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> objectTypesModuleMembers;

    public ServiceTypesGenerator() {
        this.moduleMembers = new LinkedList<>();
        this.inputObjectTypesModuleMembers = new ArrayList<>();
        this.interfaceTypesModuleMembers = new ArrayList<>();
        this.enumTypesModuleMembers = new ArrayList<>();
        this.unionTypesModuleMembers = new ArrayList<>();
        this.objectTypesModuleMembers = new ArrayList<>();
    }

    public void setUseRecordsForObjects(boolean useRecordsForObjects) {
        this.useRecordsForObjects = useRecordsForObjects;
    }

    public String generateSrc(GraphQLSchema schema) throws ServiceTypesGenerationException {
        try {
            String generatedSyntaxTree = Formatter.format(this.generateSyntaxTree(schema)).toString();
            return Formatter.format(generatedSyntaxTree);
        } catch (FormatterException e) {
            throw new ServiceTypesGenerationException(e.getMessage());
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public SyntaxTree generateSyntaxTree(GraphQLSchema schema) throws ServiceTypesGenerationException {
        NodeList<ImportDeclarationNode> imports = CodeGeneratorUtils.generateImports();
        addServiceType(schema);
        addTypeDefinitions(schema);

        NodeList<ModuleMemberDeclarationNode> moduleMemberNodes = createNodeList(moduleMembers);
        ModulePartNode modulePartNode =
                createModulePartNode(imports, moduleMemberNodes, createToken(SyntaxKind.EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private void addTypeDefinitions(GraphQLSchema schema) throws ServiceTypesGenerationException {
        this.canRecordFromObject = new LinkedHashMap<>();
        for (Map.Entry<String, GraphQLNamedType> typeEntry : schema.getTypeMap().entrySet()) {
            String key = typeEntry.getKey();
            GraphQLNamedType type = typeEntry.getValue();
            if (type instanceof GraphQLInputObjectType) {
                GraphQLInputObjectType inputObjectType = (GraphQLInputObjectType) type;
                inputObjectTypesModuleMembers.add(generateRecordType(inputObjectType));
            } else if (!key.startsWith(CodeGeneratorConstants.DOUBLE_UNDERSCORE)) {
                if (type instanceof GraphQLInterfaceType) {
                    GraphQLInterfaceType interfaceType = (GraphQLInterfaceType) type;
                    interfaceTypesModuleMembers.add(generateInterfaceType(interfaceType));
                } else if (type instanceof GraphQLEnumType) {
                    GraphQLEnumType enumType = (GraphQLEnumType) type;
                    enumTypesModuleMembers.add(generateEnumType(enumType));
                } else if (type instanceof GraphQLUnionType) {
                    GraphQLUnionType unionType = (GraphQLUnionType) type;
                    addUnionSubObjectTypesToMap(unionType);
                    unionTypesModuleMembers.add(generateUnionType(unionType));
                } else if (type instanceof GraphQLObjectType) {
                    GraphQLObjectType objectType = (GraphQLObjectType) type;
                    addObjectTypeToMap(objectType, key);
                }
            }
        }
        populateObjectTypesModuleMembers();

        moduleMembers.addAll(inputObjectTypesModuleMembers);
        moduleMembers.addAll(interfaceTypesModuleMembers);
        moduleMembers.addAll(enumTypesModuleMembers);
        moduleMembers.addAll(unionTypesModuleMembers);
        moduleMembers.addAll(objectTypesModuleMembers);
    }

    private void populateObjectTypesModuleMembers() throws ServiceTypesGenerationException {
        for (Map.Entry<GraphQLObjectType, Boolean> canRecordFromObjectItem : canRecordFromObject.entrySet()) {
            GraphQLObjectType nextObjectType = canRecordFromObjectItem.getKey();
            Boolean isPossible = canRecordFromObjectItem.getValue();
            if (isPossible && useRecordsForObjects) {
                objectTypesModuleMembers.add(generateRecordType(nextObjectType));
            } else {
                objectTypesModuleMembers.add(generateServiceClassType(nextObjectType));
            }
        }
    }

    private void addObjectTypeToMap(GraphQLObjectType objectType, String key) {
        if (!CodeGeneratorConstants.QUERY.equals(key) && !CodeGeneratorConstants.MUTATION.equals(key) &&
                !CodeGeneratorConstants.SUBSCRIPTION.equals(key)) {
            canRecordFromObject.putIfAbsent(objectType, true);
            if (hasFieldsWithInputs(objectType) || objectType.getInterfaces().size() > 0) {
                canRecordFromObject.replace(objectType, false);
            }
        }
    }

    private void addUnionSubObjectTypesToMap(GraphQLUnionType unionType) throws ServiceTypesGenerationException {
        for (GraphQLNamedOutputType subType : unionType.getTypes()) {
            if (subType instanceof GraphQLObjectType) {
                GraphQLObjectType subObjectType = (GraphQLObjectType) subType;
                canRecordFromObject.put(subObjectType, false);
            } else {
                throw new ServiceTypesGenerationException(
                        String.format(Constants.NOT_ALLOWED_UNION_SUB_TYPE, subType.getName()));
            }
        }
    }

    private ModuleMemberDeclarationNode generateInterfaceType(GraphQLInterfaceType interfaceType)
            throws ServiceTypesGenerationException {
        ObjectTypeDescriptorNode interfaceObjectType =
                createObjectTypeDescriptorNode(createNodeList(createToken(SyntaxKind.SERVICE_KEYWORD)),
                        createToken(SyntaxKind.OBJECT_KEYWORD), createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                        generateInterfaceTypeDescriptorMembers(interfaceType.getInterfaces(),
                                interfaceType.getFields()), createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        DistinctTypeDescriptorNode distinctInterfaceObjectType =
                createDistinctTypeDescriptorNode(createToken(SyntaxKind.DISTINCT_KEYWORD),
                        interfaceObjectType);
        return createTypeDefinitionNode(generateMetadataForDescription(interfaceType.getDescription()),
                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(interfaceType.getName()), distinctInterfaceObjectType,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
    }

    private NodeList<Node> generateInterfaceTypeDescriptorMembers(List<GraphQLNamedOutputType> interfaces,
                                                                  List<GraphQLFieldDefinition> fields)
            throws ServiceTypesGenerationException {
        List<Node> members = new LinkedList<>();
        List<GraphQLNamedOutputType> filteredInterfaces = getInterfacesToBeWritten(interfaces);
        List<GraphQLFieldDefinition> filteredFields = getFieldsToBeWritten(fields, filteredInterfaces);

        for (GraphQLNamedOutputType filteredInterface : filteredInterfaces) {
            TypeReferenceNode filteredInterfaceReference =
                    createTypeReferenceNode(createToken(SyntaxKind.ASTERISK_TOKEN),
                            createSimpleNameReferenceNode(createIdentifierToken(filteredInterface.getName())),
                            createToken(SyntaxKind.SEMICOLON_TOKEN));
            members.add(filteredInterfaceReference);
        }
        for (GraphQLFieldDefinition field : filteredFields) {
            NodeList<Token> resourceQualifier = createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD));
            NodeList<Node> fieldName =
                    createNodeList(createIdentifierToken(field.getDefinition().getName()));

            FunctionSignatureNode methodSignatureNode =
                    createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                            generateMethodSignatureParams(field.getArguments()),
                            createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                            generateMethodSignatureReturnType(field.getType()));
            MethodDeclarationNode methodDeclaration =
                    createMethodDeclarationNode(SyntaxKind.METHOD_DECLARATION,
                            generateMetadata(field.getDescription(), field.getArguments(),
                                    field.isDeprecated(), field.getDeprecationReason()),
                            resourceQualifier, createToken(SyntaxKind.FUNCTION_KEYWORD),
                            createIdentifierToken(CodeGeneratorConstants.GET), fieldName,
                            methodSignatureNode, createToken(SyntaxKind.SEMICOLON_TOKEN));
            members.add(methodDeclaration);
        }
        return createNodeList(members);
    }

    private List<GraphQLFieldDefinition> getFieldsToBeWritten(List<GraphQLFieldDefinition> allFields,
                                                              List<GraphQLNamedOutputType> filteredInterfaces) {
        HashSet<String> fieldNames = new HashSet<>();
        for (GraphQLFieldDefinition field : allFields) {
            fieldNames.add(field.getName());
        }
        for (GraphQLNamedOutputType interfaceType : filteredInterfaces) {
            if (interfaceType instanceof GraphQLInterfaceType) {
                GraphQLInterfaceType graphQLInterfaceType = (GraphQLInterfaceType) interfaceType;
                for (GraphQLFieldDefinition field : graphQLInterfaceType.getFields()) {
                    if (fieldNames.contains(field.getName())) {
                        fieldNames.remove(field.getName());
                    }
                }
            }
        }
        List<GraphQLFieldDefinition> filteredFields = new ArrayList<>();
        for (GraphQLFieldDefinition field : allFields) {
            if (fieldNames.contains(field.getName())) {
                filteredFields.add(field);
            }
        }
        return filteredFields;
    }


    private List<GraphQLNamedOutputType> getInterfacesToBeWritten(List<GraphQLNamedOutputType> interfaceTypes) {
        HashSet<String> interfaceTypeNames = new HashSet<>();
        for (GraphQLNamedOutputType interfaceType : interfaceTypes) {
            interfaceTypeNames.add(interfaceType.getName());
        }
        for (GraphQLNamedOutputType interfaceType : interfaceTypes) {
            if (interfaceType instanceof GraphQLInterfaceType) {
                GraphQLInterfaceType graphQLInterfaceType = (GraphQLInterfaceType) interfaceType;
                for (GraphQLNamedOutputType subInterface : graphQLInterfaceType.getInterfaces()) {
                    if (interfaceTypeNames.contains(subInterface.getName())) {
                        interfaceTypeNames.remove(subInterface.getName());
                    }
                }
            }
        }
        List<GraphQLNamedOutputType> filteredInterfaces = new ArrayList<>();
        for (GraphQLNamedOutputType interfaceType : interfaceTypes) {
            if (interfaceTypeNames.contains(interfaceType.getName())) {
                filteredInterfaces.add(interfaceType);
            }
        }
        return filteredInterfaces;
    }

    private ModuleMemberDeclarationNode generateUnionType(GraphQLUnionType unionType) {
        TypeDescriptorNode unionTypeDescriptorNode = generateUnionTypeDescriptorNode(unionType.getTypes());
        return createTypeDefinitionNode(generateMetadataForDescription(unionType.getDescription()),
                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(unionType.getName()), unionTypeDescriptorNode,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
    }

    private TypeDescriptorNode generateUnionTypeDescriptorNode(List<GraphQLNamedOutputType> types) {
        if (types.size() == 1) {
            String typeName = types.get(0).getName();
            return createSimpleNameReferenceNode(createIdentifierToken(typeName));
        }
        List<GraphQLNamedOutputType> typesSubList = types.subList(0, types.size() - 1);
        String lastTypeName = types.get(types.size() - 1).getName();
        return createUnionTypeDescriptorNode(generateUnionTypeDescriptorNode(typesSubList),
                createToken(SyntaxKind.PIPE_TOKEN), createSimpleNameReferenceNode(createIdentifierToken(lastTypeName)));
    }

    private ModuleMemberDeclarationNode generateEnumType(GraphQLEnumType enumType) {
        List<Node> enumMembers = new ArrayList<>();
        List<GraphQLEnumValueDefinition> enumValues = enumType.getValues();
        for (int i = 0; i < enumValues.size(); i++) {
            GraphQLEnumValueDefinition enumValue = enumValues.get(i);
            EnumMemberNode enumMember = createEnumMemberNode(generateMetadata(enumValue.getDescription(), null,
                            enumValue.isDeprecated(), enumValue.getDeprecationReason()),
                    createIdentifierToken(enumValue.getName()), null, null);
            enumMembers.add(enumMember);
            if (i != enumValues.size() - 1) {
                enumMembers.add(createToken(SyntaxKind.COMMA_TOKEN, createEmptyMinutiaeList(),
                        createMinutiaeList(createEndOfLineMinutiae(CodeGeneratorConstants.NEW_LINE))));
            }
        }
        SeparatedNodeList<Node> enumMemberNodes = createSeparatedNodeList(enumMembers);
        return createEnumDeclarationNode(generateMetadataForDescription(enumType.getDescription()),
                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.ENUM_KEYWORD),
                createIdentifierToken(enumType.getName()), createToken(SyntaxKind.OPEN_BRACE_TOKEN), enumMemberNodes,
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }

    private ModuleMemberDeclarationNode generateRecordType(GraphQLInputObjectType type)
            throws ServiceTypesGenerationException {
        List<GraphQLInputObjectField> typeFields = type.getFields();
        NodeList<Node> recordTypeFields = generateRecordTypeFieldsForGraphQLInputObjectFields(typeFields);
        RecordTypeDescriptorNode recordType =
                createRecordTypeDescriptorNode(createToken(SyntaxKind.RECORD_KEYWORD),
                        createToken(SyntaxKind.OPEN_BRACE_PIPE_TOKEN), recordTypeFields, null,
                        createToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN));
        return createTypeDefinitionNode(generateMetadataForDescription(type.getDescription()),
                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(type.getName()), recordType,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
    }

    private ModuleMemberDeclarationNode generateRecordType(GraphQLObjectType type)
            throws ServiceTypesGenerationException {
        List<GraphQLFieldDefinition> typeFields = type.getFields();
        NodeList<Node> recordTypeFields = generateRecordTypeFieldsForGraphQLFieldDefinitions(typeFields);
        RecordTypeDescriptorNode recordType =
                createRecordTypeDescriptorNode(createToken(SyntaxKind.RECORD_KEYWORD),
                        createToken(SyntaxKind.OPEN_BRACE_PIPE_TOKEN), recordTypeFields, null,
                        createToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN));

        return createTypeDefinitionNode(generateMetadataForDescription(type.getDescription()),
                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(type.getName()), recordType,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
    }

    private NodeList<Node> generateRecordTypeFieldsForGraphQLInputObjectFields(
            List<GraphQLInputObjectField> inputTypeFields) throws ServiceTypesGenerationException {
        List<Node> fields = new ArrayList<>();
        for (GraphQLInputObjectField field : inputTypeFields) {
            fields.add(createRecordFieldNode(generateMetadata(field.getDescription(), null,
                            field.isDeprecated(), field.getDeprecationReason()), null,
                    generateTypeDescriptor(field.getType()), createIdentifierToken(field.getName()), null,
                    createToken(SyntaxKind.SEMICOLON_TOKEN)));
        }
        return createNodeList(fields);
    }

    private NodeList<Node> generateRecordTypeFieldsForGraphQLFieldDefinitions(
            List<GraphQLFieldDefinition> typeInputFields) throws ServiceTypesGenerationException {
        List<Node> fields = new ArrayList<>();
        for (GraphQLFieldDefinition field : typeInputFields) {
            fields.add(createRecordFieldNode(generateMetadata(field.getDescription(), field.getArguments(),
                            field.isDeprecated(), field.getDeprecationReason()), null,
                    generateTypeDescriptor(field.getType()), createIdentifierToken(field.getName()), null,
                    createToken(SyntaxKind.SEMICOLON_TOKEN)));
        }
        return createNodeList(fields);
    }

    private boolean hasFieldsWithInputs(GraphQLObjectType type) {
        List<GraphQLFieldDefinition> fields = type.getFields();
        for (GraphQLFieldDefinition field : fields) {
            if (field.getArguments().size() > 0) {
                return true;
            }
        }
        return false;
    }

    private ClassDefinitionNode generateServiceClassType(GraphQLObjectType type)
            throws ServiceTypesGenerationException {
        NodeList<Token> qualifierDistinctService = createNodeList(new ArrayList<>(
                List.of(createToken(SyntaxKind.DISTINCT_KEYWORD), createToken(SyntaxKind.SERVICE_KEYWORD))));
        List<Node> serviceClassTypeMembersList = new ArrayList<>();
        for (GraphQLNamedOutputType typeInterface : getInterfacesToBeWritten(type.getInterfaces())) {
            serviceClassTypeMembersList.add(createTypeReferenceNode(createToken(SyntaxKind.ASTERISK_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(typeInterface.getName())),
                    createToken(SyntaxKind.SEMICOLON_TOKEN)));
        }
        for (Node fieldMember : generateServiceClassTypeMembers(type.getFields())) {
            serviceClassTypeMembersList.add(fieldMember);
        }

        NodeList<Node> serviceClassTypeMembers = createNodeList(serviceClassTypeMembersList);
        return createClassDefinitionNode(generateMetadataForDescription(type.getDescription()),
                createToken(SyntaxKind.PUBLIC_KEYWORD), qualifierDistinctService, createToken(SyntaxKind.CLASS_KEYWORD),
                createIdentifierToken(type.getName()), createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                serviceClassTypeMembers, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }

    private MetadataNode generateMetadataForDescription(String description) {
        if (description != null) {
            List<Node> documentationLines = generateMarkdownDocumentationLines(description);
            return createMetadataNode(createMarkdownDocumentationNode(createNodeList(documentationLines)),
                    createEmptyNodeList());
        } else {
            return null;
        }
    }

    private NodeList<Node> generateServiceClassTypeMembers(List<GraphQLFieldDefinition> fieldDefinitions)
            throws ServiceTypesGenerationException {
        List<Node> members = new ArrayList<>();
        for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
            FunctionDefinitionNode functionDefinition = generateServiceClassTypeMember(fieldDefinition);
            members.add(functionDefinition);
        }
        return createNodeList(members);
    }

    private FunctionDefinitionNode generateServiceClassTypeMember(GraphQLFieldDefinition fieldDefinition)
            throws ServiceTypesGenerationException {
        NodeList<Token> qualifierResource = createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD));
        NodeList<Node> fieldName = createNodeList(createIdentifierToken(fieldDefinition.getName()));
        FunctionSignatureNode functionSignature = createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                generateMethodSignatureParams(fieldDefinition.getArguments()),
                createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                generateMethodSignatureReturnType(fieldDefinition.getType()));
        FunctionBodyBlockNode functionBody =
                createFunctionBodyBlockNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN), null, createEmptyNodeList(),
                        createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION,
                generateMetadata(fieldDefinition.getDescription(), fieldDefinition.getArguments(),
                        fieldDefinition.isDeprecated(), fieldDefinition.getDeprecationReason()), qualifierResource,
                createToken(SyntaxKind.FUNCTION_KEYWORD), createIdentifierToken(CodeGeneratorConstants.GET),
                fieldName, functionSignature, functionBody);
    }

    private void addServiceType(GraphQLSchema schema) throws ServiceTypesGenerationException {
        ObjectTypeDescriptorNode serviceObject =
                createObjectTypeDescriptorNode(createNodeList(createToken(SyntaxKind.SERVICE_KEYWORD)),
                        createToken(SyntaxKind.OBJECT_KEYWORD), createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                        generateServiceObjectMembers(schema), createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        TypeDefinitionNode serviceObjectDefinition =
                createTypeDefinitionNode(null, null, createToken(SyntaxKind.TYPE_KEYWORD),
                        createIdentifierToken(this.fileName), serviceObject, createToken(SyntaxKind.SEMICOLON_TOKEN));
        moduleMembers.add(serviceObjectDefinition);
    }

    private NodeList<Node> generateServiceObjectMembers(GraphQLSchema schema) throws ServiceTypesGenerationException {
        List<Node> members = new ArrayList<>();
        TypeReferenceNode graphqlService = createTypeReferenceNode(createToken(SyntaxKind.ASTERISK_TOKEN),
                createIdentifierToken(CodeGeneratorConstants.GRAPHQL_SERVICE_TYPE_NAME),
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        members.add(graphqlService);

        List<Node> serviceMethodDeclarations =
                generateServiceMethodDeclarations(schema.getQueryType(), schema.getMutationType(),
                        schema.getSubscriptionType());
        members.addAll(serviceMethodDeclarations);
        return createNodeList(members);
    }

    private List<Node> generateServiceMethodDeclarations(GraphQLObjectType queryType, GraphQLObjectType mutationType,
                                                         GraphQLObjectType subscriptionType)
            throws ServiceTypesGenerationException {
        List<Node> methodDeclarations = new ArrayList<>();
        handleQueryTypeMethodDeclarations(queryType, methodDeclarations);
        handleMutationTypeMethodDeclarations(mutationType, methodDeclarations);
        handleSubscriptionTypeMethodDeclarations(subscriptionType, methodDeclarations);
        return methodDeclarations;
    }

    private void handleSubscriptionTypeMethodDeclarations(GraphQLObjectType subscriptionType,
                                                          List<Node> methodDeclarations)
            throws ServiceTypesGenerationException {
        if (subscriptionType != null) {
            for (GraphQLFieldDefinition fieldDefinition : subscriptionType.getFieldDefinitions()) {
                FunctionSignatureNode methodSignatureNode =
                        createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                                generateMethodSignatureParams(fieldDefinition.getArguments()),
                                createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                                generateMethodSignatureReturnType(fieldDefinition.getType(), true));
                MetadataNode metadata =
                        generateMetadata(fieldDefinition.getDescription(), fieldDefinition.getArguments(),
                                fieldDefinition.isDeprecated(), fieldDefinition.getDeprecationReason());
                MethodDeclarationNode methodDeclaration =
                        createMethodDeclarationNode(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION, metadata,
                                createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD)),
                                createToken(SyntaxKind.FUNCTION_KEYWORD),
                                createIdentifierToken(CodeGeneratorConstants.SUBSCRIBE),
                                createNodeList(createIdentifierToken(fieldDefinition.getDefinition().getName())),
                                methodSignatureNode, createToken(SyntaxKind.SEMICOLON_TOKEN));
                methodDeclarations.add(methodDeclaration);
            }
        }
    }

    private void handleMutationTypeMethodDeclarations(GraphQLObjectType mutationType, List<Node> methodDeclarations)
            throws ServiceTypesGenerationException {
        if (mutationType != null) {
            for (GraphQLFieldDefinition fieldDefinition : mutationType.getFieldDefinitions()) {
                FunctionSignatureNode methodSignatureNode =
                        createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                                generateMethodSignatureParams(fieldDefinition.getArguments()),
                                createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                                generateMethodSignatureReturnType(fieldDefinition.getType(), false));
                MetadataNode metadata =
                        generateMetadata(fieldDefinition.getDescription(), fieldDefinition.getArguments(),
                                fieldDefinition.isDeprecated(), fieldDefinition.getDeprecationReason());
                MethodDeclarationNode methodDeclaration =
                        createMethodDeclarationNode(SyntaxKind.METHOD_DECLARATION, metadata,
                                createNodeList(createToken(SyntaxKind.REMOTE_KEYWORD)),
                                createToken(SyntaxKind.FUNCTION_KEYWORD),
                                createIdentifierToken(CodeGeneratorConstants.EMPTY_STRING),
                                createNodeList(createIdentifierToken(fieldDefinition.getName())), methodSignatureNode,
                                createToken(SyntaxKind.SEMICOLON_TOKEN));
                methodDeclarations.add(methodDeclaration);
            }
        }
    }

    private void handleQueryTypeMethodDeclarations(GraphQLObjectType queryType, List<Node> methodDeclarations)
            throws ServiceTypesGenerationException {
        for (GraphQLFieldDefinition field : queryType.getFieldDefinitions()) {
            FunctionSignatureNode methodSignature =
                    createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                            generateMethodSignatureParams(field.getArguments()),
                            createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                            generateMethodSignatureReturnType(field.getType(), false));
            MetadataNode metadata = generateMetadata(field.getDescription(), field.getArguments(),
                    field.isDeprecated(), field.getDeprecationReason());
            MethodDeclarationNode methodDeclaration =
                    createMethodDeclarationNode(SyntaxKind.METHOD_DECLARATION, metadata,
                            createNodeList(createToken(SyntaxKind.RESOURCE_KEYWORD)),
                            createToken(SyntaxKind.FUNCTION_KEYWORD), createIdentifierToken(CodeGeneratorConstants.GET),
                            createNodeList(createIdentifierToken(field.getName())), methodSignature,
                            createToken(SyntaxKind.SEMICOLON_TOKEN));
            methodDeclarations.add(methodDeclaration);
        }
    }

    private MetadataNode generateMetadata(String description, List<GraphQLArgument> arguments,
                                          boolean isDeprecated, String deprecationReason) {
        List<AnnotationNode> annotations = new ArrayList<>();
        List<Node> markdownDocumentationLines =
                new ArrayList<>(generateMarkdownDocumentationLines(description));
        if (arguments != null) {
            for (GraphQLArgument argument : arguments) {
                markdownDocumentationLines.addAll(generateMarkdownParameterDocumentationLines(argument));
            }
        }
        if (isDeprecated) {
            AnnotationNode annotation = createAnnotationNode(createToken(SyntaxKind.AT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(CodeGeneratorConstants.DEPRECATED)), null);
            annotations.add(annotation);
            markdownDocumentationLines.addAll(
                    generateMarkdownDocumentationLinesForDeprecated(deprecationReason));
        }
        return createMetadataNode(createMarkdownDocumentationNode(createNodeList(markdownDocumentationLines)),
                createNodeList(annotations));
    }

    private List<Node> generateMarkdownDocumentationLinesForDeprecated(String deprecationReason) {
        List<Node> markdownDocumentationLines = new ArrayList<>();
        markdownDocumentationLines.add(
                createMarkdownDocumentationLineNode(SyntaxKind.MARKDOWN_DEPRECATION_DOCUMENTATION_LINE,
                        createToken(SyntaxKind.HASH_TOKEN), createNodeList(
                                createLiteralValueToken(SyntaxKind.DEPRECATION_LITERAL,
                                        CodeGeneratorConstants.HASH_DEPRECATED, createEmptyMinutiaeList(),
                                        createMinutiaeList(
                                                createEndOfLineMinutiae(CodeGeneratorConstants.NEW_LINE))))));
        markdownDocumentationLines.addAll(generateMarkdownDocumentationLines(deprecationReason));
        return markdownDocumentationLines;
    }

    private List<Node> generateMarkdownParameterDocumentationLines(GraphQLArgument argument) {
        List<Node> markdownDocumentationLines = new ArrayList<>();
        if (argument.getDescription() != null) {
            String[] lines = argument.getDescription().split(CodeGeneratorConstants.NEW_LINE);

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i == 0) {
                    markdownDocumentationLines.add(
                            generateMarkdownParameterDocumentationLine(line, argument.getName()));
                } else {
                    markdownDocumentationLines.add(generateMarkdownDocumentationLine(line));
                }
            }
        }
        return markdownDocumentationLines;
    }

    private MarkdownParameterDocumentationLineNode generateMarkdownParameterDocumentationLine(String descriptionLine,
                                                                                              String argumentName) {
        LiteralValueToken parameterName =
                createLiteralValueToken(SyntaxKind.PARAMETER_NAME, argumentName, createEmptyMinutiaeList(),
                        createMinutiaeList(createWhitespaceMinutiae(CodeGeneratorConstants.WHITESPACE)));
        NodeList<Node> parameterDescription = createNodeList(
                createLiteralValueToken(SyntaxKind.DOCUMENTATION_DESCRIPTION, descriptionLine,
                        createEmptyMinutiaeList(),
                        createMinutiaeList(createEndOfLineMinutiae(CodeGeneratorConstants.NEW_LINE))));
        return createMarkdownParameterDocumentationLineNode(SyntaxKind.MARKDOWN_PARAMETER_DOCUMENTATION_LINE,
                createToken(SyntaxKind.HASH_TOKEN), createToken(SyntaxKind.PLUS_TOKEN), parameterName,
                createToken(SyntaxKind.MINUS_TOKEN), parameterDescription);
    }

    private List<Node> generateMarkdownDocumentationLines(String description) {
        List<Node> markdownDocumentationLines = new ArrayList<>();
        if (description != null) {
            String[] lines = description.split(System.lineSeparator());
            for (String line : lines) {
                MarkdownDocumentationLineNode markdownDocumentationLine = generateMarkdownDocumentationLine(line);
                markdownDocumentationLines.add(markdownDocumentationLine);
            }
        }
        return markdownDocumentationLines;
    }

    private MarkdownDocumentationLineNode generateMarkdownDocumentationLine(String descriptionLine) {
        return createMarkdownDocumentationLineNode(SyntaxKind.MARKDOWN_DOCUMENTATION_LINE,
                createToken(SyntaxKind.HASH_TOKEN), createNodeList(
                        createLiteralValueToken(SyntaxKind.DOCUMENTATION_DESCRIPTION, descriptionLine,
                                createEmptyMinutiaeList(),
                                createMinutiaeList(createEndOfLineMinutiae(CodeGeneratorConstants.NEW_LINE)))));
    }

    private ReturnTypeDescriptorNode generateMethodSignatureReturnType(GraphQLOutputType type, boolean isStream)
            throws ServiceTypesGenerationException {
        TypeDescriptorNode typeDescriptor = generateTypeDescriptor(type);
        if (isStream) {
            StreamTypeDescriptorNode streamTypeDescriptor =
                    createStreamTypeDescriptorNode(createToken(SyntaxKind.STREAM_KEYWORD),
                            createStreamTypeParamsNode(createToken(SyntaxKind.LT_TOKEN), typeDescriptor, null, null,
                                    createToken(SyntaxKind.GT_TOKEN)));
            return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    streamTypeDescriptor);
        } else {
            return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    typeDescriptor);
        }
    }

    private ReturnTypeDescriptorNode generateMethodSignatureReturnType(GraphQLOutputType type)
            throws ServiceTypesGenerationException {
        return generateMethodSignatureReturnType(type, false);
    }

    private SeparatedNodeList<ParameterNode> generateMethodSignatureParams(List<GraphQLArgument> arguments)
            throws ServiceTypesGenerationException {
        List<Node> params = new ArrayList<>();
        List<DefaultableParameterNode> defaultParams = new ArrayList<>();
        List<RequiredParameterNode> requiredParams = new ArrayList<>();

        for (GraphQLArgument argument : arguments) {
            TypeDescriptorNode argumentType = generateTypeDescriptor(argument.getType());
            if (argument.hasSetDefaultValue()) {
                Object value = argument.getArgumentDefaultValue().getValue();
                ExpressionNode generatedDefaultValue = generateArgDefaultValue(value);
                DefaultableParameterNode defaultableParameterNode =
                        createDefaultableParameterNode(createEmptyNodeList(), argumentType,
                                createIdentifierToken(argument.getName()), createToken(SyntaxKind.EQUAL_TOKEN),
                                generatedDefaultValue);
                defaultParams.add(defaultableParameterNode);
            } else {
                RequiredParameterNode requiredParameterNode =
                        createRequiredParameterNode(createEmptyNodeList(), argumentType,
                                createIdentifierToken(argument.getName()));
                requiredParams.add(requiredParameterNode);
            }
        }
        for (int i = 0; i < requiredParams.size(); i++) {
            RequiredParameterNode requireParam = requiredParams.get(i);
            params.add(requireParam);
            if (i != requiredParams.size() - 1) {
                params.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }
        for (int i = 0; i < defaultParams.size(); i++) {
            DefaultableParameterNode defaultParam = defaultParams.get(i);
            if (i != 0 || requiredParams.size() != 0) {
                params.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
            params.add(defaultParam);
        }
        return createSeparatedNodeList(params);
    }

    private ExpressionNode generateArgDefaultValue(Object value) throws ServiceTypesGenerationException {
        if (value instanceof FloatValue) {
            FloatValue floatValue = (FloatValue) value;
            return createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL,
                    createLiteralValueToken(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN,
                            floatValue.getValue().toString(), createEmptyMinutiaeList(), createEmptyMinutiaeList()));
        } else if (value instanceof IntValue) {
            IntValue intValue = (IntValue) value;
            return createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL,
                    createLiteralValueToken(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN, intValue.getValue().toString(),
                            createEmptyMinutiaeList(), createEmptyMinutiaeList()));
        } else if (value instanceof StringValue) {
            StringValue stringValue = (StringValue) value;
            return createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                    createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                            String.format(CodeGeneratorConstants.WRITE_STRING_FORMAT, stringValue.getValue()),
                            createEmptyMinutiaeList(), createEmptyMinutiaeList()));
        } else if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            return createBasicLiteralNode(SyntaxKind.BOOLEAN_LITERAL,
                    createLiteralValueToken(booleanValue.isValue() ? SyntaxKind.TRUE_KEYWORD : SyntaxKind.FALSE_KEYWORD,
                            booleanValue.isValue() ? CodeGeneratorConstants.TRUE : CodeGeneratorConstants.FALSE,
                            createEmptyMinutiaeList(), createEmptyMinutiaeList()));
        } else if (value instanceof EnumValue) {
            EnumValue enumValue = (EnumValue) value;
            return createSimpleNameReferenceNode(createIdentifierToken(enumValue.getName()));
        } else if (value instanceof ObjectValue) {
            ObjectValue objectValue = (ObjectValue) value;
            List<ObjectField> objectFields = objectValue.getObjectFields();
            List<Node> mappingConstructorFields = new ArrayList<>();
            for (int i = 0; i < objectFields.size(); i++) {
                ObjectField objectField = objectFields.get(i);
                SpecificFieldNode specificField =
                        createSpecificFieldNode(null, createIdentifierToken(objectField.getName()),
                                createToken(SyntaxKind.COLON_TOKEN), generateArgDefaultValue(objectField.getValue()));
                mappingConstructorFields.add(specificField);
                if (i != objectFields.size() - 1) {
                    mappingConstructorFields.add(createToken(SyntaxKind.COMMA_TOKEN));
                }
            }
            return createMappingConstructorExpressionNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                    createSeparatedNodeList(mappingConstructorFields), createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        } else if (value instanceof ArrayValue) {
            ArrayValue arrayValue = (ArrayValue) value;
            List<Value> arrayElementValues = arrayValue.getValues();
            List<Node> arrayInternalExpressions = new ArrayList<>();
            for (int i = 0; i < arrayElementValues.size(); i++) {
                Value arrayElementValue = arrayElementValues.get(i);
                arrayInternalExpressions.add(generateArgDefaultValue(arrayElementValue));
                if (i != arrayElementValues.size() - 1) {
                    arrayInternalExpressions.add(createToken(SyntaxKind.COMMA_TOKEN));
                }
            }
            return createListConstructorExpressionNode(createToken(SyntaxKind.OPEN_BRACKET_TOKEN),
                    createSeparatedNodeList(arrayInternalExpressions), createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        } else {
            throw new ServiceTypesGenerationException(
                    String.format(Constants.UNSUPPORTED_DEFAULT_ARGUMENT_VALUE, value.getClass().getName()));
        }
    }

    private TypeDescriptorNode generateTypeDescriptor(GraphQLType type, boolean nonNull)
            throws ServiceTypesGenerationException {
        if (type instanceof GraphQLScalarType) {
            GraphQLScalarType scalarType = (GraphQLScalarType) type;
            BuiltinSimpleNameReferenceNode scalarName =
                    createBuiltinSimpleNameReferenceNode(getTypeDescFor(scalarType.getName()),
                            createToken(getTypeKeywordFor(scalarType.getName())));
            if (nonNull) {
                return scalarName;
            } else {
                return createOptionalTypeDescriptorNode(scalarName, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else if (type instanceof GraphQLObjectType) {
            GraphQLObjectType objectType = (GraphQLObjectType) type;
            SimpleNameReferenceNode objectName =
                    createSimpleNameReferenceNode(createIdentifierToken(objectType.getName()));
            if (nonNull) {
                return objectName;
            } else {
                return createOptionalTypeDescriptorNode(objectName, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else if (type instanceof GraphQLInputObjectType) {
            GraphQLInputObjectType inputObjectType = (GraphQLInputObjectType) type;
            SimpleNameReferenceNode inputObjectName =
                    createSimpleNameReferenceNode(createIdentifierToken(inputObjectType.getName()));
            if (nonNull) {
                return inputObjectName;
            } else {
                return createOptionalTypeDescriptorNode(inputObjectName, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else if (type instanceof GraphQLEnumType) {
            GraphQLEnumType enumType = (GraphQLEnumType) type;
            SimpleNameReferenceNode enumName = createSimpleNameReferenceNode(createIdentifierToken(enumType.getName()));
            if (nonNull) {
                return enumName;
            } else {
                return createOptionalTypeDescriptorNode(enumName, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else if (type instanceof GraphQLInterfaceType) {
            GraphQLInterfaceType interfaceType = (GraphQLInterfaceType) type;
            SimpleNameReferenceNode interfaceName =
                    createSimpleNameReferenceNode(createIdentifierToken(interfaceType.getName()));
            if (nonNull) {
                return interfaceName;
            } else {
                return createOptionalTypeDescriptorNode(interfaceName, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else if (type instanceof GraphQLUnionType) {
            GraphQLUnionType unionType = (GraphQLUnionType) type;
            SimpleNameReferenceNode unionName =
                    createSimpleNameReferenceNode(createIdentifierToken(unionType.getName()));
            if (nonNull) {
                return unionName;
            } else {
                return createOptionalTypeDescriptorNode(unionName, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else if (type instanceof GraphQLNonNull) {
            GraphQLNonNull nonNullType = (GraphQLNonNull) type;
            return generateTypeDescriptor(nonNullType.getWrappedType(), true);
        } else if (type instanceof GraphQLList) {
            GraphQLList listType = (GraphQLList) type;
            ArrayDimensionNode arrayDimension =
                    createArrayDimensionNode(createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                            createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            TypeDescriptorNode wrappedType = generateTypeDescriptor(listType.getWrappedType());
            ArrayTypeDescriptorNode arrayTypeNode =
                    createArrayTypeDescriptorNode(wrappedType, createNodeList(arrayDimension));
            if (nonNull) {
                return arrayTypeNode;
            } else {
                return createOptionalTypeDescriptorNode(arrayTypeNode, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
        } else {
            throw new ServiceTypesGenerationException(
                    String.format(Constants.UNSUPPORTED_TYPE, type.getClass().getName()));
        }
    }

    private TypeDescriptorNode generateTypeDescriptor(GraphQLType argumentType) throws ServiceTypesGenerationException {
        return generateTypeDescriptor(argumentType, false);
    }

    private SyntaxKind getTypeKeywordFor(String typeName) throws ServiceTypesGenerationException {
        if (Constants.GRAPHQL_STRING_TYPE.equals(typeName)) {
            return SyntaxKind.STRING_KEYWORD;
        } else if (Constants.GRAPHQL_INT_TYPE.equals(typeName)) {
            return SyntaxKind.INT_KEYWORD;
        } else if (Constants.GRAPHQL_FLOAT_TYPE.equals(typeName)) {
            return SyntaxKind.FLOAT_KEYWORD;
        } else if (Constants.GRAPHQL_BOOLEAN_TYPE.equals(typeName)) {
            return SyntaxKind.BOOLEAN_KEYWORD;
        } else if (Constants.GRAPHQL_ID_TYPE.equals(typeName)) {
            return SyntaxKind.STRING_KEYWORD;
        } else {
            throw new ServiceTypesGenerationException(String.format(Constants.ONLY_SCALAR_TYPE_ALLOWED, typeName));
        }
    }

    private SyntaxKind getTypeDescFor(String typeName) throws ServiceTypesGenerationException {
        if (Constants.GRAPHQL_STRING_TYPE.equals(typeName)) {
            return SyntaxKind.STRING_TYPE_DESC;
        } else if (Constants.GRAPHQL_INT_TYPE.equals(typeName)) {
            return SyntaxKind.INT_TYPE_DESC;
        } else if (Constants.GRAPHQL_FLOAT_TYPE.equals(typeName)) {
            return SyntaxKind.FLOAT_TYPE_DESC;
        } else if (Constants.GRAPHQL_BOOLEAN_TYPE.equals(typeName)) {
            return SyntaxKind.BOOLEAN_TYPE_DESC;
        } else if (Constants.GRAPHQL_ID_TYPE.equals(typeName)) {
            return SyntaxKind.STRING_TYPE_DESC;
        } else {
            throw new ServiceTypesGenerationException(String.format(Constants.ONLY_SCALAR_TYPE_ALLOWED, typeName));
        }
    }
}
