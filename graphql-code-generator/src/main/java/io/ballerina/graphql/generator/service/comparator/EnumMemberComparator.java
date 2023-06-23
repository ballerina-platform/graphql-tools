package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.Node;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getEnumMemberName;

/**
 * Utility class to store result comparing enum members.
 */
public class EnumMemberComparator {
    private final Node prevMember;
    private final Node nextMember;

    public EnumMemberComparator(Node prevMember, Node nextMember) {
        this.prevMember = prevMember;
        this.nextMember = nextMember;
    }

    public boolean isMatch() {
        return getEnumMemberName(prevMember).equals(getEnumMemberName(nextMember));
    }

    public Node generateCombinedResult() {
        if (nextMember instanceof EnumMemberNode) {
            EnumMemberNode nextEnumMember = (EnumMemberNode) nextMember;
            return nextEnumMember.modify(nextEnumMember.metadata().orElse(null), nextEnumMember.identifier(),
                    nextEnumMember.equalToken().orElse(null), nextEnumMember.constExprNode().orElse(null));
        }
        return null;
    }
}