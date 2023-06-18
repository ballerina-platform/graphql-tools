package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMainQualifier;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isMethodDeclarationEquals;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isResolverMethod;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isTypeEquals;


/**
 * Utility class to store result comparing two service objects.
 */
public class ServiceObjectEqualityResult {
    private final ObjectTypeDescriptorNode prevObjectType;
    private final ObjectTypeDescriptorNode nextObjectType;
    private List<MethodDeclarationNode> removedMethodDeclarations;
    private List<MethodDeclarationNode> addedMethodDeclarations;
    private List<TypeReferenceNode> removedTypeReferences;
    private List<TypeReferenceNode> addedTypeReferences;
    private List<MethodDeclarationEqualityResult> updatedMethodDeclarations;
    private List<Node> finalMembers;

    public ServiceObjectEqualityResult(ObjectTypeDescriptorNode prevObjectType,
                                       ObjectTypeDescriptorNode nextObjectType) {
        removedTypeReferences = new ArrayList<>();
        removedMethodDeclarations = new ArrayList<>();
        addedMethodDeclarations = new ArrayList<>();
        addedTypeReferences = new ArrayList<>();
        updatedMethodDeclarations = new ArrayList<>();
        finalMembers = new ArrayList<>();
        this.prevObjectType = prevObjectType;
        this.nextObjectType = nextObjectType;
        separateMembers();
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextServiceObjectMemberAvailable = new LinkedHashMap<>();
        for (Node nextMember : nextObjectType.members()) {
            nextServiceObjectMemberAvailable.put(nextMember, false);
        }
        for (Node prevMember : prevObjectType.members()) {
            boolean foundMatch = false;
            for (Node nextMember : nextObjectType.members()) {
                if (prevMember instanceof TypeReferenceNode && nextMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
                    TypeReferenceNode nextTypeRefMember = (TypeReferenceNode) nextMember;
                    TypeEqualityResult typeEquality =
                            isTypeEquals(prevTypeRefMember.typeName(), nextTypeRefMember.typeName());
                    if (typeEquality.isEqual()) {
                        foundMatch = true;
                        nextServiceObjectMemberAvailable.put(nextMember, true);
                        finalMembers.add(prevTypeRefMember);
                        break;
                    }
                } else if (prevMember instanceof MethodDeclarationNode && nextMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
                    MethodDeclarationNode nextMethodDeclaration = (MethodDeclarationNode) nextMember;
                    MethodDeclarationEqualityResult methodDeclarationEquals =
                            isMethodDeclarationEquals(prevMethodDeclaration, nextMethodDeclaration);
                    if (methodDeclarationEquals.isEqual()) {
                        foundMatch = true;
                        nextServiceObjectMemberAvailable.put(nextMember, true);
                        finalMembers.add(prevMethodDeclaration);
                        break;
                    } else if (methodDeclarationEquals.isMatch()) {
                        foundMatch = true;
                        nextServiceObjectMemberAvailable.put(nextMember, true);
                        finalMembers.add(methodDeclarationEquals.generateCombinedMethodDeclaration());
                        updatedMethodDeclarations.add(methodDeclarationEquals);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                if (prevMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
                    finalMembers.add(prevTypeRefMember);
                } else if (prevMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
                    if (isResolverMethod(prevMethodDeclaration)) {
                        removedMethodDeclarations.add(prevMethodDeclaration);
                    } else {
                        finalMembers.add(prevMethodDeclaration);
                    }
                }
            }
        }
        for (Map.Entry<Node, Boolean> nextMemberAvailableEntry : nextServiceObjectMemberAvailable.entrySet()) {
            Boolean nextMemberAvailable = nextMemberAvailableEntry.getValue();
            if (!nextMemberAvailable) {
                Node newServiceObjectMember = nextMemberAvailableEntry.getKey();
                if (newServiceObjectMember instanceof TypeReferenceNode) {
                    TypeReferenceNode nextTypeRefMember = (TypeReferenceNode) newServiceObjectMember;
                    finalMembers.add(nextTypeRefMember);
                } else if (newServiceObjectMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode nextMethodDeclaration = (MethodDeclarationNode) newServiceObjectMember;
                    finalMembers.add(nextMethodDeclaration);
                }
            }
        }
    }

    public boolean isEqual() {
        return removedTypeReferences.isEmpty() && removedMethodDeclarations.isEmpty() &&
                addedMethodDeclarations.isEmpty() && updatedMethodDeclarations.isEmpty() &&
                addedTypeReferences.isEmpty();
    }

    public void addToRemovedMethodDeclarations(MethodDeclarationNode methodDeclarationName) {
        removedMethodDeclarations.add(methodDeclarationName);
    }

    public void addToUpdatedMethodDeclarations(MethodDeclarationEqualityResult methodDeclarationEquality) {
        updatedMethodDeclarations.add(methodDeclarationEquality);
    }

    public ObjectTypeDescriptorNode generateCombinedObjectTypeDescriptor() {
        return prevObjectType.modify(prevObjectType.objectTypeQualifiers(), nextObjectType.objectKeyword(),
                nextObjectType.openBrace(), createNodeList(finalMembers), nextObjectType.closeBrace());
    }

    public List<MethodDeclarationNode> getRemovedMethodDeclarations() {
        return removedMethodDeclarations;
    }

    public List<MethodDeclarationEqualityResult> getUpdatedMethodDeclarations() {
        return updatedMethodDeclarations;
    }

    public List<MethodDeclarationNode> getRemovedResolverMethodDeclarations() {
        List<MethodDeclarationNode> removedResolverMethodDeclarations = new ArrayList<>();
        for (MethodDeclarationNode removedMethodDeclaration : removedMethodDeclarations) {
            Token mainQualifier = getMainQualifier(removedMethodDeclaration.qualifierList());
            if (mainQualifier != null && (mainQualifier.text().equals(Constants.RESOURCE) ||
                    mainQualifier.text().equals(Constants.REMOTE))) {
                removedResolverMethodDeclarations.add(removedMethodDeclaration);
            }
        }
        return removedResolverMethodDeclarations;
    }
}
