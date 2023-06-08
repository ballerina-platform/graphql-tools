package io.ballerina.graphql.generator.service;

import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.SDLDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;

/**
 * Utility class for combining available service with generated service for a GraphQL schema.
 */
public class ServiceCombiner {
    private static final String REMOVE_INPUT_TYPE_FIELD_MESSAGE =
            "warning: In '%s' input type '%s' field has removed. This can brake clients";
    private static final String WARNING_MESSAGE_REMOVE_UNION_MEMBER =
            "warning: In '%s' union type '%s' member has " + "removed. This can break existing clients.";
    private static final String ADD_VIOLATED_INPUT_TYPE_FIELD_MESSAGE = "warning: In '%s' input type '%s' field is " +
            "introduced without a default value. This can brake available clients";
    private static final String REMOVE_ENUM_MEMBER_MESSAGE =
            "warning: In '%s' enum '%s' member has removed. This can break existing clients.";
    private static final String REMOVE_SERVICE_CLASS_FUNC_DEF_MESSAGE =
            "warning: In '%s' service class '%s' function " +
                    "definition has removed. This can break available clients";
    private static final String WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE = "warning: In '%s' class " +
            "'%s' function definition return type has changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT = "warning: " +
            "In '%s' service object '%s' method declaration return type has changed from '%s' to '%s'. This can break" +
            " existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_CLASS = "warning: In '%s'" +
            " class '%s' function definition '%s' parameter added without default value. This can break existing " +
            "clients.";
    private static final String WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD = "warning:" +
            " In '%s' service object '%s' method declaration '%s' parameter added without default value. This can " +
            "break existing clients.";
    private static final String WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD = "warning: " +
            "In '%s' service object '%s' method declaration '%s' parameter assigned '%s' default value has removed. " +
            "This can break existing clients.";
    private static final String WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_CLASS_FUNC = "warning: " +
            "In '%s' service class '%s' function '%s' parameter assigned '%s' default value has removed. " +
            "This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_CLASS = "warning: In '%s' service " +
            "class '%s' function qualifier list changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_OBJECT = "warning: In '%s' service " +
            "object '%s' function qualifier list changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD =
            "warning: In '%s' service object '%s' method changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_CLASS_METHOD = "warning: In " +
            "'%s' service class '%s' method changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_INTERFACE = "warning: In '%s' " +
            "interface '%s' function qualifier list changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD = "warning: In '%s' record type" +
            " '%s' field assigned '%s' default value has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_RECORD_FIELD_TYPE_CHANGED = "warning: In '%s' record type '%s' " +
            "field type has changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE_CLASS = "warning: In '%s' class '%s' " +
            "function definition '%s' parameter removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT = "warning: In '%s' service " +
            "object '%s' method declaration '%s' parameter has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_CLASS = "warning: In '%s' class " +
            "'%s' function definition '%s' parameter type change from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT = "warning: In '%s' service " +
            "object '%s' method declaration '%s' parameter type change from '%s' to '%s'. This can break existing " +
            "clients.";
    private static final String WARNING_MESSAGE_REMOVE_SERVICE_OBJECT_METHOD_DECLARATION = "warning: In '%s' service " +
            "object '%s' method declaration has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_REMOVE_INTERFACE_SERVICE_OBJECT_METHOD_DECLARATION = "warning: In " +
            "'%s' interface service object '%s' method declaration has removed. This can break existing clients.";
    private final ModulePartNode nextContentNode;
    private ModulePartNode prevContentNode;
    private Map<Node, Node> targetAndReplacement;
    private List<ModuleMemberDeclarationNode> moduleMembers;
    private List<ModuleMemberDeclarationNode> inputObjectTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> interfaceTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> enumTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> unionTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> objectTypesModuleMembers;
    private List<String> breakingChangeWarnings;

    public ServiceCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;
        this.targetAndReplacement = new HashMap<>();

        moduleMembers = new ArrayList<>();
        inputObjectTypesModuleMembers = new ArrayList<>();
        interfaceTypesModuleMembers = new ArrayList<>();
        enumTypesModuleMembers = new ArrayList<>();
        unionTypesModuleMembers = new ArrayList<>();
        objectTypesModuleMembers = new ArrayList<>();

        breakingChangeWarnings = new ArrayList<>();
    }

    public static void getChangesOfTypeDefinitionRegistry(TypeDefinitionRegistry prevRegistry,
                                                          TypeDefinitionRegistry newRegistry) {
    }

    public static void getChangesOfNewSchema(GraphQLSchema prevSchema, GraphQLSchema newSchema) {
        prevSchema.toString();
        prevSchema.getQueryType().getFields();
    }

    public static void getChangesOfFields(List<GraphQLFieldDefinition> prevFields,
                                          List<GraphQLFieldDefinition> newFields) {

    }

    public static void getChangesOfField(GraphQLFieldDefinition prevField, GraphQLFieldDefinition newField) {
        if (prevField.getName().equals(newField.getName())) {
            prevField.getType();
        }
    }

    public static void getChangesOfObjectType(GraphQLObjectType prevObject, GraphQLObjectType newObject) {
        if (prevObject.getName().equals(newObject.getName())) {
            return;
        }
    }

    public SyntaxTree mergeRootNodes() throws Exception {
        List<ModuleMemberDeclarationNode> newMembers =
                generateNewMembers(prevContentNode.members(), nextContentNode.members());

        moduleMembers.addAll(inputObjectTypesModuleMembers);
        moduleMembers.addAll(interfaceTypesModuleMembers);
        moduleMembers.addAll(enumTypesModuleMembers);
        moduleMembers.addAll(unionTypesModuleMembers);
        moduleMembers.addAll(objectTypesModuleMembers);
        moduleMembers.addAll(newMembers);

        NodeList<ModuleMemberDeclarationNode> allMembers = createNodeList(moduleMembers);
        ModulePartNode contentNode =
                createModulePartNode(prevContentNode.imports(), allMembers, createToken(SyntaxKind.EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(contentNode);
    }

    private List<ModuleMemberDeclarationNode> generateNewMembers(NodeList<ModuleMemberDeclarationNode> prevMembers,
                                                                 NodeList<ModuleMemberDeclarationNode> nextMembers)
            throws Exception {
        List<ModuleMemberDeclarationNode> newMembers = new ArrayList<>();
        for (ModuleMemberDeclarationNode nextMember : nextMembers) {
            boolean isFound = false;
            for (ModuleMemberDeclarationNode prevMember : prevMembers) {
                if (isMemberEquals(prevMember, nextMember)) {
                    isFound = true;
                } else {
                    //findChanges(nextMember, prevMember);
                }
            }
            if (!isFound) {
                newMembers.add(nextMember);
            }
        }
        return newMembers;
    }

    private boolean isMemberEquals(ModuleMemberDeclarationNode prevMember, ModuleMemberDeclarationNode nextMember)
            throws Exception {
        if (prevMember instanceof ClassDefinitionNode && nextMember instanceof ClassDefinitionNode) {
            ClassDefinitionNode prevClassDef = (ClassDefinitionNode) prevMember;
            ClassDefinitionNode nextClassDef = (ClassDefinitionNode) nextMember;
            if (isClassDefEquals(prevClassDef, nextClassDef)) {
                return true;
            }
        } else if (prevMember instanceof TypeDefinitionNode && nextMember instanceof TypeDefinitionNode) {
            TypeDefinitionNode prevTypeDef = (TypeDefinitionNode) prevMember;
            TypeDefinitionNode nextTypeDef = (TypeDefinitionNode) nextMember;
            if (isTypeDefEquals(prevTypeDef, nextTypeDef)) {
                return true;
            }
        } else if (prevMember instanceof EnumDeclarationNode && nextMember instanceof EnumDeclarationNode) {
            EnumDeclarationNode prevEnumDec = (EnumDeclarationNode) prevMember;
            EnumDeclarationNode nextEnumDec = (EnumDeclarationNode) nextMember;
            if (isEnumDecEquals(prevEnumDec, nextEnumDec)) {
                return true;
            }
        } else if (prevMember.getClass().toString().equals(nextMember.getClass().toString())) {
            throw new Exception("No valid member: " + prevMember.getClass().toString());
        }
        return false;
    }

    private boolean isEnumDecEquals(EnumDeclarationNode prevEnumDec, EnumDeclarationNode nextEnumDec) {
        if (!prevEnumDec.qualifier().orElseThrow().text().equals(nextEnumDec.qualifier().orElseThrow().text())) {
            return false;
        }
        if (!prevEnumDec.identifier().text().equals(nextEnumDec.identifier().text())) {
            return false;
        }
        enumTypesModuleMembers.add(nextEnumDec);
        MembersEqualityResult comparedResult =
                compareEnumMembers(prevEnumDec.enumMemberList(), nextEnumDec.enumMemberList());
        for (String removedField : comparedResult.getRemovals()) {
            breakingChangeWarnings.add(
                    String.format(REMOVE_ENUM_MEMBER_MESSAGE, prevEnumDec.identifier().text(), removedField));
        }
        return true;
    }

    private MembersEqualityResult compareEnumMembers(SeparatedNodeList<Node> prevEnumMembers,
                                                     SeparatedNodeList<Node> nextEnumMembers) {
        MembersEqualityResult result = new MembersEqualityResult();
        for (Node prevEnumMemberNode : prevEnumMembers) {
            boolean prevFoundMatch = false;
            for (Node nextEnumMemberNode : nextEnumMembers) {
                if (prevEnumMemberNode instanceof EnumMemberNode && nextEnumMemberNode instanceof EnumMemberNode) {
                    EnumMemberNode nextEnumMember = (EnumMemberNode) nextEnumMemberNode;
                    EnumMemberNode prevEnumMember = (EnumMemberNode) prevEnumMemberNode;
                    if (nextEnumMember.identifier().text().equals(prevEnumMember.identifier().text())) {
                        prevFoundMatch = true;
                    }
                }
            }
            if (!prevFoundMatch) {
                if (prevEnumMemberNode instanceof EnumMemberNode) {
                    EnumMemberNode prevEnumMember = (EnumMemberNode) prevEnumMemberNode;
                    result.addToRemovals(prevEnumMember.identifier().text());
                }
            }
        }
        for (Node nextEnumMemberNode : nextEnumMembers) {
            boolean nextFoundMatch = false;
            for (Node prevEnumMemberNode : prevEnumMembers) {
                if (nextEnumMemberNode instanceof EnumMemberNode && prevEnumMemberNode instanceof EnumMemberNode) {
                    EnumMemberNode nextEnumMember = (EnumMemberNode) nextEnumMemberNode;
                    EnumMemberNode prevEnumMember = (EnumMemberNode) prevEnumMemberNode;
                    if (nextEnumMember.identifier().text().equals(prevEnumMember.identifier().text())) {
                        nextFoundMatch = true;
                    }
                }
            }
            if (!nextFoundMatch) {
                if (nextEnumMemberNode instanceof EnumMemberNode) {
                    EnumMemberNode nextEnumMember = (EnumMemberNode) nextEnumMemberNode;
                    result.addToAdditions(nextEnumMember.identifier().text());
                }
            }
        }
        return result;
    }

    private boolean isTypeDefEquals(TypeDefinitionNode prevTypeDef, TypeDefinitionNode nextTypeDef) throws Exception {
        if (!prevTypeDef.typeName().text().equals(nextTypeDef.typeName().text())) {
            return false;
        }
        if (prevTypeDef.typeDescriptor() instanceof ObjectTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof ObjectTypeDescriptorNode) {
            ObjectTypeDescriptorNode prevServiceObject = (ObjectTypeDescriptorNode) prevTypeDef.typeDescriptor();
            ObjectTypeDescriptorNode nextServiceObject = (ObjectTypeDescriptorNode) nextTypeDef.typeDescriptor();
            ServiceObjectEqualityResult serviceObjectEquals =
                    isServiceObjectEquals(prevServiceObject, nextServiceObject);
            if (serviceObjectEquals.isEqual()) {

            } else {
                if (!serviceObjectEquals.getRemovedMethodDeclarations().isEmpty()) {
                    for (String removedMethodDeclarationName : serviceObjectEquals.getRemovedMethodDeclarations()) {
                        breakingChangeWarnings.add(
                                String.format(WARNING_MESSAGE_REMOVE_SERVICE_OBJECT_METHOD_DECLARATION,
                                        prevTypeDef.typeName().text(), removedMethodDeclarationName));
                    }
                }
                List<MethodDeclarationEqualityResult> updatedMethodDeclarations =
                        serviceObjectEquals.getUpdatedMethodDeclarations();
                if (!updatedMethodDeclarations.isEmpty()) {
                    for (MethodDeclarationEqualityResult updatedMethodDeclaration : updatedMethodDeclarations) {
                        if (!updatedMethodDeclaration.getFunctionSignatureEqualityResult().isEqual()) {
                            for (String removedParameterName :
                                    updatedMethodDeclaration.getFunctionSignatureEqualityResult()
                                    .getRemovedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                                                prevTypeDef.typeName().text(),
                                                updatedMethodDeclaration.getPrevFunctionName(), removedParameterName));
                            }
                        }
                        if (!updatedMethodDeclaration.getFunctionSignatureEqualityResult().getTypeChangedParameters()
                                .isEmpty()) {
                            for (ParameterEqualityResult parameterEquality :
                                    updatedMethodDeclaration.getFunctionSignatureEqualityResult()
                                    .getTypeChangedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT,
                                                prevTypeDef.typeName().text(),
                                                updatedMethodDeclaration.getPrevFunctionName(),
                                                parameterEquality.getPrevParameterName(),
                                                parameterEquality.getTypeEquality().getPrevType(),
                                                parameterEquality.getTypeEquality().getNextType()));
                            }
                        }
                        if (updatedMethodDeclaration.isGetAndSubscribeInterchanged()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD,
                                            prevTypeDef.typeName().text(),
                                            updatedMethodDeclaration.getPrevFunctionName(),
                                            updatedMethodDeclaration.getPrevMethodType(),
                                            updatedMethodDeclaration.getNextMethodType()));
                        }
                        if (!updatedMethodDeclaration.isQualifierSimilar()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_OBJECT,
                                            prevTypeDef.typeName().text(),
                                            updatedMethodDeclaration.getPrevFunctionName(),
                                            updatedMethodDeclaration.getPrevMainQualifier(),
                                            updatedMethodDeclaration.getNextMainQualifier()));
                        }
                        if (!updatedMethodDeclaration.getFunctionSignatureEqualityResult().getReturnTypeEqualityResult()
                                .isEqual()) {
                            breakingChangeWarnings.add(String.format(
                                    WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT,
                                    prevTypeDef.typeName().text(), updatedMethodDeclaration.getPrevFunctionName(),
                                    updatedMethodDeclaration.getFunctionSignatureEqualityResult()
                                            .getReturnTypeEqualityResult().getPrevType(),
                                    updatedMethodDeclaration.getFunctionSignatureEqualityResult()
                                            .getReturnTypeEqualityResult().getNextType()));
                        }
                        FunctionSignatureEqualityResult updatedMethodSignatureEquality =
                                updatedMethodDeclaration.getFunctionSignatureEqualityResult();
                        if (!updatedMethodSignatureEquality.getAddedViolatedParameters().isEmpty()) {
                            for (String addedViolatedParameterName :
                                    updatedMethodSignatureEquality.getAddedViolatedParameters()) {
                                breakingChangeWarnings.add(String.format(
                                        WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD,
                                        prevTypeDef.typeName().text(), updatedMethodDeclaration.getPrevFunctionName(),
                                        addedViolatedParameterName));
                            }
                        }
                        if (!updatedMethodSignatureEquality.getDefaultValueRemovedParameters().isEmpty()) {
                            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                                    updatedMethodSignatureEquality.getDefaultValueRemovedParameters()) {
                                breakingChangeWarnings.add(String.format(
                                        WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD,
                                        prevTypeDef.typeName().text(), updatedMethodDeclaration.getPrevFunctionName(),
                                        defaultValueRemovedParameterEquality.getPrevParameterName(),
                                        defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
                            }
                        }
                    }
                }
            }
//
//            NodeList<Node> serviceObjectNewMembers = getServiceObjectNewMembers(prevServiceObject, nextServiceObject);
//
//            ObjectTypeDescriptorNode modifiedPrevServiceObject =
//                    prevServiceObject.modify(prevServiceObject.objectTypeQualifiers(),
//                            prevServiceObject.objectKeyword(), prevServiceObject.openBrace(),
//                            serviceObjectNewMembers,
//                            prevServiceObject.closeBrace());
//            TypeDefinitionNode modifiedPrevServiceType = prevTypeDef.modify(prevTypeDef.metadata().orElse(null),
//                    prevTypeDef.visibilityQualifier().orElse(null),
//                    prevTypeDef.typeKeyword(), prevTypeDef.typeName(), modifiedPrevServiceObject,
//                    prevTypeDef.semicolonToken());
//            moduleMembers.add(modifiedPrevServiceType);
            moduleMembers.add(nextTypeDef);
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof DistinctTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof DistinctTypeDescriptorNode) {
            DistinctTypeDescriptorNode prevDistinctServiceObject =
                    (DistinctTypeDescriptorNode) prevTypeDef.typeDescriptor();
            DistinctTypeDescriptorNode nextDistinctServiceObject =
                    (DistinctTypeDescriptorNode) nextTypeDef.typeDescriptor();

            ObjectTypeDescriptorNode prevServiceObject =
                    (ObjectTypeDescriptorNode) prevDistinctServiceObject.typeDescriptor();
            ObjectTypeDescriptorNode nextServiceObject =
                    (ObjectTypeDescriptorNode) nextDistinctServiceObject.typeDescriptor();
            ServiceObjectEqualityResult serviceObjectEquals =
                    isServiceObjectEquals(prevServiceObject, nextServiceObject);
            if (!serviceObjectEquals.isEqual()) {
                for (MethodDeclarationEqualityResult updatedMethodDeclaration :
                        serviceObjectEquals.getUpdatedMethodDeclarations()) {
                    if (!updatedMethodDeclaration.isQualifierSimilar()) {
                        breakingChangeWarnings.add(String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_INTERFACE,
                                prevTypeDef.typeName().text(), updatedMethodDeclaration.getPrevFunctionName(),
                                updatedMethodDeclaration.getPrevMainQualifier(),
                                updatedMethodDeclaration.getNextMainQualifier()));
                    }
                    if (updatedMethodDeclaration.isGetAndSubscribeInterchanged()) {
                        breakingChangeWarnings.add(
                                String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD,
                                        prevTypeDef.typeName().text(), updatedMethodDeclaration.getPrevFunctionName(),
                                        updatedMethodDeclaration.getPrevMethodType(),
                                        updatedMethodDeclaration.getNextMethodType()));
                    }
                    if (!updatedMethodDeclaration.getFunctionSignatureEqualityResult().getReturnTypeEqualityResult()
                            .isEqual()) {
                        breakingChangeWarnings.add(String.format(
                                WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT,
                                prevTypeDef.typeName().text(), updatedMethodDeclaration.getPrevFunctionName(),
                                updatedMethodDeclaration.getFunctionSignatureEqualityResult()
                                        .getReturnTypeEqualityResult().getPrevType(),
                                updatedMethodDeclaration.getFunctionSignatureEqualityResult()
                                        .getReturnTypeEqualityResult().getNextType()));
                    }
                }
                for (String removedMethod : serviceObjectEquals.getRemovedMethodDeclarations()) {
                    breakingChangeWarnings.add(
                            String.format(WARNING_MESSAGE_REMOVE_INTERFACE_SERVICE_OBJECT_METHOD_DECLARATION,
                                    prevTypeDef.typeName().text(), removedMethod));
                }
            }
            interfaceTypesModuleMembers.add(nextTypeDef);
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof RecordTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode prevRecordType = (RecordTypeDescriptorNode) prevTypeDef.typeDescriptor();
            RecordTypeDescriptorNode nextRecordType = (RecordTypeDescriptorNode) nextTypeDef.typeDescriptor();
            RecordTypeEqualityResult equalityResult = isRecordTypeEquals(prevRecordType, nextRecordType);
            if (!equalityResult.isEqual()) {
                for (String removedField : equalityResult.getRemovals()) {
                    breakingChangeWarnings.add(
                            String.format(REMOVE_INPUT_TYPE_FIELD_MESSAGE, prevTypeDef.typeName().text(),
                                    removedField));
                }
                for (String addedViolatedField : equalityResult.getViolatedAdditions()) {
                    breakingChangeWarnings.add(
                            String.format(ADD_VIOLATED_INPUT_TYPE_FIELD_MESSAGE, prevTypeDef.typeName().text(),
                                    addedViolatedField));
                }
                for (RecordFieldEqualityResult recordFieldEquality : equalityResult.getDefaultValueRemovedFields()) {
                    breakingChangeWarnings.add(String.format(WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD,
                            prevTypeDef.typeName().text(), recordFieldEquality.getPrevRecordFieldName(),
                            recordFieldEquality.getPrevRecordFieldDefaultValue()));
                }

                for (RecordFieldEqualityResult recordFieldEquality : equalityResult.getTypeChangedRecordFields()) {
                    breakingChangeWarnings.add(
                            String.format(WARNING_MESSAGE_RECORD_FIELD_TYPE_CHANGED, prevTypeDef.typeName().text(),
                                    recordFieldEquality.getPrevRecordFieldName(),
                                    recordFieldEquality.getTypeEquality().getPrevType(),
                                    recordFieldEquality.getTypeEquality().getNextType()));
                }
            }
            inputObjectTypesModuleMembers.add(nextTypeDef);
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof UnionTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionType = (UnionTypeDescriptorNode) prevTypeDef.typeDescriptor();
            UnionTypeDescriptorNode nextUnionType = (UnionTypeDescriptorNode) nextTypeDef.typeDescriptor();
            UnionTypeEqualityResult unionTypeEqualityResult = isUnionTypeEquals(prevUnionType, nextUnionType);
            if (!unionTypeEqualityResult.isEqual()) {
                for (String removedUnionMemberName : unionTypeEqualityResult.getRemovals()) {
                    breakingChangeWarnings.add(
                            String.format(WARNING_MESSAGE_REMOVE_UNION_MEMBER, prevTypeDef.typeName().text(),
                                    removedUnionMemberName));
                }
            }
            unionTypesModuleMembers.add(nextTypeDef);
            return true;
        }
        return true;
    }

    private UnionTypeEqualityResult isUnionTypeEquals(UnionTypeDescriptorNode prevUnionType,
                                                      UnionTypeDescriptorNode nextUnionType) throws Exception {
        UnionTypeEqualityResult unionTypeEqualityResult = new UnionTypeEqualityResult();
        List<String> prevUnionTypeMembers = new ArrayList<>();
        List<String> nextUnionTypeMembers = new ArrayList<>();
        populateUnionMemberNames(prevUnionType, prevUnionTypeMembers);
        populateUnionMemberNames(nextUnionType, nextUnionTypeMembers);
        HashMap<String, Boolean> nextUnionMemberAvailable = new HashMap<>();
        for (String nextUnionTypeMember : nextUnionTypeMembers) {
            nextUnionMemberAvailable.put(nextUnionTypeMember, false);
        }
        for (String prevUnionTypeMember : prevUnionTypeMembers) {
            boolean foundMatch = false;
            for (String nextUnionTypeMember : nextUnionTypeMembers) {
                if (prevUnionTypeMember.equals(nextUnionTypeMember)) {
                    foundMatch = true;
                    nextUnionMemberAvailable.put(nextUnionTypeMember, true);
                    break;
                }
            }
            if (!foundMatch) {
                unionTypeEqualityResult.addToRemovals(prevUnionTypeMember);
            }
        }
        for (Map.Entry<String, Boolean> availableEntry : nextUnionMemberAvailable.entrySet()) {
            Boolean available = availableEntry.getValue();
            if (!available) {
                String notAvailableNextUnionMember = availableEntry.getKey();
                unionTypeEqualityResult.addToAdditions(notAvailableNextUnionMember);
            }
        }
//        if (prevUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode &&
//                nextUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode) {
//            UnionTypeDescriptorNode prevUnionLeftType = (UnionTypeDescriptorNode) prevUnionType.leftTypeDesc();
//            UnionTypeDescriptorNode nextUnionLeftType = (UnionTypeDescriptorNode) nextUnionType.leftTypeDesc();
//            if (!isUnionTypeEquals(prevUnionLeftType, nextUnionLeftType)) {
//                targetAndReplacement.put(prevUnionType, nextUnionType);
//                return true;
//            }
//        }
////        else if (!leftTypeEquality.isEqual()) {
////            return false;
////        }
//        if (!prevUnionType.pipeToken().text().equals(nextUnionType.pipeToken().text())) {
//            return false;
//        }
//        TypeEqualityResult rightTypeEquality =
//                isTypeEquals(prevUnionType.rightTypeDesc(), nextUnionType.rightTypeDesc());
//        if (!rightTypeEquality.isEqual()) {
//            return false;
//        }
        return unionTypeEqualityResult;
    }

    private void populateUnionMemberNames(UnionTypeDescriptorNode unionType, List<String> unionTypeMembers)
            throws Exception {
        unionTypeMembers.add(getTypeName(unionType.rightTypeDesc()));
        if (unionType.leftTypeDesc() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode leftUnionType = (UnionTypeDescriptorNode) unionType.leftTypeDesc();
            populateUnionMemberNames(leftUnionType, unionTypeMembers);
        } else {
            unionTypeMembers.add(getTypeName(unionType.leftTypeDesc()));
        }
    }

    private RecordTypeEqualityResult isRecordTypeEquals(RecordTypeDescriptorNode prevRecordType,
                                                        RecordTypeDescriptorNode nextRecordType) throws Exception {
        MembersEqualityResult result = new MembersEqualityResult();
        RecordTypeEqualityResult recordTypeEquality =
                new RecordTypeEqualityResult(prevRecordType.recordKeyword().text(),
                        nextRecordType.recordKeyword().text(), prevRecordType.bodyStartDelimiter().text(),
                        nextRecordType.bodyStartDelimiter().text(), prevRecordType.bodyEndDelimiter().text(),
                        nextRecordType.bodyEndDelimiter().text());
//        if (!prevRecordType.recordKeyword().text().equals(nextRecordType.recordKeyword().text())) {
//            return result;
//        }
//        if (!prevRecordType.bodyStartDelimiter().text().equals(nextRecordType.bodyStartDelimiter().text()) ||
//                !prevRecordType.bodyEndDelimiter().text().equals(nextRecordType.bodyEndDelimiter().text())) {
//            return result;
//        }
        HashMap<Node, Boolean> nextRecordTypeFieldsAvailable = new HashMap<>();
        for (Node nextField : nextRecordType.fields()) {
            nextRecordTypeFieldsAvailable.put(nextField, true);
        }
        for (Node prevField : prevRecordType.fields()) {
            boolean foundPrevMatch = false;
            for (Node nextField : nextRecordType.fields()) {
                RecordFieldEqualityResult recordFieldEqualityResult = isRecordFieldEquals(prevField, nextField);
                if (recordFieldEqualityResult.isEqual()) {
                    foundPrevMatch = true;
                    break;
                } else if (recordFieldEqualityResult.isMatch()) {
                    foundPrevMatch = true;
                    if (!recordFieldEqualityResult.getTypeEquality().isEqual()) {
                        recordTypeEquality.addToTypeChangedRecordFields(recordFieldEqualityResult);
                    }
                    if (recordFieldEqualityResult.isDefaultValueRemoved()) {
                        recordTypeEquality.addToDefaultValueRemovedFields(recordFieldEqualityResult);
                    }
                    break;
                }
//                if (prevField instanceof RecordFieldNode && nextField instanceof RecordFieldNode) {
//                    RecordFieldNode prevRecordField = (RecordFieldNode) prevField;
//                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
//                    RecordFieldEqualityResult recordFieldEqualityResult =
//                    isRecordFieldEquals(prevRecordField, nextRecordField);
//                    if (recordFieldEqualityResult.isEqual()) {
//                        foundPrevMatch = true;
//                        break;
//                    } else if (recordFieldEqualityResult.isMatch()) {
//                        foundPrevMatch = true;
//                        if (!recordFieldEqualityResult.getTypeEquality().isEqual()) {
//                            recordTypeEquality.addToTypeChangedRecordFields(recordFieldEqualityResult);
//                        }
//                        break;
//                    }
//                } else if (prevField instanceof RecordFieldWithDefaultValueNode &&
//                        nextField instanceof RecordFieldWithDefaultValueNode) {
//                    RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue =
//                            (RecordFieldWithDefaultValueNode) prevField;
//                    RecordFieldWithDefaultValueNode nextRecordFieldWithDefaultValue =
//                            (RecordFieldWithDefaultValueNode) nextField;
//                    RecordFieldWithDefaultValueEqualityResult recordFieldWithDefaultValueEqualityResult =
//                            isRecordFieldWithDefaultValueEquals(prevRecordFieldWithDefaultValue,
//                                    nextRecordFieldWithDefaultValue);
//                    if (recordFieldWithDefaultValueEqualityResult.isEqual()) {
//                        foundPrevMatch = true;
//                        break;
//                    } else if (recordFieldWithDefaultValueEqualityResult.isMatch()) {
//                        foundPrevMatch = true;
//                        if (!recordFieldWithDefaultValueEqualityResult.getTypeEquality().isEqual()) {
//                            recordTypeEquality.
//                            addToTypeChangedRecordFieldsWithDefaultValues(recordFieldWithDefaultValueEqualityResult);
//                        }
//                        break;
//                    }
//                } else if (prevField instanceof RecordFieldWithDefaultValueNode &&
//                nextField instanceof RecordFieldNode) {
//                    RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue =
//                            (RecordFieldWithDefaultValueNode) prevField;
//                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
//                    if (prevRecordFieldWithDefaultValue.fieldName().text()
//                    .equals(nextRecordField.fieldName().text())) {
//                        TypeEqualityResult typeEqualityResult =
//                                isTypeEquals(prevRecordFieldWithDefaultValue.typeName(), nextRecordField.typeName());
//                        if ()
//                    }
//                }
            }
            if (!foundPrevMatch) {
                recordTypeEquality.addToRemovals(getRecordFieldName(prevField));
//                if (prevField instanceof RecordFieldNode) {
//                    RecordFieldNode nextRecordField = (RecordFieldNode) prevField;
//                    result.addToRemovals(nextRecordField.fieldName().text());
//
//                } else if (prevField instanceof RecordFieldWithDefaultValueNode) {
//                    RecordFieldWithDefaultValueNode nextRecordField = (RecordFieldWithDefaultValueNode) prevField;
//                    result.addToRemovals(nextRecordField.fieldName().text());
//                }
            }
        }
        for (Map.Entry<Node, Boolean> entry : nextRecordTypeFieldsAvailable.entrySet()) {
            Boolean available = entry.getValue();
            if (!available) {
                Node notAvailableField = entry.getKey();
                recordTypeEquality.addToAdditions(getRecordFieldName(notAvailableField));
                if (notAvailableField instanceof RecordFieldNode) {
                    RecordFieldNode notAvailableRecordField = (RecordFieldNode) notAvailableField;
                    recordTypeEquality.addToViolatedAdditions(notAvailableRecordField.fieldName().text());
                }
            }
        }
//        for (Node nextField : nextRecordType.fields()) {
//            boolean foundNextMatch = false;
//            for (Node prevField : prevRecordType.fields()) {
//                if (prevField instanceof RecordFieldNode && nextField instanceof RecordFieldNode) {
//                    RecordFieldNode prevRecordField = (RecordFieldNode) prevField;
//                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
//                    RecordFieldEqualityResult recordFieldEquals =
//                    isRecordFieldEquals(prevRecordField, nextRecordField);
//                    if (recordFieldEquals.isEqual()) {
//                        foundNextMatch = true;
//                        break;
//                    }
//                } else if (prevField instanceof RecordFieldWithDefaultValueNode &&
//                        nextField instanceof RecordFieldWithDefaultValueNode) {
//                    RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue =
//                            (RecordFieldWithDefaultValueNode) prevField;
//                    RecordFieldWithDefaultValueNode nextRecordFieldWithDefaultValue =
//                            (RecordFieldWithDefaultValueNode) nextField;
//                    RecordFieldWithDefaultValueEqualityResult recordFieldWithDefaultValueEquals =
//                            isRecordFieldWithDefaultValueEquals(prevRecordFieldWithDefaultValue,
//                                    nextRecordFieldWithDefaultValue);
//                    if (recordFieldWithDefaultValueEquals.isEqual()) {
//                        foundNextMatch = true;
//                        break;
//                    }
//                }
//            }
//            if (!foundNextMatch) {
//                if (nextField instanceof RecordFieldNode) {
//                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
//                    result.addToAdditions(nextRecordField.fieldName().text());
//                    result.addToViolatedAdditions(nextRecordField.fieldName().text());
//                } else if (nextField instanceof RecordFieldWithDefaultValueNode) {
//                    RecordFieldWithDefaultValueNode nextRecordField = (RecordFieldWithDefaultValueNode) nextField;
//                    result.addToAdditions(nextRecordField.fieldName().text());
//                }
//            }
//        }
        return recordTypeEquality;
    }

    private RecordFieldWithDefaultValueEqualityResult isRecordFieldWithDefaultValueEquals(
            RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue,
            RecordFieldWithDefaultValueNode nextRecordFieldWithDefaultValue) throws Exception {
        TypeEqualityResult typeEquality =
                isTypeEquals(prevRecordFieldWithDefaultValue.typeName(), nextRecordFieldWithDefaultValue.typeName());
        return new RecordFieldWithDefaultValueEqualityResult(typeEquality,
                prevRecordFieldWithDefaultValue.fieldName().text(), nextRecordFieldWithDefaultValue.fieldName().text(),
                prevRecordFieldWithDefaultValue.expression().toString(),
                nextRecordFieldWithDefaultValue.expression().toString());
    }

    private RecordFieldEqualityResult isRecordFieldEquals(Node prevRecordField, Node nextRecordField) throws Exception {
        TypeEqualityResult typeEquality =
                isTypeEquals(getRecordFieldType(prevRecordField), getRecordFieldType(nextRecordField));
        return new RecordFieldEqualityResult(typeEquality, getRecordFieldName(prevRecordField),
                getRecordFieldName(nextRecordField), getRecordFieldDefaultValue(prevRecordField),
                getRecordFieldDefaultValue(nextRecordField));
    }

    private String getRecordFieldDefaultValue(Node field) {
        if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.expression().toString();
        }
        return null;
    }

    private String getRecordFieldName(Node field) {
        if (field instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.fieldName().text();
        } else if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.fieldName().text();
        }
        return null;
    }

    private Node getRecordFieldType(Node field) {
        if (field instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.typeName();
        } else if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.typeName();
        }
        return null;
    }

//    private NodeList<Node> getServiceObjectNewMembers(ObjectTypeDescriptorNode prevServiceObject,
//                                                      ObjectTypeDescriptorNode nextServiceObject) throws Exception {
//        NodeList<Node> members = prevServiceObject.members();
//        for (Node nextMember : nextServiceObject.members()) {
//            boolean foundMatch = false;
//            for (Node prevMember : prevServiceObject.members()) {
//                if (prevMember instanceof TypeReferenceNode && nextMember instanceof TypeReferenceNode) {
//                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
//                    TypeReferenceNode nextTypeRefMember = (TypeReferenceNode) nextMember;
//                    TypeEqualityResult typeEquality =
//                            isTypeEquals(prevTypeRefMember.typeName(), nextTypeRefMember.typeName());
//                    if (typeEquality.isEqual()) {
//                        foundMatch = true;
//                        break;
//                    }
//                } else if (prevMember instanceof MethodDeclarationNode
//                && nextMember instanceof MethodDeclarationNode) {
//                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
//                    MethodDeclarationNode nextMethodDeclaration = (MethodDeclarationNode) nextMember;
//                    MethodDeclarationEqualityResult methodDeclarationEquals =
//                            isMethodDeclarationEquals(prevMethodDeclaration, nextMethodDeclaration);
//                    if (methodDeclarationEquals.isEqual()) {
//                        foundMatch = true;
//                        break;
//                    } else if (methodDeclarationEquals.isMatch()) {
//
//                    }
//                }
//            }
//            if (!foundMatch) {
//                members.add(nextMember);
//            }
//        }
//        return members;
//    }

    private ServiceObjectEqualityResult isServiceObjectEquals(ObjectTypeDescriptorNode prevServiceObject,
                                                              ObjectTypeDescriptorNode nextServiceObject)
            throws Exception {
        ServiceObjectEqualityResult serviceObjectEquality = new ServiceObjectEqualityResult();

        for (Node prevMember : prevServiceObject.members()) {
            boolean foundMatch = false;
            for (Node nextMember : nextServiceObject.members()) {
                if (prevMember instanceof TypeReferenceNode && nextMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
                    TypeReferenceNode nextTypeRefMember = (TypeReferenceNode) nextMember;
                    TypeEqualityResult typeEquality =
                            isTypeEquals(prevTypeRefMember.typeName(), nextTypeRefMember.typeName());
                    if (typeEquality.isEqual()) {
                        foundMatch = true;
                    }
                } else if (prevMember instanceof MethodDeclarationNode && nextMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
                    MethodDeclarationNode nextMethodDeclaration = (MethodDeclarationNode) nextMember;
                    MethodDeclarationEqualityResult methodDeclarationEquals =
                            isMethodDeclarationEquals(prevMethodDeclaration, nextMethodDeclaration);
                    if (methodDeclarationEquals.isEqual()) {
                        foundMatch = true;
                    } else if (methodDeclarationEquals.isMatch()) {
                        foundMatch = true;
                        serviceObjectEquality.addToUpdatedMethodDeclarations(methodDeclarationEquals);
                    }
                }
            }
            if (!foundMatch) {
                if (prevMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
                } else if (prevMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
                    serviceObjectEquality.addToRemovedMethodDeclarations(
                            getMethodDeclarationName(prevMethodDeclaration));
                }
            }
        }
        return serviceObjectEquality;
    }

    private MethodDeclarationEqualityResult isMethodDeclarationEquals(MethodDeclarationNode prevMethodDeclaration,
                                                                      MethodDeclarationNode nextMethodDeclaration)
            throws Exception {
        MethodDeclarationEqualityResult methodDeclarationEquality = new MethodDeclarationEqualityResult();
        methodDeclarationEquality.setPrevFunctionName(getMethodDeclarationName(prevMethodDeclaration));
        methodDeclarationEquality.setNextFunctionName(getMethodDeclarationName(nextMethodDeclaration));
        methodDeclarationEquality.setPrevQualifiers(prevMethodDeclaration.qualifierList());
        methodDeclarationEquality.setNextQualifiers(nextMethodDeclaration.qualifierList());
        methodDeclarationEquality.setPrevMethodType(prevMethodDeclaration.methodName().text());
        methodDeclarationEquality.setNextMethodType(nextMethodDeclaration.methodName().text());
        if (isRelativeResourcePathEquals(prevMethodDeclaration.relativeResourcePath(),
                nextMethodDeclaration.relativeResourcePath())) {
            methodDeclarationEquality.setRelativeResourcePathsEqual(true);
        }
        FunctionSignatureEqualityResult funcSignatureEquals =
                isFuncSignatureEquals(prevMethodDeclaration.methodSignature(), nextMethodDeclaration.methodSignature());
        methodDeclarationEquality.setFunctionSignatureEqualityResult(funcSignatureEquals);
        return methodDeclarationEquality;
    }

    private boolean isRelativeResourcePathEquals(NodeList<Node> prevRelativeResourcePath,
                                                 NodeList<Node> nextRelativeResourcePath) {
        if (!(prevRelativeResourcePath.size() == nextRelativeResourcePath.size())) {
            return false;
        }
        for (int i = 0; i < prevRelativeResourcePath.size(); i++) {
            Node prevRelativeResource = prevRelativeResourcePath.get(i);
            Node nextRelativeResource = nextRelativeResourcePath.get(i);
            if (prevRelativeResource instanceof IdentifierToken && nextRelativeResource instanceof IdentifierToken) {
                IdentifierToken prevIdentifier = (IdentifierToken) prevRelativeResource;
                IdentifierToken nextIdentifier = (IdentifierToken) nextRelativeResource;
                if (!prevIdentifier.text().equals(nextIdentifier.text())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isQualifierListEquals(NodeList<Token> prevQualifierList, NodeList<Token> nextQualifierList) {
        if (!(prevQualifierList.size() == nextQualifierList.size())) {
            return false;
        }
        for (int i = 0; i < prevQualifierList.size(); i++) {
            Token prevQualifier = prevQualifierList.get(i);
            Token nextQualifier = nextQualifierList.get(i);
            if (!prevQualifier.text().equals(nextQualifier.text())) {
                return false;
            }
        }
        return true;
    }

    private boolean isClassDefEquals(ClassDefinitionNode prevClassDef, ClassDefinitionNode nextClassDef)
            throws Exception {
        if (!prevClassDef.className().text().equals(nextClassDef.className().text())) {
            return false;
        }
        NodeList<Node> finalClassFuncDefinitions = createNodeList();
        MembersEqualityResult equalityResult = new MembersEqualityResult();
        HashMap<FunctionDefinitionNode, Boolean> nextClassMemberAvailable = new HashMap<>();
        for (Node nextClassMember : nextClassDef.members()) {
            if (nextClassMember instanceof FunctionDefinitionNode) {
                FunctionDefinitionNode nextClassFuncDef = (FunctionDefinitionNode) nextClassMember;
                nextClassMemberAvailable.put(nextClassFuncDef, false);
            } else if (nextClassMember instanceof TypeReferenceNode) {
                TypeReferenceNode nextClassTypeReference = (TypeReferenceNode) nextClassMember;
                finalClassFuncDefinitions = finalClassFuncDefinitions.add(nextClassTypeReference);
            }
        }
        for (Node prevClassMember : prevClassDef.members()) {
            boolean foundMatch = false;
            for (Node nextClassMember : nextClassDef.members()) {
                if (prevClassMember instanceof FunctionDefinitionNode &&
                        nextClassMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode nextClassFuncDef = (FunctionDefinitionNode) nextClassMember;
                    FunctionDefinitionNode prevClassFuncDef = (FunctionDefinitionNode) prevClassMember;
                    FunctionDefinitionEqualityResult funcDefEquals =
                            isFuncDefEquals(prevClassFuncDef, nextClassFuncDef);
                    if (funcDefEquals.isEqual()) {
                        foundMatch = true;
                        finalClassFuncDefinitions = finalClassFuncDefinitions.add(prevClassFuncDef);
                        nextClassMemberAvailable.put(nextClassFuncDef, true);
                        break;
                    } else if (funcDefEquals.isMatch()) {
                        foundMatch = true;
                        FunctionSignatureEqualityResult functionSignatureEquality =
                                funcDefEquals.getFunctionSignatureEqualityResult();
                        if (!funcDefEquals.getFunctionSignatureEqualityResult().getReturnTypeEqualityResult()
                                .isEqual()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE,
                                            prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                            funcDefEquals.getFunctionSignatureEqualityResult()
                                                    .getReturnTypeEqualityResult().getPrevType(),
                                            funcDefEquals.getFunctionSignatureEqualityResult()
                                                    .getReturnTypeEqualityResult().getNextType()));
                        }
                        if (!funcDefEquals.getFunctionSignatureEqualityResult().getAddedViolatedParameters()
                                .isEmpty()) {
                            for (String addedParameterName : funcDefEquals.getFunctionSignatureEqualityResult()
                                    .getAddedViolatedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_CLASS,
                                                prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                                addedParameterName));
                            }
                        }
                        if (!funcDefEquals.getFunctionSignatureEqualityResult().getRemovedParameters().isEmpty()) {
                            for (String removedParameterName : funcDefEquals.getFunctionSignatureEqualityResult()
                                    .getRemovedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE_CLASS,
                                                prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                                removedParameterName));
                            }
                        }
                        if (!funcDefEquals.getFunctionSignatureEqualityResult().getTypeChangedParameters().isEmpty()) {
                            for (ParameterEqualityResult parameterEquals :
                                    funcDefEquals.getFunctionSignatureEqualityResult()
                                            .getTypeChangedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_CLASS,
                                                prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                                parameterEquals.getPrevParameterName(),
                                                parameterEquals.getTypeEquality().getPrevType(),
                                                parameterEquals.getTypeEquality().getNextType()));
                            }
                        }
                        if (!functionSignatureEquality.getDefaultValueRemovedParameters().isEmpty()) {
                            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                                    functionSignatureEquality.getDefaultValueRemovedParameters()) {
                                breakingChangeWarnings.add(String.format(
                                        WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_CLASS_FUNC,
                                        prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                        defaultValueRemovedParameterEquality.getPrevParameterName(),
                                        defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
                            }
                        }
                        if (!funcDefEquals.isQualifierSimilar()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_CLASS,
                                            prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                            funcDefEquals.getPrevMainQualifier(),
                                            funcDefEquals.getNextMainQualifier()));
                        }
                        if (funcDefEquals.isGetAndSubscribeInterchanged()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_CLASS_METHOD,
                                            prevClassDef.className().text(), funcDefEquals.getPrevFunctionName(),
                                            funcDefEquals.getPrevMethodType(), funcDefEquals.getNextMethodType()));
                        }
                        FunctionDefinitionNode modifiedNextFuncDef = nextClassFuncDef.modify(nextClassFuncDef.kind(),
                                nextClassFuncDef.metadata().orElse(null), nextClassFuncDef.qualifierList(),
                                nextClassFuncDef.functionKeyword(), nextClassFuncDef.functionName(),
                                nextClassFuncDef.relativeResourcePath(), nextClassFuncDef.functionSignature(),
                                prevClassFuncDef.functionBody());
                        finalClassFuncDefinitions = finalClassFuncDefinitions.add(modifiedNextFuncDef);
                        nextClassMemberAvailable.put(nextClassFuncDef, true);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                if (prevClassMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevClassFuncDef = (FunctionDefinitionNode) prevClassMember;
                    equalityResult.addToRemovals(getFunctionName(prevClassFuncDef));
                }
            }
        }
        for (Map.Entry<FunctionDefinitionNode, Boolean> entry : nextClassMemberAvailable.entrySet()) {
            FunctionDefinitionNode nextClassFuncDef = entry.getKey();
            Boolean available = entry.getValue();
            if (!available) {
                finalClassFuncDefinitions = finalClassFuncDefinitions.add(nextClassFuncDef);
                equalityResult.addToAdditions(nextClassFuncDef.functionName().text());
            }
        }
        for (String removedFuncDefName : equalityResult.getRemovals()) {
            breakingChangeWarnings.add(
                    String.format(REMOVE_SERVICE_CLASS_FUNC_DEF_MESSAGE, prevClassDef.className().text(),
                            removedFuncDefName));
        }
        ClassDefinitionNode modifiedNextClassDef = nextClassDef.modify(prevClassDef.metadata().orElse(null),
                nextClassDef.visibilityQualifier().orElse(null), nextClassDef.classTypeQualifiers(),
                nextClassDef.classKeyword(), nextClassDef.className(), nextClassDef.openBrace(),
                finalClassFuncDefinitions, nextClassDef.closeBrace(), nextClassDef.semicolonToken().orElse(null));
        objectTypesModuleMembers.add(modifiedNextClassDef);
        return true;
    }

    private String getMethodDeclarationName(MethodDeclarationNode methodDeclaration) {
        if (methodDeclaration.qualifierList().size() > 0) {
            String firstQualifier = methodDeclaration.qualifierList().get(0).text();
            if (firstQualifier.equals(Constants.RESOURCE)) {
                Node methodDeclarationNameNode = methodDeclaration.relativeResourcePath().get(0);
                if (methodDeclarationNameNode instanceof IdentifierToken) {
                    IdentifierToken methodDeclarationName = (IdentifierToken) methodDeclarationNameNode;
                    return methodDeclarationName.text();
                }
            } else if (firstQualifier.equals(Constants.REMOTE)) {
                return methodDeclaration.methodName().text();
            }
        } else {
            return methodDeclaration.methodName().text();
        }
        return null;
    }

    private String getFunctionName(FunctionDefinitionNode functionDefinition) {
        String firstQualifier = functionDefinition.qualifierList().get(0).text();
        if (firstQualifier.equals(Constants.RESOURCE)) {
            Node functionNameNode = functionDefinition.relativeResourcePath().get(0);
            if (functionNameNode instanceof IdentifierToken) {
                IdentifierToken functionNameToken = (IdentifierToken) functionNameNode;
                return functionNameToken.text();
            }
        } else if (firstQualifier.equals(Constants.REMOTE)) {
            return functionDefinition.functionName().text();
        }
        return null;
    }

    private FunctionDefinitionEqualityResult isFuncDefEquals(FunctionDefinitionNode prevClassFuncDef,
                                                             FunctionDefinitionNode nextClassFuncDef) throws Exception {
        FunctionDefinitionEqualityResult functionDefinitionEquality = new FunctionDefinitionEqualityResult();
        functionDefinitionEquality.setPrevFunctionName(getFunctionName(prevClassFuncDef));
        functionDefinitionEquality.setNextFunctionName(getFunctionName(nextClassFuncDef));
        functionDefinitionEquality.setPrevQualifiers(prevClassFuncDef.qualifierList());
        functionDefinitionEquality.setNextQualifiers(nextClassFuncDef.qualifierList());
        functionDefinitionEquality.setPrevMethodType(prevClassFuncDef.functionName().text());
        functionDefinitionEquality.setNextMethodType(nextClassFuncDef.functionName().text());
//        if (!isQualifierListEquals(prevClassFuncDef.qualifierList(), nextClassFuncDef.qualifierList())) {
//            functionDefinitionEquality.setEqual(false);
//            functionDefinitionEquality.setQualifierListEqual(false);
////            return functionDefinitionEquality;
////            return false;
//        }
        functionDefinitionEquality.setFunctionNameEqual(
                prevClassFuncDef.functionName().text().equals(nextClassFuncDef.functionName().text()));
//        if (!prevClassFuncDef.functionName().text().equals(nextClassFuncDef.functionName().text())) {
//            functionDefinitionEquality.setEqual(false);
//            functionDefinitionEquality.setFunctionNameEqual(false);
////            return functionDefinitionEquality;
////            return false;
//        }
        functionDefinitionEquality.setRelativeResourcePathsEqual(
                isRelativeResourcePathEquals(prevClassFuncDef.relativeResourcePath(),
                        nextClassFuncDef.relativeResourcePath()));
//        if (!isRelativeResourcePathEquals(prevClassFuncDef.relativeResourcePath(),
//                nextClassFuncDef.relativeResourcePath())) {
//            functionDefinitionEquality.setEqual(false);
//            functionDefinitionEquality.setRelativeResourcePathsEqual(false);
////            return functionDefinitionEquality;
////            return false;
//        }
        FunctionSignatureEqualityResult funcSignatureEquals =
                isFuncSignatureEquals(prevClassFuncDef.functionSignature(), nextClassFuncDef.functionSignature());
        functionDefinitionEquality.setFunctionSignatureEqualityResult(funcSignatureEquals);
//        if (funcSignatureEquals.isEqual()) {
//            functionDefinitionEquality.setEqual(true);
//        }
        return functionDefinitionEquality;
    }

    private FunctionSignatureEqualityResult isFuncSignatureEquals(FunctionSignatureNode prevFunctionSignature,
                                                                  FunctionSignatureNode nextFunctionSignature)
            throws Exception {
        FunctionSignatureEqualityResult equalityResult = new FunctionSignatureEqualityResult();
        HashMap<ParameterNode, Boolean> nextParameterAvailable = new HashMap<>();
        for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
            nextParameterAvailable.put(nextParameter, false);
        }
        for (ParameterNode prevParameter : prevFunctionSignature.parameters()) {
            boolean foundMatch = false;
            for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
                ParameterEqualityResult parameterEquals = isParameterEquals(prevParameter, nextParameter);
                if (parameterEquals.isEqual()) {
                    foundMatch = true;
                    nextParameterAvailable.put(nextParameter, true);
                    break;
                } else {
                    if (parameterEquals.isMatch()) {
                        foundMatch = true;
                        nextParameterAvailable.put(nextParameter, true);
                        if (!parameterEquals.getTypeEquality().isEqual()) {
                            equalityResult.addToTypeChangedParameters(parameterEquals);
                        }
                        if (parameterEquals.isDefaultValueRemoved()) {
                            equalityResult.addToDefaultValueRemovedParameters(parameterEquals);
                        }
                    }
                }
            }
            if (!foundMatch) {
                equalityResult.addToRemovedParameters(getParameterName(prevParameter));
            }
        }
        for (Map.Entry<ParameterNode, Boolean> entry : nextParameterAvailable.entrySet()) {
            Boolean parameterAvailable = entry.getValue();
            if (!parameterAvailable) {
                ParameterNode newParameter = entry.getKey();
                String newParameterName = getParameterName(newParameter);
                if (newParameter instanceof RequiredParameterNode) {
                    equalityResult.addToAddedViolatedParameters(newParameterName);
                }
                equalityResult.addToAddedParameters(newParameterName);
            }
        }
        ReturnTypeDescriptorNode prevReturnType = prevFunctionSignature.returnTypeDesc().orElseThrow();
        ReturnTypeDescriptorNode nextReturnType = nextFunctionSignature.returnTypeDesc().orElseThrow();

        TypeEqualityResult returnTypeEqualityResult = isTypeEquals(prevReturnType.type(), nextReturnType.type());
        equalityResult.setTypeEqualityResult(returnTypeEqualityResult);
        return equalityResult;
    }

    private String getParameterName(ParameterNode parameter) {
        if (parameter instanceof RequiredParameterNode) {
            RequiredParameterNode requiredParameter = (RequiredParameterNode) parameter;
            return requiredParameter.paramName().orElse(null).text();
        } else if (parameter instanceof DefaultableParameterNode) {
            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
            return defaultableParameter.paramName().orElse(null).text();
        }
        return null;
    }

    private String getParameterDefaultValue(ParameterNode parameter) {
        if (parameter instanceof DefaultableParameterNode) {
            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
            Node defaultExpression = defaultableParameter.expression();
            return defaultExpression.toString();
        }
        return null;
    }

//    private String getParameterDefaultValue(ParameterNode parameter) {
//        if (parameter instanceof DefaultableParameterNode) {
//            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
//            return defaultableParameter.expression().;
//        }
//        return null;
//    }

    private TypeEqualityResult isTypeEquals(Node prevType, Node nextType) throws Exception {
        TypeEqualityResult equalityResult = new TypeEqualityResult();
        equalityResult.setPrevType(getTypeName(prevType));
        equalityResult.setNextType(getTypeName(nextType));
//        if (prevType instanceof BuiltinSimpleNameReferenceNode
//        && nextType instanceof BuiltinSimpleNameReferenceNode) {
//            BuiltinSimpleNameReferenceNode prevTypeName = (BuiltinSimpleNameReferenceNode) prevType;
//            BuiltinSimpleNameReferenceNode nextTypeName = (BuiltinSimpleNameReferenceNode) nextType;
//            equalityResult.setPrevType(prevTypeName.name().text());
//            equalityResult.setNextType(nextTypeName.name().text());
//            equalityResult.setEqual(prevTypeName.name().text().equals(nextTypeName.name().text()));
//            //            return prevTypeName.name().text().equals(nextTypeName.name().text());
//        } else if (prevType instanceof SimpleNameReferenceNode && nextType instanceof SimpleNameReferenceNode) {
//            SimpleNameReferenceNode prevTypeName = (SimpleNameReferenceNode) prevType;
//            SimpleNameReferenceNode nextTypeName = (SimpleNameReferenceNode) nextType;
//            equalityResult.setPrevType(prevTypeName.name().text());
//            equalityResult.setNextType(nextTypeName.name().text());
//            equalityResult.setEqual(prevTypeName.name().text().equals(nextTypeName.name().text()));
////            return prevTypeName.name().text().equals(nextTypeName.name().text());
//        } else if (prevType instanceof QualifiedNameReferenceNode && nextType instanceof QualifiedNameReferenceNode) {
//            QualifiedNameReferenceNode prevTypeName = (QualifiedNameReferenceNode) prevType;
//            QualifiedNameReferenceNode nextTypeName = (QualifiedNameReferenceNode) nextType;
//            if (!prevTypeName.modulePrefix().text().equals(nextTypeName.modulePrefix().text())) {
//                equalityResult.setEqual(false);
//            } else {
//                equalityResult.setPrevType(prevTypeName.identifier().text());
//                equalityResult.setNextType(nextTypeName.identifier().text());
//                equalityResult.setEqual(prevTypeName.identifier().text().equals(nextTypeName.identifier().text()));
//            }
////            return prevTypeName.identifier().text().equals(nextTypeName.identifier().text());
//        } else if (prevType instanceof OptionalTypeDescriptorNode && nextType instanceof OptionalTypeDescriptorNode) {
//            OptionalTypeDescriptorNode prevWrappedType = (OptionalTypeDescriptorNode) prevType;
//            OptionalTypeDescriptorNode nextWrappedType = (OptionalTypeDescriptorNode) nextType;
//            return isTypeEquals(prevWrappedType.typeDescriptor(), nextWrappedType.typeDescriptor());
//        } else if (prevType instanceof ArrayTypeDescriptorNode && nextType instanceof ArrayTypeDescriptorNode) {
//            ArrayTypeDescriptorNode prevWrappedType = (ArrayTypeDescriptorNode) prevType;
//            ArrayTypeDescriptorNode nextWrappedType = (ArrayTypeDescriptorNode) nextType;
//            return isTypeEquals(prevWrappedType.memberTypeDesc(), nextWrappedType.memberTypeDesc());
//        } else if (prevType instanceof StreamTypeDescriptorNode && nextType instanceof StreamTypeDescriptorNode) {
//            StreamTypeDescriptorNode prevWrappedType = (StreamTypeDescriptorNode) prevType;
//            StreamTypeDescriptorNode nextWrappedType = (StreamTypeDescriptorNode) nextType;
//            StreamTypeParamsNode prevStreamParams =
//                    (StreamTypeParamsNode) prevWrappedType.streamTypeParamsNode().orElseThrow();
//            StreamTypeParamsNode nextStreamParams =
//                    (StreamTypeParamsNode) nextWrappedType.streamTypeParamsNode().orElseThrow();
//            TypeEqualityResult typeEqualResult =
//                    isTypeEquals(prevStreamParams.leftTypeDescNode(), nextStreamParams.leftTypeDescNode());
//            if (!typeEqualResult.isEqual()) {
//                typeEqualResult.setEqual(false);
//            }
//            if (prevStreamParams.rightTypeDescNode().isPresent()
//            && nextStreamParams.rightTypeDescNode().isPresent()) {
//                return isTypeEquals(prevStreamParams.rightTypeDescNode().orElseThrow(),
//                        nextStreamParams.rightTypeDescNode().orElseThrow());
//            } else {
//                typeEqualResult.setEqual(true);
//            }
//            return typeEqualResult;
//        } else {
//            if (prevType.getClass().toString().equals(nextType.getClass().toString())) {
//                throw new Exception("No valid type: " + prevType.getClass());
//            }
//            equalityResult.setEqual(false);
//        }
        return equalityResult;
    }

    private String getTypeName(Node type) throws Exception {
        if (type instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode typeName = (BuiltinSimpleNameReferenceNode) type;
            return typeName.name().text();
        } else if (type instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode typeName = (SimpleNameReferenceNode) type;
            return typeName.name().text();
        } else if (type instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode typeName = (QualifiedNameReferenceNode) type;
            return typeName.identifier().text();
        } else if (type instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode wrappedType = (OptionalTypeDescriptorNode) type;
            return String.format("%s?", getTypeName(wrappedType.typeDescriptor()));
        } else if (type instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode wrappedType = (ArrayTypeDescriptorNode) type;
            return String.format("%s[]", getTypeName(wrappedType.memberTypeDesc()));
        } else if (type instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode wrappedType = (StreamTypeDescriptorNode) type;
            StreamTypeParamsNode streamParams = (StreamTypeParamsNode) wrappedType.streamTypeParamsNode().orElseThrow();
            String typesCommaSeparatedNames = getTypeName(streamParams.leftTypeDescNode());
            if (streamParams.rightTypeDescNode().isPresent()) {
                typesCommaSeparatedNames = typesCommaSeparatedNames.concat(",");
                typesCommaSeparatedNames =
                        typesCommaSeparatedNames.concat(getTypeName(streamParams.rightTypeDescNode().orElseThrow()));
            }
            return String.format("stream<%s>", typesCommaSeparatedNames);
        } else {
            throw new Exception("No valid type: " + type.getClass());
        }
    }

    private ParameterEqualityResult isParameterEquals(ParameterNode prevParameter, ParameterNode nextParameter)
            throws Exception {
        ParameterEqualityResult parameterEquality = new ParameterEqualityResult(prevParameter, nextParameter);
        parameterEquality.setPrevParameterName(getParameterName(prevParameter));
        parameterEquality.setNextParameterName(getParameterName(nextParameter));
        parameterEquality.setPrevParameterDefaultValue(getParameterDefaultValue(prevParameter));
        parameterEquality.setNextParameterDefaultValue(getParameterDefaultValue(nextParameter));
        TypeEqualityResult typeEquals = isTypeEquals(getParameterType(prevParameter), getParameterType(nextParameter));
        parameterEquality.setTypeEquality(typeEquals);
//        if (prevParameter instanceof RequiredParameterNode && nextParameter instanceof RequiredParameterNode) {
//            RequiredParameterNode prevRequiredParam = (RequiredParameterNode) prevParameter;
//            RequiredParameterNode nextRequiredParam = (RequiredParameterNode) nextParameter;
//
//            TypeEqualityResult typeEquality =
//              isTypeEquals(prevRequiredParam.typeName(), nextRequiredParam.typeName());
//            parameterEquality.setTypeEquality(typeEquality);
//            return parameterEquality;
//        } else if (prevParameter instanceof DefaultableParameterNode &&
//                nextParameter instanceof DefaultableParameterNode) {
//            DefaultableParameterNode prevDefaultableParam = (DefaultableParameterNode) prevParameter;
//            DefaultableParameterNode nextDefaultableParam = (DefaultableParameterNode) nextParameter;
//
//            TypeEqualityResult typeEquality =
//                    isTypeEquals(prevDefaultableParam.typeName(), nextDefaultableParam.typeName());
//            parameterEquality.setTypeEquality(typeEquality);
//            return parameterEquality;
//        } else {
//            parameterEquality.setTypeEquality(new TypeEqualityResult());
//        }
        return parameterEquality;
    }

    private Node getParameterType(ParameterNode parameter) throws Exception {
        if (parameter instanceof RequiredParameterNode) {
            RequiredParameterNode requiredParameter = (RequiredParameterNode) parameter;
            return requiredParameter.typeName();
        } else if (parameter instanceof DefaultableParameterNode) {
            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
            return defaultableParameter.typeName();
        } else {
            throw new Exception("No valid parameter type: " + parameter.getClass().toString());
        }
    }

    public TypeDefinitionRegistry getChangesOfDocuments(Document prevDocument, Document nextDocument) {
        TypeDefinitionRegistry changesTypeRegistry = new TypeDefinitionRegistry();
        for (Definition nextDefinition : nextDocument.getDefinitions()) {
            boolean isDefinitionFound = false;
            for (Definition prevDefinition : prevDocument.getDefinitions()) {
                if (nextDefinition.isEqualTo(prevDefinition)) {
                    isDefinitionFound = true;
                    break;
                }
            }
            if (!isDefinitionFound) {
                changesTypeRegistry.add((SDLDefinition) nextDefinition);
            }
        }
        return changesTypeRegistry;
    }

    public List<String> getBreakingChangeWarnings() {
        return breakingChangeWarnings;
    }
}
