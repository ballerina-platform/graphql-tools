package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to store result comparing two service objects.
 */
public class ServiceObjectEqualityResult {
    private List<MethodDeclarationNode> keptMethodDeclarations;
    private List<MethodDeclarationNode> removedMethodDeclarations;
    private List<MethodDeclarationNode> addedMethodDeclarations;
    private List<TypeReferenceNode> keptTypeReferences;
    private List<TypeReferenceNode> removedTypeReferences;
    private List<TypeReferenceNode> addedTypeReferences;
    private List<MethodDeclarationEqualityResult> updatedMethodDeclarations;
    private List<MethodDeclarationEqualityResult> qualifierListChangedMethodDeclarations;

    public ServiceObjectEqualityResult() {
        removedTypeReferences = new ArrayList<>();
        removedMethodDeclarations = new ArrayList<>();
        addedMethodDeclarations = new ArrayList<>();
        keptMethodDeclarations = new ArrayList<>();
        addedTypeReferences = new ArrayList<>();
        keptTypeReferences = new ArrayList<>();
        updatedMethodDeclarations = new ArrayList<>();
    }

    public boolean isEqual() {
        return removedTypeReferences.isEmpty() && removedMethodDeclarations.isEmpty() &&
                addedMethodDeclarations.isEmpty() && updatedMethodDeclarations.isEmpty() &&
                addedTypeReferences.isEmpty();
    }

    public void addToRemovedMethodDeclarations(MethodDeclarationNode methodDeclarationName) {
        removedMethodDeclarations.add(methodDeclarationName);
    }

    public void addToKeptMethodDeclarations(MethodDeclarationNode methodDeclaration) {
        keptMethodDeclarations.add(methodDeclaration);
    }

    public void addToUpdatedMethodDeclarations(MethodDeclarationEqualityResult methodDeclarationEquality) {
        updatedMethodDeclarations.add(methodDeclarationEquality);
    }

    public void addToAddedMethodDeclarations(MethodDeclarationNode methodDeclarationName) {
        addedMethodDeclarations.add(methodDeclarationName);
    }

    public void addToQualifierListChangedMethodDeclarations(MethodDeclarationEqualityResult methodDeclarationEquality) {
        qualifierListChangedMethodDeclarations.add(methodDeclarationEquality);
    }

    public void addToRemovedTypeReferences(TypeReferenceNode typeReference) {
        removedTypeReferences.add(typeReference);
    }

    public void addToKeptTypeReferences(TypeReferenceNode typeReference) {
        keptTypeReferences.add(typeReference);
    }

    public void addToAddedTypeReferences(TypeReferenceNode typeReference) {
        addedTypeReferences.add(typeReference);
    }

    public List<Node> generateCombinedMembers() {
        List<Node> combinedMembers = new ArrayList<>();
        combinedMembers.addAll(keptTypeReferences);
        combinedMembers.addAll(removedTypeReferences);
        combinedMembers.addAll(addedTypeReferences);
        combinedMembers.addAll(keptMethodDeclarations);
        combinedMembers.addAll(addedMethodDeclarations);
        for (MethodDeclarationNode removedMethodDeclaration : removedMethodDeclarations) {
            if (removedMethodDeclaration.qualifierList().size() == 0) {
                combinedMembers.add(removedMethodDeclaration);
            }
        }
        for (MethodDeclarationEqualityResult updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            combinedMembers.add(updatedMethodDeclarationEquality.generateCombinedMethodDeclaration());
        }
        return combinedMembers;
    }

    public List<MethodDeclarationNode> getAddedMethodDeclarations() {
        return addedMethodDeclarations;
    }

    public List<MethodDeclarationNode> getRemovedMethodDeclarations() {
        return removedMethodDeclarations;
    }

    public List<MethodDeclarationEqualityResult> getUpdatedMethodDeclarations() {
        return updatedMethodDeclarations;
    }

    public List<MethodDeclarationNode> getKeptMethodDeclarations() {
        return keptMethodDeclarations;
    }

    public List<TypeReferenceNode> getKeptTypeReferences() {
        return keptTypeReferences;
    }

    public List<TypeReferenceNode> getRemovedTypeReferences() {
        return removedTypeReferences;
    }

    public List<TypeReferenceNode> getAddedTypeReferences() {
        return addedTypeReferences;
    }

    public List<MethodDeclarationNode> getRemovedResolverMethodDeclarations() {
        List<MethodDeclarationNode> removedResolverMethodDeclarations = new ArrayList<>();
        for (MethodDeclarationNode removedMethodDeclaration : removedMethodDeclarations) {
            String mainQualifier = BaseCombiner.getMainQualifier(removedMethodDeclaration.qualifierList());
            if (mainQualifier != null && (mainQualifier.equals(Constants.RESOURCE) ||
                    mainQualifier.equals(Constants.REMOTE))) {
                removedResolverMethodDeclarations.add(removedMethodDeclaration);
            }
        }
        return removedResolverMethodDeclarations;
    }
}
