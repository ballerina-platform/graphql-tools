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
    private static final String removeInputTypeFieldMessage =
            "warning: In '%s' input type '%s' field has removed. This can brake clients";
    private static final String addViolatedInputTypeFieldMessage = "warning: In '%s' input type '%s' field is " +
            "introduced without a default value. This can brake available clients";
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
//
//        for (Node nextEnumMemberNode : nextEnumDec.enumMemberList()) {
//            boolean foundMatch = false;
//            for (Node prevEnumMemberNode : prevEnumDec.enumMemberList()) {
//                if (nextEnumMemberNode instanceof EnumMemberNode && prevEnumMemberNode instanceof EnumMemberNode) {
//                    EnumMemberNode nextEnumMember = (EnumMemberNode) nextEnumMemberNode;
//                    EnumMemberNode prevEnumMember = (EnumMemberNode) prevEnumMemberNode;
//                    if (nextEnumMember.identifier().text().equals(prevEnumMember.identifier().text())) {
//                        foundMatch = true;
//                    }
//                }
//            }
//            if (!foundMatch) {
//                return false;
//            }
//        }
        return true;
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
            TypeEqualityResult equalityResult = isRecordTypeEquals(prevRecordType, nextRecordType);
            if (!equalityResult.isEqual()) {
//                TypeDefinitionNode modifiedPrevRecordType = prevTypeDef.modify(prevTypeDef.metadata().orElse(null),
//                        prevTypeDef.visibilityQualifier().orElse(null),
//                        prevTypeDef.typeKeyword(), prevTypeDef.typeName(), nextRecordType,
//                        prevTypeDef.semicolonToken());
//                inputObjectTypesModuleMembers.add(modifiedPrevRecordType);
                for (String removedField : equalityResult.getRemovals()) {
                    breakingChangeWarnings.add(
                            String.format(removeInputTypeFieldMessage, prevTypeDef.typeName().text(), removedField));
                }
                for (String addedViolatedField : equalityResult.getViolatedAdditions()) {
                    breakingChangeWarnings.add(
                            String.format(addViolatedInputTypeFieldMessage, prevTypeDef.typeName().text(),
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
        if (prevUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode &&
                nextUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionLeftType = (UnionTypeDescriptorNode) prevUnionType.leftTypeDesc();
            UnionTypeDescriptorNode nextUnionLeftType = (UnionTypeDescriptorNode) nextUnionType.leftTypeDesc();
            if (!isUnionTypeEquals(prevUnionLeftType, nextUnionLeftType)) {
                targetAndReplacement.put(prevUnionType, nextUnionType);
                return true;
            }
        } else if (!isTypeEquals(prevUnionType.leftTypeDesc(), nextUnionType.leftTypeDesc())) {
            return false;
        }
        if (!prevUnionType.pipeToken().text().equals(nextUnionType.pipeToken().text())) {
            return false;
        }
        if (!isTypeEquals(prevUnionType.rightTypeDesc(), nextUnionType.rightTypeDesc())) {
            return false;
        }
        return true;
    }

    private TypeEqualityResult isRecordTypeEquals(RecordTypeDescriptorNode prevRecordType,
                                                  RecordTypeDescriptorNode nextRecordType) throws Exception {
        TypeEqualityResult result = new TypeEqualityResult();
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
        if (!isTypeEquals(prevRecordFieldWithDefaultValue.typeName(), nextRecordFieldWithDefaultValue.typeName())) {
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
        if (!isTypeEquals(prevRecordField.typeName(), nextRecordField.typeName())) {
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
                    if (isTypeEquals(prevTypeRefMember.typeName(), nextTypeRefMember.typeName())) {
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
                    if (isTypeEquals(prevTypeRefMember.typeName(), nextTypeRefMember.typeName())) {
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
        return isFuncSignatureEquals(prevMethodDeclaration.methodSignature(), nextMethodDeclaration.methodSignature());
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
        NodeList<Node> updatedPrevClassFuncDefinitions = prevClassDef.members();
        for (Node nextClassMember : nextClassDef.members()) {
            if (!(nextClassMember instanceof FunctionDefinitionNode)) {
                continue;
            }
            FunctionDefinitionNode nextClassFuncDef = (FunctionDefinitionNode) nextClassMember;
            boolean foundMatch = false;
            for (Node prevClassMember : prevClassDef.members()) {
                if (!(prevClassMember instanceof FunctionDefinitionNode)) {
                    continue;
                }
                FunctionDefinitionNode prevClassFuncDef = (FunctionDefinitionNode) prevClassMember;
                if (isFuncDefEquals(prevClassFuncDef, nextClassFuncDef)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                updatedPrevClassFuncDefinitions = updatedPrevClassFuncDefinitions.add(nextClassFuncDef);
            }
        }
        ClassDefinitionNode modifiedPrevClassDef = prevClassDef.modify(prevClassDef.metadata().orElse(null),
                prevClassDef.visibilityQualifier().orElse(null), prevClassDef.classTypeQualifiers(),
                prevClassDef.classKeyword(), prevClassDef.className(), prevClassDef.openBrace(),
                updatedPrevClassFuncDefinitions, prevClassDef.closeBrace(), prevClassDef.semicolonToken().orElse(null));
        objectTypesModuleMembers.add(modifiedPrevClassDef);
        return true;
    }

    private boolean isFuncDefEquals(FunctionDefinitionNode prevClassFuncDef, FunctionDefinitionNode nextClassFuncDef)
            throws Exception {
        if (!isQualifierListEquals(prevClassFuncDef.qualifierList(), nextClassFuncDef.qualifierList())) {
            return false;
        }
        if (!prevClassFuncDef.functionName().text().equals(nextClassFuncDef.functionName().text())) {
            return false;
        }
        if (!isRelativeResourcePathEquals(prevClassFuncDef.relativeResourcePath(),
                nextClassFuncDef.relativeResourcePath())) {
            return false;
        }
        return isFuncSignatureEquals(prevClassFuncDef.functionSignature(), nextClassFuncDef.functionSignature());
    }

    private boolean isFuncSignatureEquals(FunctionSignatureNode prevFunctionSignature,
                                          FunctionSignatureNode nextFunctionSignature) throws Exception {
        if (prevFunctionSignature.parameters().size() != nextFunctionSignature.parameters().size()) {
            return false;
        }
        for (int i = 0; i < nextFunctionSignature.parameters().size(); i++) {
            ParameterNode nextParameter = nextFunctionSignature.parameters().get(i);
            ParameterNode prevParameter = prevFunctionSignature.parameters().get(i);
            if (!isParameterEquals(prevParameter, nextParameter)) {
                return false;
            }
        }
        ReturnTypeDescriptorNode prevReturnType = prevFunctionSignature.returnTypeDesc().orElseThrow();
        ReturnTypeDescriptorNode nextReturnType = nextFunctionSignature.returnTypeDesc().orElseThrow();

        return isTypeEquals(prevReturnType.type(), nextReturnType.type());
    }

    private boolean isTypeEquals(Node prevType, Node nextType) throws Exception {
        if (prevType instanceof BuiltinSimpleNameReferenceNode && nextType instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode prevTypeName = (BuiltinSimpleNameReferenceNode) prevType;
            BuiltinSimpleNameReferenceNode nextTypeName = (BuiltinSimpleNameReferenceNode) nextType;
            return prevTypeName.name().text().equals(nextTypeName.name().text());
        } else if (prevType instanceof SimpleNameReferenceNode && nextType instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode prevTypeName = (SimpleNameReferenceNode) prevType;
            SimpleNameReferenceNode nextTypeName = (SimpleNameReferenceNode) nextType;
            return prevTypeName.name().text().equals(nextTypeName.name().text());
        } else if (prevType instanceof QualifiedNameReferenceNode && nextType instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode prevTypeName = (QualifiedNameReferenceNode) prevType;
            QualifiedNameReferenceNode nextTypeName = (QualifiedNameReferenceNode) nextType;
            if (!prevTypeName.modulePrefix().text().equals(nextTypeName.modulePrefix().text())) {
                return false;
            }
            return prevTypeName.identifier().text().equals(nextTypeName.identifier().text());
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
            if (!isTypeEquals(prevStreamParams.leftTypeDescNode(), nextStreamParams.leftTypeDescNode())) {
                return false;
            }
            if (prevStreamParams.rightTypeDescNode().isPresent() && nextStreamParams.rightTypeDescNode().isPresent()) {
                return isTypeEquals(prevStreamParams.rightTypeDescNode().orElseThrow(),
                        nextStreamParams.rightTypeDescNode().orElseThrow());
            } else {
                return true;
            }
        } else {
            if (prevType.getClass().toString().equals(nextType.getClass().toString())) {
                throw new Exception("No valid type: " + prevType.getClass());
            }
            return false;
        }
    }

    private boolean isParameterEquals(ParameterNode prevParameter, ParameterNode nextParameter) throws Exception {
        if (prevParameter instanceof RequiredParameterNode && nextParameter instanceof RequiredParameterNode) {
            RequiredParameterNode prevRequiredParam = (RequiredParameterNode) prevParameter;
            RequiredParameterNode nextRequiredParam = (RequiredParameterNode) nextParameter;

            Token prevParamName = prevRequiredParam.paramName().orElseThrow();
            Token nextParamName = nextRequiredParam.paramName().orElseThrow();
            if (!prevParamName.text().equals(nextParamName.text())) {
                return false;
            }
            if (!isTypeEquals(prevRequiredParam.typeName(), nextRequiredParam.typeName())) {
                return false;
            } else {
                return true;
            }
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
