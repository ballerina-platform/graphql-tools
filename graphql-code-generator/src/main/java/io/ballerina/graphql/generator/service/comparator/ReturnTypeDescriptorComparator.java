package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;

/**
 * Utility class to store result comparing return type descriptors.
 */
public class ReturnTypeDescriptorComparator {
    private final ReturnTypeDescriptorNode prevReturnTypeDescriptor;
    private final ReturnTypeDescriptorNode nextReturnTypeDescriptor;
    private TypeComparator returnTypeEquality;

    public ReturnTypeDescriptorComparator(ReturnTypeDescriptorNode prevReturnTypeDescriptor,
                                          ReturnTypeDescriptorNode nextReturnTypeDescriptor) {
        this.prevReturnTypeDescriptor = prevReturnTypeDescriptor;
        this.nextReturnTypeDescriptor = nextReturnTypeDescriptor;
        if (prevReturnTypeDescriptor != null && nextReturnTypeDescriptor != null) {
            returnTypeEquality =
                    new TypeComparator(prevReturnTypeDescriptor.type(), nextReturnTypeDescriptor.type());
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
