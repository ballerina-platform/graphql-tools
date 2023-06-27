package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.CodeGeneratorConstants;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getFunctionDefinitionResolverType;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getFunctionName;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMainQualifier;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMergedFunctionDefinitionQualifiers;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.isRelativeResourcePathEquals;

/**
 * Utility class to store result of comparing two function definitions.
 */
public class FunctionDefinitionComparator {
    private FunctionSignatureComparator functionSignatureComparator;
    private FunctionDefinitionNode prevFunctionDefinition;
    private FunctionDefinitionNode nextFunctionDefinition;

    public FunctionDefinitionComparator(FunctionDefinitionNode prevFunctionDefinition,
                                        FunctionDefinitionNode nextFunctionDefinition) {
        this.prevFunctionDefinition = prevFunctionDefinition;
        this.nextFunctionDefinition = nextFunctionDefinition;
        this.functionSignatureComparator =
                new FunctionSignatureComparator(prevFunctionDefinition.functionSignature(),
                        nextFunctionDefinition.functionSignature());
    }

    public boolean isEqual() {
        return isQualifiersEquals() && isMetadataEquals() && isFunctionNameEquals() &&
                isRelativeResourcePathEquals(this.prevFunctionDefinition.relativeResourcePath(),
                        this.nextFunctionDefinition.relativeResourcePath()) &&
                this.functionSignatureComparator.isEqual();
    }

    private boolean isFunctionNameEquals() {
        return this.prevFunctionDefinition.functionName().text()
                .equals(this.nextFunctionDefinition.functionName().text());
    }

    public boolean isMatch() {
        return getFunctionName(this.prevFunctionDefinition).equals(getFunctionName(this.nextFunctionDefinition));
    }

    public FunctionSignatureComparator getFunctionSignatureEqualityResult() {
        return this.functionSignatureComparator;
    }

    public String getPrevFunctionName() {
        return getFunctionName(this.prevFunctionDefinition);
    }

    public String getPrevMainQualifier() {
        return getMainQualifier(this.prevFunctionDefinition.qualifierList()).text();
    }

    public String getNextMainQualifier() {
        return getMainQualifier(this.nextFunctionDefinition.qualifierList()).text();
    }

    public String getPrevMethodType() {
        return getFunctionDefinitionResolverType(this.prevFunctionDefinition);
    }

    public String getNextMethodType() {
        return getFunctionDefinitionResolverType(this.nextFunctionDefinition);
    }

    public boolean isGetAndSubscribeInterchanged() {
        String prevMethodType = getFunctionDefinitionResolverType(this.prevFunctionDefinition);
        String nextMethodType = getFunctionDefinitionResolverType(this.nextFunctionDefinition);
        if (prevMethodType != null && nextMethodType != null) {
            return (prevMethodType.equals(CodeGeneratorConstants.GET) &&
                    nextMethodType.equals(CodeGeneratorConstants.SUBSCRIBE)) ||
                    (prevMethodType.equals(CodeGeneratorConstants.SUBSCRIBE) &&
                            nextMethodType.equals(CodeGeneratorConstants.GET));
        }
        return false;
    }

    private boolean isQualifiersEquals() {
        if (this.prevFunctionDefinition.qualifierList().size() == this.nextFunctionDefinition.qualifierList().size()) {
            for (int i = 0; i < this.prevFunctionDefinition.qualifierList().size(); i++) {
                String prevQualifierName = this.prevFunctionDefinition.qualifierList().get(i).text();
                String nextQualifierName = this.nextFunctionDefinition.qualifierList().get(i).text();
                if (!prevQualifierName.equals(nextQualifierName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isQualifierSimilar() {
        Token prevMainQualifier = getMainQualifier(this.prevFunctionDefinition.qualifierList());
        Token nextMainQualifier = getMainQualifier(this.nextFunctionDefinition.qualifierList());
        if (prevMainQualifier != null && nextMainQualifier != null) {
            return prevMainQualifier.text().equals(nextMainQualifier.text());
        }
        return prevMainQualifier == null && nextMainQualifier == null;
    }

    public boolean isMetadataEquals() {
        MetadataNode prevMetadata = this.prevFunctionDefinition.metadata().orElse(null);
        MetadataNode nextMetadata = this.nextFunctionDefinition.metadata().orElse(null);
        if (prevMetadata != null && nextMetadata != null) {
            return prevMetadata.toString().equals(nextMetadata.toString());
        }
        return prevMetadata == null && nextMetadata == null;
    }

    public FunctionDefinitionNode generateCombinedFunctionDefinition(boolean isFirstFunctionDefinition) {
        return this.prevFunctionDefinition.modify(this.prevFunctionDefinition.kind(),
                this.nextFunctionDefinition.metadata().orElse(null),
                getMergedFunctionDefinitionQualifiers(
                        this.prevFunctionDefinition.qualifierList(),
                        this.nextFunctionDefinition.qualifierList(),
                        isFirstFunctionDefinition
                ),
                this.prevFunctionDefinition.functionKeyword(),
                this.nextFunctionDefinition.functionName(), this.nextFunctionDefinition.relativeResourcePath(),
                this.nextFunctionDefinition.functionSignature(), this.prevFunctionDefinition.functionBody());
    }
}
