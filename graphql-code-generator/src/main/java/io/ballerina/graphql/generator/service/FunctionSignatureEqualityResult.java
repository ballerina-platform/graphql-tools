package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;

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
    private List<ParameterEqualityResult> defaultValueRemovedParameters;
    private List<ParameterEqualityResult> defaultValueChangedParameters;
    private ReturnTypeDescriptorEqualityResult returnTypeEqualityResult;
    private final FunctionSignatureNode prevFunctionSignature;
    private final FunctionSignatureNode nextFunctionSignature;

    public FunctionSignatureEqualityResult(FunctionSignatureNode prevFunctionSignature,
                                           FunctionSignatureNode nextFunctionSignature) {
        this.prevFunctionSignature = prevFunctionSignature;
        this.nextFunctionSignature = nextFunctionSignature;
        addedParameters = new ArrayList<>();
        addedViolatedParameters = new ArrayList<>();
        removedParameters = new ArrayList<>();
        typeChangedParameters = new ArrayList<>();
        defaultValueRemovedParameters = new ArrayList<>();
        defaultValueChangedParameters = new ArrayList<>();
    }

    public boolean isEqual() {
        return addedParameters.isEmpty() && removedParameters.isEmpty()
                && typeChangedParameters.isEmpty() && returnTypeEqualityResult.isEqual()
                && defaultValueRemovedParameters.isEmpty() && defaultValueChangedParameters.isEmpty() &&
                prevFunctionSignature.openParenToken().text().equals(nextFunctionSignature.openParenToken().text()) &&
                prevFunctionSignature.closeParenToken().text().equals(nextFunctionSignature.closeParenToken().text());
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

    public void addToDefaultValueRemovedParameters(ParameterEqualityResult parameterEqualityResult) {
        defaultValueRemovedParameters.add(parameterEqualityResult);
    }

    public void addToDefaultValueChangedParameters(ParameterEqualityResult parameterEqualityResult) {
        defaultValueChangedParameters.add(parameterEqualityResult);
    }

    public void addToRemovedParameters(String parameterName) {
        removedParameters.add(parameterName);
    }

    public void setReturnTypeEqualityResult(ReturnTypeDescriptorEqualityResult returnTypeEqualityResult) {
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

    public ReturnTypeDescriptorEqualityResult getReturnTypeEqualityResult() {
        return returnTypeEqualityResult;
    }

    public List<ParameterEqualityResult> getDefaultValueRemovedParameters() {
        return defaultValueRemovedParameters;
    }
}
