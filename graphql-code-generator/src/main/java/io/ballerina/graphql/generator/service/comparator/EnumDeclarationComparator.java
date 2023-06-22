package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getCommaAddedSeparatedNodeList;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getEnumMemberName;

/**
 * Utility class to store result comparing enum declarations.
 */
public class EnumDeclarationComparator {
    private static final String REMOVE_ENUM_MEMBER_MESSAGE =
            "warning: In '%s' enum '%s' member has removed. This can break existing clients.";

    private final EnumDeclarationNode prevEnum;
    private final EnumDeclarationNode nextEnum;
    private List<String> removedMembers;
    private List<Node> finalMembers;

    public EnumDeclarationComparator(EnumDeclarationNode prevEnum, EnumDeclarationNode nextEnum) {
        this.prevEnum = prevEnum;
        this.nextEnum = nextEnum;
        removedMembers = new ArrayList<>();
        finalMembers = new ArrayList<>();
        if (isMatch()) {
            separateMembers();
        }
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
                EnumMemberComparator enumMemberEquality =
                        new EnumMemberComparator(prevMember, nextMember);
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

    public EnumDeclarationNode generateCombinedResult() {
        return nextEnum.modify(nextEnum.metadata().orElse(null), prevEnum.qualifier().orElse(null),
                nextEnum.enumKeywordToken(), nextEnum.identifier(), prevEnum.openBraceToken(),
                getCommaAddedSeparatedNodeList(finalMembers),
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
