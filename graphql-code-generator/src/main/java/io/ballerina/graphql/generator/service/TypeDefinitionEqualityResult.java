package io.ballerina.graphql.generator.service;

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLType;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMethodDeclarationName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getRecordFieldName;

/**
 * Utility class to store result comparing type definitions.
 */
public class TypeDefinitionEqualityResult {
    private static final String WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD = "warning: In '%s' record type" +
            " '%s' field assigned '%s' default value has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_RECORD_FIELD_TYPE_CHANGED = "warning: In '%s' record type '%s' " +
            "field type has changed from '%s' to '%s'. This can break existing clients.";
    private static final String REMOVE_INPUT_TYPE_FIELD_MESSAGE =
            "warning: In '%s' input type '%s' field has removed. This can brake clients";
    private static final String WARNING_MESSAGE_REMOVE_UNION_MEMBER =
            "warning: In '%s' union type '%s' member has " + "removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_ADD_INPUT_TYPE_FIELD_WITH_NO_DEFAULT_VALUE = "warning: In '%s' input " +
            "type '%s' field is introduced without a default value. This can brake available clients";
    private static final String WARNING_MESSAGE_REMOVE_SERVICE_OBJECT_METHOD_DECLARATION = "warning: In '%s' service " +
            "object '%s' method declaration has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT = "warning: In '%s' service " +
            "object '%s' method declaration '%s' parameter has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT = "warning: In '%s' service " +
            "object '%s' method declaration '%s' parameter type change from '%s' to '%s'. This can break existing " +
            "clients.";
    private static final String WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD =
            "warning: In '%s' service object '%s' method changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_OBJECT = "warning: In '%s' service " +
            "object '%s' function qualifier list changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT = "warning: " +
            "In '%s' service object '%s' method declaration return type has changed from '%s' to '%s'. This can break" +
            " existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD = "warning:" +
            " In '%s' service object '%s' method declaration '%s' parameter added without default value. This can " +
            "break existing clients.";
    private static final String WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD = "warning: " +
            "In '%s' service object '%s' method declaration '%s' parameter assigned '%s' default value has removed. " +
            "This can break existing clients.";
    private static final String WARNING_MESSAGE_REMOVE_INTERFACE_SERVICE_OBJECT_METHOD_DECLARATION = "warning: In " +
            "'%s' interface service object '%s' method declaration has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_INTERFACE = "warning: In '%s' " +
            "interface '%s' function qualifier list changed from '%s' to '%s'. This can break existing clients.";

    private final TypeDefinitionNode prevTypeDefinition;
    private final TypeDefinitionNode nextTypeDefinition;
    private List<String> breakingChangeWarnings;
    private Node mergedTypeDescriptor;

    public TypeDefinitionEqualityResult(TypeDefinitionNode prevTypeDefinition, TypeDefinitionNode nextTypeDefinition
    ) {
        this.prevTypeDefinition = prevTypeDefinition;
        this.nextTypeDefinition = nextTypeDefinition;
        breakingChangeWarnings = new ArrayList<>();
    }

    public boolean isMatch() {
        return prevTypeDefinition.typeName().text().equals(nextTypeDefinition.typeName().text());
    }

    public Node getMergedTypeDescriptor() {
        return mergedTypeDescriptor;
    }

    public void handleMergeTypeDescriptor(GraphQLType graphqlType) {
        if (prevTypeDefinition.typeDescriptor() instanceof ObjectTypeDescriptorNode &&
                nextTypeDefinition.typeDescriptor() instanceof ObjectTypeDescriptorNode) {
            ObjectTypeDescriptorNode prevObjectType = (ObjectTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            ObjectTypeDescriptorNode nextObjectType = (ObjectTypeDescriptorNode) nextTypeDefinition.typeDescriptor();
            ServiceObjectEqualityResult objectTypeEquality =
                    new ServiceObjectEqualityResult(prevObjectType, nextObjectType);
            handleObjectTypeBreakingChanges(objectTypeEquality);
            mergedTypeDescriptor = objectTypeEquality.generateCombinedObjectTypeDescriptor();
        } else if (prevTypeDefinition.typeDescriptor() instanceof DistinctTypeDescriptorNode &&
                nextTypeDefinition.typeDescriptor() instanceof DistinctTypeDescriptorNode) {
            DistinctTypeDescriptorNode prevDistinctServiceObject =
                    (DistinctTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            DistinctTypeDescriptorNode nextDistinctServiceObject =
                    (DistinctTypeDescriptorNode) nextTypeDefinition.typeDescriptor();

            ObjectTypeDescriptorNode prevServiceObject =
                    (ObjectTypeDescriptorNode) prevDistinctServiceObject.typeDescriptor();
            ObjectTypeDescriptorNode nextServiceObject =
                    (ObjectTypeDescriptorNode) nextDistinctServiceObject.typeDescriptor();
            ServiceObjectEqualityResult objectTypeEquality =
                    new ServiceObjectEqualityResult(prevServiceObject, nextServiceObject);
            handleDistinctObjectTypeBreakingChanges(objectTypeEquality);
            mergedTypeDescriptor = prevDistinctServiceObject.modify(nextDistinctServiceObject.distinctKeyword(),
                    objectTypeEquality.generateCombinedObjectTypeDescriptor());
        } else if (prevTypeDefinition.typeDescriptor() instanceof RecordTypeDescriptorNode &&
                nextTypeDefinition.typeDescriptor() instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode prevRecordType = (RecordTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            RecordTypeDescriptorNode nextRecordType = (RecordTypeDescriptorNode) nextTypeDefinition.typeDescriptor();
            RecordTypeEqualityResult recordTypeEquality =
                    new RecordTypeEqualityResult(prevRecordType, nextRecordType);
            recordTypeEquality.separateMembers();
            handleRecordTypeBreakingChanges(recordTypeEquality, graphqlType instanceof GraphQLInputObjectType);
            mergedTypeDescriptor = recordTypeEquality.generateCombinedRecordType();
        } else if (prevTypeDefinition.typeDescriptor() instanceof UnionTypeDescriptorNode &&
                nextTypeDefinition.typeDescriptor() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionType = (UnionTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            UnionTypeDescriptorNode nextUnionType = (UnionTypeDescriptorNode) nextTypeDefinition.typeDescriptor();
            UnionTypeEqualityResult unionTypeEquality = new UnionTypeEqualityResult(prevUnionType, nextUnionType);
            unionTypeEquality.separateMembers();
            handleUnionTypeBreakingChanges(unionTypeEquality);
            mergedTypeDescriptor = unionTypeEquality.generateCombinedUnionType();
        }
    }

    private void handleUnionTypeBreakingChanges(UnionTypeEqualityResult unionTypeEquality) {
        for (String removedUnionMember : unionTypeEquality.getRemovedUnionMembers()) {
                    breakingChangeWarnings.add(
                            String.format(WARNING_MESSAGE_REMOVE_UNION_MEMBER, prevTypeDefinition.typeName().text(),
                                    removedUnionMember));
        }
    }

    private void handleRecordTypeBreakingChanges(RecordTypeEqualityResult recordTypeEquality,
                                                 boolean isGraphqlInputType) {
        for (Node removedField : recordTypeEquality.getRemovedFields()) {
            breakingChangeWarnings.add(
                    String.format(REMOVE_INPUT_TYPE_FIELD_MESSAGE, prevTypeDefinition.typeName().text(),
                            getRecordFieldName(removedField)));
        }
        for (Node addedField : recordTypeEquality.getAddedFields()) {
            if (addedField instanceof RecordFieldNode) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_ADD_INPUT_TYPE_FIELD_WITH_NO_DEFAULT_VALUE,
                                prevTypeDefinition.typeName().text(), getRecordFieldName(addedField)));
            }
        }
        for (RecordFieldEqualityResult updatedRecordFieldEquality : recordTypeEquality.getUpdatedRecordFields()) {
            if (updatedRecordFieldEquality.isDefaultValueRemoved() && isGraphqlInputType) {
                breakingChangeWarnings.add(String.format(
                        WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD,
                        prevTypeDefinition.typeName().text(), updatedRecordFieldEquality.getPrevRecordFieldName(),
                        updatedRecordFieldEquality.getPrevRecordFieldDefaultValue()));
            }
            if (updatedRecordFieldEquality.isFieldTypeChanged()) {
                breakingChangeWarnings.add(String.format(
                        WARNING_MESSAGE_RECORD_FIELD_TYPE_CHANGED,
                        prevTypeDefinition.typeName().text(), updatedRecordFieldEquality.getPrevRecordFieldName(),
                        updatedRecordFieldEquality.getTypeEquality().getPrevType(),
                        updatedRecordFieldEquality.getTypeEquality().getNextType()));
            }
        }
    }

    private void handleDistinctObjectTypeBreakingChanges(ServiceObjectEqualityResult objectTypeEquality) {
        for (MethodDeclarationNode removedMethodDeclaration :
                objectTypeEquality.getRemovedResolverMethodDeclarations()) {
            breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_INTERFACE_SERVICE_OBJECT_METHOD_DECLARATION,
                    prevTypeDefinition.typeName().text(), getMethodDeclarationName(removedMethodDeclaration)));
        }
        List<MethodDeclarationEqualityResult> updatedMethodDeclarations =
                objectTypeEquality.getUpdatedMethodDeclarations();
        for (MethodDeclarationEqualityResult updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            FunctionSignatureEqualityResult updatedMethodSignatureEquality =
                    updatedMethodDeclarationEquality.getFunctionSignatureEqualityResult();
            for (String removedParameterName : updatedMethodSignatureEquality.getRemovedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterEqualityResult parameterEquality :
                    updatedMethodSignatureEquality.getTypeChangedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        parameterEquality.getPrevParameterName(), parameterEquality.getTypeEquality().getPrevType(),
                        parameterEquality.getTypeEquality().getNextType()));
            }
            if (updatedMethodDeclarationEquality.isGetAndSubscribeInterchanged()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodDeclarationEquality.getPrevMethodType(),
                                updatedMethodDeclarationEquality.getNextMethodType()));
            }
            if (!updatedMethodDeclarationEquality.isQualifierSimilar()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_INTERFACE,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        updatedMethodDeclarationEquality.getPrevMainQualifier(),
                        updatedMethodDeclarationEquality.getNextMainQualifier()));
            }
            if (!updatedMethodSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getNextType()));
            }
            for (String addedViolatedParameterName : updatedMethodSignatureEquality.getAddedViolatedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(), addedViolatedParameterName));
            }
            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                    updatedMethodSignatureEquality.getDefaultValueRemovedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                defaultValueRemovedParameterEquality.getPrevParameterName(),
                                defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
            }
        }
    }

    private void handleObjectTypeBreakingChanges(ServiceObjectEqualityResult objectTypeEquality) {
        for (MethodDeclarationNode removedMethodDeclaration :
                objectTypeEquality.getRemovedResolverMethodDeclarations()) {
            breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_SERVICE_OBJECT_METHOD_DECLARATION,
                    prevTypeDefinition.typeName().text(), getMethodDeclarationName(removedMethodDeclaration)));
        }
        List<MethodDeclarationEqualityResult> updatedMethodDeclarations =
                objectTypeEquality.getUpdatedMethodDeclarations();
        for (MethodDeclarationEqualityResult updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            FunctionSignatureEqualityResult updatedMethodSignatureEquality =
                    updatedMethodDeclarationEquality.getFunctionSignatureEqualityResult();
            for (String removedParameterName : updatedMethodSignatureEquality.getRemovedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterEqualityResult parameterEquality :
                    updatedMethodSignatureEquality.getTypeChangedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        parameterEquality.getPrevParameterName(), parameterEquality.getTypeEquality().getPrevType(),
                        parameterEquality.getTypeEquality().getNextType()));
            }
            if (updatedMethodDeclarationEquality.isGetAndSubscribeInterchanged()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodDeclarationEquality.getPrevMethodType(),
                                updatedMethodDeclarationEquality.getNextMethodType()));
            }
            if (!updatedMethodDeclarationEquality.isQualifierSimilar()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        updatedMethodDeclarationEquality.getPrevMainQualifier(),
                        updatedMethodDeclarationEquality.getNextMainQualifier()));
            }
            if (!updatedMethodSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getNextType()));
            }
            for (String addedViolatedParameterName : updatedMethodSignatureEquality.getAddedViolatedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(), addedViolatedParameterName));
            }
            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                    updatedMethodSignatureEquality.getDefaultValueRemovedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD,
                                prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                defaultValueRemovedParameterEquality.getPrevParameterName(),
                                defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
            }
        }
    }

    public TypeDefinitionNode generateCombinedTypeDefinition() {
        return prevTypeDefinition.modify(
                nextTypeDefinition.metadata().orElse(null),
                prevTypeDefinition.visibilityQualifier().orElse(null),
                nextTypeDefinition.typeKeyword(), nextTypeDefinition.typeName(), mergedTypeDescriptor,
                nextTypeDefinition.semicolonToken()
        );
    }

    public List<String> getBreakingChangeWarnings() {
        return breakingChangeWarnings;
    }
}
