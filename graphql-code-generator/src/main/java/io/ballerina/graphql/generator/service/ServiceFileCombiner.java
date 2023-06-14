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
        String prevServiceNameStr = "";
        String nextServiceNameStr = "";
        if (prevServiceName != null && nextServiceName != null) {
            prevServiceNameStr = BaseCombiner.getTypeName(prevServiceName);
            nextServiceNameStr = BaseCombiner.getTypeName(nextServiceName);
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
                        FunctionSignatureEqualityResult functionSignatureEquality =
                                funcDefEquals.getFunctionSignatureEqualityResult();
                        if (!functionSignatureEquality.getReturnTypeEqualityResult().isEqual()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_FUNCTION_DEFINITION_CHANGE_RETURN_TYPE,
                                            prevServiceNameStr, funcDefEquals.getPrevFunctionName(),
                                            functionSignatureEquality.getReturnTypeEqualityResult().getPrevType(),
                                            functionSignatureEquality.getReturnTypeEqualityResult().getNextType()));
                        }
                        if (!functionSignatureEquality.getAddedViolatedParameters().isEmpty()) {
                            for (String addedParameterName : functionSignatureEquality.getAddedViolatedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_ADDED_NO_DEFAULT_VALUE_IN_SERVICE,
                                                prevServiceNameStr, funcDefEquals.getPrevFunctionName(),
                                                addedParameterName));
                            }
                        }
                        if (!functionSignatureEquality.getRemovedParameters().isEmpty()) {
                            for (String removedParameterName : functionSignatureEquality.getRemovedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_REMOVED_IN_SERVICE, prevServiceNameStr,
                                                funcDefEquals.getPrevFunctionName(), removedParameterName));
                            }
                        }
                        if (!functionSignatureEquality.getTypeChangedParameters().isEmpty()) {
                            for (ParameterEqualityResult parameterEquals :
                                    functionSignatureEquality.getTypeChangedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_PARAMETER_TYPE_CHANGED_IN_SERVICE,
                                                prevServiceNameStr, funcDefEquals.getPrevFunctionName(),
                                                parameterEquals.getPrevParameterName(),
                                                parameterEquals.getTypeEquality().getPrevType(),
                                                parameterEquals.getTypeEquality().getNextType()));
                            }
                        }
                        if (!functionSignatureEquality.getDefaultValueRemovedParameters().isEmpty()) {
                            for (ParameterEqualityResult defaultValueRemovedParameterEquality :
                                    functionSignatureEquality.getDefaultValueRemovedParameters()) {
                                breakingChangeWarnings.add(
                                        String.format(WARNING_MESSAGE_DEFAULT_PARAMETER_VALUE_REMOVED_IN_SERVICE_FUNC,
                                                prevServiceNameStr, funcDefEquals.getPrevFunctionName(),
                                                defaultValueRemovedParameterEquality.getPrevParameterName(),
                                                defaultValueRemovedParameterEquality.getPrevParameterDefaultValue()));
                            }
                        }
                        if (!funcDefEquals.isQualifierSimilar()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_QUALIFIER_LIST_CHANGED_IN_SERVICE_FUNC,
                                            prevServiceNameStr, funcDefEquals.getPrevFunctionName(),
                                            funcDefEquals.getPrevMainQualifier(),
                                            funcDefEquals.getNextMainQualifier()));
                        }
                        if (funcDefEquals.isGetAndSubscribeInterchanged()) {
                            breakingChangeWarnings.add(
                                    String.format(WARNING_MESSAGE_GET_SUBSCRIBE_INTERCHANGED_IN_SERVICE_FUNC,
                                            prevServiceNameStr, funcDefEquals.getPrevFunctionName(),
                                            funcDefEquals.getPrevMethodType(), funcDefEquals.getNextMethodType()));
                        }
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
                if (prevMember instanceof FunctionDefinitionNode) {
                    FunctionDefinitionNode prevFuncDef = (FunctionDefinitionNode) prevMember;
                    if (prevFuncDef.qualifierList().size() == 0) {
                        finalServiceDeclarationMembers.add(prevFuncDef);
                    }
                }
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
                        nextServiceDeclaration.absoluteResourcePath(), nextServiceDeclaration.onKeyword(),
                        nextServiceDeclaration.expressions(), nextServiceDeclaration.openBraceToken(),
                        createNodeList(finalServiceDeclarationMembers), nextServiceDeclaration.closeBraceToken(),
                        nextServiceDeclaration.semicolonToken().orElse(null));
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

    public List<String> getBreakingChangeWarnings() {
        return breakingChangeWarnings;
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
