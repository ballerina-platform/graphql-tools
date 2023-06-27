package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.ParameterNode;

import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getParameterDefaultValue;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getParameterName;

/**
 * Utility class to store result comparing two parameters.
 */
public class ParameterComparator {
    private String prevParameterName;
    private String nextParameterName;
    private String prevParameterDefaultValue;
    private String nextParameterDefaultValue;
    private TypeComparator typeEquality;
    private ParameterNode prevParameter;
    private ParameterNode nextParameter;

    public ParameterComparator(ParameterNode prevParameter, ParameterNode nextParameter) {
        this.prevParameter = prevParameter;
        this.nextParameter = nextParameter;
        this.prevParameterName = getParameterName(prevParameter);
        this.nextParameterName = getParameterName(nextParameter);
        this.prevParameterDefaultValue = getParameterDefaultValue(prevParameter);
        this.nextParameterDefaultValue = getParameterDefaultValue(nextParameter);
    }

    public void setTypeEquality(TypeComparator typeEquality) {
        this.typeEquality = typeEquality;
    }

    public boolean isEqual() {
        return isMatch() && this.typeEquality.isEqual() && isDefaultValueEquals();
    }

    public boolean isMatch() {
        return this.prevParameterName.equals(this.nextParameterName);
    }

    public boolean isDefaultValueEquals() {
        if (this.prevParameterDefaultValue != null && this.nextParameterDefaultValue != null) {
            return this.prevParameterDefaultValue.equals(this.nextParameterDefaultValue);
        }
        return this.prevParameterDefaultValue == null && this.nextParameterDefaultValue == null;
    }

    public boolean isDefaultValueRemoved() {
        return this.prevParameterDefaultValue != null && this.nextParameterDefaultValue == null;
    }

    public TypeComparator getTypeEquality() {
        return this.typeEquality;
    }

    public String getPrevParameterName() {
        return this.prevParameterName;
    }

    public String getPrevParameterDefaultValue() {
        return this.prevParameterDefaultValue;
    }

    public boolean isDefaultValueChanged() {
        if (this.prevParameterDefaultValue != null && this.nextParameterDefaultValue != null) {
            if (!this.prevParameterDefaultValue.equals(this.nextParameterDefaultValue)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
