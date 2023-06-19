package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getFunctionName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getMergedMetadata;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.getTypeName;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isFuncDefEquals;
import static io.ballerina.graphql.generator.service.EqualityResultUtils.isResolverFunction;

/**
 * Utility class to store result comparing service declarations.
 */
public class ServiceDeclarationEqualityResult {
    private static final String WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE = "warning: In '%s' " +
            "GraphQL service '%s' function definition return type has changed from '%s' to '%s'. This can break " +
            "existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE = "warning: In '%s' " +
            "GraphQL service '%s' function definition '%s' parameter added without default value. This can break " +
            "existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE = "warning: In '%s' GraphQL service " +
            "'%s' function definition '%s' parameter removed. This can break existing clients.";
    private static final String WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE = "warning: In '%s' GraphQL " +
            "service '%s' function definition '%s' parameter type change from '%s' to '%s'. This can break existing " +
            "clients.";
    private static final String WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_FUNC =
            "warning: In '%s' GraphQL service '%s' function '%s' parameter assigned '%s' default value has removed. " +
                    "This can break existing clients.";
    private static final String WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_FUNC = "warning: In '%s' service " +
            "'%s' function qualifier changed from '%s' to '%s'. This can break existing clients.";
    private static final String WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_FUNC = "warning: In " +
            "'%s' service class '%s' method changed from '%s' to '%s'. This can break existing clients.";

    private final ServiceDeclarationNode prevServiceDeclaration;
    private final ServiceDeclarationNode nextServiceDeclaration;
    private List<Node> finalMembers;
    private List<FunctionDefinitionEqualityResult> updatedFunctionEqualityResults;
    private List<String> removedFunctionDefinitions;

    public ServiceDeclarationEqualityResult(ServiceDeclarationNode prevServiceDeclaration,
                                            ServiceDeclarationNode nextServiceDeclaration) {
        this.prevServiceDeclaration = prevServiceDeclaration;
        this.nextServiceDeclaration = nextServiceDeclaration;
        finalMembers = new ArrayList<>();
        removedFunctionDefinitions = new ArrayList<>();
        updatedFunctionEqualityResults = new ArrayList<>();
    }

    public boolean isMatch() {
        TypeDescriptorNode prevServiceName = prevServiceDeclaration.typeDescriptor().orElse(null);
        TypeDescriptorNode nextServiceName = nextServiceDeclaration.typeDescriptor().orElse(null);
        if (prevServiceName != null && nextServiceName != null) {
            String prevServiceNameString = getTypeName(prevServiceName);
            String nextServiceNameString = getTypeName(nextServiceName);
            return prevServiceNameString.equals(nextServiceNameString);
        } else {
            return false;
        }
    }

    public void separateMembers() {
        LinkedHashMap<Node, Boolean> nextServiceMemberAvailability = new LinkedHashMap<>();
        for (Node nextServiceMember : nextServiceDeclaration.members()) {
            nextServiceMemberAvailability.put(nextServiceMember, false);
        }
        boolean isFirstMember = true;
        for (Node prevServiceMember : prevServiceDeclaration.members()) {
            boolean foundMatch = false;
            for (Node nextServiceMember : nextServiceDeclaration.members()) {
                if (prevServiceMember instanceof FunctionDefinitionNode &&
                        nextServiceMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDef = (FunctionDefinitionNode) prevServiceMember;
                    FunctionDefinitionNode nextFunctionDef = (FunctionDefinitionNode) nextServiceMember;
                    FunctionDefinitionEqualityResult funcDefEquality =
                            isFuncDefEquals(prevFunctionDef, nextFunctionDef);
                    if (funcDefEquality.isEqual()) {
                        foundMatch = true;
                        nextServiceMemberAvailability.put(nextServiceMember, true);
                        finalMembers.add(prevServiceMember);
                        break;
                    } else if (funcDefEquality.isMatch()) {
                        foundMatch = true;
                        finalMembers.add(funcDefEquality.generateCombinedFunctionDefinition(isFirstMember));
                        nextServiceMemberAvailability.put(nextServiceMember, true);
                        updatedFunctionEqualityResults.add(funcDefEquality);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                if (prevServiceMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDef = (FunctionDefinitionNode) prevServiceMember;
                    if (isResolverFunction(prevFunctionDef)) {
                        removedFunctionDefinitions.add(getFunctionName(prevFunctionDef));
                    } else {
                        finalMembers.add(prevFunctionDef);
                    }
                }
            }
            isFirstMember = false;
        }
        for (Map.Entry<Node, Boolean> nextServiceMemberAvailableEntry : nextServiceMemberAvailability.entrySet()) {
            Boolean nextServiceMemberAvailable = nextServiceMemberAvailableEntry.getValue();
            if (!nextServiceMemberAvailable) {
                Node nextServiceMember = nextServiceMemberAvailableEntry.getKey();
                if (nextServiceMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode nextFunctionDef = (FunctionDefinitionNode) nextServiceMember;
                    finalMembers.add(nextFunctionDef);
                }
            }
        }
    }

    private String getServiceDeclarationName(ServiceDeclarationNode serviceDeclaration) {
        TypeDescriptorNode serviceName = serviceDeclaration.typeDescriptor().orElse(null);
        if (serviceName != null) {
            return getTypeName(serviceName);
        } else {
            return null;
        }
    }

    public List<String> generateBreakingChangeWarnings() {
        List<String> breakingChangeWarnings = new ArrayList<>();
        for (FunctionDefinitionEqualityResult updatedFunctionEquality : updatedFunctionEqualityResults) {
            FunctionSignatureEqualityResult functionSignatureEquality =
                    updatedFunctionEquality.getFunctionSignatureEqualityResult();
            if (!functionSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                breakingChangeWarnings.add(String.format(WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE,
                        getServiceDeclarationName(prevServiceDeclaration),
                        updatedFunctionEquality.getPrevFunctionName(),
                        functionSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                        functionSignatureEquality.getReturnTypeEqualityResult().getNextType()));
            }
            for (String addedParameterName : functionSignatureEquality.getAddedViolatedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE,
                                getServiceDeclarationName(prevServiceDeclaration),
                                updatedFunctionEquality.getPrevFunctionName(),
                                addedParameterName));
            }
            for (String removedParameterName : functionSignatureEquality.getRemovedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE,
                                getServiceDeclarationName(prevServiceDeclaration),
                                updatedFunctionEquality.getPrevFunctionName(), removedParameterName));
            }
            for (ParameterEqualityResult parameterEquals :
                    functionSignatureEquality.getTypeChangedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE,
                                getServiceDeclarationName(prevServiceDeclaration),
                                updatedFunctionEquality.getPrevFunctionName(),
                                parameterEquals.getPrevParameterName(),
                                parameterEquals.getTypeEquality().getPrevType(),
                                parameterEquals.getTypeEquality().getNextType()));
            }
            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                    functionSignatureEquality.getDefaultValueRemovedParameters()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_FUNC,
                                getServiceDeclarationName(prevServiceDeclaration),
                                updatedFunctionEquality.getPrevFunctionName(),
                                defaultValueRemovedParameterEquality.getPrevParameterName(),
                                defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
            }
            if (!updatedFunctionEquality.isQualifierSimilar()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_FUNC,
                                getServiceDeclarationName(prevServiceDeclaration),
                                updatedFunctionEquality.getPrevFunctionName(),
                                updatedFunctionEquality.getPrevMainQualifier(),
                                updatedFunctionEquality.getNextMainQualifier()));
            }
            if (updatedFunctionEquality.isGetAndSubscribeInterchanged()) {
                breakingChangeWarnings.add(
                        String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_FUNC,
                                getServiceDeclarationName(prevServiceDeclaration),
                                updatedFunctionEquality.getPrevFunctionName(),
                                updatedFunctionEquality.getPrevMethodType(),
                                updatedFunctionEquality.getNextMethodType()));
            }
        }
        return breakingChangeWarnings;
    }

    public ModuleMemberDeclarationNode generateCombinedResult() {
        MetadataNode mergedMetadata =
                getMergedMetadata(prevServiceDeclaration.metadata().orElse(null),
                        nextServiceDeclaration.metadata().orElse(null));
        return nextServiceDeclaration.modify(mergedMetadata, prevServiceDeclaration.qualifiers(),
                nextServiceDeclaration.serviceKeyword(), nextServiceDeclaration.typeDescriptor().orElse(null),
                prevServiceDeclaration.absoluteResourcePath(), nextServiceDeclaration.onKeyword(),
                prevServiceDeclaration.expressions(), prevServiceDeclaration.openBraceToken(),
                createNodeList(finalMembers),
                prevServiceDeclaration.closeBraceToken(), prevServiceDeclaration.semicolonToken().orElse(null));
    }
}
