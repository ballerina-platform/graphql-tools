package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import static io.ballerina.graphql.generator.service.EqualityResultUtils.getCommaAddedSeparatedNodeList;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getEnumMemberName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getEnumMetadata;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMergedMetadata;

/**
 * Utility class to store result comparing enum declarations.
 */
public class EnumDeclarationEqualityResult {
    private static final String REMOVE_ENUM_MEMBER_MESSAGE =
            "warning: In '%s' enum '%s' member has removed. This can break existing clients.";

    private final EnumDeclarationNode prevEnum;
    private final EnumDeclarationNode nextEnum;
    private List<String> removedMembers;
    private List<Node> finalMembers;

    public EnumDeclarationEqualityResult(EnumDeclarationNode prevEnum, EnumDeclarationNode nextEnum) {
        this.prevEnum = prevEnum;
        this.nextEnum = nextEnum;
        removedMembers = new ArrayList<>();
        finalMembers = new ArrayList<>();
    }

    public boolean isMatch() {
        return prevEnum.identifier().text().equals(nextEnum.identifier().text());
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextMemberAvailability = new LinkedHashMap<>();
        for (Node nextMember : nextEnum.enumMemberList()) {
            nextMemberAvailability.put(nextMember, false);
        }
        for (Node prevMember : prevEnum.enumMemberList()) {
            boolean foundMatch = false;
            for (Node nextMember : nextEnum.enumMemberList()) {
                EnumMemberEqualityResult enumMemberEquality =
                        new EnumMemberEqualityResult(prevMember, nextMember);
                if (enumMemberEquality.isMatch()) {
                    foundMatch = true;
                    nextMemberAvailability.put(nextMember, true);
                    finalMembers.add(enumMemberEquality.generateCombinedResult());
                    break;
                }
            }
            if (!foundMatch) {
                removedMembers.add(getEnumMemberName(prevMember));
            }
        }
        for (Map.Entry<Node, Boolean> availabilityEntry : nextMemberAvailability.entrySet()) {
            Boolean nextEnumMemberAvailable = availabilityEntry.getValue();
            if (!nextEnumMemberAvailable) {
                Node newEnumMember = availabilityEntry.getKey();
                finalMembers.add(newEnumMember);
            }
        }
    }

    public List<String> getRemovedMembers() {
        return removedMembers;
    }

    public EnumDeclarationNode generateCombinedResult() {
        MetadataNode mergedMetadata = getMergedMetadata(getEnumMetadata(prevEnum), getEnumMetadata(nextEnum));
        return nextEnum.modify(mergedMetadata, prevEnum.qualifier().orElse(null), nextEnum.enumKeywordToken(),
                nextEnum.identifier(), prevEnum.openBraceToken(), getCommaAddedSeparatedNodeList(finalMembers),
                prevEnum.closeBraceToken(), nextEnum.semicolonToken().orElse(null));
    }

    public List<String> generateBreakingChangeWarnings() {
        List<String> breakingChangeWarnings = new ArrayList<>();
        for (String removedMember : removedMembers) {
            breakingChangeWarnings.add(
                    String.format(REMOVE_ENUM_MEMBER_MESSAGE, prevEnum.identifier().text(), removedMember));
        }
        return breakingChangeWarnings;
    }
}
