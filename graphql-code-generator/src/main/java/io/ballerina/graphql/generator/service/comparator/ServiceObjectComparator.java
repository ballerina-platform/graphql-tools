package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.graphql.generator.service.Constants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMainQualifier;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.isResolverMethod;


/**
 * Utility class to store result comparing two service objects.
 */
public class ServiceObjectComparator {
    private final ObjectTypeDescriptorNode prevObjectType;
    private final ObjectTypeDescriptorNode nextObjectType;
    private List<MethodDeclarationNode> removedMethodDeclarations;
    private List<MethodDeclarationNode> addedMethodDeclarations;
    private List<TypeReferenceNode> removedTypeReferences;
    private List<TypeReferenceNode> addedTypeReferences;
    private List<MethodDeclarationComparator> updatedMethodDeclarations;
    private List<Node> finalMembers;

    public ServiceObjectComparator(ObjectTypeDescriptorNode prevObjectType,
                                   ObjectTypeDescriptorNode nextObjectType) {
        this.prevObjectType = prevObjectType;
        this.nextObjectType = nextObjectType;
        this.removedTypeReferences = new ArrayList<>();
        this.removedMethodDeclarations = new ArrayList<>();
        this.addedMethodDeclarations = new ArrayList<>();
        this.addedTypeReferences = new ArrayList<>();
        this.updatedMethodDeclarations = new ArrayList<>();
        this.finalMembers = new ArrayList<>();
        separateMembers();
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextServiceObjectMemberAvailable = new LinkedHashMap<>();
        for (Node nextMember : this.nextObjectType.members()) {
            nextServiceObjectMemberAvailable.put(nextMember, false);
        }
        for (Node prevMember : this.prevObjectType.members()) {
            boolean foundMatch = false;
            for (Node nextMember : this.nextObjectType.members()) {
                if (prevMember instanceof TypeReferenceNode && nextMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
                    TypeReferenceNode nextTypeRefMember = (TypeReferenceNode) nextMember;
                    TypeComparator typeEquality =
                            new TypeComparator(prevTypeRefMember.typeName(), nextTypeRefMember.typeName());
                    if (typeEquality.isEqual()) {
                        foundMatch = true;
                        nextServiceObjectMemberAvailable.put(nextMember, true);
                        this.finalMembers.add(prevTypeRefMember);
                        break;
                    }
                } else if (prevMember instanceof MethodDeclarationNode && nextMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
                    MethodDeclarationNode nextMethodDeclaration = (MethodDeclarationNode) nextMember;
                    MethodDeclarationComparator methodDeclarationEquals =
                            new MethodDeclarationComparator(prevMethodDeclaration, nextMethodDeclaration);
                    if (methodDeclarationEquals.isEqual()) {
                        foundMatch = true;
                        nextServiceObjectMemberAvailable.put(nextMember, true);
                        this.finalMembers.add(prevMethodDeclaration);
                        break;
                    } else if (methodDeclarationEquals.isMatch()) {
                        foundMatch = true;
                        nextServiceObjectMemberAvailable.put(nextMember, true);
                        this.finalMembers.add(methodDeclarationEquals.generateCombinedMethodDeclaration());
                        this.updatedMethodDeclarations.add(methodDeclarationEquals);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                if (prevMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevMember;
                    this.finalMembers.add(prevTypeRefMember);
                } else if (prevMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode prevMethodDeclaration = (MethodDeclarationNode) prevMember;
                    if (isResolverMethod(prevMethodDeclaration)) {
                        this.removedMethodDeclarations.add(prevMethodDeclaration);
                    } else {
                        this.finalMembers.add(prevMethodDeclaration);
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
                    this.finalMembers.add(nextTypeRefMember);
                } else if (newServiceObjectMember instanceof MethodDeclarationNode) {
                    MethodDeclarationNode nextMethodDeclaration = (MethodDeclarationNode) newServiceObjectMember;
                    this.finalMembers.add(nextMethodDeclaration);
                }
            }
        }
    }

    public boolean isEqual() {
        return this.removedTypeReferences.isEmpty() && this.removedMethodDeclarations.isEmpty() &&
                this.addedMethodDeclarations.isEmpty() && this.updatedMethodDeclarations.isEmpty() &&
                this.addedTypeReferences.isEmpty();
    }

    public ObjectTypeDescriptorNode generateCombinedObjectTypeDescriptor() {
        return this.prevObjectType.modify(this.prevObjectType.objectTypeQualifiers(),
                this.nextObjectType.objectKeyword(), this.prevObjectType.openBrace(),
                createNodeList(this.finalMembers), this.prevObjectType.closeBrace());
    }

    public List<MethodDeclarationComparator> getUpdatedMethodDeclarations() {
        return this.updatedMethodDeclarations;
    }

    public List<MethodDeclarationNode> getRemovedResolverMethodDeclarations() {
        List<MethodDeclarationNode> removedResolverMethodDeclarations = new ArrayList<>();
        for (MethodDeclarationNode removedMethodDeclaration : this.removedMethodDeclarations) {
            Token mainQualifier = getMainQualifier(removedMethodDeclaration.qualifierList());
            if (mainQualifier != null && (mainQualifier.text().equals(Constants.RESOURCE) ||
                    mainQualifier.text().equals(Constants.REMOTE))) {
                removedResolverMethodDeclarations.add(removedMethodDeclaration);
            }
        }
        return removedResolverMethodDeclarations;
    }
}
