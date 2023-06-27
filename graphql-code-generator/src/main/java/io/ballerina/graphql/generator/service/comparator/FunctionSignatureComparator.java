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
    private final FunctionSignatureNode prevFunctionSignature;
    private final FunctionSignatureNode nextFunctionSignature;
    private List<String> addedParameters;
    private List<String> addedViolatedParameters;
    private List<String> removedParameters;
    private List<ParameterComparator> typeChangedParameters;
    private List<ParameterComparator> defaultValueRemovedParameters;
    private List<ParameterComparator> defaultValueChangedParameters;
    private ReturnTypeDescriptorComparator returnTypeEqualityResult;

    public FunctionSignatureComparator(FunctionSignatureNode prevFunctionSignature,
                                       FunctionSignatureNode nextFunctionSignature) {
        this.prevFunctionSignature = prevFunctionSignature;
        this.nextFunctionSignature = nextFunctionSignature;
        this.addedParameters = new ArrayList<>();
        this.addedViolatedParameters = new ArrayList<>();
        this.removedParameters = new ArrayList<>();
        this.typeChangedParameters = new ArrayList<>();
        this.defaultValueRemovedParameters = new ArrayList<>();
        this.defaultValueChangedParameters = new ArrayList<>();
        separateMembers();
    }

    public void separateMembers() {
        LinkedHashMap<ParameterNode, Boolean> nextParameterAvailable = new LinkedHashMap<>();
        for (ParameterNode nextParameter : this.nextFunctionSignature.parameters()) {
            nextParameterAvailable.put(nextParameter, false);
        }
        for (ParameterNode prevParameter : this.prevFunctionSignature.parameters()) {
            boolean foundMatch = false;
            for (ParameterNode nextParameter : this.nextFunctionSignature.parameters()) {
                ParameterComparator parameterEquals = isParameterEquals(prevParameter, nextParameter);
                if (parameterEquals.isEqual()) {
                    foundMatch = true;
                    nextParameterAvailable.put(nextParameter, true);
                    break;
                } else if (parameterEquals.isMatch()) {
                    foundMatch = true;
                    nextParameterAvailable.put(nextParameter, true);
                    if (!parameterEquals.getTypeEquality().isEqual()) {
                        this.typeChangedParameters.add(parameterEquals);
                    }
                    if (parameterEquals.isDefaultValueRemoved()) {
                        this.defaultValueRemovedParameters.add(parameterEquals);
                    } else if (parameterEquals.isDefaultValueChanged()) {
                        this.defaultValueChangedParameters.add(parameterEquals);
                    }
                }
            }
            if (!foundMatch) {
                this.removedParameters.add(getParameterName(prevParameter));
            }
        }
        for (Map.Entry<ParameterNode, Boolean> entry : nextParameterAvailable.entrySet()) {
            Boolean parameterAvailable = entry.getValue();
            if (!parameterAvailable) {
                ParameterNode newParameter = entry.getKey();
                String newParameterName = getParameterName(newParameter);
                if (newParameter instanceof RequiredParameterNode) {
                    this.addedViolatedParameters.add(newParameterName);
                }
                this.addedParameters.add(newParameterName);
            }
        }
        this.returnTypeEqualityResult =
                new ReturnTypeDescriptorComparator(this.prevFunctionSignature.returnTypeDesc().orElse(null),
                        this.nextFunctionSignature.returnTypeDesc().orElse(null));
    }

    public boolean isEqual() {
        return this.addedParameters.isEmpty() && this.removedParameters.isEmpty()
                && this.typeChangedParameters.isEmpty() && this.returnTypeEqualityResult.isEqual()
                && this.defaultValueRemovedParameters.isEmpty() && this.defaultValueChangedParameters.isEmpty() &&
                this.prevFunctionSignature.openParenToken().text()
                        .equals(this.nextFunctionSignature.openParenToken().text()) &&
                this.prevFunctionSignature.closeParenToken().text()
                        .equals(this.nextFunctionSignature.closeParenToken().text());
    }

    public List<String> getAddedViolatedParameters() {
        return this.addedViolatedParameters;
    }

    public List<String> getRemovedParameters() {
        return this.removedParameters;
    }

    public List<ParameterComparator> getTypeChangedParameters() {
        return this.typeChangedParameters;
    }

    public ReturnTypeDescriptorComparator getReturnTypeEqualityResult() {
        return this.returnTypeEqualityResult;
    }

    public List<ParameterComparator> getDefaultValueRemovedParameters() {
        return this.defaultValueRemovedParameters;
    }
}
