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
            this.returnTypeEquality =
                    new TypeComparator(prevReturnTypeDescriptor.type(), nextReturnTypeDescriptor.type());
        }
    }

    public boolean isEqual() {
        if (this.prevReturnTypeDescriptor != null && this.nextReturnTypeDescriptor != null) {
            return this.returnTypeEquality.isEqual();
        } else if (this.prevReturnTypeDescriptor == null && this.nextReturnTypeDescriptor == null) {
            return true;
        }
        return false;
    }

    public String getPrevType() {
        if (this.returnTypeEquality != null) {
            return this.returnTypeEquality.getPrevType();
        }
        return null;
    }

    public String getNextType() {
        if (this.returnTypeEquality != null) {
            return this.returnTypeEquality.getNextType();
        }
        return null;
    }
}
