package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getFunctionName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isResolverFunction;

/**
 * Utility class used to store result comparing two class definitions.
 */
public class ClassDefinitionEqualityResult {
    private static final String WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE = "warning: In '%s' class " +
            "'%s' function definition return type has changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_CLASS = "warning: In '%s'" +
            " class '%s' function definition '%s' parameter added without default value. This can break existing " +
            "clients.";
    private static final String WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE_CLASS = "warning: In '%s' class '%s' " +
            "function definition '%s' parameter removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_CLASS = "warning: In '%s' class " +
            "'%s' function definition '%s' parameter type change from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_CLASS_FUNC = "warning: " +
            "In '%s' service class '%s' function '%s' parameter assigned '%s' default value has removed. " +
            "This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_CLASS = "warning: In '%s' service " +
            "class '%s' function qualifier list changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_CLASS_METHOD = "warning: In " +
            "'%s' service class '%s' method changed from '%s' to '%s'. This can break existing clients.";
    private static final String REMOVE_SERVICE_CLASS_FUNC_DEF_MESSAGE = "warning: In '%s' service class " +
            "'%s' function definition has removed. This can break available clients";

    private final ClassDefinitionNode prevClassDefinition;
    private final ClassDefinitionNode nextClassDefinition;
    private List<FunctionDefinitionNode> removedFunctionDefinitions;
    private List<FunctionDefinitionEqualityResult> updatedFunctionDefinitions;
    private List<TypeReferenceNode> removedTypeReferences;
    private List<Node> finalMembers;

    public ClassDefinitionEqualityResult(ClassDefinitionNode prevClassDefinition,
                                         ClassDefinitionNode nextClassDefinition) {
        this.prevClassDefinition = prevClassDefinition;
        this.nextClassDefinition = nextClassDefinition;
        removedFunctionDefinitions = new ArrayList<>();
        updatedFunctionDefinitions = new ArrayList<>();
        removedTypeReferences = new ArrayList<>();
        finalMembers = new ArrayList<>();
        if (isMatch()) {
            separateClassMembers();
        }
    }

    public boolean isMatch() {
        return prevClassDefinition.className().text().equals(nextClassDefinition.className().text());
    }

    public void separateClassMembers() {
        LinkedHashMap<Node, Boolean> nextClassMemberAvailability = new LinkedHashMap<>();
        for (Node nextClassMember : nextClassDefinition.members()) {
            if (nextClassMember instanceof FunctionDefinitionNode) {
                FunctionDefinitionNode nextClassFuncDef = (FunctionDefinitionNode) nextClassMember;
                nextClassMemberAvailability.put(nextClassFuncDef, false);
            } else if (nextClassMember instanceof TypeReferenceNode) {
                TypeReferenceNode nextClassTypeReference = (TypeReferenceNode) nextClassMember;
                nextClassMemberAvailability.put(nextClassTypeReference, false);
            }
        }
        boolean isFirstMember = true;
        for (Node prevClassMember : prevClassDefinition.members()) {
            boolean foundMatch = false;
            for (Node nextClassMember : nextClassDefinition.members()) {
                if (prevClassMember instanceof TypeReferenceNode && nextClassMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevClassMember;
                    TypeReferenceNode nextTypeRefMember = (TypeReferenceNode) nextClassMember;
                    TypeEqualityResult typeEquality =
                            new TypeEqualityResult(prevTypeRefMember.typeName(), nextTypeRefMember.typeName());
                    if (typeEquality.isEqual()) {
                        foundMatch = true;
                        nextClassMemberAvailability.put(nextTypeRefMember, true);
                        finalMembers.add(prevTypeRefMember);
                        break;
                    }
                } else if (prevClassMember instanceof FunctionDefinitionNode &&
                        nextClassMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDefinition = (FunctionDefinitionNode) prevClassMember;
                    FunctionDefinitionNode nextFunctionDefinition = (FunctionDefinitionNode) nextClassMember;
                    FunctionDefinitionEqualityResult funcDefEquals =
                            new FunctionDefinitionEqualityResult(prevFunctionDefinition, nextFunctionDefinition);
                    if (funcDefEquals.isEqual()) {
                        foundMatch = true;
                        nextClassMemberAvailability.put(nextFunctionDefinition, true);
                        finalMembers.add(prevFunctionDefinition);
                        break;
                    } else if (funcDefEquals.isMatch()) {
                        foundMatch = true;
                        nextClassMemberAvailability.put(nextFunctionDefinition, true);
                        finalMembers.add(funcDefEquals.generateCombinedFunctionDefinition(isFirstMember));
                        updatedFunctionDefinitions.add(funcDefEquals);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                if (prevClassMember instanceof TypeReferenceNode) {
                    TypeReferenceNode prevTypeRefMember = (TypeReferenceNode) prevClassMember;
                    removedTypeReferences.add(prevTypeRefMember);
                } else if (prevClassMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDefinition = (FunctionDefinitionNode) prevClassMember;
                    if (isResolverFunction(prevFunctionDefinition)) {
                        removedFunctionDefinitions.add(prevFunctionDefinition);
                    } else {
                        finalMembers.add(prevFunctionDefinition);
                    }
                } else if (prevClassMember instanceof ObjectFieldNode) {
                    finalMembers.add(prevClassMember);
                }
            }
            isFirstMember = false;
        }
        for (Map.Entry<Node, Boolean> nextClassMemberAvailableEntry : nextClassMemberAvailability.entrySet()) {
            Boolean nextClassMemberAvailable = nextClassMemberAvailableEntry.getValue();
            if (!nextClassMemberAvailable) {
                Node newClassMember = nextClassMemberAvailableEntry.getKey();
                if (newClassMember instanceof TypeReferenceNode) {
                    TypeReferenceNode nextTypeRefMember =
                            (TypeReferenceNode) nextClassMemberAvailableEntry.getKey();
                    finalMembers.add(nextTypeRefMember);
                } else if (newClassMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode nextFunctionDefinition =
                            (FunctionDefinitionNode) nextClassMemberAvailableEntry.getKey();
                    finalMembers.add(nextFunctionDefinition);
                }
            }
        }
    }

    public ClassDefinitionNode generateCombinedResult() {
        return prevClassDefinition.modify(nextClassDefinition.metadata().orElse(null),
                prevClassDefinition.visibilityQualifier().orElse(null),
                prevClassDefinition.classTypeQualifiers(), prevClassDefinition.classKeyword(),
                prevClassDefinition.className(), prevClassDefinition.openBrace(),
                createNodeList(finalMembers), prevClassDefinition.closeBrace(),
                prevClassDefinition.semicolonToken().orElse(null));
    }

    public List<String> generateBreakingChangeWarnings() {
        List<String> breakingChangeWarnings = new ArrayList<>();
        for (FunctionDefinitionEqualityResult updatedFunctionDefinitionEquality : updatedFunctionDefinitions) {
            FunctionSignatureEqualityResult updateFunctionSignatureEquality =
                    updatedFunctionDefinitionEquality.getFunctionSignatureEqualityResult();
            if (!updateFunctionSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE,
                        prevClassDefinition.className().text(), updatedFunctionDefinitionEquality.getPrevFunctionName(),
                        updateFunctionSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                        updateFunctionSignatureEquality.getReturnTypeEqualityResult().getNextType()));
            }
            for (String addedViolatedParameterName : updateFunctionSignatureEquality.getAddedViolatedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE_CLASS,
                                prevClassDefinition.className().text(),
                                updatedFunctionDefinitionEquality.getPrevFunctionName(), addedViolatedParameterName,
                                addedViolatedParameterName));
            }
            for (String removedParameterName : updateFunctionSignatureEquality.getRemovedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE_CLASS,
                        prevClassDefinition.className().text(), updatedFunctionDefinitionEquality.getPrevFunctionName(),
                        removedParameterName));
            }
            for (ParameterEqualityResult parameterEquals : updateFunctionSignatureEquality.getTypeChangedParameters()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE_CLASS,
                        prevClassDefinition.className().text(), updatedFunctionDefinitionEquality.getPrevFunctionName(),
                        parameterEquals.getPrevParameterName(), parameterEquals.getTypeEquality().getPrevType(),
                        parameterEquals.getTypeEquality().getNextType()));
            }
            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                    updateFunctionSignatureEquality.getDefaultValueRemovedParameters()) {
                breakingChangeWarnings.add(String.format(
                        WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_CLASS_FUNC,
                        prevClassDefinition.className().text(), updatedFunctionDefinitionEquality.getPrevFunctionName(),
                        defaultValueRemovedParameterEquality.getPrevParameterName(),
                        defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
            }
            if (!updatedFunctionDefinitionEquality.isQualifierSimilar()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_CLASS,
                                prevClassDefinition.className().text(),
                                updatedFunctionDefinitionEquality.getPrevFunctionName(),
                                updatedFunctionDefinitionEquality.getPrevMainQualifier(),
                                updatedFunctionDefinitionEquality.getNextMainQualifier()));
            }
            if (updatedFunctionDefinitionEquality.isGetAndSubscribeInterchanged()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_CLASS_METHOD,
                                prevClassDefinition.className().text(),
                                updatedFunctionDefinitionEquality.getPrevFunctionName(),
                                updatedFunctionDefinitionEquality.getPrevMethodType(),
                                updatedFunctionDefinitionEquality.getNextMethodType()));
            }
        }
        for (FunctionDefinitionNode removedFunctionDefinition : removedFunctionDefinitions) {
            breakingChangeWarnings.add(
                    String.format(REMOVE_SERVICE_CLASS_FUNC_DEF_MESSAGE, prevClassDefinition.className().text(),
                            getFunctionName(removedFunctionDefinition)));
        }
        return breakingChangeWarnings;
    }
}
