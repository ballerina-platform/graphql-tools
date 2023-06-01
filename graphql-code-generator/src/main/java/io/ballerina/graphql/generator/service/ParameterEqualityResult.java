package io.ballerina.graphql.generator.service;

/**
 * Utility class to store result comparing two parameters.
 */
public class ParameterEqualityResult {
    private String prevParameterName;
    private String nextParameterName;
    private boolean isEqual;
    private TypeEqualityResult typeEquality;

    public ParameterEqualityResult() {
        isEqual = false;
    }

    public void setPrevParameterName(String prevParameterName) {
        this.prevParameterName = prevParameterName;
    }

    public void setNextParameterName(String nextParameterName) {
        this.nextParameterName = nextParameterName;
    }

    public void setEqual(boolean equal) {
        isEqual = equal;
    }

    public void setTypeEquality(TypeEqualityResult typeEquality) {
        this.typeEquality = typeEquality;
    }

    public boolean isEqual() {
        return isMatch() && typeEquality.isEqual();
    }

    public boolean isMatch() {
        return prevParameterName.equals(nextParameterName);
    }

    public TypeEqualityResult getTypeEquality() {
        return typeEquality;
    }
}
