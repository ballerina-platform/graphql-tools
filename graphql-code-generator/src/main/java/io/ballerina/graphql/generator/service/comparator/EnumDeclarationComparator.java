package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
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
    private MetadataNode mergedMetadata;
    private Token mergedQualifier;
    private Token mergedEnumKeyword;

    public EnumDeclarationComparator(EnumDeclarationNode prevEnum, EnumDeclarationNode nextEnum) {
        this.prevEnum = prevEnum;
        this.nextEnum = nextEnum;
        this.removedMembers = new ArrayList<>();
        this.finalMembers = new ArrayList<>();
        this.mergedMetadata = nextEnum.metadata().orElse(null);
        this.mergedQualifier = prevEnum.qualifier().orElse(null);
        this.mergedEnumKeyword = prevEnum.enumKeywordToken();
        if (isMatch()) {
            separateMembers();
            handleFrontNewLine();
        }
    }

    public boolean isMatch() {
        return this.prevEnum.identifier().text().equals(this.nextEnum.identifier().text());
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextMemberAvailability = new LinkedHashMap<>();
        for (Node nextMember : this.nextEnum.enumMemberList()) {
            nextMemberAvailability.put(nextMember, false);
        }
        for (Node prevMember : this.prevEnum.enumMemberList()) {
            boolean foundMatch = false;
            for (Node nextMember : this.nextEnum.enumMemberList()) {
                EnumMemberComparator enumMemberEquality =
                        new EnumMemberComparator(prevMember, nextMember);
                if (enumMemberEquality.isMatch()) {
                    foundMatch = true;
                    nextMemberAvailability.put(nextMember, true);
                    this.finalMembers.add(enumMemberEquality.generateCombinedResult());
                    break;
                }
            }
            if (!foundMatch) {
                this.removedMembers.add(getEnumMemberName(prevMember));
            }
        }
        for (Map.Entry<Node, Boolean> availabilityEntry : nextMemberAvailability.entrySet()) {
            Boolean nextEnumMemberAvailable = availabilityEntry.getValue();
            if (!nextEnumMemberAvailable) {
                Node newEnumMember = availabilityEntry.getKey();
                this.finalMembers.add(newEnumMember);
            }
        }
    }

    public EnumDeclarationNode generateCombinedResult() {
        return this.nextEnum.modify(this.mergedMetadata, this.mergedQualifier, this.mergedEnumKeyword,
                this.nextEnum.identifier(), this.prevEnum.openBraceToken(),
                getCommaAddedSeparatedNodeList(this.finalMembers), this.prevEnum.closeBraceToken(),
                this.nextEnum.semicolonToken().orElse(null));
    }

    private void handleFrontNewLine() {
        if (this.mergedMetadata != null) {
            if (this.mergedQualifier != null) {
                this.mergedQualifier = this.mergedQualifier.modify(createEmptyMinutiaeList(),
                        createEmptyMinutiaeList());
            }
            this.mergedEnumKeyword = this.mergedEnumKeyword.modify(createEmptyMinutiaeList(),
                    createEmptyMinutiaeList());
        }
    }

    public List<String> generateBreakingChangeWarnings() {
        List<String> breakingChangeWarnings = new ArrayList<>();
        for (String removedMember : this.removedMembers) {
            breakingChangeWarnings.add(
                    String.format(REMOVE_ENUM_MEMBER_MESSAGE, this.prevEnum.identifier().text(), removedMember));
        }
        return breakingChangeWarnings;
    }
}
