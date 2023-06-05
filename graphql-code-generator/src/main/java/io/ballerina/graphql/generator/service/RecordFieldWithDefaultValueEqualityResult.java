package io.ballerina.graphql.generator.service;

/**
 * Utility class to store result comparing record fields with default values.
 */
public class RecordFieldWithDefaultValueEqualityResult {
    private TypeEqualityResult typeEquality;
    private String prevRecordFieldName;
    private String nextRecordFieldName;
    private String prevRecordFieldDefaultValue;
    private String nextRecordFieldDefaultValue;

    public RecordFieldWithDefaultValueEqualityResult(TypeEqualityResult typeEquality, String prevRecordFieldName,
                                                     String nextRecordFieldName, String prevRecordFieldDefaultValue,
                                                     String nextRecordFieldDefaultValue) {
        this.typeEquality = typeEquality;
        this.prevRecordFieldName = prevRecordFieldName;
        this.nextRecordFieldName = nextRecordFieldName;
        this.prevRecordFieldDefaultValue = prevRecordFieldDefaultValue;
        this.nextRecordFieldDefaultValue = nextRecordFieldDefaultValue;
    }

    public boolean isEqual() {
        return isRecordFieldNameEqual() && typeEquality.isEqual() && isDefaultValueEqual();
    }

    public boolean isMatch() {
        return isRecordFieldNameEqual();
    }

    private boolean isRecordFieldNameEqual() {
        return prevRecordFieldName.equals(nextRecordFieldName);
    }

    private boolean isDefaultValueEqual() {
        return prevRecordFieldDefaultValue.equals(nextRecordFieldDefaultValue);
    }

    public TypeEqualityResult getTypeEquality() {
        return typeEquality;
    }

    public String getPrevRecordFieldDefaultValue() {
        return prevRecordFieldDefaultValue;
    }
}
