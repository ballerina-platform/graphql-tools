package io.ballerina.graphql.generator.service.combiner;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.service.ClassDefinitionEqualityResult;
import io.ballerina.graphql.generator.service.EnumDeclarationEqualityResult;
import io.ballerina.graphql.generator.service.TypeDefinitionEqualityResult;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;

/**
 * Utility class for combining available service with generated service for a GraphQL schema.
 */
public class ServiceCombiner {
    private final ModulePartNode nextContentNode;
    private final ModulePartNode prevContentNode;
    private GraphQLSchema nextGraphqlSchema;
    private List<ModuleMemberDeclarationNode> moduleMembers;
    private List<ModuleMemberDeclarationNode> inputObjectTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> interfaceTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> enumTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> unionTypesModuleMembers;
    private List<ModuleMemberDeclarationNode> objectTypesModuleMembers;
    private List<String> breakingChangeWarnings;

    public ServiceCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode,
                           GraphQLSchema nextGraphqlSchema) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;
        this.nextGraphqlSchema = nextGraphqlSchema;

        moduleMembers = new ArrayList<>();
        inputObjectTypesModuleMembers = new ArrayList<>();
        interfaceTypesModuleMembers = new ArrayList<>();
        enumTypesModuleMembers = new ArrayList<>();
        unionTypesModuleMembers = new ArrayList<>();
        objectTypesModuleMembers = new ArrayList<>();

        breakingChangeWarnings = new ArrayList<>();
    }

    public String generateMergedSrc() throws FormatterException {
        String generatedSyntaxTree = Formatter.format(generateMergedSyntaxTree()).toString();
        return Formatter.format(generatedSyntaxTree);
    }

    public SyntaxTree generateMergedSyntaxTree() {
        List<ModuleMemberDeclarationNode> newMembers =
                generateNewMembers(prevContentNode.members(), nextContentNode.members());

        moduleMembers.addAll(inputObjectTypesModuleMembers);
        moduleMembers.addAll(interfaceTypesModuleMembers);
        moduleMembers.addAll(enumTypesModuleMembers);
        moduleMembers.addAll(unionTypesModuleMembers);
        moduleMembers.addAll(objectTypesModuleMembers);
        moduleMembers.addAll(newMembers);

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
        for (ModuleMemberDeclarationNode nextMember : nextMembers) {
            boolean isFound = false;
            for (ModuleMemberDeclarationNode prevMember : prevMembers) {
                if (isMemberEquals(prevMember, nextMember)) {
                    isFound = true;
                }
            }
            if (!isFound) {
                newMembers.add(nextMember);
            }
        }
        return newMembers;
    }

    private boolean isMemberEquals(ModuleMemberDeclarationNode prevMember, ModuleMemberDeclarationNode nextMember) {
        if (prevMember instanceof ClassDefinitionNode && nextMember instanceof ClassDefinitionNode) {
            ClassDefinitionNode prevClassDef = (ClassDefinitionNode) prevMember;
            ClassDefinitionNode nextClassDef = (ClassDefinitionNode) nextMember;
            if (isClassDefEquals(prevClassDef, nextClassDef)) {
                return true;
            }
        } else if (prevMember instanceof TypeDefinitionNode && nextMember instanceof TypeDefinitionNode) {
            TypeDefinitionNode prevTypeDef = (TypeDefinitionNode) prevMember;
            TypeDefinitionNode nextTypeDef = (TypeDefinitionNode) nextMember;
            if (isTypeDefEquals(prevTypeDef, nextTypeDef)) {
                return true;
            }
        } else if (prevMember instanceof EnumDeclarationNode && nextMember instanceof EnumDeclarationNode) {
            EnumDeclarationNode prevEnumDec = (EnumDeclarationNode) prevMember;
            EnumDeclarationNode nextEnumDec = (EnumDeclarationNode) nextMember;
            if (isEnumDecEquals(prevEnumDec, nextEnumDec)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnumDecEquals(EnumDeclarationNode prevEnumDec, EnumDeclarationNode nextEnumDec) {
        EnumDeclarationEqualityResult enumDeclarationEquality =
                new EnumDeclarationEqualityResult(prevEnumDec, nextEnumDec);
        if (!enumDeclarationEquality.isMatch()) {
            return false;
        }
        breakingChangeWarnings.addAll(enumDeclarationEquality.generateBreakingChangeWarnings());
        enumTypesModuleMembers.add(enumDeclarationEquality.generateCombinedResult());
        return true;
    }

    private boolean isTypeDefEquals(TypeDefinitionNode prevTypeDef, TypeDefinitionNode nextTypeDef) {
        TypeDefinitionEqualityResult typeDefinitionEquality =
                new TypeDefinitionEqualityResult(prevTypeDef, nextTypeDef);
        if (!typeDefinitionEquality.isMatch()) {
            return false;
        }
        typeDefinitionEquality.handleMergeTypeDescriptor(nextGraphqlSchema.getType(nextTypeDef.typeName().text()));
        breakingChangeWarnings.addAll(typeDefinitionEquality.getBreakingChangeWarnings());
        Node mergedTypeDescriptor = typeDefinitionEquality.getMergedTypeDescriptor();
        if (mergedTypeDescriptor instanceof ObjectTypeDescriptorNode) {
            moduleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        } else if (mergedTypeDescriptor instanceof DistinctTypeDescriptorNode ||
                mergedTypeDescriptor instanceof IntersectionTypeDescriptorNode) {
            interfaceTypesModuleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        } else if (mergedTypeDescriptor instanceof RecordTypeDescriptorNode) {
            inputObjectTypesModuleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        } else if (mergedTypeDescriptor instanceof UnionTypeDescriptorNode) {
            unionTypesModuleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        }
        return true;
    }

    private boolean isClassDefEquals(ClassDefinitionNode prevClassDef, ClassDefinitionNode nextClassDef) {
        ClassDefinitionEqualityResult classDefinitionEqualityResult =
                new ClassDefinitionEqualityResult(prevClassDef, nextClassDef);
        if (!classDefinitionEqualityResult.isMatch()) {
            return false;
        }
        breakingChangeWarnings.addAll(classDefinitionEqualityResult.generateBreakingChangeWarnings());
        objectTypesModuleMembers.add(classDefinitionEqualityResult.generateCombinedResult());
        return true;
    }

    public List<String> getBreakingChangeWarnings() {
        return breakingChangeWarnings;
    }
}
