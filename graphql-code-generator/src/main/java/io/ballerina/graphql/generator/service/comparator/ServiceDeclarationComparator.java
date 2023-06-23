package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getFunctionName;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.getTypeName;
import static io.ballerina.graphql.generator.service.comparator.ComparatorUtils.isResolverFunction;

/**
 * Utility class to store result comparing service declarations.
 */
public class ServiceDeclarationComparator {
    private final ServiceDeclarationNode prevServiceDeclaration;
    private final ServiceDeclarationNode nextServiceDeclaration;
    private List<Node> finalMembers;
    private List<FunctionDefinitionComparator> updatedFunctionEqualityResults;
    private List<String> removedFunctionDefinitions;

    public ServiceDeclarationComparator(ServiceDeclarationNode prevServiceDeclaration,
                                        ServiceDeclarationNode nextServiceDeclaration) {
        this.prevServiceDeclaration = prevServiceDeclaration;
        this.nextServiceDeclaration = nextServiceDeclaration;
        finalMembers = new ArrayList<>();
        removedFunctionDefinitions = new ArrayList<>();
        updatedFunctionEqualityResults = new ArrayList<>();
        if (isMatch()) {
            separateMembers();
        }
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
                    FunctionDefinitionComparator funcDefEquality =
                            new FunctionDefinitionComparator(prevFunctionDef, nextFunctionDef);
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

    public ModuleMemberDeclarationNode generateCombinedResult() {
        return nextServiceDeclaration.modify(
                nextServiceDeclaration.metadata().orElse(null), prevServiceDeclaration.qualifiers(),
                nextServiceDeclaration.serviceKeyword(), nextServiceDeclaration.typeDescriptor().orElse(null),
                prevServiceDeclaration.absoluteResourcePath(), nextServiceDeclaration.onKeyword(),
                prevServiceDeclaration.expressions(), prevServiceDeclaration.openBraceToken(),
                createNodeList(finalMembers),
                prevServiceDeclaration.closeBraceToken(), prevServiceDeclaration.semicolonToken().orElse(null));
    }
}
