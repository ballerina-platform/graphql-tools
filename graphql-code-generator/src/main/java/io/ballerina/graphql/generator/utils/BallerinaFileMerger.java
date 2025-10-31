/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.graphql.generator.utils;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for merging Ballerina files during refresh operations.
 * This class preserves user modifications while incorporating new schema changes.
 */
public class BallerinaFileMerger {

    // Markers to identify user-added code
    //private static final String USER_CODE_START = "// USER CODE START";
    //private static final String USER_CODE_END = "// USER CODE END";

    /**
     * Merges the generated content with the existing file content.
     *
     * @param existingFilePath the path to the existing file
     * @param generatedContent the newly generated content
     * @return the merged content
     * @throws IOException if an I/O error occurs
     */
    public static String mergeFiles(Path existingFilePath, String generatedContent) throws IOException {
        if (existingFilePath == null || generatedContent == null) {
            throw new IllegalArgumentException("File path and generated content cannot be null");
        }
        
        if (!Files.exists(existingFilePath)) {
            throw new IOException("Existing file does not exist: " + existingFilePath);
        }
        
        String existingContent = Files.readString(existingFilePath);
        return mergeContent(existingContent, generatedContent);
    }

    /**
     * Merges existing content with generated content.
     *
     * @param existingContent the existing file content
     * @param generatedContent the newly generated content
     * @return the merged content
     */
    public static String mergeContent(String existingContent, String generatedContent) {
        if (existingContent == null || generatedContent == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        
        try {
            // Parse both contents
            SyntaxTree existingSyntaxTree = SyntaxTree.from(TextDocuments.from(existingContent));
            SyntaxTree generatedSyntaxTree = SyntaxTree.from(TextDocuments.from(generatedContent));
            
            ModulePartNode existingModule = existingSyntaxTree.rootNode();
            ModulePartNode generatedModule = generatedSyntaxTree.rootNode();
            
            // Merge imports - preserve all imports from existing file, add new ones from generated
            NodeList<ImportDeclarationNode> mergedImports = mergeImports(
                    existingModule.imports(), generatedModule.imports());
            
            // Merge module members - preserve user-added functions, update generated ones
            NodeList<ModuleMemberDeclarationNode> mergedMembers = mergeModuleMembers(
                    existingModule.members(), generatedModule.members());
            
            // Create merged module
            ModulePartNode mergedModule = NodeFactory.createModulePartNode(
                    mergedImports, mergedMembers, NodeFactory.createToken(SyntaxKind.EOF_TOKEN));
            
            // Format and return
            return mergedModule.toSourceCode();
        } catch (Exception e) {
            // If formatting fails, return the generated content as fallback
            return generatedContent;
        }
    }

    /**
     * Merges import declarations from existing and generated files.
     *
     * @param existingImports the existing import declarations
     * @param generatedImports the generated import declarations
     * @return the merged import declarations
     */
    private static NodeList<ImportDeclarationNode> mergeImports(
            NodeList<ImportDeclarationNode> existingImports, NodeList<ImportDeclarationNode> generatedImports) {
        if (existingImports == null || generatedImports == null) {
            throw new IllegalArgumentException("Import lists cannot be null");
        }
        
        Set<String> importStrings = new HashSet<>();
        List<ImportDeclarationNode> mergedImports = new ArrayList<>();
        
        for (ImportDeclarationNode importNode : existingImports) {
            if (importNode != null) {
                String importStr = importNode.toSourceCode();
                if (importStrings.add(importStr)) {
                    mergedImports.add(importNode);
                }
            }
        }
        
        for (ImportDeclarationNode importNode : generatedImports) {
            if (importNode != null) {
                String importStr = importNode.toSourceCode();
                if (importStrings.add(importStr)) {
                    mergedImports.add(importNode);
                }
            }
        }
        
        return NodeFactory.createNodeList(mergedImports);
    }

    /**
     * Merges module members from existing and generated files.
     *
     * @param existingMembers the existing module members
     * @param generatedMembers the generated module members
     * @return the merged module members
     */
    private static NodeList<ModuleMemberDeclarationNode> mergeModuleMembers(
            NodeList<ModuleMemberDeclarationNode> existingMembers, 
            NodeList<ModuleMemberDeclarationNode> generatedMembers) {
        
        if (existingMembers == null || generatedMembers == null) {
            throw new IllegalArgumentException("Member lists cannot be null");
        }
        
        List<ModuleMemberDeclarationNode> mergedMembers = new ArrayList<>();
        
        Map<String, ModuleMemberDeclarationNode> existingMemberMap = createMemberMap(existingMembers);
        Map<String, ModuleMemberDeclarationNode> generatedMemberMap = createMemberMap(generatedMembers);
        
        for (Map.Entry<String, ModuleMemberDeclarationNode> entry : existingMemberMap.entrySet()) {
            String key = entry.getKey();
            ModuleMemberDeclarationNode existingMember = entry.getValue();
            
            if (generatedMemberMap.containsKey(key)) {
                // This is a generated member, use the updated version
                mergedMembers.add(generatedMemberMap.get(key));
                // Remove from generated map so we don't add it again
                generatedMemberMap.remove(key);
            } else {
                // This is a user-added member, preserve it
                mergedMembers.add(existingMember);
            }
        }
        
        // Add any remaining generated members (newly added types/functions)
        for (ModuleMemberDeclarationNode member : generatedMemberMap.values()) {
            if (member != null) {
                mergedMembers.add(member);
            }
        }
        
        return NodeFactory.createNodeList(mergedMembers);
    }

    /**
     * Creates a map of module members keyed by their signature.
     *
     * @param members the module members
     * @return a map of members keyed by signature
     */
    private static Map<String, ModuleMemberDeclarationNode> createMemberMap(
            NodeList<ModuleMemberDeclarationNode> members) {
        if (members == null) {
            return new HashMap<>();
        }
        
        Map<String, ModuleMemberDeclarationNode> memberMap = new HashMap<>();
        
        for (ModuleMemberDeclarationNode member : members) {
            if (member != null) {
                String key = getMemberKey(member);
                if (key != null && !key.isEmpty()) {
                    memberMap.put(key, member);
                }
            }
        }
        
        return memberMap;
    }

    /**
     * Gets a unique key for a module member.
     *
     * @param member the module member
     * @return a unique key for the member
     */
    private static String getMemberKey(ModuleMemberDeclarationNode member) {
        if (member == null) {
            return null;
        }
        
        if (member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
            FunctionDefinitionNode function = (FunctionDefinitionNode) member;
            if (function.functionName() != null) {
                return "function:" + function.functionName().text();
            }
        } else if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
            ServiceDeclarationNode service = (ServiceDeclarationNode) member;
            if (service.absoluteResourcePath() != null && service.absoluteResourcePath().size() > 0) {
                return "service:" + service.absoluteResourcePath().get(0).toString();
            }
            return "service";
        } else if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
            TypeDefinitionNode type = (TypeDefinitionNode) member;
            if (type.typeName() != null) {
                return "type:" + type.typeName().text();
            }
        } else if (member.kind() == SyntaxKind.MODULE_VAR_DECL) {
            ModuleVariableDeclarationNode var = (ModuleVariableDeclarationNode) member;
            // For variables, we'll use a simple approach
            return "variable:" + var.toString().hashCode();
        }
        
        // For other types, use a hash of the toString representation
        return member.kind().name() + ":" + member.toString().hashCode();
    }
}
