package io.ballerina.graphql.generator.service;

/**
 * Utility class used to store result camparing two method declarations.
 */
public class MethodDeclarationEqualityResult {
    private FunctionSignatureEqualityResult functionSignatureEqualityResult;
    private String prevFunctionName;
    private String nextFunctionName;
    private boolean isQualifierListEqual;
    private boolean isFunctionNameEqual;
    private boolean isRelativeResourcePathsEqual;

    public MethodDeclarationEqualityResult() {
        isQualifierListEqual = false;
        isFunctionNameEqual = false;
        isRelativeResourcePathsEqual = false;
    }

    public void setFunctionSignatureEqualityResult(
            FunctionSignatureEqualityResult functionSignatureEqualityResult) {
        this.functionSignatureEqualityResult = functionSignatureEqualityResult;
    }

    public void setPrevFunctionName(String prevFunctionName) {
        this.prevFunctionName = prevFunctionName;
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

    public boolean isMatch() {
        return prevFunctionName.equals(nextFunctionName);
    }

    public boolean isEqual() {
        return isQualifierListEqual && isMatch()
                && isRelativeResourcePathsEqual && functionSignatureEqualityResult.isEqual();
    }

    public FunctionSignatureEqualityResult getFunctionSignatureEqualityResult() {
        return functionSignatureEqualityResult;
    }

    public String getPrevFunctionName() {
        return prevFunctionName;
    }
}
