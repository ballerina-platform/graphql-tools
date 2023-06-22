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
        prevParameterName = getParameterName(prevParameter);
        nextParameterName = getParameterName(nextParameter);
        prevParameterDefaultValue = getParameterDefaultValue(prevParameter);
        nextParameterDefaultValue = getParameterDefaultValue(nextParameter);
    }

    public void setTypeEquality(TypeComparator typeEquality) {
        this.typeEquality = typeEquality;
    }

    public boolean isEqual() {
        return isMatch() && typeEquality.isEqual() && isDefaultValueEquals();
    }

    public boolean isMatch() {
        return prevParameterName.equals(nextParameterName);
    }

    public boolean isDefaultValueEquals() {
        if (prevParameterDefaultValue != null && nextParameterDefaultValue != null) {
            return prevParameterDefaultValue.equals(nextParameterDefaultValue);
        }
        return prevParameterDefaultValue == null && nextParameterDefaultValue == null;
    }

    public boolean isDefaultValueRemoved() {
        return prevParameterDefaultValue != null && nextParameterDefaultValue == null;
    }

    public TypeComparator getTypeEquality() {
        return typeEquality;
    }

    public String getPrevParameterName() {
        return prevParameterName;
    }

    public String getPrevParameterDefaultValue() {
        return prevParameterDefaultValue;
    }

    public boolean isDefaultValueChanged() {
        if (prevParameterDefaultValue != null && nextParameterDefaultValue != null) {
            if (!prevParameterDefaultValue.equals(nextParameterDefaultValue)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
