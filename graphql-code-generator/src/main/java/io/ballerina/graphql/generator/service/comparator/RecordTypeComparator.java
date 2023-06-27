package io.ballerina.graphql.generator.service.comparator;

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
public class RecordTypeComparator {
    private final RecordTypeDescriptorNode prevRecordType;
    private final RecordTypeDescriptorNode nextRecordType;
    private List<Node> addedFields;
    private List<RecordFieldComparator> updatedRecordFields;
    private List<Node> removedFields;
    private List<Node> finalMembers;

    public RecordTypeComparator(RecordTypeDescriptorNode prevRecordType, RecordTypeDescriptorNode nextRecordType) {
        this.prevRecordType = prevRecordType;
        this.nextRecordType = nextRecordType;
        this.updatedRecordFields = new ArrayList<>();
        this.addedFields = new ArrayList<>();
        this.removedFields = new ArrayList<>();
        this.finalMembers = new ArrayList<>();
        separateMembers();
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextRecordFieldsAvailability = new LinkedHashMap<>();
        for (Node nextField : this.nextRecordType.fields()) {
            nextRecordFieldsAvailability.put(nextField, false);
        }
        for (Node prevField : this.prevRecordType.fields()) {
            boolean foundPrevMatch = false;
            for (Node nextField : this.nextRecordType.fields()) {
                RecordFieldComparator recordFieldEquality =
                        new RecordFieldComparator(prevField, nextField);
                if (recordFieldEquality.isEqual()) {
                    foundPrevMatch = true;
                    nextRecordFieldsAvailability.put(nextField, true);
                    this.finalMembers.add(recordFieldEquality.generateCombinedRecordField());
                    break;
                } else if (recordFieldEquality.isMatch()) {
                    foundPrevMatch = true;
                    nextRecordFieldsAvailability.put(nextField, true);
                    this.updatedRecordFields.add(recordFieldEquality);
                    this.finalMembers.add(recordFieldEquality.generateCombinedRecordField());
                    break;
                }
            }
            if (!foundPrevMatch) {
                this.removedFields.add(prevField);
            }
        }
        for (Map.Entry<Node, Boolean> availabilityMapEntry : nextRecordFieldsAvailability.entrySet()) {
            Boolean nextRecordFieldAvailable = availabilityMapEntry.getValue();
            if (!nextRecordFieldAvailable) {
                Node newRecordField = availabilityMapEntry.getKey();
                this.addedFields.add(newRecordField);
                this.finalMembers.add(newRecordField);
            }
        }
    }

    public List<Node> getRemovedFields() {
        return this.removedFields;
    }

    private boolean isRecordKeywordEquals() {
        return this.prevRecordType.recordKeyword().text().equals(this.nextRecordType.recordKeyword().text());
    }

    private boolean isStartDelimiterEquals() {
        return this.prevRecordType.bodyStartDelimiter().text().equals(this.nextRecordType.bodyStartDelimiter().text());
    }

    private boolean isEndDelimiterEquals() {
        return this.prevRecordType.bodyEndDelimiter().text().equals(this.nextRecordType.bodyEndDelimiter().text());
    }

    public List<RecordFieldComparator> getUpdatedRecordFields() {
        return this.updatedRecordFields;
    }

    public boolean isEqual() {
        return isRecordKeywordEquals() && isStartDelimiterEquals() && isEndDelimiterEquals() &&
                this.addedFields.isEmpty() && this.removedFields.isEmpty() && this.updatedRecordFields.isEmpty();
    }

    public RecordTypeDescriptorNode generateCombinedRecordType() {
        return this.prevRecordType.modify(this.nextRecordType.recordKeyword(), this.prevRecordType.bodyStartDelimiter(),
                createNodeList(this.finalMembers), this.prevRecordType.recordRestDescriptor().orElse(null),
                this.prevRecordType.bodyEndDelimiter());
    }

    public List<Node> getAddedFields() {
        return this.addedFields;
    }
}
