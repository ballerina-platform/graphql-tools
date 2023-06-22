package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.generator.service.EqualityResultUtils.getParameterName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isParameterEquals;

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

    public void separateMembers() {
        LinkedHashMap<ParameterNode, Boolean> nextParameterAvailable = new LinkedHashMap<>();
        for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
            nextParameterAvailable.put(nextParameter, false);
        }
        for (ParameterNode prevParameter : prevFunctionSignature.parameters()) {
            boolean foundMatch = false;
            for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
                ParameterEqualityResult parameterEquals = isParameterEquals(prevParameter, nextParameter);
                if (parameterEquals.isEqual()) {
                    foundMatch = true;
                    nextParameterAvailable.put(nextParameter, true);
                    break;
                } else if (parameterEquals.isMatch()) {
                    foundMatch = true;
                    nextParameterAvailable.put(nextParameter, true);
                    if (!parameterEquals.getTypeEquality().isEqual()) {
                        typeChangedParameters.add(parameterEquals);
                    }
                    if (parameterEquals.isDefaultValueRemoved()) {
                        defaultValueRemovedParameters.add(parameterEquals);
                    } else if (parameterEquals.isDefaultValueChanged()) {
                        defaultValueChangedParameters.add(parameterEquals);
                    }
                }
            }
            if (!foundMatch) {
                removedParameters.add(getParameterName(prevParameter));
            }
        }
        for (Map.Entry<ParameterNode, Boolean> entry : nextParameterAvailable.entrySet()) {
            Boolean parameterAvailable = entry.getValue();
            if (!parameterAvailable) {
                ParameterNode newParameter = entry.getKey();
                String newParameterName = getParameterName(newParameter);
                if (newParameter instanceof RequiredParameterNode) {
                    addedViolatedParameters.add(newParameterName);
                }
                addedParameters.add(newParameterName);
            }
        }
        returnTypeEqualityResult =
                new ReturnTypeDescriptorEqualityResult(prevFunctionSignature.returnTypeDesc().orElse(null),
                        nextFunctionSignature.returnTypeDesc().orElse(null));
    }

    public boolean isEqual() {
        return addedParameters.isEmpty() && removedParameters.isEmpty()
                && typeChangedParameters.isEmpty() && returnTypeEqualityResult.isEqual()
                && defaultValueRemovedParameters.isEmpty() && defaultValueChangedParameters.isEmpty() &&
                prevFunctionSignature.openParenToken().text().equals(nextFunctionSignature.openParenToken().text()) &&
                prevFunctionSignature.closeParenToken().text().equals(nextFunctionSignature.closeParenToken().text());
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
