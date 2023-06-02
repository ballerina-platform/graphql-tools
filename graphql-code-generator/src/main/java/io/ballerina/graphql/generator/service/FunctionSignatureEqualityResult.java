package io.ballerina.graphql.generator.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to store result comparing two function signatures.
 */
public class FunctionSignatureEqualityResult {
    private List<String> addedParameters;
    private List<String> addedViolatedParameters;
    private List<String> removedParameters;
    private List<ParameterEqualityResult> typeChangedParameters;
    private TypeEqualityResult returnTypeEqualityResult;


    public FunctionSignatureEqualityResult() {
        addedParameters = new ArrayList<>();
        addedViolatedParameters = new ArrayList<>();
        removedParameters = new ArrayList<>();
        typeChangedParameters = new ArrayList<>();
    }

    public boolean isEqual() {
        return addedParameters.isEmpty() && removedParameters.isEmpty()
                && typeChangedParameters.isEmpty() && returnTypeEqualityResult.isEqual();
    }

    public void addToAddedParameters(String parameterName) {
        addedParameters.add(parameterName);
    }

    public void addToAddedViolatedParameters(String parameterName) {
        addedViolatedParameters.add(parameterName);
    }

    public void addToTypeChangedParameters(ParameterEqualityResult parameterEqualityResult) {
        typeChangedParameters.add(parameterEqualityResult);
    }

    public void addToRemovedParameters(String parameterName) {
        removedParameters.add(parameterName);
    }

    public void setTypeEqualityResult(TypeEqualityResult returnTypeEqualityResult) {
        this.returnTypeEqualityResult = returnTypeEqualityResult;
    }

    public List<String> getAddedParameters() {
        return addedParameters;
    }

    public List<String> getAddedViolatedParameters() {
        return addedViolatedParameters;
    }

    public List<String> getRemovedParameters() {
        return removedParameters;
    }

    public List<ParameterEqualityResult> getTypeChangedParameters() {
        return typeChangedParameters;
    }

    public TypeEqualityResult getReturnTypeEqualityResult() {
        return returnTypeEqualityResult;
    }
}
