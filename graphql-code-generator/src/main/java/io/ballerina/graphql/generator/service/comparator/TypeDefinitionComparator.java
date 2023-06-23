package io.ballerina.graphql.generator.service.comparator;

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLType;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.generateObjectType;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getMethodDeclarationName;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getRecordFieldName;

/**
 * Utility class to store result comparing type definitions.
 */
public class TypeDefinitionComparator {
    private static final String WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD = "warning: In '%s' record type" +
            " '%s' field assigned '%s' default value has removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_CHANGE_OBJECT_RECORD_FIELD_TYPE = "warning: In '%s' record type '%s' " +
            "field type has changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_CHANGE_INPUT_RECORD_FIELD_TYPE = "warning: In '%s' record type '%s' " +
            "field type has changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_REMOVE_INPUT_RECORD_FIELD = "warning: In '%s' input type '%s' field " +
            "has removed. This can brake clients.";
    private static final String WARNING_MESSAGE_REMOVE_OBJECT_RECORD_FIELD = "warning: In '%s' record type " +
            "'%s' field has removed. This can brake clients.";
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
    private MetadataNode mergedMetadata;
    private Token mergedVisibilityQualifier;
    private Token mergedTypeKeyword;

    public TypeDefinitionComparator(TypeDefinitionNode prevTypeDefinition, TypeDefinitionNode nextTypeDefinition
    ) {
        this.prevTypeDefinition = prevTypeDefinition;
        this.nextTypeDefinition = nextTypeDefinition;
        mergedMetadata = nextTypeDefinition.metadata().orElse(null);
        mergedVisibilityQualifier = prevTypeDefinition.visibilityQualifier().orElse(null);
        mergedTypeKeyword = prevTypeDefinition.typeKeyword();
        breakingChangeWarnings = new ArrayList<>();
        if (isMatch()) {
            handleFrontNewLine();
        }
    }

    public boolean isMatch() {
        return prevTypeDefinition.typeName().text().equals(nextTypeDefinition.typeName().text());
    }

    public Node getMergedTypeDescriptor() {
        return mergedTypeDescriptor;
    }

    public void handleMergeTypeDescriptor(GraphQLType graphqlType) {
        ObjectTypeDescriptorNode nextObjectType = generateObjectType(nextTypeDefinition.typeDescriptor());
        if (prevTypeDefinition.typeDescriptor() instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode prevIntersectionType =
                    (IntersectionTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            if (nextObjectType != null) {
                mergedTypeDescriptor = handleMergeObjectType(prevIntersectionType, nextObjectType);
            }
        } else if (prevTypeDefinition.typeDescriptor() instanceof DistinctTypeDescriptorNode) {
            DistinctTypeDescriptorNode prevDistinctType =
                    (DistinctTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            if (nextObjectType != null) {
                mergedTypeDescriptor = handleMergeObjectType(prevDistinctType, nextObjectType);
            }
        } else if (prevTypeDefinition.typeDescriptor() instanceof ObjectTypeDescriptorNode) {
            ObjectTypeDescriptorNode prevObjectType =
                    (ObjectTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            if (nextObjectType != null) {
                mergedTypeDescriptor = handleMergeObjectType(prevObjectType, nextObjectType);
            }
        } else if (prevTypeDefinition.typeDescriptor() instanceof RecordTypeDescriptorNode &&
                nextTypeDefinition.typeDescriptor() instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode prevRecordType = (RecordTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            RecordTypeDescriptorNode nextRecordType = (RecordTypeDescriptorNode) nextTypeDefinition.typeDescriptor();
            RecordTypeComparator recordTypeEquality =
                    new RecordTypeComparator(prevRecordType, nextRecordType);
            handleRecordTypeBreakingChanges(recordTypeEquality, graphqlType instanceof GraphQLInputObjectType);
            mergedTypeDescriptor = recordTypeEquality.generateCombinedRecordType();
        } else if (prevTypeDefinition.typeDescriptor() instanceof UnionTypeDescriptorNode &&
                nextTypeDefinition.typeDescriptor() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionType = (UnionTypeDescriptorNode) prevTypeDefinition.typeDescriptor();
            UnionTypeDescriptorNode nextUnionType = (UnionTypeDescriptorNode) nextTypeDefinition.typeDescriptor();
            UnionTypeComparator unionTypeEquality = new UnionTypeComparator(prevUnionType, nextUnionType);
            handleUnionTypeBreakingChanges(unionTypeEquality);
            mergedTypeDescriptor = unionTypeEquality.generateCombinedUnionType();
        }
    }

    private Node handleMergeObjectType(Node prevType, ObjectTypeDescriptorNode nextObjectType) {
        if (prevType instanceof ObjectTypeDescriptorNode) {
            ObjectTypeDescriptorNode prevObjectType = (ObjectTypeDescriptorNode) prevType;
            ServiceObjectComparator objectTypeEquality =
                    new ServiceObjectComparator(prevObjectType, nextObjectType);
            handleObjectTypeBreakingChanges(objectTypeEquality);
            return objectTypeEquality.generateCombinedObjectTypeDescriptor();
        } else if (prevType instanceof DistinctTypeDescriptorNode) {
            DistinctTypeDescriptorNode prevDistinctObjectType = (DistinctTypeDescriptorNode) prevType;
            ObjectTypeDescriptorNode prevServiceObject =
                    (ObjectTypeDescriptorNode) prevDistinctObjectType.typeDescriptor();
            ServiceObjectComparator objectTypeEquality =
                    new ServiceObjectComparator(prevServiceObject, nextObjectType);
            handleDistinctObjectTypeBreakingChanges(objectTypeEquality);
            return prevDistinctObjectType.modify(prevDistinctObjectType.distinctKeyword(),
                    objectTypeEquality.generateCombinedObjectTypeDescriptor());
        } else if (prevType instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode prevIntersectionType = (IntersectionTypeDescriptorNode) prevType;
            return prevIntersectionType.modify(prevIntersectionType.leftTypeDesc(),
                    prevIntersectionType.bitwiseAndToken(),
                    handleMergeObjectType(prevIntersectionType.rightTypeDesc(), nextObjectType));
        }
        return null;
    }

    private void handleUnionTypeBreakingChanges(UnionTypeComparator unionTypeEquality) {
        for (String removedUnionMember : unionTypeEquality.getRemovedUnionMembers()) {
            breakingChangeWarnings.add(
                    String.format(WARNING_MESSAGE_REMOVE_UNION_MEMBER, prevTypeDefinition.typeName().text(),
                            removedUnionMember));
        }
    }

    private void handleRecordTypeBreakingChanges(RecordTypeComparator recordTypeEquality,
                                                 boolean isGraphqlInputType) {
        for (Node removedField : recordTypeEquality.getRemovedFields()) {
            if (isGraphqlInputType) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_REMOVE_INPUT_RECORD_FIELD, prevTypeDefinition.typeName().text(),
                                getRecordFieldName(removedField)));
            } else {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_OBJECT_RECORD_FIELD,
                        prevTypeDefinition.typeName().text(), getRecordFieldName(removedField)));
            }
        }
        for (Node addedField : recordTypeEquality.getAddedFields()) {
            if (addedField instanceof RecordFieldNode && isGraphqlInputType) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_ADD_INPUT_TYPE_FIELD_WITH_NO_DEFAULT_VALUE,
                                prevTypeDefinition.typeName().text(), getRecordFieldName(addedField)));
            }
        }
        for (RecordFieldComparator updatedRecordFieldEquality : recordTypeEquality.getUpdatedRecordFields()) {
            if (updatedRecordFieldEquality.isDefaultValueRemoved() && isGraphqlInputType) {
                breakingChangeWarnings.add(String.format(
                        WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD,
                        prevTypeDefinition.typeName().text(), updatedRecordFieldEquality.getPrevRecordFieldName(),
                        updatedRecordFieldEquality.getPrevRecordFieldDefaultValue()));
            }
            if (updatedRecordFieldEquality.isFieldTypeChanged()) {
                if (isGraphqlInputType) {
                    breakingChangeWarnings.add(String.format(
                            WARNING_MESSAGE_CHANGE_INPUT_RECORD_FIELD_TYPE,
                            prevTypeDefinition.typeName().text(), updatedRecordFieldEquality.getPrevRecordFieldName(),
                            updatedRecordFieldEquality.getTypeEquality().getPrevType(),
                            updatedRecordFieldEquality.getTypeEquality().getNextType()));
                } else {
                    breakingChangeWarnings.add(String.format(
                            WARNING_MESSAGE_CHANGE_OBJECT_RECORD_FIELD_TYPE,
                            prevTypeDefinition.typeName().text(), updatedRecordFieldEquality.getPrevRecordFieldName(),
                            updatedRecordFieldEquality.getTypeEquality().getPrevType(),
                            updatedRecordFieldEquality.getTypeEquality().getNextType()));
                }
            }
        }
    }

    private void handleDistinctObjectTypeBreakingChanges(ServiceObjectComparator objectTypeEquality) {
        for (MethodDeclarationNode removedMethodDeclaration :
                objectTypeEquality.getRemovedResolverMethodDeclarations()) {
            breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_INTERFACE_SERVICE_OBJECT_METHOD_DECLARATION,
                    prevTypeDefinition.typeName().text(), getMethodDeclarationName(removedMethodDeclaration)));
        }
        List<MethodDeclarationComparator> updatedMethodDeclarations =
                objectTypeEquality.getUpdatedMethodDeclarations();
        for (MethodDeclarationComparator updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            FunctionSignatureComparator updatedMethodSignatureEquality =
                    updatedMethodDeclarationEquality.getFunctionSignatureEqualityResult();
            for (String removedParameterName : updatedMethodSignatureEquality.getRemovedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterComparator parameterEquality :
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
            for (ParameterComparator defaultValueRemovedParameterEquality :
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

    private void handleObjectTypeBreakingChanges(ServiceObjectComparator objectTypeEquality) {
        for (MethodDeclarationNode removedMethodDeclaration :
                objectTypeEquality.getRemovedResolverMethodDeclarations()) {
            breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_SERVICE_OBJECT_METHOD_DECLARATION,
                    prevTypeDefinition.typeName().text(), getMethodDeclarationName(removedMethodDeclaration)));
        }
        List<MethodDeclarationComparator> updatedMethodDeclarations =
                objectTypeEquality.getUpdatedMethodDeclarations();
        for (MethodDeclarationComparator updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            FunctionSignatureComparator updatedMethodSignatureEquality =
                    updatedMethodDeclarationEquality.getFunctionSignatureEqualityResult();
            for (String removedParameterName : updatedMethodSignatureEquality.getRemovedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                        prevTypeDefinition.typeName().text(), updatedMethodDeclarationEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterComparator parameterEquality :
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
            for (ParameterComparator defaultValueRemovedParameterEquality :
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
        return prevTypeDefinition.modify(mergedMetadata, mergedVisibilityQualifier, mergedTypeKeyword,
                nextTypeDefinition.typeName(), mergedTypeDescriptor,
                nextTypeDefinition.semicolonToken()
        );
    }

    private void handleFrontNewLine() {
        if (mergedMetadata != null) {
            if (mergedVisibilityQualifier != null) {
                mergedVisibilityQualifier = mergedVisibilityQualifier.modify(createEmptyMinutiaeList(),
                        createEmptyMinutiaeList());
            }
            mergedTypeKeyword = mergedTypeKeyword.modify(createEmptyMinutiaeList(), createEmptyMinutiaeList());
        }
    }

    public List<String> getBreakingChangeWarnings() {
        return breakingChangeWarnings;
    }
}
