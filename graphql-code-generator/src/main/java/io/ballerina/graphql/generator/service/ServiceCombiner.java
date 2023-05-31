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
    private static final String ADD_VIOLATED_INPUT_TYPE_FIELD_MESSAGE = "warning: In '%s' input type '%s' field is " +
            "introduced without a default value. This can brake available clients";
    private static final String REMOVE_ENUM_MEMBER_MESSAGE =
            "warning: In '%s' enum '%s' member has removed. This can " + "brake existing clients.";
    private static final String REMOVE_SERVICE_CLASS_FUNC_DEF_MESSAGE =
            "warning: In '%s' service class '%s' function " +
                    "definition has removed. This can brake available clients";
    private static final String WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE = "warning: In '%s' class " +
            "'%s' function definition return type has changed from '%s' to '%s'. This can brake existing clients.";
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
            if (isServiceObjectEquals(prevServiceObject, nextServiceObject)) {

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
            if (!isServiceObjectEquals(prevServiceObject, nextServiceObject)) {

            }
            interfaceTypesModuleMembers.add(nextTypeDef);
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof RecordTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode prevRecordType = (RecordTypeDescriptorNode) prevTypeDef.typeDescriptor();
            RecordTypeDescriptorNode nextRecordType = (RecordTypeDescriptorNode) nextTypeDef.typeDescriptor();
            MembersEqualityResult equalityResult = isRecordTypeEquals(prevRecordType, nextRecordType);
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
            }
            inputObjectTypesModuleMembers.add(nextTypeDef);
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof UnionTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionType = (UnionTypeDescriptorNode) prevTypeDef.typeDescriptor();
            UnionTypeDescriptorNode nextUnionType = (UnionTypeDescriptorNode) nextTypeDef.typeDescriptor();
            if (isUnionTypeEquals(prevUnionType, nextUnionType)) {

            }
            unionTypesModuleMembers.add(nextTypeDef);
            return true;
        }
        return true;
    }

    private boolean isUnionTypeEquals(UnionTypeDescriptorNode prevUnionType, UnionTypeDescriptorNode nextUnionType)
            throws Exception {
        TypeEqualityResult leftTypeEquality = isTypeEquals(prevUnionType.leftTypeDesc(), nextUnionType.leftTypeDesc());
        if (prevUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode &&
                nextUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionLeftType = (UnionTypeDescriptorNode) prevUnionType.leftTypeDesc();
            UnionTypeDescriptorNode nextUnionLeftType = (UnionTypeDescriptorNode) nextUnionType.leftTypeDesc();
            if (!isUnionTypeEquals(prevUnionLeftType, nextUnionLeftType)) {
                targetAndReplacement.put(prevUnionType, nextUnionType);
                return true;
            }
        } else if (!leftTypeEquality.isEqual()) {
            return false;
        }
        if (!prevUnionType.pipeToken().text().equals(nextUnionType.pipeToken().text())) {
            return false;
        }
        TypeEqualityResult rightTypeEquality =
                isTypeEquals(prevUnionType.rightTypeDesc(), nextUnionType.rightTypeDesc());
        if (!rightTypeEquality.isEqual()) {
            return false;
        }
        return true;
    }

    private MembersEqualityResult isRecordTypeEquals(RecordTypeDescriptorNode prevRecordType,
                                                     RecordTypeDescriptorNode nextRecordType) throws Exception {
        MembersEqualityResult result = new MembersEqualityResult();
        if (!prevRecordType.recordKeyword().text().equals(nextRecordType.recordKeyword().text())) {
            return result;
        }
        if (!prevRecordType.bodyStartDelimiter().text().equals(nextRecordType.bodyStartDelimiter().text()) ||
                !prevRecordType.bodyEndDelimiter().text().equals(nextRecordType.bodyEndDelimiter().text())) {
            return result;
        }
        for (Node prevField : prevRecordType.fields()) {
            boolean foundPrevMatch = false;
            for (Node nextField : nextRecordType.fields()) {
                if (prevField instanceof RecordFieldNode && nextField instanceof RecordFieldNode) {
                    RecordFieldNode prevRecordField = (RecordFieldNode) prevField;
                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
                    if (isRecordFieldEquals(prevRecordField, nextRecordField)) {
                        foundPrevMatch = true;
                        break;
                    }
                } else if (prevField instanceof RecordFieldWithDefaultValueNode &&
                        nextField instanceof RecordFieldWithDefaultValueNode) {
                    RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue =
                            (RecordFieldWithDefaultValueNode) prevField;
                    RecordFieldWithDefaultValueNode nextRecordFieldWithDefaultValue =
                            (RecordFieldWithDefaultValueNode) nextField;
                    if (isRecordFieldWithDefaultValueEquals(prevRecordFieldWithDefaultValue,
                            nextRecordFieldWithDefaultValue)) {
                        foundPrevMatch = true;
                        break;
                    }
                }
            }
            if (!foundPrevMatch) {
                if (prevField instanceof RecordFieldNode) {
                    RecordFieldNode nextRecordField = (RecordFieldNode) prevField;
                    result.addToRemovals(nextRecordField.fieldName().text());
                } else if (prevField instanceof RecordFieldWithDefaultValueNode) {
                    RecordFieldWithDefaultValueNode nextRecordField = (RecordFieldWithDefaultValueNode) prevField;
                    result.addToRemovals(nextRecordField.fieldName().text());
                }
            }
        }
        for (Node nextField : nextRecordType.fields()) {
            boolean foundNextMatch = false;
            for (Node prevField : prevRecordType.fields()) {
                if (prevField instanceof RecordFieldNode && nextField instanceof RecordFieldNode) {
                    RecordFieldNode prevRecordField = (RecordFieldNode) prevField;
                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;

                    if (isRecordFieldEquals(prevRecordField, nextRecordField)) {
                        foundNextMatch = true;
                        break;
                    }
                } else if (prevField instanceof RecordFieldWithDefaultValueNode &&
                        nextField instanceof RecordFieldWithDefaultValueNode) {
                    RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue =
                            (RecordFieldWithDefaultValueNode) prevField;
                    RecordFieldWithDefaultValueNode nextRecordFieldWithDefaultValue =
                            (RecordFieldWithDefaultValueNode) nextField;
                    if (isRecordFieldWithDefaultValueEquals(prevRecordFieldWithDefaultValue,
                            nextRecordFieldWithDefaultValue)) {
                        foundNextMatch = true;
                        break;
                    }
                }
            }
            if (!foundNextMatch) {
                if (nextField instanceof RecordFieldNode) {
                    RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
                    result.addToAdditions(nextRecordField.fieldName().text());
                    result.addToViolatedAdditions(nextRecordField.fieldName().text());
                } else if (nextField instanceof RecordFieldWithDefaultValueNode) {
                    RecordFieldWithDefaultValueNode nextRecordField = (RecordFieldWithDefaultValueNode) nextField;
                    result.addToAdditions(nextRecordField.fieldName().text());
                }
            }
        }
        return result;
    }

    private boolean isRecordFieldWithDefaultValueEquals(RecordFieldWithDefaultValueNode prevRecordFieldWithDefaultValue,
                                                        RecordFieldWithDefaultValueNode nextRecordFieldWithDefaultValue)
            throws Exception {
        TypeEqualityResult typeEquality =
                isTypeEquals(prevRecordFieldWithDefaultValue.typeName(), nextRecordFieldWithDefaultValue.typeName());
        if (!typeEquality.isEqual()) {
            return false;
        }
        if (!prevRecordFieldWithDefaultValue.fieldName().text()
                .equals(nextRecordFieldWithDefaultValue.fieldName().text())) {
            return false;
        }
        return true;
    }

    private boolean isRecordFieldEquals(RecordFieldNode prevRecordField, RecordFieldNode nextRecordField)
            throws Exception {
        TypeEqualityResult typeEquality = isTypeEquals(prevRecordField.typeName(), nextRecordField.typeName());
        if (!typeEquality.isEqual()) {
            return false;
        }
        if (!prevRecordField.fieldName().text().equals(nextRecordField.fieldName().text())) {
            return false;
        }
        return true;
    }

    private NodeList<Node> getServiceObjectNewMembers(ObjectTypeDescriptorNode prevServiceObject,
                                                      ObjectTypeDescriptorNode nextServiceObject) throws Exception {
        NodeList<Node> members = prevServiceObject.members();
        for (Node nextMember : nextServiceObject.members()) {
            boolean foundMatch = false;
            for (Node prevMember : prevServiceObject.members()) {
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
                    if (isMethodDeclarationEquals(prevMethodDeclaration, nextMethodDeclaration)) {
                        foundMatch = true;
                    }
                }
            }
            if (!foundMatch) {
                members.add(nextMember);
            }
        }
        return members;
    }

    private boolean isServiceObjectEquals(ObjectTypeDescriptorNode prevServiceObject,
                                          ObjectTypeDescriptorNode nextServiceObject) throws Exception {
        for (Node nextMember : nextServiceObject.members()) {
            boolean foundMatch = false;
            for (Node prevMember : prevServiceObject.members()) {
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
                    if (isMethodDeclarationEquals(prevMethodDeclaration, nextMethodDeclaration)) {
                        foundMatch = true;
                    }
                }
            }
            if (!foundMatch) {
                return false;
            }
        }
        return true;
    }

    private boolean isMethodDeclarationEquals(MethodDeclarationNode prevMethodDeclaration,
                                              MethodDeclarationNode nextMethodDeclaration) throws Exception {
        if (!isQualifierListEquals(prevMethodDeclaration.qualifierList(), nextMethodDeclaration.qualifierList())) {
            return false;
        }
        if (!prevMethodDeclaration.methodName().text().equals(nextMethodDeclaration.methodName().text())) {
            return false;
        }
        if (!isRelativeResourcePathEquals(prevMethodDeclaration.relativeResourcePath(),
                nextMethodDeclaration.relativeResourcePath())) {
            return false;
        }
        FunctionSignatureEqualityResult funcSignatureEquals =
                isFuncSignatureEquals(prevMethodDeclaration.methodSignature(), nextMethodDeclaration.methodSignature());
        return funcSignatureEquals.isEqual();
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

    private String getFunctionName(FunctionDefinitionNode functionDefinition) {
        String firstQualifier = functionDefinition.qualifierList().get(0).text();
        if (firstQualifier.equals(Constants.RESOURCE)) {
            Node functionNameNode = functionDefinition.relativeResourcePath().get(0);
            if (functionNameNode instanceof IdentifierToken) {
                IdentifierToken functionNameToken = (IdentifierToken) functionNameNode;
                return functionNameToken.text();
            }
        } else if (firstQualifier.equals(Constants.REMOTE)) {

        }
        return null;
    }

    private FunctionDefinitionEqualityResult isFuncDefEquals(FunctionDefinitionNode prevClassFuncDef,
                                                             FunctionDefinitionNode nextClassFuncDef) throws Exception {
        FunctionDefinitionEqualityResult functionDefinitionEquality = new FunctionDefinitionEqualityResult();
        functionDefinitionEquality.setPrevFunctionName(getFunctionName(prevClassFuncDef));
        functionDefinitionEquality.setNextFunctionName(getFunctionName(nextClassFuncDef));

        functionDefinitionEquality.setQualifierListEqual(
                isQualifierListEquals(prevClassFuncDef.qualifierList(), nextClassFuncDef.qualifierList()));
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
        if (prevFunctionSignature.parameters().size() != nextFunctionSignature.parameters().size()) {
            equalityResult.setEqual(false);
//            return false;
        }
        for (int i = 0; i < nextFunctionSignature.parameters().size(); i++) {
            ParameterNode nextParameter = nextFunctionSignature.parameters().get(i);
            ParameterNode prevParameter = prevFunctionSignature.parameters().get(i);
            ParameterEqualityResult parameterEquality = isParameterEquals(prevParameter, nextParameter);
            if (!parameterEquality.isEqual()) {
//                return false;
                equalityResult.setEqual(false);
            }
        }
        ReturnTypeDescriptorNode prevReturnType = prevFunctionSignature.returnTypeDesc().orElseThrow();
        ReturnTypeDescriptorNode nextReturnType = nextFunctionSignature.returnTypeDesc().orElseThrow();

        TypeEqualityResult returnTypeEqualityResult = isTypeEquals(prevReturnType.type(), nextReturnType.type());
        equalityResult.setTypeEqualityResult(returnTypeEqualityResult);
        if (returnTypeEqualityResult.isEqual()) {
            equalityResult.setEqual(true);
        } else {
            equalityResult.setEqual(false);
        }
        return equalityResult;
    }

    private TypeEqualityResult isTypeEquals(Node prevType, Node nextType) throws Exception {
        TypeEqualityResult equalityResult = new TypeEqualityResult();
        if (prevType instanceof BuiltinSimpleNameReferenceNode && nextType instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode prevTypeName = (BuiltinSimpleNameReferenceNode) prevType;
            BuiltinSimpleNameReferenceNode nextTypeName = (BuiltinSimpleNameReferenceNode) nextType;
            equalityResult.setPrevType(prevTypeName.name().text());
            equalityResult.setNextType(nextTypeName.name().text());
            equalityResult.setEqual(prevTypeName.name().text().equals(nextTypeName.name().text()));
            //            return prevTypeName.name().text().equals(nextTypeName.name().text());
        } else if (prevType instanceof SimpleNameReferenceNode && nextType instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode prevTypeName = (SimpleNameReferenceNode) prevType;
            SimpleNameReferenceNode nextTypeName = (SimpleNameReferenceNode) nextType;
            equalityResult.setPrevType(prevTypeName.name().text());
            equalityResult.setNextType(nextTypeName.name().text());
            equalityResult.setEqual(prevTypeName.name().text().equals(nextTypeName.name().text()));
//            return prevTypeName.name().text().equals(nextTypeName.name().text());
        } else if (prevType instanceof QualifiedNameReferenceNode && nextType instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode prevTypeName = (QualifiedNameReferenceNode) prevType;
            QualifiedNameReferenceNode nextTypeName = (QualifiedNameReferenceNode) nextType;
            if (!prevTypeName.modulePrefix().text().equals(nextTypeName.modulePrefix().text())) {
                equalityResult.setEqual(false);
            } else {
                equalityResult.setPrevType(prevTypeName.identifier().text());
                equalityResult.setNextType(nextTypeName.identifier().text());
                equalityResult.setEqual(prevTypeName.identifier().text().equals(nextTypeName.identifier().text()));
            }
//            return prevTypeName.identifier().text().equals(nextTypeName.identifier().text());
        } else if (prevType instanceof OptionalTypeDescriptorNode && nextType instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode prevWrappedType = (OptionalTypeDescriptorNode) prevType;
            OptionalTypeDescriptorNode nextWrappedType = (OptionalTypeDescriptorNode) nextType;
            return isTypeEquals(prevWrappedType.typeDescriptor(), nextWrappedType.typeDescriptor());
        } else if (prevType instanceof ArrayTypeDescriptorNode && nextType instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode prevWrappedType = (ArrayTypeDescriptorNode) prevType;
            ArrayTypeDescriptorNode nextWrappedType = (ArrayTypeDescriptorNode) nextType;
            return isTypeEquals(prevWrappedType.memberTypeDesc(), nextWrappedType.memberTypeDesc());
        } else if (prevType instanceof StreamTypeDescriptorNode && nextType instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode prevWrappedType = (StreamTypeDescriptorNode) prevType;
            StreamTypeDescriptorNode nextWrappedType = (StreamTypeDescriptorNode) nextType;
            StreamTypeParamsNode prevStreamParams =
                    (StreamTypeParamsNode) prevWrappedType.streamTypeParamsNode().orElseThrow();
            StreamTypeParamsNode nextStreamParams =
                    (StreamTypeParamsNode) nextWrappedType.streamTypeParamsNode().orElseThrow();
            TypeEqualityResult typeEqualResult =
                    isTypeEquals(prevStreamParams.leftTypeDescNode(), nextStreamParams.leftTypeDescNode());
            if (!typeEqualResult.isEqual()) {
                typeEqualResult.setEqual(false);
            }
            if (prevStreamParams.rightTypeDescNode().isPresent() && nextStreamParams.rightTypeDescNode().isPresent()) {
                return isTypeEquals(prevStreamParams.rightTypeDescNode().orElseThrow(),
                        nextStreamParams.rightTypeDescNode().orElseThrow());
            } else {
                typeEqualResult.setEqual(true);
            }
            return typeEqualResult;
        } else {
            if (prevType.getClass().toString().equals(nextType.getClass().toString())) {
                throw new Exception("No valid type: " + prevType.getClass());
            }
            equalityResult.setEqual(false);
        }
        return equalityResult;
    }

    private ParameterEqualityResult isParameterEquals(ParameterNode prevParameter, ParameterNode nextParameter)
            throws Exception {
        ParameterEqualityResult parameterEquality = new ParameterEqualityResult();
        if (prevParameter instanceof RequiredParameterNode && nextParameter instanceof RequiredParameterNode) {
            RequiredParameterNode prevRequiredParam = (RequiredParameterNode) prevParameter;
            RequiredParameterNode nextRequiredParam = (RequiredParameterNode) nextParameter;

            Token prevParamName = prevRequiredParam.paramName().orElseThrow();
            Token nextParamName = nextRequiredParam.paramName().orElseThrow();
            if (!prevParamName.text().equals(nextParamName.text())) {
                parameterEquality.setEqual(false);
//                return false;
            }
            TypeEqualityResult typeEquality = isTypeEquals(prevRequiredParam.typeName(), nextRequiredParam.typeName());
            parameterEquality.setTypeEquality(typeEquality);
            if (!typeEquality.isEqual()) {
                parameterEquality.setEqual(false);
//                return false;
            } else {
                parameterEquality.setEqual(true);
//                return true;
            }
            return parameterEquality;
        } else {
            throw new Exception(String.format("Invalid parameterNode: %s", prevParameter.getClass()));
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
