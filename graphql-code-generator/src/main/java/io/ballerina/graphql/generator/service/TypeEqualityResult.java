package io.ballerina.graphql.generator.service;

/**
 * Utility class to store result comparing two types.
 */
public class TypeEqualityResult {
    private String prevType;
    private String nextType;
    private boolean isEqual;

    public TypeEqualityResult() {
    }

    public boolean isEqual() {
//        if ((prevType != null) && (nextType != null)) {
//            return true;
//        } else return prevType == null && nextType == null;
        return isEqual;
    }

    public void setPrevType(String prevType) {
        this.prevType = prevType;
    }

    public void setNextType(String nextType) {
        this.nextType = nextType;
    }

    public void setEqual(boolean equal) {
        isEqual = equal;
    }

    public String getPrevType() {
        return prevType;
    }

    public String getNextType() {
        return nextType;
    }
}
