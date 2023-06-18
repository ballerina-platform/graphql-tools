package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;


import static io.ballerina.graphql.generator.service.EqualityResultUtils.getEnumMemberMetadata;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getEnumMemberName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMergedMetadata;

/**
 * Utility class to store result comparing enum members.
 */
public class EnumMemberEqualityResult {
    private final Node prevMember;
    private final Node nextMember;

    public EnumMemberEqualityResult(Node prevMember, Node nextMember) {
        this.prevMember = prevMember;
        this.nextMember = nextMember;
    }

    public boolean isMatch() {
        return getEnumMemberName(prevMember).equals(getEnumMemberName(nextMember));
    }

    public Node generateCombinedResult() {
        MetadataNode mergedMetadata =
                getMergedMetadata(getEnumMemberMetadata(prevMember), getEnumMemberMetadata(nextMember));
        if (nextMember instanceof EnumMemberNode) {
            EnumMemberNode nextEnumMember = (EnumMemberNode) nextMember;
            return nextEnumMember.modify(mergedMetadata, nextEnumMember.identifier(),
                    nextEnumMember.equalToken().orElse(null), nextEnumMember.constExprNode().orElse(null));
        }
        return null;
    }
}
