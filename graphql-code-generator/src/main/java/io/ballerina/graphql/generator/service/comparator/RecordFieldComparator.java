package io.ballerina.graphql.generator.service.comparator;


import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.Token;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getRecordFieldType;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getTypeName;

/**
 * Utility class to store result comparing record fields.
 */
public class RecordFieldComparator {
    private TypeComparator typeEquality;
    private Node prevField;
    private Node nextField;

    public RecordFieldComparator(Node prevField, Node nextField) {
        this.prevField = prevField;
        this.nextField = nextField;
        typeEquality = new TypeComparator(getRecordFieldType(prevField), getRecordFieldType(nextField));
    }

    public boolean isEqual() {
        return isMatch() && isDefaultValueEqual() && typeEquality.isEqual();
    }

    public boolean isMatch() {
        String prevFieldName = getRecordFieldName(prevField);
        String nextFieldName = getRecordFieldName(nextField);
        return prevFieldName != null && nextFieldName != null && prevFieldName.equals(nextFieldName);
    }

    private String getRecordFieldName(Node field) {
        if (field instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.fieldName().text();
        } else if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.fieldName().text();
        }
        return null;
    }

    private String getRecordFieldDefaultValue(Node field) {
        if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.expression().toString();
        }
        return null;
    }

    private boolean isDefaultValueEqual() {
        String prevFieldDefaultValue = getRecordFieldDefaultValue(prevField);
        String nextFieldDefaultValue = getRecordFieldDefaultValue(nextField);
        if (prevFieldDefaultValue == null && nextFieldDefaultValue == null) {
            return true;
        } else if (prevFieldDefaultValue != null && nextFieldDefaultValue != null) {
            return prevFieldDefaultValue.equals(nextFieldDefaultValue);
        }
        return false;
    }

    public boolean isDefaultValueRemoved() {
        return prevField instanceof RecordFieldWithDefaultValueNode && nextField instanceof RecordFieldNode;
    }

    public boolean isFieldTypeChanged() {
        Node prevFieldType = getRecordFieldType(prevField);
        Node nextFieldType = getRecordFieldType(nextField);
        String prevFieldTypeName = getTypeName(prevFieldType);
        String nextFieldTypeName = getTypeName(nextFieldType);
        return !prevFieldTypeName.equals(nextFieldTypeName);
    }

    public TypeComparator getTypeEquality() {
        return typeEquality;
    }

    public String getPrevRecordFieldName() {
        return getRecordFieldName(prevField);
    }

    public String getPrevRecordFieldDefaultValue() {
        return getRecordFieldDefaultValue(prevField);
    }

    public Node generateCombinedRecordField() {
        if (nextField instanceof RecordFieldNode) {
            RecordFieldNode nextRecordField = (RecordFieldNode) nextField;
            return nextRecordField.modify(nextRecordField.metadata().orElse(null), getReadonlyKeyword(prevField),
                    nextRecordField.typeName(), nextRecordField.fieldName(),
                    nextRecordField.questionMarkToken().orElse(null), nextRecordField.semicolonToken());
        } else if (nextField instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode nextRecordField = (RecordFieldWithDefaultValueNode) nextField;
            return nextRecordField.modify(nextRecordField.metadata().orElse(null), getReadonlyKeyword(prevField),
                    nextRecordField.typeName(), nextRecordField.fieldName(),
                    nextRecordField.equalsToken(),
                    nextRecordField.expression(), nextRecordField.semicolonToken());
        }
        return null;
    }

    private Token getReadonlyKeyword(Node field) {
        if (field instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.readonlyKeyword().orElse(null);
        } else if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.readonlyKeyword().orElse(null);
        }
        return null;
    }
}
