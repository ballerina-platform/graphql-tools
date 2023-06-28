package io.ballerina.graphql.generator.service.combiner;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.service.comparator.ClassDefinitionComparator;
import io.ballerina.graphql.generator.service.comparator.EnumDeclarationComparator;
import io.ballerina.graphql.generator.service.comparator.TypeDefinitionComparator;
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
public class ServiceTypesFileCombiner {
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

    public ServiceTypesFileCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode,
                                    GraphQLSchema nextGraphqlSchema) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;
        this.nextGraphqlSchema = nextGraphqlSchema;

        this.moduleMembers = new ArrayList<>();
        this.inputObjectTypesModuleMembers = new ArrayList<>();
        this.interfaceTypesModuleMembers = new ArrayList<>();
        this.enumTypesModuleMembers = new ArrayList<>();
        this.unionTypesModuleMembers = new ArrayList<>();
        this.objectTypesModuleMembers = new ArrayList<>();

        this.breakingChangeWarnings = new ArrayList<>();
    }

    public String generateMergedSrc() throws FormatterException {
        String generatedSyntaxTree = Formatter.format(generateMergedSyntaxTree()).toString();
        return Formatter.format(generatedSyntaxTree);
    }

    public SyntaxTree generateMergedSyntaxTree() {
        List<ModuleMemberDeclarationNode> newMembers =
                generateNewMembers(this.prevContentNode.members(), this.nextContentNode.members());

        this.moduleMembers.addAll(this.inputObjectTypesModuleMembers);
        this.moduleMembers.addAll(this.interfaceTypesModuleMembers);
        this.moduleMembers.addAll(this.enumTypesModuleMembers);
        this.moduleMembers.addAll(this.unionTypesModuleMembers);
        this.moduleMembers.addAll(this.objectTypesModuleMembers);
        this.moduleMembers.addAll(newMembers);

        NodeList<ModuleMemberDeclarationNode> allMembers = createNodeList(this.moduleMembers);
        ModulePartNode contentNode =
                createModulePartNode(this.prevContentNode.imports(), allMembers, createToken(SyntaxKind.EOF_TOKEN));

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
        if (prevMember.kind() == SyntaxKind.CLASS_DEFINITION && nextMember.kind() == SyntaxKind.CLASS_DEFINITION) {
            ClassDefinitionNode prevClassDef = (ClassDefinitionNode) prevMember;
            ClassDefinitionNode nextClassDef = (ClassDefinitionNode) nextMember;
            if (isClassDefEquals(prevClassDef, nextClassDef)) {
                return true;
            }
        } else if (prevMember.kind() == SyntaxKind.TYPE_DEFINITION && nextMember.kind() == SyntaxKind.TYPE_DEFINITION) {
            TypeDefinitionNode prevTypeDef = (TypeDefinitionNode) prevMember;
            TypeDefinitionNode nextTypeDef = (TypeDefinitionNode) nextMember;
            if (isTypeDefEquals(prevTypeDef, nextTypeDef)) {
                return true;
            }
        } else if (prevMember.kind() == SyntaxKind.ENUM_DECLARATION &&
                nextMember.kind() == SyntaxKind.ENUM_DECLARATION) {
            EnumDeclarationNode prevEnumDec = (EnumDeclarationNode) prevMember;
            EnumDeclarationNode nextEnumDec = (EnumDeclarationNode) nextMember;
            if (isEnumDecEquals(prevEnumDec, nextEnumDec)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnumDecEquals(EnumDeclarationNode prevEnumDec, EnumDeclarationNode nextEnumDec) {
        EnumDeclarationComparator enumDeclarationEquality =
                new EnumDeclarationComparator(prevEnumDec, nextEnumDec);
        if (!enumDeclarationEquality.isMatch()) {
            return false;
        }
        this.breakingChangeWarnings.addAll(enumDeclarationEquality.generateBreakingChangeWarnings());
        this.enumTypesModuleMembers.add(enumDeclarationEquality.generateCombinedResult());
        return true;
    }

    private boolean isTypeDefEquals(TypeDefinitionNode prevTypeDef, TypeDefinitionNode nextTypeDef) {
        TypeDefinitionComparator typeDefinitionEquality =
                new TypeDefinitionComparator(prevTypeDef, nextTypeDef);
        if (!typeDefinitionEquality.isMatch()) {
            return false;
        }
        typeDefinitionEquality.handleMergeTypeDescriptor(this.nextGraphqlSchema.getType(nextTypeDef.typeName().text()));
        this.breakingChangeWarnings.addAll(typeDefinitionEquality.getBreakingChangeWarnings());
        Node mergedTypeDescriptor = typeDefinitionEquality.getMergedTypeDescriptor();
        if (mergedTypeDescriptor.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            this.moduleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        } else if (mergedTypeDescriptor.kind() == SyntaxKind.DISTINCT_TYPE_DESC ||
                mergedTypeDescriptor.kind() == SyntaxKind.INTERSECTION_TYPE_DESC) {
            this.interfaceTypesModuleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        } else if (mergedTypeDescriptor.kind() == SyntaxKind.RECORD_TYPE_DESC) {
            this.inputObjectTypesModuleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        } else if (mergedTypeDescriptor.kind() == SyntaxKind.UNION_TYPE_DESC) {
            this.unionTypesModuleMembers.add(typeDefinitionEquality.generateCombinedTypeDefinition());
        }
        return true;
    }

    private boolean isClassDefEquals(ClassDefinitionNode prevClassDef, ClassDefinitionNode nextClassDef) {
        ClassDefinitionComparator classDefinitionComparator =
                new ClassDefinitionComparator(prevClassDef, nextClassDef);
        if (!classDefinitionComparator.isMatch()) {
            return false;
        }
        this.breakingChangeWarnings.addAll(classDefinitionComparator.generateBreakingChangeWarnings());
        this.objectTypesModuleMembers.add(classDefinitionComparator.generateCombinedResult());
        return true;
    }

    public List<String> getBreakingChangeWarnings() {
        return this.breakingChangeWarnings;
    }
}
