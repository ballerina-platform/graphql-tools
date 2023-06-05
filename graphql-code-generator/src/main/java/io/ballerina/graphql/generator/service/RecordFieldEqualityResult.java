package io.ballerina.graphql.generator.service;


/**
 * Utility class to store result comparing record fields.
 */
public class RecordFieldEqualityResult {
    private TypeEqualityResult typeEquality;
    private String prevRecordFieldName;
    private String nextRecordFieldName;
    private String prevRecordFieldDefaultValue;
    private String nextRecordFieldDefaultValue;

    public RecordFieldEqualityResult(TypeEqualityResult typeEquality, String prevRecordFieldName,
                                     String nextRecordFieldName, String prevRecordFieldDefaultValue,
                                     String nextRecordFieldDefaultValue) {
        this.typeEquality = typeEquality;
        this.prevRecordFieldName = prevRecordFieldName;
        this.nextRecordFieldName = nextRecordFieldName;
        this.prevRecordFieldDefaultValue = prevRecordFieldDefaultValue;
        this.nextRecordFieldDefaultValue = nextRecordFieldDefaultValue;
    }

    public RecordFieldEqualityResult(TypeEqualityResult typeEquality, String prevRecordFieldName,
                                     String nextRecordFieldName) {
        this.typeEquality = typeEquality;
        this.prevRecordFieldName = prevRecordFieldName;
        this.nextRecordFieldName = nextRecordFieldName;
        this.prevRecordFieldDefaultValue = null;
        this.nextRecordFieldDefaultValue = null;
    }

    public boolean isEqual() {
        return isRecordFieldNameEqual() && isDefaultValueEqual() && typeEquality.isEqual();
    }

    public boolean isMatch() {
        return isRecordFieldNameEqual();
    }

    private boolean isRecordFieldNameEqual() {
        return prevRecordFieldName.equals(nextRecordFieldName);
    }

    private boolean isDefaultValueEqual() {
        if (prevRecordFieldDefaultValue != null && nextRecordFieldDefaultValue != null) {
            return prevRecordFieldDefaultValue.equals(nextRecordFieldDefaultValue);
        }
        return prevRecordFieldDefaultValue == null && nextRecordFieldDefaultValue == null;
    }

    public boolean isDefaultValueRemoved() {
        return prevRecordFieldDefaultValue != null && nextRecordFieldDefaultValue == null;
    }

    public TypeEqualityResult getTypeEquality() {
        return typeEquality;
    }

    public String getPrevRecordFieldName() {
        return prevRecordFieldName;
    }

    public String getPrevRecordFieldDefaultValue() {
        return prevRecordFieldDefaultValue;
    }
}
