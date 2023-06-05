package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to store result comparing record types.
 */
public class RecordTypeEqualityResult {
    private Node prevRecordField;

    private List<String> additions;
    private List<String> violatedAdditions;
    private List<String> removals;
    private String prevRecordKeyword;
    private String nextRecordKeyword;
    private String prevStartDelimiter;
    private String nextStartDelimiter;
    private String prevEndDelimiter;
    private String nextEndDelimiter;
    private List<RecordFieldEqualityResult> typeChangedRecordFields;
    private List<RecordFieldWithDefaultValueEqualityResult> typeChangedRecordFieldsWithDefaultValues;
    private List<RecordFieldEqualityResult> defaultValueRemovedFields;

    public RecordTypeEqualityResult(String prevRecordKeyword, String nextRecordKeyword, String prevStartDelimiter,
                                    String nextStartDelimiter, String prevEndDelimiter, String nextEndDelimiter) {
        this.prevRecordKeyword = prevRecordKeyword;
        this.nextRecordKeyword = nextRecordKeyword;
        this.prevStartDelimiter = prevStartDelimiter;
        this.nextStartDelimiter = nextStartDelimiter;
        this.prevEndDelimiter = prevEndDelimiter;
        this.nextEndDelimiter = nextEndDelimiter;
        typeChangedRecordFields = new ArrayList<>();
        defaultValueRemovedFields = new ArrayList<>();
        additions = new ArrayList<>();
        violatedAdditions = new ArrayList<>();
        removals = new ArrayList<>();
    }

    private boolean isRecordKeywordEquals() {
        return prevRecordKeyword.equals(nextRecordKeyword);
    }

    private boolean isStartDelimiterEquals() {
        return prevStartDelimiter.equals(nextStartDelimiter);
    }

    private boolean isEndDelimiterEquals() {
        return prevEndDelimiter.equals(nextEndDelimiter);
    }

    public void addToTypeChangedRecordFields(RecordFieldEqualityResult recordFieldEqualityResult) {
        typeChangedRecordFields.add(recordFieldEqualityResult);
    }

    public void addToTypeChangedRecordFieldsWithDefaultValues(
            RecordFieldWithDefaultValueEqualityResult recordFieldWithDefaultValueEqualityResult) {
        typeChangedRecordFieldsWithDefaultValues.add(recordFieldWithDefaultValueEqualityResult);
    }

    public void addToDefaultValueRemovedFields(RecordFieldEqualityResult recordFieldEqualityResult) {
        defaultValueRemovedFields.add(recordFieldEqualityResult);
    }

    public void addToAdditions(String addition) {
        additions.add(addition);
    }

    public void addToViolatedAdditions(String violatedAddition) {
        violatedAdditions.add(violatedAddition);
    }

    public void addToRemovals(String removal) {
        removals.add(removal);
    }

    public List<RecordFieldEqualityResult> getTypeChangedRecordFields() {
        return typeChangedRecordFields;
    }

    public List<RecordFieldWithDefaultValueEqualityResult> getTypeChangedRecordFieldsWithDefaultValues() {
        return typeChangedRecordFieldsWithDefaultValues;
    }

    public boolean isEqual() {
        return isRecordKeywordEquals() && isStartDelimiterEquals() && isEndDelimiterEquals() && additions.isEmpty() &&
                removals.isEmpty() && defaultValueRemovedFields.isEmpty() && typeChangedRecordFields.isEmpty();
    }

    public List<String> getViolatedAdditions() {
        return violatedAdditions;
    }

    public List<String> getRemovals() {
        return removals;
    }

    public List<RecordFieldEqualityResult> getDefaultValueRemovedFields() {
        return defaultValueRemovedFields;
    }
}
