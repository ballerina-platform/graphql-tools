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
        this.finalMembers = new ArrayList<>();
        this.removedFunctionDefinitions = new ArrayList<>();
        this.updatedFunctionEqualityResults = new ArrayList<>();
        if (isMatch()) {
            separateMembers();
        }
    }

    public boolean isMatch() {
        TypeDescriptorNode prevServiceName = this.prevServiceDeclaration.typeDescriptor().orElse(null);
        TypeDescriptorNode nextServiceName = this.nextServiceDeclaration.typeDescriptor().orElse(null);
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
        for (Node nextServiceMember : this.nextServiceDeclaration.members()) {
            nextServiceMemberAvailability.put(nextServiceMember, false);
        }
        boolean isFirstMember = true;
        for (Node prevServiceMember : this.prevServiceDeclaration.members()) {
            boolean foundMatch = false;
            for (Node nextServiceMember : this.nextServiceDeclaration.members()) {
                if (prevServiceMember instanceof FunctionDefinitionNode &&
                        nextServiceMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDef = (FunctionDefinitionNode) prevServiceMember;
                    FunctionDefinitionNode nextFunctionDef = (FunctionDefinitionNode) nextServiceMember;
                    FunctionDefinitionComparator funcDefEquality =
                            new FunctionDefinitionComparator(prevFunctionDef, nextFunctionDef);
                    if (funcDefEquality.isEqual()) {
                        foundMatch = true;
                        nextServiceMemberAvailability.put(nextServiceMember, true);
                        this.finalMembers.add(prevServiceMember);
                        break;
                    } else if (funcDefEquality.isMatch()) {
                        foundMatch = true;
                        this.finalMembers.add(funcDefEquality.generateCombinedFunctionDefinition(isFirstMember));
                        nextServiceMemberAvailability.put(nextServiceMember, true);
                        this.updatedFunctionEqualityResults.add(funcDefEquality);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                if (prevServiceMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDef = (FunctionDefinitionNode) prevServiceMember;
                    if (isResolverFunction(prevFunctionDef)) {
                        this.removedFunctionDefinitions.add(getFunctionName(prevFunctionDef));
                    } else {
                        this.finalMembers.add(prevFunctionDef);
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
                    this.finalMembers.add(nextFunctionDef);
                }
            }
        }
    }

    public ModuleMemberDeclarationNode generateCombinedResult() {
        return this.nextServiceDeclaration.modify(
                this.nextServiceDeclaration.metadata().orElse(null), this.prevServiceDeclaration.qualifiers(),
                this.nextServiceDeclaration.serviceKeyword(), this.nextServiceDeclaration.typeDescriptor().orElse(null),
                this.prevServiceDeclaration.absoluteResourcePath(), this.nextServiceDeclaration.onKeyword(),
                this.prevServiceDeclaration.expressions(), this.prevServiceDeclaration.openBraceToken(),
                createNodeList(this.finalMembers), this.prevServiceDeclaration.closeBraceToken(),
                this.prevServiceDeclaration.semicolonToken().orElse(null));
    }
}
