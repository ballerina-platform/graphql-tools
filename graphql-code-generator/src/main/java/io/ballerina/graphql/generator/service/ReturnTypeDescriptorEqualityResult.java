package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;

import static io.ballerina.graphql.generator.service.EqualityResultUtils.isTypeEquals;

/**
 * Utility class to store result comparing return type descriptors.
 */
public class ReturnTypeDescriptorEqualityResult {
    private final ReturnTypeDescriptorNode prevReturnTypeDescriptor;
    private final ReturnTypeDescriptorNode nextReturnTypeDescriptor;
    private TypeEqualityResult returnTypeEquality;

    public ReturnTypeDescriptorEqualityResult(ReturnTypeDescriptorNode prevReturnTypeDescriptor,
                                              ReturnTypeDescriptorNode nextReturnTypeDescriptor) {
        this.prevReturnTypeDescriptor = prevReturnTypeDescriptor;
        this.nextReturnTypeDescriptor = nextReturnTypeDescriptor;
        if (prevReturnTypeDescriptor != null && nextReturnTypeDescriptor != null) {
            returnTypeEquality = isTypeEquals(prevReturnTypeDescriptor.type(), nextReturnTypeDescriptor.type());
        }
    }

    public boolean isEqual() {
        if (prevReturnTypeDescriptor != null && nextReturnTypeDescriptor != null) {
            return returnTypeEquality.isEqual();
        } else if (prevReturnTypeDescriptor == null && nextReturnTypeDescriptor == null) {
            return true;
        }
        return false;
    }

    public String getPrevType() {
        if (returnTypeEquality != null) {
            return returnTypeEquality.getPrevType();
        }
        return null;
    }

    public String getNextType() {
        if (returnTypeEquality != null) {
            return returnTypeEquality.getNextType();
        }
        return null;
    }
}
