package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.ParameterNode;

/**
 * Utility class to store result comparing two parameters.
 */
public class ParameterEqualityResult {
    private String prevParameterName;
    private String nextParameterName;
    private String prevParameterDefaultValue;
    private String nextParameterDefaultValue;
    private boolean isEqual;
    private TypeEqualityResult typeEquality;
    private ParameterNode prevParameter;
    private ParameterNode nextParameter;

    public ParameterEqualityResult() {
        isEqual = false;
    }

    public ParameterEqualityResult(ParameterNode prevParameter, ParameterNode nextParameter) {
        this.prevParameter = prevParameter;
        this.nextParameter = nextParameter;
    }

    public void setPrevParameterName(String prevParameterName) {
        this.prevParameterName = prevParameterName;
    }

    public void setNextParameterName(String nextParameterName) {
        this.nextParameterName = nextParameterName;
    }

    public void setTypeEquality(TypeEqualityResult typeEquality) {
        this.typeEquality = typeEquality;
    }

    public boolean isEqual() {
        return isMatch() && typeEquality.isEqual() && isDefaultValueEquals();
    }

    public boolean isMatch() {
        return prevParameterName.equals(nextParameterName);
    }

    public boolean isDefaultValueEquals() {
        if (prevParameterDefaultValue != null && nextParameterDefaultValue != null) {
            return prevParameterDefaultValue.equals(nextParameterDefaultValue);
        }
        return prevParameterDefaultValue == null && nextParameterDefaultValue == null;
    }

    public boolean isDefaultValueRemoved() {
        return prevParameterDefaultValue != null && nextParameterDefaultValue == null;
    }

    public TypeEqualityResult getTypeEquality() {
        return typeEquality;
    }

    public String getPrevParameterName() {
        return prevParameterName;
    }

    public void setPrevParameterDefaultValue(String prevParameterDefaultValue) {
        this.prevParameterDefaultValue = prevParameterDefaultValue;
    }

    public void setNextParameterDefaultValue(String nextParameterDefaultValue) {
        this.nextParameterDefaultValue = nextParameterDefaultValue;
    }

    public String getPrevParameterDefaultValue() {
        return prevParameterDefaultValue;
    }
}
