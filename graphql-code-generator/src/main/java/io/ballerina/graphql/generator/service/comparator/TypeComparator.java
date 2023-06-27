package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.Node;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getTypeName;

/**
 * Utility class to store result comparing two types.
 */
public class TypeComparator {
    private String prevType;
    private String nextType;

    public TypeComparator(Node prevType, Node nextType) {
        this.prevType = getTypeName(prevType);
        this.nextType = getTypeName(nextType);
    }

    public boolean isEqual() {
        if ((this.prevType != null) && (this.nextType != null)) {
            return this.prevType.equals(this.nextType);
        }
        return false;
    }

    public String getPrevType() {
        return this.prevType;
    }

    public String getNextType() {
        return this.nextType;
    }
}
