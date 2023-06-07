package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to store result comparing two method declarations.
 */
public class MethodDeclarationEqualityResult {
    private FunctionSignatureEqualityResult functionSignatureEqualityResult;
    private String prevFunctionName;
    private String nextFunctionName;
    private NodeList<Token> prevQualifiers;
    private NodeList<Token> nextQualifiers;
    private boolean isRelativeResourcePathsEqual;

    public MethodDeclarationEqualityResult() {
        isRelativeResourcePathsEqual = false;
    }

    public void setNextFunctionName(String nextFunctionName) {
        this.nextFunctionName = nextFunctionName;
    }

    public void setRelativeResourcePathsEqual(boolean relativeResourcePathsEqual) {
        isRelativeResourcePathsEqual = relativeResourcePathsEqual;
    }

    public boolean isMatch() {
        return prevFunctionName.equals(nextFunctionName);
    }

    public boolean isEqual() {
        return isQualifierSimilar() && isMatch() && isRelativeResourcePathsEqual &&
                functionSignatureEqualityResult.isEqual();
    }

    public FunctionSignatureEqualityResult getFunctionSignatureEqualityResult() {
        return functionSignatureEqualityResult;
    }

    public void setFunctionSignatureEqualityResult(FunctionSignatureEqualityResult functionSignatureEqualityResult) {
        this.functionSignatureEqualityResult = functionSignatureEqualityResult;
    }

    public String getPrevFunctionName() {
        return prevFunctionName;
    }

    public void setPrevFunctionName(String prevFunctionName) {
        this.prevFunctionName = prevFunctionName;
    }

    private List<String> getPrevQualifiers() {
        List<String> results = new ArrayList<>();
        for (Token qualifierToken : prevQualifiers) {
            results.add(qualifierToken.text());
        }
        return results;
    }

    public void setPrevQualifiers(NodeList<Token> prevQualifiers) {
        this.prevQualifiers = prevQualifiers;
    }

    private List<String> getNextQualifiers() {
        List<String> results = new ArrayList<>();
        for (Token qualifierToken : nextQualifiers) {
            results.add(qualifierToken.text());
        }
        return results;
    }

    public void setNextQualifiers(NodeList<Token> nextQualifiers) {
        this.nextQualifiers = nextQualifiers;
    }

    public String getPrevMainQualifier() {
        if (getPrevQualifiers().contains(Constants.RESOURCE)) {
            return Constants.RESOURCE;
        } else if (getPrevQualifiers().contains(Constants.REMOTE)) {
            return Constants.REMOTE;
        }
        return null;
    }

    public String getNextMainQualifier() {
        if (getNextQualifiers().contains(Constants.RESOURCE)) {
            return Constants.RESOURCE;
        } else if (getNextQualifiers().contains(Constants.REMOTE)) {
            return Constants.REMOTE;
        }
        return null;
    }

    public boolean isQualifierSimilar() {
        if (getPrevQualifiers().contains(Constants.RESOURCE) && getNextQualifiers().contains(Constants.RESOURCE)) {
            return true;
        } else if (getPrevQualifiers().contains(Constants.REMOTE) && getNextQualifiers().contains(Constants.REMOTE)) {
            return true;
        }
        return false;
    }
}
