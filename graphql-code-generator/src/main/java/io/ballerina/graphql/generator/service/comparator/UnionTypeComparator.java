package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getTypeName;

/**
 * Utility class to store result comparing union types.
 */
public class UnionTypeComparator {
    private List<String> addedUnionMembers;
    private List<String> removedUnionMembers;
    private UnionTypeDescriptorNode prevUnionType;
    private UnionTypeDescriptorNode nextUnionType;
    private List<String> prevUnionMembers;
    private List<String> nextUnionMembers;

    public UnionTypeComparator(UnionTypeDescriptorNode prevUnionType, UnionTypeDescriptorNode nextUnionType) {
        this.prevUnionType = prevUnionType;
        this.nextUnionType = nextUnionType;
        this.prevUnionMembers = new ArrayList<>();
        this.nextUnionMembers = new ArrayList<>();
        this.removedUnionMembers = new ArrayList<>();
        this.addedUnionMembers = new ArrayList<>();
        populateUnionMemberNames(prevUnionType, this.prevUnionMembers);
        populateUnionMemberNames(nextUnionType, this.nextUnionMembers);
        separateMembers();
    }

    public UnionTypeComparator() {
        this.addedUnionMembers = new ArrayList<>();
        this.removedUnionMembers = new ArrayList<>();
    }

    public void separateMembers() {
        HashMap<String, Boolean> nextUnionMemberAvailability = new HashMap<>();
        for (String nextUnionTypeMember : this.nextUnionMembers) {
            nextUnionMemberAvailability.put(nextUnionTypeMember, false);
        }
        for (String prevUnionTypeMember : this.prevUnionMembers) {
            boolean foundMatch = false;
            for (String nextUnionTypeMember : this.nextUnionMembers) {
                if (prevUnionTypeMember.equals(nextUnionTypeMember)) {
                    foundMatch = true;
                    nextUnionMemberAvailability.put(nextUnionTypeMember, true);
                    break;
                }
            }
            if (!foundMatch) {
                this.removedUnionMembers.add(prevUnionTypeMember);
            }
        }
        for (Map.Entry<String, Boolean> availableEntry : nextUnionMemberAvailability.entrySet()) {
            Boolean nextUnionMemberAvailable = availableEntry.getValue();
            if (!nextUnionMemberAvailable) {
                String notAvailableNextUnionMember = availableEntry.getKey();
                this.addedUnionMembers.add(notAvailableNextUnionMember);
            }
        }
    }

    public UnionTypeDescriptorNode generateCombinedUnionType() {
        return this.nextUnionType;
    }

    public boolean isEqual() {
        return this.addedUnionMembers.isEmpty() && this.removedUnionMembers.isEmpty();
    }

    public List<String> getRemovedUnionMembers() {
        return this.removedUnionMembers;
    }

    private void populateUnionMemberNames(UnionTypeDescriptorNode unionType, List<String> unionTypeMembers) {
        unionTypeMembers.add(getTypeName(unionType.rightTypeDesc()));
        if (unionType.leftTypeDesc().kind() == SyntaxKind.UNION_TYPE_DESC) {
            UnionTypeDescriptorNode leftUnionType = (UnionTypeDescriptorNode) unionType.leftTypeDesc();
            populateUnionMemberNames(leftUnionType, unionTypeMembers);
        } else {
            unionTypeMembers.add(getTypeName(unionType.leftTypeDesc()));
        }
    }
}
