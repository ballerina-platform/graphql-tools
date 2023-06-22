package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getParameterName;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.isParameterEquals;

/**
 * Utility class to store result comparing two function signatures.
 */
public class FunctionSignatureComparator {
    private List<String> addedParameters;
    private List<String> addedViolatedParameters;
    private List<String> removedParameters;
    private List<ParameterComparator> typeChangedParameters;
    private List<ParameterComparator> defaultValueRemovedParameters;
    private List<ParameterComparator> defaultValueChangedParameters;
    private ReturnTypeDescriptorComparator returnTypeEqualityResult;
    private final FunctionSignatureNode prevFunctionSignature;
    private final FunctionSignatureNode nextFunctionSignature;

    public FunctionSignatureComparator(FunctionSignatureNode prevFunctionSignature,
                                       FunctionSignatureNode nextFunctionSignature) {
        this.prevFunctionSignature = prevFunctionSignature;
        this.nextFunctionSignature = nextFunctionSignature;
        addedParameters = new ArrayList<>();
        addedViolatedParameters = new ArrayList<>();
        removedParameters = new ArrayList<>();
        typeChangedParameters = new ArrayList<>();
        defaultValueRemovedParameters = new ArrayList<>();
        defaultValueChangedParameters = new ArrayList<>();
        separateMembers();
    }

    public void separateMembers() {
        LinkedHashMap<ParameterNode, Boolean> nextParameterAvailable = new LinkedHashMap<>();
        for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
            nextParameterAvailable.put(nextParameter, false);
        }
        for (ParameterNode prevParameter : prevFunctionSignature.parameters()) {
            boolean foundMatch = false;
            for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
                ParameterComparator parameterEquals = isParameterEquals(prevParameter, nextParameter);
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
                new ReturnTypeDescriptorComparator(prevFunctionSignature.returnTypeDesc().orElse(null),
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

    public List<ParameterComparator> getTypeChangedParameters() {
        return typeChangedParameters;
    }

    public ReturnTypeDescriptorComparator getReturnTypeEqualityResult() {
        return returnTypeEqualityResult;
    }

    public List<ParameterComparator> getDefaultValueRemovedParameters() {
        return defaultValueRemovedParameters;
    }
}
