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
        this.mergedMetadata = nextTypeDefinition.metadata().orElse(null);
        this.mergedVisibilityQualifier = prevTypeDefinition.visibilityQualifier().orElse(null);
        this.mergedTypeKeyword = prevTypeDefinition.typeKeyword();
        this.breakingChangeWarnings = new ArrayList<>();
        if (isMatch()) {
            handleFrontNewLine();
        }
    }

    public boolean isMatch() {
        return this.prevTypeDefinition.typeName().text().equals(this.nextTypeDefinition.typeName().text());
    }

    public Node getMergedTypeDescriptor() {
        return this.mergedTypeDescriptor;
    }

    public void handleMergeTypeDescriptor(GraphQLType graphqlType) {
        ObjectTypeDescriptorNode nextObjectType = generateObjectType(this.nextTypeDefinition.typeDescriptor());
        RecordTypeDescriptorNode nextRecordType = generateRecordType(this.nextTypeDefinition.typeDescriptor());
        if (nextObjectType != null) {
            if (this.prevTypeDefinition.typeDescriptor() instanceof IntersectionTypeDescriptorNode) {
                IntersectionTypeDescriptorNode prevIntersectionType =
                        (IntersectionTypeDescriptorNode) this.prevTypeDefinition.typeDescriptor();
                this.mergedTypeDescriptor = handleMergeObjectType(prevIntersectionType, nextObjectType);
            } else if (this.prevTypeDefinition.typeDescriptor() instanceof DistinctTypeDescriptorNode) {
                DistinctTypeDescriptorNode prevDistinctType =
                        (DistinctTypeDescriptorNode) this.prevTypeDefinition.typeDescriptor();
                this.mergedTypeDescriptor = handleMergeObjectType(prevDistinctType, nextObjectType);
            } else if (this.prevTypeDefinition.typeDescriptor() instanceof ObjectTypeDescriptorNode) {
                ObjectTypeDescriptorNode prevObjectType =
                        (ObjectTypeDescriptorNode) this.prevTypeDefinition.typeDescriptor();
                this.mergedTypeDescriptor = handleMergeObjectType(prevObjectType, nextObjectType);
            }
        } else if (nextRecordType != null) {
            if (this.prevTypeDefinition.typeDescriptor() instanceof IntersectionTypeDescriptorNode) {
                IntersectionTypeDescriptorNode prevIntersectionType =
                        (IntersectionTypeDescriptorNode) this.prevTypeDefinition.typeDescriptor();
                this.mergedTypeDescriptor = handleMergeRecordType(prevIntersectionType, nextRecordType,
                        graphqlType instanceof GraphQLInputObjectType);
            } else if (this.prevTypeDefinition.typeDescriptor() instanceof RecordTypeDescriptorNode) {
                RecordTypeDescriptorNode prevRecordType =
                        (RecordTypeDescriptorNode) this.prevTypeDefinition.typeDescriptor();
                this.mergedTypeDescriptor = handleMergeRecordType(prevRecordType, nextRecordType,
                        graphqlType instanceof GraphQLInputObjectType);
            }
        } else if (this.prevTypeDefinition.typeDescriptor() instanceof UnionTypeDescriptorNode &&
                this.nextTypeDefinition.typeDescriptor() instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode prevUnionType = (UnionTypeDescriptorNode) this.prevTypeDefinition.typeDescriptor();
            UnionTypeDescriptorNode nextUnionType = (UnionTypeDescriptorNode) this.nextTypeDefinition.typeDescriptor();
            UnionTypeComparator unionTypeEquality = new UnionTypeComparator(prevUnionType, nextUnionType);
            handleUnionTypeBreakingChanges(unionTypeEquality);
            this.mergedTypeDescriptor = unionTypeEquality.generateCombinedUnionType();
        }
    }

    private Node handleMergeRecordType(Node prevType, RecordTypeDescriptorNode nextRecordType,
                                       boolean isGraphqlInputType) {
        if (prevType instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode prevRecordType = (RecordTypeDescriptorNode) prevType;
            RecordTypeComparator recordTypeEquality =
                    new RecordTypeComparator(prevRecordType, nextRecordType);
            handleRecordTypeBreakingChanges(recordTypeEquality, isGraphqlInputType);
            return recordTypeEquality.generateCombinedRecordType();
        } else if (prevType instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode prevIntersectionType = (IntersectionTypeDescriptorNode) prevType;
            return prevIntersectionType.modify(prevIntersectionType.leftTypeDesc(),
                    prevIntersectionType.bitwiseAndToken(),
                    handleMergeRecordType(prevIntersectionType.rightTypeDesc(), nextRecordType, isGraphqlInputType));
        }
        return null;
    }

    private RecordTypeDescriptorNode generateRecordType(Node typeDescriptor) {
        if (typeDescriptor instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode intersectionTypeDescriptor = (IntersectionTypeDescriptorNode) typeDescriptor;
            return generateRecordType(intersectionTypeDescriptor.rightTypeDesc());
        } else if (typeDescriptor instanceof RecordTypeDescriptorNode) {
            return (RecordTypeDescriptorNode) typeDescriptor;
        } else {
            return null;
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
            this.breakingChangeWarnings.add(
                    String.format(WARNING_MESSAGE_REMOVE_UNION_MEMBER, this.prevTypeDefinition.typeName().text(),
                            removedUnionMember));
        }
    }

    private void handleRecordTypeBreakingChanges(RecordTypeComparator recordTypeEquality,
                                                 boolean isGraphqlInputType) {
        for (Node removedField : recordTypeEquality.getRemovedFields()) {
            if (isGraphqlInputType) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_REMOVE_INPUT_RECORD_FIELD,
                                this.prevTypeDefinition.typeName().text(),
                                getRecordFieldName(removedField)));
            } else {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_OBJECT_RECORD_FIELD,
                        this.prevTypeDefinition.typeName().text(), getRecordFieldName(removedField)));
            }
        }
        for (Node addedField : recordTypeEquality.getAddedFields()) {
            if (addedField instanceof RecordFieldNode && isGraphqlInputType) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_ADD_INPUT_TYPE_FIELD_WITH_NO_DEFAULT_VALUE,
                                this.prevTypeDefinition.typeName().text(), getRecordFieldName(addedField)));
            }
        }
        for (RecordFieldComparator updatedRecordFieldEquality : recordTypeEquality.getUpdatedRecordFields()) {
            if (updatedRecordFieldEquality.isDefaultValueRemoved() && isGraphqlInputType) {
                this.breakingChangeWarnings.add(String.format(
                        WARNING_MESSAGE_DEFAULT_VALUE_REMOVED_IN_RECORD_FIELD,
                        this.prevTypeDefinition.typeName().text(), updatedRecordFieldEquality.getPrevRecordFieldName(),
                        updatedRecordFieldEquality.getPrevRecordFieldDefaultValue()));
            }
            if (updatedRecordFieldEquality.isFieldTypeChanged()) {
                if (isGraphqlInputType) {
                    this.breakingChangeWarnings.add(String.format(
                            WARNING_MESSAGE_CHANGE_INPUT_RECORD_FIELD_TYPE,
                            this.prevTypeDefinition.typeName().text(),
                            updatedRecordFieldEquality.getPrevRecordFieldName(),
                            updatedRecordFieldEquality.getTypeEquality().getPrevType(),
                            updatedRecordFieldEquality.getTypeEquality().getNextType()));
                } else {
                    this.breakingChangeWarnings.add(String.format(
                            WARNING_MESSAGE_CHANGE_OBJECT_RECORD_FIELD_TYPE,
                            this.prevTypeDefinition.typeName().text(),
                            updatedRecordFieldEquality.getPrevRecordFieldName(),
                            updatedRecordFieldEquality.getTypeEquality().getPrevType(),
                            updatedRecordFieldEquality.getTypeEquality().getNextType()));
                }
            }
        }
    }

    private void handleDistinctObjectTypeBreakingChanges(ServiceObjectComparator objectTypeEquality) {
        for (MethodDeclarationNode removedMethodDeclaration :
                objectTypeEquality.getRemovedResolverMethodDeclarations()) {
            this.breakingChangeWarnings.add(
                    String.format(WARNING_MESSAGE_REMOVE_INTERFACE_SERVICE_OBJECT_METHOD_DECLARATION,
                            this.prevTypeDefinition.typeName().text(),
                            getMethodDeclarationName(removedMethodDeclaration)));
        }
        List<MethodDeclarationComparator> updatedMethodDeclarations =
                objectTypeEquality.getUpdatedMethodDeclarations();
        for (MethodDeclarationComparator updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            FunctionSignatureComparator updatedMethodSignatureEquality =
                    updatedMethodDeclarationEquality.getFunctionSignatureEqualityResult();
            for (String removedParameterName : updatedMethodSignatureEquality.getRemovedParameters()) {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                        this.prevTypeDefinition.typeName().text(),
                        updatedMethodDeclarationEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterComparator parameterEquality :
                    updatedMethodSignatureEquality.getTypeChangedParameters()) {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT,
                        this.prevTypeDefinition.typeName().text(),
                        updatedMethodDeclarationEquality.getPrevFunctionName(),
                        parameterEquality.getPrevParameterName(), parameterEquality.getTypeEquality().getPrevType(),
                        parameterEquality.getTypeEquality().getNextType()));
            }
            if (updatedMethodDeclarationEquality.isGetAndSubscribeInterchanged()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodDeclarationEquality.getPrevMethodType(),
                                updatedMethodDeclarationEquality.getNextMethodType()));
            }
            if (!updatedMethodDeclarationEquality.isQualifierSimilar()) {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_INTERFACE,
                        this.prevTypeDefinition.typeName().text(),
                        updatedMethodDeclarationEquality.getPrevFunctionName(),
                        updatedMethodDeclarationEquality.getPrevMainQualifier(),
                        updatedMethodDeclarationEquality.getNextMainQualifier()));
            }
            if (!updatedMethodSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getNextType()));
            }
            for (String addedViolatedParameterName : updatedMethodSignatureEquality.getAddedViolatedParameters()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(), addedViolatedParameterName));
            }
            for (ParameterComparator defaultValueRemovedParameterEquality :
                    updatedMethodSignatureEquality.getDefaultValueRemovedParameters()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                defaultValueRemovedParameterEquality.getPrevParameterName(),
                                defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
            }
        }
    }

    private void handleObjectTypeBreakingChanges(ServiceObjectComparator objectTypeEquality) {
        for (MethodDeclarationNode removedMethodDeclaration :
                objectTypeEquality.getRemovedResolverMethodDeclarations()) {
            this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_SERVICE_OBJECT_METHOD_DECLARATION,
                    this.prevTypeDefinition.typeName().text(), getMethodDeclarationName(removedMethodDeclaration)));
        }
        List<MethodDeclarationComparator> updatedMethodDeclarations =
                objectTypeEquality.getUpdatedMethodDeclarations();
        for (MethodDeclarationComparator updatedMethodDeclarationEquality : updatedMethodDeclarations) {
            FunctionSignatureComparator updatedMethodSignatureEquality =
                    updatedMethodDeclarationEquality.getFunctionSignatureEqualityResult();
            for (String removedParameterName : updatedMethodSignatureEquality.getRemovedParameters()) {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_REMOVE_PARAMETER_IN_SERVICE_OBJECT,
                        this.prevTypeDefinition.typeName().text(),
                        updatedMethodDeclarationEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterComparator parameterEquality :
                    updatedMethodSignatureEquality.getTypeChangedParameters()) {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_OBJECT,
                        this.prevTypeDefinition.typeName().text(),
                        updatedMethodDeclarationEquality.getPrevFunctionName(),
                        parameterEquality.getPrevParameterName(), parameterEquality.getTypeEquality().getPrevType(),
                        parameterEquality.getTypeEquality().getNextType()));
            }
            if (updatedMethodDeclarationEquality.isGetAndSubscribeInterchanged()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_OBJECT_METHOD,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodDeclarationEquality.getPrevMethodType(),
                                updatedMethodDeclarationEquality.getNextMethodType()));
            }
            if (!updatedMethodDeclarationEquality.isQualifierSimilar()) {
                this.breakingChangeWarnings.add(String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_OBJECT,
                        this.prevTypeDefinition.typeName().text(),
                        updatedMethodDeclarationEquality.getPrevFunctionName(),
                        updatedMethodDeclarationEquality.getPrevMainQualifier(),
                        updatedMethodDeclarationEquality.getNextMainQualifier()));
            }
            if (!updatedMethodSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_METHOD_DECLARATION_CHANGE_RETURN_TYPE_IN_SERVICE_OBJECT,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                                updatedMethodSignatureEquality.getReturnTypeEqualityResult().getNextType()));
            }
            for (String addedViolatedParameterName : updatedMethodSignatureEquality.getAddedViolatedParameters()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_OBJECT_METHOD,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(), addedViolatedParameterName));
            }
            for (ParameterComparator defaultValueRemovedParameterEquality :
                    updatedMethodSignatureEquality.getDefaultValueRemovedParameters()) {
                this.breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_OBJECT_METHOD,
                                this.prevTypeDefinition.typeName().text(),
                                updatedMethodDeclarationEquality.getPrevFunctionName(),
                                defaultValueRemovedParameterEquality.getPrevParameterName(),
                                defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
            }
        }
    }

    public TypeDefinitionNode generateCombinedTypeDefinition() {
        return this.prevTypeDefinition.modify(this.mergedMetadata, this.mergedVisibilityQualifier,
                this.mergedTypeKeyword,
                this.nextTypeDefinition.typeName(), this.mergedTypeDescriptor,
                this.nextTypeDefinition.semicolonToken()
        );
    }

    private void handleFrontNewLine() {
        if (this.mergedMetadata != null) {
            if (this.mergedVisibilityQualifier != null) {
                this.mergedVisibilityQualifier = this.mergedVisibilityQualifier.modify(createEmptyMinutiaeList(),
                        createEmptyMinutiaeList());
            }
            this.mergedTypeKeyword = this.mergedTypeKeyword.modify(createEmptyMinutiaeList(),
                    createEmptyMinutiaeList());
        }
    }

    public List<String> getBreakingChangeWarnings() {
        return this.breakingChangeWarnings;
    }
}
