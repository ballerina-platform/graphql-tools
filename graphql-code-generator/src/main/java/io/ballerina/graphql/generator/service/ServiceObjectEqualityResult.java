package io.ballerina.graphql.generator.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to store result comparing two service objects.
 */
public class ServiceObjectEqualityResult {
    private List<String> removedTypeReferences;
    private List<String> removedMethodDeclarations;
    private List<String> addedMethodDeclarations;
    private List<String> addedTypeReferences;
    private List<MethodDeclarationEqualityResult> updatedMethodDeclarations;

    public ServiceObjectEqualityResult() {
        removedTypeReferences = new ArrayList<>();
        removedMethodDeclarations = new ArrayList<>();
        addedMethodDeclarations = new ArrayList<>();
        addedTypeReferences = new ArrayList<>();
        updatedMethodDeclarations = new ArrayList<>();
    }

    public boolean isEqual() {
        return removedTypeReferences.isEmpty() && removedMethodDeclarations.isEmpty() &&
                addedMethodDeclarations.isEmpty() && updatedMethodDeclarations.isEmpty() &&
                addedTypeReferences.isEmpty();
    }

    public void addToRemovedMethodDeclarations(String methodDeclarationName) {
        removedMethodDeclarations.add(methodDeclarationName);
    }

    public void addToUpdatedMethodDeclarations(MethodDeclarationEqualityResult methodDeclarationEquality) {
        updatedMethodDeclarations.add(methodDeclarationEquality);
    }

    public void addToAddedMethodDeclarations(String methodDeclarationName) {
        addedMethodDeclarations.add(methodDeclarationName);
    }

    public List<String> getRemovedMethodDeclarations() {
        return removedMethodDeclarations;
    }

    public List<MethodDeclarationEqualityResult> getUpdatedMethodDeclarations() {
        return updatedMethodDeclarations;
    }
}
