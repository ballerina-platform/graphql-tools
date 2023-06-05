package io.ballerina.graphql.generator.service;

/**
 * Utility class to store result comparing two types.
 */
public class TypeEqualityResult {
    private String prevType;
    private String nextType;

    public TypeEqualityResult() {
    }

    public boolean isEqual() {
        if ((prevType != null) && (nextType != null)) {
            return prevType.equals(nextType);
        }
        return false;
    }

    public String getPrevType() {
        return prevType;
    }

    public void setPrevType(String prevType) {
        this.prevType = prevType;
    }

    public String getNextType() {
        return nextType;
    }

    public void setNextType(String nextType) {
        this.nextType = nextType;
    }
}
