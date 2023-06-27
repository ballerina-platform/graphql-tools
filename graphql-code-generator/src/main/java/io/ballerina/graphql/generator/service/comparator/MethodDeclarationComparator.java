package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.CodeGeneratorConstants;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMainQualifier;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMergedMethodDeclarationQualifiers;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMethodDeclarationName;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMethodDeclarationResolverType;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.isMetadataEqual;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.isRelativeResourcePathEquals;

/**
 * Utility class used to store result comparing two method declarations.
 */
public class MethodDeclarationComparator {
    private FunctionSignatureComparator functionSignatureComparator;
    private MethodDeclarationNode prevMethodDeclaration;
    private MethodDeclarationNode nextMethodDeclaration;

    public MethodDeclarationComparator(MethodDeclarationNode prevMethodDeclaration,
                                       MethodDeclarationNode nextMethodDeclaration) {
        this.prevMethodDeclaration = prevMethodDeclaration;
        this.nextMethodDeclaration = nextMethodDeclaration;
        this.functionSignatureComparator = new FunctionSignatureComparator(prevMethodDeclaration.methodSignature(),
                nextMethodDeclaration.methodSignature());
    }

    public boolean isMatch() {
        return getMethodDeclarationName(this.prevMethodDeclaration)
                .equals(getMethodDeclarationName(this.nextMethodDeclaration));
    }

    public boolean isEqual() {
        return isQualifiersEquals() && isMetadataEqual(this.prevMethodDeclaration.metadata().orElse(null),
                this.nextMethodDeclaration.metadata().orElse(null)) && isMethodNameEquals() &&
                isRelativeResourcePathEquals(this.prevMethodDeclaration.relativeResourcePath(),
                        this.nextMethodDeclaration.relativeResourcePath()) &&
                this.functionSignatureComparator.isEqual();
    }

    private boolean isQualifiersEquals() {
        if (this.prevMethodDeclaration.qualifierList().size() == this.nextMethodDeclaration.qualifierList().size()) {
            for (int i = 0; i < this.prevMethodDeclaration.qualifierList().size(); i++) {
                String prevQualifierName = this.prevMethodDeclaration.qualifierList().get(i).text();
                String nextQualifierName = this.nextMethodDeclaration.qualifierList().get(i).text();
                if (!prevQualifierName.equals(nextQualifierName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isMethodNameEquals() {
        return this.prevMethodDeclaration.methodName().text().equals(this.nextMethodDeclaration.methodName().text());
    }

    public FunctionSignatureComparator getFunctionSignatureEqualityResult() {
        return this.functionSignatureComparator;
    }

    public String getPrevFunctionName() {
        return getMethodDeclarationName(this.prevMethodDeclaration);
    }

    public String getPrevMainQualifier() {
        return getMainQualifier(this.prevMethodDeclaration.qualifierList()).text();
    }

    public String getNextMainQualifier() {
        return getMainQualifier(this.nextMethodDeclaration.qualifierList()).text();
    }

    public String getPrevMethodType() {
        return getMethodDeclarationResolverType(this.prevMethodDeclaration);
    }

    public String getNextMethodType() {
        return getMethodDeclarationResolverType(this.nextMethodDeclaration);
    }

    public boolean isGetAndSubscribeInterchanged() {
        String prevMethodType = getMethodDeclarationResolverType(this.prevMethodDeclaration);
        String nextMethodType = getMethodDeclarationResolverType(this.nextMethodDeclaration);
        if (prevMethodType != null && nextMethodType != null) {
            return (prevMethodType.equals(CodeGeneratorConstants.GET) &&
                    nextMethodType.equals(CodeGeneratorConstants.SUBSCRIBE)) ||
                    (prevMethodType.equals(CodeGeneratorConstants.SUBSCRIBE) &&
                            nextMethodType.equals(CodeGeneratorConstants.GET));
        }
        return false;
    }

    public boolean isQualifierSimilar() {
        Token prevMainQualifier = getMainQualifier(this.prevMethodDeclaration.qualifierList());
        Token nextMainQualifier = getMainQualifier(this.nextMethodDeclaration.qualifierList());
        if (prevMainQualifier != null && nextMainQualifier != null) {
            return prevMainQualifier.text().equals(nextMainQualifier.text());
        }
        return prevMainQualifier == null && nextMainQualifier == null;
    }

    public MethodDeclarationNode generateCombinedMethodDeclaration() {
        return this.prevMethodDeclaration.modify(this.prevMethodDeclaration.kind(),
                this.nextMethodDeclaration.metadata().orElse(null),
                getMergedMethodDeclarationQualifiers(
                        this.prevMethodDeclaration.qualifierList(), this.nextMethodDeclaration.qualifierList()),
                this.nextMethodDeclaration.functionKeyword(),
                this.nextMethodDeclaration.methodName(), this.nextMethodDeclaration.relativeResourcePath(),
                this.nextMethodDeclaration.methodSignature(), this.nextMethodDeclaration.semicolon());
    }
}
