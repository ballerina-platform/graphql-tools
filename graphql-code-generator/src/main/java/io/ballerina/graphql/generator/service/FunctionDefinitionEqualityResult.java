package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.CodeGeneratorConstants;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMergedQualifiers;

/**
 * Utility class to store result of comparing two function definitions.
 */
public class FunctionDefinitionEqualityResult {
    private FunctionSignatureEqualityResult functionSignatureEqualityResult;
    private String prevFunctionName;
    private String nextFunctionName;
    private NodeList<Token> prevQualifiers;
    private NodeList<Token> nextQualifiers;
    private String prevMethodType;
    private String nextMethodType;
    private boolean isFunctionNameEqual;
    private boolean isRelativeResourcePathsEqual;
    private FunctionDefinitionNode prevFunctionDefinition;
    private FunctionDefinitionNode nextFunctionDefinition;

    public FunctionDefinitionEqualityResult(FunctionDefinitionNode prevFunctionDefinition,
                                            FunctionDefinitionNode nextFunctionDefinition) {
        this.prevFunctionDefinition = prevFunctionDefinition;
        this.nextFunctionDefinition = nextFunctionDefinition;
    }

    public boolean isEqual() {
        return isQualifierSimilar() && isFunctionNameEqual && isRelativeResourcePathsEqual &&
                functionSignatureEqualityResult.isEqual() && isMatch();
    }

    public boolean isMatch() {
        return prevFunctionName.equals(nextFunctionName);
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

    public void setNextFunctionName(String nextFunctionName) {
        this.nextFunctionName = nextFunctionName;
    }

    public void setFunctionNameEqual(boolean functionNameEqual) {
        isFunctionNameEqual = functionNameEqual;
    }

    public void setRelativeResourcePathsEqual(boolean relativeResourcePathsEqual) {
        isRelativeResourcePathsEqual = relativeResourcePathsEqual;
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

    public String getPrevMethodType() {
        return prevMethodType;
    }

    public void setPrevMethodType(String methodType) {
        if (methodType.equals(CodeGeneratorConstants.GET)) {
            this.prevMethodType = methodType;
        } else if (methodType.equals(CodeGeneratorConstants.SUBSCRIBE)) {
            this.prevMethodType = methodType;
        } else {
            this.prevMethodType = null;
        }
    }

    public String getNextMethodType() {
        return nextMethodType;
    }

    public void setNextMethodType(String methodType) {
        if (methodType.equals(CodeGeneratorConstants.GET)) {
            this.nextMethodType = methodType;
        } else if (methodType.equals(CodeGeneratorConstants.SUBSCRIBE)) {
            this.nextMethodType = methodType;
        } else {
            this.nextMethodType = null;
        }
    }

    public boolean isGetAndSubscribeInterchanged() {
        if (prevMethodType != null && nextMethodType != null) {
            return (prevMethodType.equals(CodeGeneratorConstants.GET) &&
                    nextMethodType.equals(CodeGeneratorConstants.SUBSCRIBE)) ||
                    (prevMethodType.equals(CodeGeneratorConstants.SUBSCRIBE) &&
                            nextMethodType.equals(CodeGeneratorConstants.GET));
        }
        return false;
    }

    public boolean isQualifierSimilar() {
        if (getPrevQualifiers().contains(Constants.RESOURCE) && getNextQualifiers().contains(Constants.RESOURCE)) {
            return true;
        } else if (getPrevQualifiers().contains(Constants.REMOTE) && getNextQualifiers().contains(Constants.REMOTE)) {
            return true;
        }
        return false;
    }

    public FunctionDefinitionNode generateCombinedFunctionDefinition() {
        MetadataNode finalMetadata = nextFunctionDefinition.metadata().orElse(null);
        if (finalMetadata == null) {
            finalMetadata = prevFunctionDefinition.metadata().orElse(null);
        }
        return prevFunctionDefinition.modify(prevFunctionDefinition.kind(), finalMetadata,
                getMergedQualifiers(prevFunctionDefinition.qualifierList(), nextFunctionDefinition.qualifierList()),
                prevFunctionDefinition.functionKeyword(),
                nextFunctionDefinition.functionName(), nextFunctionDefinition.relativeResourcePath(),
                nextFunctionDefinition.functionSignature(), prevFunctionDefinition.functionBody());
    }
}
