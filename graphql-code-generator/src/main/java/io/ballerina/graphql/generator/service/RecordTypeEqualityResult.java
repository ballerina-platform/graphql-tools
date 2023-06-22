package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;

/**
 * Utility class to store result comparing record types.
 */
public class RecordTypeEqualityResult {
    private final RecordTypeDescriptorNode prevRecordType;
    private final RecordTypeDescriptorNode nextRecordType;
    private List<Node> addedFields;
    private List<RecordFieldEqualityResult> updatedRecordFields;
    private List<Node> removedFields;
    private List<Node> finalMembers;

    public RecordTypeEqualityResult(RecordTypeDescriptorNode prevRecordType, RecordTypeDescriptorNode nextRecordType) {
        this.prevRecordType = prevRecordType;
        this.nextRecordType = nextRecordType;
        updatedRecordFields = new ArrayList<>();
        addedFields = new ArrayList<>();
        removedFields = new ArrayList<>();
        finalMembers = new ArrayList<>();
        separateMembers();
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextRecordFieldsAvailability = new LinkedHashMap<>();
        for (Node nextField : nextRecordType.fields()) {
            nextRecordFieldsAvailability.put(nextField, false);
        }
        for (Node prevField : prevRecordType.fields()) {
            boolean foundPrevMatch = false;
            for (Node nextField : nextRecordType.fields()) {
                RecordFieldEqualityResult recordFieldEquality =
                        new RecordFieldEqualityResult(prevField, nextField);
                if (recordFieldEquality.isEqual()) {
                    foundPrevMatch = true;
                    nextRecordFieldsAvailability.put(nextField, true);
                    finalMembers.add(recordFieldEquality.generateCombinedRecordField());
                    break;
                } else if (recordFieldEquality.isMatch()) {
                    foundPrevMatch = true;
                    nextRecordFieldsAvailability.put(nextField, true);
                    updatedRecordFields.add(recordFieldEquality);
                    finalMembers.add(recordFieldEquality.generateCombinedRecordField());
                    break;
                }
            }
            if (!foundPrevMatch) {
                removedFields.add(prevField);
            }
        }
        for (Map.Entry<Node, Boolean> availabilityMapEntry : nextRecordFieldsAvailability.entrySet()) {
            Boolean nextRecordFieldAvailable = availabilityMapEntry.getValue();
            if (!nextRecordFieldAvailable) {
                Node newRecordField = availabilityMapEntry.getKey();
                addedFields.add(newRecordField);
                finalMembers.add(newRecordField);
            }
        }
    }

    public List<Node> getRemovedFields() {
        return removedFields;
    }

    private boolean isRecordKeywordEquals() {
        return prevRecordType.recordKeyword().text().equals(nextRecordType.recordKeyword().text());
    }

    private boolean isStartDelimiterEquals() {
        return prevRecordType.bodyStartDelimiter().text().equals(nextRecordType.bodyStartDelimiter().text());
    }

    private boolean isEndDelimiterEquals() {
        return prevRecordType.bodyEndDelimiter().text().equals(nextRecordType.bodyEndDelimiter().text());
    }

    public List<RecordFieldEqualityResult> getUpdatedRecordFields() {
        return updatedRecordFields;
    }

    public boolean isEqual() {
        return isRecordKeywordEquals() && isStartDelimiterEquals() && isEndDelimiterEquals() && addedFields.isEmpty() &&
                removedFields.isEmpty() && updatedRecordFields.isEmpty();
    }

    public RecordTypeDescriptorNode generateCombinedRecordType() {
        return prevRecordType.modify(nextRecordType.recordKeyword(), prevRecordType.bodyStartDelimiter(),
                createNodeList(finalMembers), prevRecordType.recordRestDescriptor().orElse(null),
                prevRecordType.bodyEndDelimiter());
    }

    public List<Node> getAddedFields() {
        return addedFields;
    }
}
