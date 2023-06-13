package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
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
    private List<String> breakingChangeWarnings;

    public ServiceFileCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;
        moduleMembers = new ArrayList<>();
        moduleVariables = new ArrayList<>();
        moduleServiceDeclarations = new ArrayList<>();
        breakingChangeWarnings = new ArrayList<>();
    }

    public String generateMergedSrc() throws FormatterException {
        String generatedSyntaxTree = Formatter.format(generateMergedSyntaxTree()).toString();
        return Formatter.format(generatedSyntaxTree);
    }

    public SyntaxTree generateMergedSyntaxTree() {
        generateNewMembers(prevContentNode.members(), nextContentNode.members());
//        List<ModuleMemberDeclarationNode> newMembers =
//                generateNewMembers(prevContentNode.members(), nextContentNode.members());
//
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
                if (mergeIfMatch(prevMember, nextMember)) {
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

    private boolean mergeIfMatch(ModuleMemberDeclarationNode prevMember, ModuleMemberDeclarationNode nextMember) {
        if (prevMember instanceof ServiceDeclarationNode && nextMember instanceof ServiceDeclarationNode) {
            ServiceDeclarationNode prevServiceDeclaration = (ServiceDeclarationNode) prevMember;
            ServiceDeclarationNode nextServiceDeclaration = (ServiceDeclarationNode) nextMember;
            if (isServiceDeclarationMatch(prevServiceDeclaration, nextServiceDeclaration)) {
                return true;
            }
        }
        return false;
    }

    private boolean isModuleVariableDeclarationMatch(ModuleVariableDeclarationNode prevModuleVariable,
                                                     ModuleVariableDeclarationNode nextModuleVariable) {
        BindingPatternNode prevVariableName = prevModuleVariable.typedBindingPattern().bindingPattern();
        BindingPatternNode nextVariableName = nextModuleVariable.typedBindingPattern().bindingPattern();
        return false;
    }

    private boolean isServiceDeclarationMatch(ServiceDeclarationNode prevServiceDeclaration,
                                              ServiceDeclarationNode nextServiceDeclaration) {
        TypeDescriptorNode prevServiceName = prevServiceDeclaration.typeDescriptor().orElse(null);
        TypeDescriptorNode nextServiceName = nextServiceDeclaration.typeDescriptor().orElse(null);
        if (prevServiceName == null ^ nextServiceName == null) {
            return false;
        }
        if (prevServiceName != null && nextServiceName != null) {
            String prevServiceNameStr = BaseCombiner.getTypeName(prevServiceName);
            String nextServiceNameStr = BaseCombiner.getTypeName(nextServiceName);
            if (!prevServiceNameStr.equals(nextServiceNameStr)) {
                return false;
            }
        }
        List<Node> finalServiceDeclarationMembers = new ArrayList<>();
        HashMap<Node, Boolean> nextMemberAvailable = new HashMap<>();
        for (Node nextMember : nextServiceDeclaration.members()) {
            nextMemberAvailable.put(nextMember, false);
        }
        for (Node prevMember : prevServiceDeclaration.members()) {
            boolean foundMatch = false;
            for (Node nextMember : nextServiceDeclaration.members()) {
                if (prevMember instanceof FunctionDefinitionNode && nextMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFunctionDef = (FunctionDefinitionNode) prevMember;
                    FunctionDefinitionNode nextFunctionDef = (FunctionDefinitionNode) nextMember;
                    FunctionDefinitionEqualityResult funcDefEquals =
                            BaseCombiner.isFuncDefEquals(prevFunctionDef, nextFunctionDef);
                    if (funcDefEquals.isEqual()) {
                        foundMatch = true;
                        finalServiceDeclarationMembers.add(prevMember);
                        nextMemberAvailable.put(nextMember, true);
                        break;
                    } else if (funcDefEquals.isMatch()) {
                        foundMatch = true;
                        FunctionDefinitionNode modifiedFunctionDef =
                                nextFunctionDef.modify(nextFunctionDef.kind(), prevFunctionDef.metadata().orElse(null),
                                        nextFunctionDef.qualifierList(), nextFunctionDef.functionKeyword(),
                                        nextFunctionDef.functionName(), nextFunctionDef.relativeResourcePath(),
                                        nextFunctionDef.functionSignature(), prevFunctionDef.functionBody());
                        finalServiceDeclarationMembers.add(modifiedFunctionDef);
                        nextMemberAvailable.put(nextMember, true);
                        break;
                    }
                }
            }
            if (!foundMatch) {
                // prevMember removed
            }
        }
        for (Map.Entry<Node, Boolean> availableEntry : nextMemberAvailable.entrySet()) {
            Boolean available = availableEntry.getValue();
            if (!available) {
                Node newMember = availableEntry.getKey();
                if (newMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode newFunctionDef = (FunctionDefinitionNode) newMember;
                    finalServiceDeclarationMembers.add(newFunctionDef);
                }
                // nextMember added
            }
        }
        ServiceDeclarationNode modifiedServiceDeclaration =
                nextServiceDeclaration.modify(prevServiceDeclaration.metadata().orElse(null),
                        nextServiceDeclaration.qualifiers(), nextServiceDeclaration.serviceKeyword(),
                        nextServiceDeclaration.typeDescriptor().orElse(null),
                        nextServiceDeclaration.absoluteResourcePath(),
                        nextServiceDeclaration.onKeyword(), nextServiceDeclaration.expressions(),
                        nextServiceDeclaration.openBraceToken(), createNodeList(finalServiceDeclarationMembers),
                        nextServiceDeclaration.closeBraceToken(), nextServiceDeclaration.semicolonToken().orElse(null));
        moduleServiceDeclarations.add(modifiedServiceDeclaration);
        return true;
    }

    private boolean canDirectlyAdded(ModuleMemberDeclarationNode member) {
        if (member instanceof ModuleVariableDeclarationNode) {
            moduleVariables.add(member);
            return true;
        }
        return false;
    }

//    private boolean isMemberEquals(ModuleMemberDeclarationNode prevMember, ModuleMemberDeclarationNode nextMember) {
//        if (prevMember instanceof ModuleVariableDeclarationNode &&
//        nextMember instanceof ModuleVariableDeclarationNode) {
//            ModuleVariableDeclarationNode prevModuleVariable = (ModuleVariableDeclarationNode) prevMember;
//            ModuleVariableDeclarationNode nextModuleVariable = (ModuleVariableDeclarationNode) nextMember;
//            if (isModuleVariableEquals(prevModuleVariable, nextModuleVariable)) {
//
//            }
//        }
//        return false;
//    }
}
