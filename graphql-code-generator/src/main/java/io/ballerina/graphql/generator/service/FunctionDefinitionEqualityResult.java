package io.ballerina.graphql.generator.service;

/**
 * Utility class to store result of comparing two function definitions.
 */
public class FunctionDefinitionEqualityResult {
    private FunctionSignatureEqualityResult functionSignatureEqualityResult;
    private String prevFunctionName;
    private String nextFunctionName;
    private boolean isEqual;
    private boolean isQualifierListEqual;
    private boolean isFunctionNameEqual;
    private boolean isRelativeResourcePathsEqual;

    public FunctionDefinitionEqualityResult() {
        isEqual = false;
    }

    public boolean isEqual() {
        return isQualifierListEqual && isFunctionNameEqual && isRelativeResourcePathsEqual &&
                functionSignatureEqualityResult.isEqual();
    }

    public void setEqual(boolean equal) {
        isEqual = equal;
    }

    public boolean isMatch() {
        return prevFunctionName.equals(nextFunctionName) && isRelativeResourcePathsEqual;
    }

    public FunctionSignatureEqualityResult getFunctionSignatureEqualityResult() {
        return functionSignatureEqualityResult;
    }

    public void setFunctionSignatureEqualityResult(
            FunctionSignatureEqualityResult functionSignatureEqualityResult) {
        this.functionSignatureEqualityResult = functionSignatureEqualityResult;
    }

    public String getPrevFunctionName() {
        return prevFunctionName;
    }

    public void setPrevFunctionName(String prevFunctionName) {
        this.prevFunctionName = prevFunctionName;
    }

    public String getNextFunctionName() {
        return nextFunctionName;
    }

    public void setNextFunctionName(String nextFunctionName) {
        this.nextFunctionName = nextFunctionName;
    }

    public void setQualifierListEqual(boolean qualifierListEqual) {
        isQualifierListEqual = qualifierListEqual;
    }

    public void setFunctionNameEqual(boolean functionNameEqual) {
        isFunctionNameEqual = functionNameEqual;
    }

    public void setRelativeResourcePathsEqual(boolean relativeResourcePathsEqual) {
        isRelativeResourcePathsEqual = relativeResourcePathsEqual;
    }


}
