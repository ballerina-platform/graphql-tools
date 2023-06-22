package io.ballerina.graphql.generator.service.combiner;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.service.ServiceDeclarationEqualityResult;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;

/**
 * Utility class for combining available service file content with generated service file content for a GraphQL schema.
 */
public class ServiceFileCombiner {
    private ModulePartNode prevContentNode;
    private ModulePartNode nextContentNode;
    private List<ModuleMemberDeclarationNode> moduleMembers;
    private List<ModuleMemberDeclarationNode> moduleVariables;
    private List<ModuleMemberDeclarationNode> moduleServiceDeclarations;

    public ServiceFileCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;
        moduleMembers = new ArrayList<>();
        moduleVariables = new ArrayList<>();
        moduleServiceDeclarations = new ArrayList<>();
    }

    public String generateMergedSrc() throws FormatterException {
        String generatedSyntaxTree = Formatter.format(generateMergedSyntaxTree()).toString();
        return Formatter.format(generatedSyntaxTree);
    }

    public SyntaxTree generateMergedSyntaxTree() {
        generateNewMembers(prevContentNode.members(), nextContentNode.members());
        moduleMembers.addAll(moduleVariables);
        moduleMembers.addAll(moduleServiceDeclarations);

        NodeList<ModuleMemberDeclarationNode> allMembers = createNodeList(moduleMembers);
        ModulePartNode contentNode =
                createModulePartNode(prevContentNode.imports(), allMembers, createToken(SyntaxKind.EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(contentNode);
    }

    private List<ModuleMemberDeclarationNode> generateNewMembers(NodeList<ModuleMemberDeclarationNode> prevMembers,
                                                                 NodeList<ModuleMemberDeclarationNode> nextMembers) {
        List<ModuleMemberDeclarationNode> newMembers = new ArrayList<>();
        HashMap<ModuleMemberDeclarationNode, Boolean> nextMemberAvailable = new HashMap<>();
        for (ModuleMemberDeclarationNode nextMember : nextMembers) {
            nextMemberAvailable.put(nextMember, false);
        }
        for (ModuleMemberDeclarationNode prevMember : prevMembers) {
            boolean foundMatch = false;
            for (ModuleMemberDeclarationNode nextMember : nextMembers) {
                if (isMemberEquals(prevMember, nextMember)) {
                    foundMatch = true;
                    nextMemberAvailable.put(nextMember, true);
                    break;
                }
            }
            if (!foundMatch) {
                addMemberToRespectiveMemberList(prevMember);
            }
        }
        for (Map.Entry<ModuleMemberDeclarationNode, Boolean> availableEntry : nextMemberAvailable.entrySet()) {
            Boolean available = availableEntry.getValue();
            if (!available) {
                ModuleMemberDeclarationNode notAvailableMember = availableEntry.getKey();
                if (notAvailableMember instanceof ServiceDeclarationNode) {
                    moduleServiceDeclarations.add(notAvailableMember);
                }
            }
        }
        return newMembers;
    }

    private void addMemberToRespectiveMemberList(ModuleMemberDeclarationNode member) {
        if (member instanceof ModuleVariableDeclarationNode) {
            moduleVariables.add(member);
        } else if (member instanceof ServiceDeclarationNode) {
            moduleServiceDeclarations.add(member);
        }
    }

    private boolean isMemberEquals(ModuleMemberDeclarationNode prevMember, ModuleMemberDeclarationNode nextMember) {
        if (prevMember instanceof ServiceDeclarationNode && nextMember instanceof ServiceDeclarationNode) {
            ServiceDeclarationNode prevServiceDeclaration = (ServiceDeclarationNode) prevMember;
            ServiceDeclarationNode nextServiceDeclaration = (ServiceDeclarationNode) nextMember;
            if (isServiceDeclarationMatch(prevServiceDeclaration, nextServiceDeclaration)) {
                return true;
            }
        }
        return false;
    }

    private boolean isServiceDeclarationMatch(ServiceDeclarationNode prevServiceDeclaration,
                                              ServiceDeclarationNode nextServiceDeclaration) {
        ServiceDeclarationEqualityResult serviceDeclarationEquality =
                new ServiceDeclarationEqualityResult(prevServiceDeclaration, nextServiceDeclaration);
        if (!serviceDeclarationEquality.isMatch()) {
            return false;
        }
        serviceDeclarationEquality.separateMembers();
        moduleServiceDeclarations.add(serviceDeclarationEquality.generateCombinedResult());
        return true;
    }
}
