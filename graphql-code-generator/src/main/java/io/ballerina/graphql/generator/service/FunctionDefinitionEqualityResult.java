package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.CodeGeneratorConstants;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.service.EqualityResultUtils.getFunctionName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMainQualifier;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMergedFunctionDefinitionQualifiers;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isRelativeResourcePathEquals;

/**
 * Utility class to store result of comparing two function definitions.
 */
public class FunctionDefinitionEqualityResult {
    private FunctionSignatureEqualityResult functionSignatureEqualityResult;
    private FunctionDefinitionNode prevFunctionDefinition;
    private FunctionDefinitionNode nextFunctionDefinition;

    public FunctionDefinitionEqualityResult(FunctionDefinitionNode prevFunctionDefinition,
                                            FunctionDefinitionNode nextFunctionDefinition) {
        this.prevFunctionDefinition = prevFunctionDefinition;
        this.nextFunctionDefinition = nextFunctionDefinition;
    }

    public boolean isEqual() {
        return isQualifiersEquals() && isMetadataEquals() && isFunctionNameEquals() &&
                isRelativeResourcePathEquals(prevFunctionDefinition.relativeResourcePath(),
                        nextFunctionDefinition.relativeResourcePath()) &&
                functionSignatureEqualityResult.isEqual();
    }

    private boolean isFunctionNameEquals() {
        return prevFunctionDefinition.functionName().text().equals(nextFunctionDefinition.functionName().text());
    }

    public boolean isMatch() {
        return getFunctionName(prevFunctionDefinition).equals(getFunctionName(nextFunctionDefinition));
    }

    public FunctionSignatureEqualityResult getFunctionSignatureEqualityResult() {
        return functionSignatureEqualityResult;
    }

    public void setFunctionSignatureEqualityResult(
            FunctionSignatureEqualityResult functionSignatureEqualityResult) {
        this.functionSignatureEqualityResult = functionSignatureEqualityResult;
    }

    public String getPrevFunctionName() {
        return getFunctionName(prevFunctionDefinition);
    }

    public String getPrevMainQualifier() {
        return getMainQualifier(prevFunctionDefinition.qualifierList()).text();
    }

    public String getNextMainQualifier() {
        return getMainQualifier(nextFunctionDefinition.qualifierList()).text();
    }

    public String getPrevMethodType() {
        return getFunctionDefinitionResolverType(prevFunctionDefinition);
    }

    public String getNextMethodType() {
        return getFunctionDefinitionResolverType(nextFunctionDefinition);
    }

    public String getFunctionDefinitionResolverType(FunctionDefinitionNode functionDefinition) {
        String prevFunctionName = functionDefinition.functionName().text();
        if (prevFunctionName.equals(CodeGeneratorConstants.GET)) {
            return CodeGeneratorConstants.GET;
        } else if (prevFunctionName.equals(CodeGeneratorConstants.SUBSCRIBE)) {
            return CodeGeneratorConstants.SUBSCRIBE;
        }
        return null;
    }

    public boolean isGetAndSubscribeInterchanged() {
        String prevMethodType = getFunctionDefinitionResolverType(prevFunctionDefinition);
        String nextMethodType = getFunctionDefinitionResolverType(nextFunctionDefinition);
        if (prevMethodType != null && nextMethodType != null) {
            return (prevMethodType.equals(CodeGeneratorConstants.GET) &&
                    nextMethodType.equals(CodeGeneratorConstants.SUBSCRIBE)) ||
                    (prevMethodType.equals(CodeGeneratorConstants.SUBSCRIBE) &&
                            nextMethodType.equals(CodeGeneratorConstants.GET));
        }
        return false;
    }

    private boolean isQualifiersEquals() {
        if (prevFunctionDefinition.qualifierList().size() == nextFunctionDefinition.qualifierList().size()) {
            for (int i = 0; i < prevFunctionDefinition.qualifierList().size(); i++) {
                String prevQualifierName = prevFunctionDefinition.qualifierList().get(i).text();
                String nextQualifierName = nextFunctionDefinition.qualifierList().get(i).text();
                if (!prevQualifierName.equals(nextQualifierName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isQualifierSimilar() {
        Token prevMainQualifier = getMainQualifier(prevFunctionDefinition.qualifierList());
        Token nextMainQualifier = getMainQualifier(nextFunctionDefinition.qualifierList());
        if (prevMainQualifier != null && nextMainQualifier != null) {
            return prevMainQualifier.text().equals(nextMainQualifier.text());
        }
        return prevMainQualifier == null && nextMainQualifier == null;
    }

    public boolean isMetadataEquals() {
        MetadataNode prevMetadata = prevFunctionDefinition.metadata().orElse(null);
        MetadataNode nextMetadata = nextFunctionDefinition.metadata().orElse(null);
        if (prevMetadata != null && nextMetadata != null) {
            return prevMetadata.toString().equals(nextMetadata.toString());
        }
        return prevMetadata == null && nextMetadata == null;
    }

    public FunctionDefinitionNode generateCombinedFunctionDefinition(boolean isFirstFunctionDefinition) {
        return prevFunctionDefinition.modify(prevFunctionDefinition.kind(), nextFunctionDefinition.metadata().orElse(null),
                getMergedFunctionDefinitionQualifiers(
                        prevFunctionDefinition.qualifierList(),
                        nextFunctionDefinition.qualifierList(),
                        isFirstFunctionDefinition
                ),
                prevFunctionDefinition.functionKeyword(),
                nextFunctionDefinition.functionName(), nextFunctionDefinition.relativeResourcePath(),
                nextFunctionDefinition.functionSignature(), prevFunctionDefinition.functionBody());
    }
}
