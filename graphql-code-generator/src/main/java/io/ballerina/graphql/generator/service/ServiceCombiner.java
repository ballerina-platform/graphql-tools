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

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;

/**
 * Utility class for combining available service with generated service for a GraphQL schema.
 */
public class ServiceCombiner {
    private final ModulePartNode nextContentNode;
    private ModulePartNode prevContentNode;
    private Map<Node, Node> targetAndReplacement;

    public ServiceCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;
        this.targetAndReplacement = new HashMap<>();
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

        for (Map.Entry<Node, Node> entry : targetAndReplacement.entrySet()) {
            prevContentNode = prevContentNode.replace(entry.getKey(), entry.getValue());
        }

        NodeList<ModuleMemberDeclarationNode> combinedMembers = prevContentNode.members().addAll(newMembers);
        ModulePartNode contentNode =
                createModulePartNode(prevContentNode.imports(), combinedMembers, createToken(SyntaxKind.EOF_TOKEN));

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
        targetAndReplacement.put(prevEnumDec, nextEnumDec);
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
            List<Node> serviceObjectNewMembers = getServiceObjectNewMembers(prevServiceObject, nextServiceObject);
            if (serviceObjectNewMembers.size() > 0) {
                ObjectTypeDescriptorNode modifiedPrevServiceObject =
                        prevServiceObject.modify(prevServiceObject.objectTypeQualifiers(),
                                prevServiceObject.objectKeyword(), prevServiceObject.openBrace(),
                                prevServiceObject.members().addAll(serviceObjectNewMembers),
                                prevServiceObject.closeBrace());
                targetAndReplacement.put(prevTypeDef.typeDescriptor(), modifiedPrevServiceObject);
            }
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof DistinctTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof DistinctTypeDescriptorNode) {
            DistinctTypeDescriptorNode prevDistinctServiceObject =
                    (DistinctTypeDescriptorNode) prevTypeDef.typeDescriptor();
            DistinctTypeDescriptorNode nextDistinctServiceObject =
                    (DistinctTypeDescriptorNode) nextTypeDef.typeDescriptor();
            // is it need to check for distinct keyword equality?
            ObjectTypeDescriptorNode prevServiceObject =
                    (ObjectTypeDescriptorNode) prevDistinctServiceObject.typeDescriptor();
            ObjectTypeDescriptorNode nextServiceObject =
                    (ObjectTypeDescriptorNode) nextDistinctServiceObject.typeDescriptor();
            return isServiceObjectEquals(prevServiceObject, nextServiceObject);
        } else if (prevTypeDef.typeDescriptor() instanceof RecordTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode prevRecordType = (RecordTypeDescriptorNode) prevTypeDef.typeDescriptor();
            RecordTypeDescriptorNode nextRecordType = (RecordTypeDescriptorNode) nextTypeDef.typeDescriptor();
            targetAndReplacement.put(prevRecordType, nextRecordType);
            return true;
        } else if (prevTypeDef.typeDescriptor() instanceof UnionTypeDescriptorNode &&
                nextTypeDef.typeDescriptor() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionType = (UnionTypeDescriptorNode) prevTypeDef.typeDescriptor();
            UnionTypeDescriptorNode nextUnionType = (UnionTypeDescriptorNode) nextTypeDef.typeDescriptor();
            return isUnionTypeEquals(prevUnionType, nextUnionType);
        } else {
            return false;
        }
    }

    private boolean isUnionTypeEquals(UnionTypeDescriptorNode prevUnionType, UnionTypeDescriptorNode nextUnionType)
            throws Exception {
        if (prevUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode &&
                nextUnionType.leftTypeDesc() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionLeftType = (UnionTypeDescriptorNode) prevUnionType.leftTypeDesc();
            UnionTypeDescriptorNode nextUnionLeftType = (UnionTypeDescriptorNode) nextUnionType.leftTypeDesc();
            if (!isUnionTypeEquals(prevUnionLeftType, nextUnionLeftType)) {
                return false;
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

    private boolean isRecordTypeEquals(RecordTypeDescriptorNode prevRecordType, RecordTypeDescriptorNode nextRecordType)
            throws Exception {
        if (!prevRecordType.recordKeyword().text().equals(nextRecordType.recordKeyword().text())) {
            return false;
        }
        if (!prevRecordType.bodyStartDelimiter().text().equals(nextRecordType.bodyStartDelimiter().text()) ||
                !prevRecordType.bodyEndDelimiter().text().equals(nextRecordType.bodyEndDelimiter().text())) {
            return false;
        }
        for (Node nextField : nextRecordType.fields()) {
            boolean foundMatch = false;
            for (Node prevField : prevRecordType.fields()) {
                RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
                RecordFieldNode prevRecordField = (RecordFieldNode) prevField;
                if (isRecordFieldEquals(prevRecordField, nextRecordField)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                return false;
            }
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

    private List<Node> getServiceObjectNewMembers(ObjectTypeDescriptorNode prevServiceObject,
                                                  ObjectTypeDescriptorNode nextServiceObject) throws Exception {
        List<Node> members = new ArrayList<>();
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
                }
            }
            if (!foundMatch) {
                return false;
            }
        }
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
}
