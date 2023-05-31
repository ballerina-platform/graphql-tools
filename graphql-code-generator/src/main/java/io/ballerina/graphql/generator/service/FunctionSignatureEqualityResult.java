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
    private String prevReturnType;
    private String nextReturnType;
    private TypeEqualityResult returnTypeEqualityResult;
    private Boolean isEqual;

    public FunctionSignatureEqualityResult() {
        addedParameters = new ArrayList<>();
        addedViolatedParameters = new ArrayList<>();
        removedParameters = new ArrayList<>();
        prevReturnType = "";
        nextReturnType = "";
        isEqual = false;
    }

    public Boolean isEqual() {
//        return addedParameters.isEmpty() && removedParameters.isEmpty() && prevReturnType.equals(nextReturnType);
        return isEqual;
    }

    public void addToAddedParameters(String parameterName) {
        addedParameters.add(parameterName);
    }

    public void addToAddedViolatedParameters(String parameterName) {
        addedViolatedParameters.add(parameterName);
    }

    public void addToRemovedParameters(String parameterName) {
        removedParameters.add(parameterName);
    }

    public void setPrevReturnType(String prevReturnType) {
        this.prevReturnType = prevReturnType;
    }

    public void setNextReturnType(String nextReturnType) {
        this.nextReturnType = nextReturnType;
    }

    public void setEqual(Boolean equal) {
        isEqual = equal;
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

    public TypeEqualityResult getReturnTypeEqualityResult() {
        return returnTypeEqualityResult;
    }
}
