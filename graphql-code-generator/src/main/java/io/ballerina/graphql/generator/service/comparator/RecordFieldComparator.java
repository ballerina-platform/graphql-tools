package io.ballerina.graphql.generator.service.comparator;


import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
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
        this.typeEquality = new TypeComparator(getRecordFieldType(prevField), getRecordFieldType(nextField));
    }

    public boolean isEqual() {
        return isMatch() && isDefaultValueEqual() && this.typeEquality.isEqual();
    }

    public boolean isMatch() {
        String prevFieldName = getRecordFieldName(this.prevField);
        String nextFieldName = getRecordFieldName(this.nextField);
        return prevFieldName != null && nextFieldName != null && prevFieldName.equals(nextFieldName);
    }

    private String getRecordFieldName(Node field) {
        if (field.kind() == SyntaxKind.RECORD_FIELD) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.fieldName().text();
        } else if (field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.fieldName().text();
        }
        return null;
    }

    private String getRecordFieldDefaultValue(Node field) {
        if (field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.expression().toString();
        }
        return null;
    }

    private boolean isDefaultValueEqual() {
        String prevFieldDefaultValue = getRecordFieldDefaultValue(this.prevField);
        String nextFieldDefaultValue = getRecordFieldDefaultValue(this.nextField);
        if (prevFieldDefaultValue == null && nextFieldDefaultValue == null) {
            return true;
        } else if (prevFieldDefaultValue != null && nextFieldDefaultValue != null) {
            return prevFieldDefaultValue.equals(nextFieldDefaultValue);
        }
        return false;
    }

    public boolean isDefaultValueRemoved() {
        return this.prevField.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE &&
                this.nextField.kind() == SyntaxKind.RECORD_FIELD;
    }

    public boolean isFieldTypeChanged() {
        Node prevFieldType = getRecordFieldType(this.prevField);
        Node nextFieldType = getRecordFieldType(this.nextField);
        String prevFieldTypeName = getTypeName(prevFieldType);
        String nextFieldTypeName = getTypeName(nextFieldType);
        return !prevFieldTypeName.equals(nextFieldTypeName);
    }

    public TypeComparator getTypeEquality() {
        return this.typeEquality;
    }

    public String getPrevRecordFieldName() {
        return getRecordFieldName(this.prevField);
    }

    public String getPrevRecordFieldDefaultValue() {
        return getRecordFieldDefaultValue(this.prevField);
    }

    public Node generateCombinedRecordField() {
        if (this.nextField.kind() == SyntaxKind.RECORD_FIELD) {
            RecordFieldNode nextRecordField = (RecordFieldNode) this.nextField;
            return nextRecordField.modify(nextRecordField.metadata().orElse(null), getReadonlyKeyword(this.prevField),
                    nextRecordField.typeName(), nextRecordField.fieldName(),
                    nextRecordField.questionMarkToken().orElse(null), nextRecordField.semicolonToken());
        } else if (this.nextField.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
            RecordFieldWithDefaultValueNode nextRecordField = (RecordFieldWithDefaultValueNode) this.nextField;
            return nextRecordField.modify(nextRecordField.metadata().orElse(null), getReadonlyKeyword(this.prevField),
                    nextRecordField.typeName(), nextRecordField.fieldName(),
                    nextRecordField.equalsToken(),
                    nextRecordField.expression(), nextRecordField.semicolonToken());
        }
        return null;
    }

    private Token getReadonlyKeyword(Node field) {
        if (field.kind() == SyntaxKind.RECORD_FIELD) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.readonlyKeyword().orElse(null);
        } else if (field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.readonlyKeyword().orElse(null);
        }
        return null;
    }
}
