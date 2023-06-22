package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.Node;

import static io.ballerina.graphql.generator.service.EqualityResultUtils.getTypeName;

/**
 * Utility class to store result comparing two types.
 */
public class TypeEqualityResult {
    private String prevType;
    private String nextType;

    public TypeEqualityResult(Node prevType, Node nextType) {
        this.prevType = getTypeName(prevType);
        this.nextType = getTypeName(nextType);
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

    public String getNextType() {
        return nextType;
    }
}
