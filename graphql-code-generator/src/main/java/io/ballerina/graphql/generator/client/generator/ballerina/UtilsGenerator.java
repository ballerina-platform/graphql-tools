/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.generator.client.generator.ballerina;

import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.client.exception.UtilsGenerationException;
import io.ballerina.graphql.generator.client.generator.model.AuthConfig;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSingletonTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

/**
 * This class is used to generate utility functions in the ballerina utils file.
 */
public class UtilsGenerator {
    private static final Log log = LogFactory.getLog(UtilsGenerator.class);
    private static UtilsGenerator utilsGenerator = null;

    public static UtilsGenerator getInstance() {
        if (utilsGenerator == null) {
            utilsGenerator = new UtilsGenerator();
        }
        return utilsGenerator;
    }

    /**
     * Generates the utils file content.
     *
     * @param authConfig the object instance representing authentication config information
     * @return the client file content
     * @throws UtilsGenerationException when an utils code generation error occurs
     */
    public String generateSrc(AuthConfig authConfig) throws UtilsGenerationException {
        try {
            return Formatter.format(generateSyntaxTree(authConfig)).toString();
        } catch (FormatterException | IOException e) {
            throw new UtilsGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the utils syntax tree.
     *
     * @param authConfig the object instance representing authentication config information
     * @return Syntax tree for the Ballerina utils file code
     * @throws IOException If an I/O error occurs
     */
    public SyntaxTree generateSyntaxTree(AuthConfig authConfig) throws IOException {
        NodeList<ImportDeclarationNode> importsList = generateImports();

        List<ModuleMemberDeclarationNode> members = new ArrayList<>();
        if (authConfig.isApiKeysConfig()) {
            members.add(getSimpleBasicTypeDefinitionNode());
        }
        members.add(getOperationResponseTypeDefinitionNode());
        members.add(getDataResponseTypeDefinitionNode());

        Path path = getResourceFilePath();
        Project project = ProjectLoader.loadProject(path);
        Package currentPackage = project.currentPackage();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree utilSyntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();

        ModulePartNode utilModulePartNode = utilSyntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> memberDeclarationNodes = utilModulePartNode.members();
        for (ModuleMemberDeclarationNode node : memberDeclarationNodes) {
            if (node.kind().equals(SyntaxKind.FUNCTION_DEFINITION)) {
                for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                    if (childNodeEntry.name().equals("functionName")) {
                        if (authConfig.isApiKeysConfig()) {
                            if (childNodeEntry.node().get().toString().equals("getMapForHeaders")) {
                                members.add(node);
                            }
                        }
                        if (childNodeEntry.node().get().toString().equals("performDataBinding")) {
                            members.add(node);
                        }
                    }
                }
            }
        }

        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(members), createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(CodeGeneratorConstants.EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generates the imports in the utils file.
     *
     * @return the node list which represent imports in the utils file
     */
    private NodeList<ImportDeclarationNode> generateImports() {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForGraphql =
                CodeGeneratorUtils.getImportDeclarationNode(CodeGeneratorConstants.BALLERINA,
                        CodeGeneratorConstants.GRAPHQL);
        imports.add(importForGraphql);
        return createNodeList(imports);
    }

    /**
     * Generates `SimpleBasicType` type.
     * <pre>
     *     type SimpleBasicType string|boolean|int|float|decimal;
     * </pre>
     *
     * @return the `SimpleBasicType` type definition node
     */
    private TypeDefinitionNode getSimpleBasicTypeDefinitionNode() {
        TypeDescriptorNode typeDescriptorNode = createSingletonTypeDescriptorNode(
                createSimpleNameReferenceNode(createIdentifierToken("string|boolean|int|float|decimal")));

        return createTypeDefinitionNode(null, null, createToken(TYPE_KEYWORD), createIdentifierToken("SimpleBasicType"),
                typeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates `OperationResponse` type.
     * <pre>
     *     type OperationResponse record {| anydata...; |}|record {| anydata...; |}[]|boolean|string|int|float|();
     * </pre>
     *
     * @return the `OperationResponse` type definition node
     */
    private TypeDefinitionNode getOperationResponseTypeDefinitionNode() {
        TypeDescriptorNode typeDescriptorNode = createSingletonTypeDescriptorNode(createSimpleNameReferenceNode(
                createIdentifierToken(
                        "record {| anydata...; |}|" + "record {| anydata...; |}[]|boolean|string|int|float|()")));

        return createTypeDefinitionNode(null, null, createToken(TYPE_KEYWORD),
                createIdentifierToken("OperationResponse"), typeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates `DataResponse` type.
     * <pre>
     *      type DataResponse record {|
     *          map<json?> __extensions?;
     *          OperationResponse ...;
     *      |};
     * </pre>
     *
     * @return the `DataResponse` type definition node
     */
    private TypeDefinitionNode getDataResponseTypeDefinitionNode() {
        TypeDescriptorNode typeDescriptorNode = createSingletonTypeDescriptorNode(createSimpleNameReferenceNode(
                createIdentifierToken(
                        "record {|\n" + "   map<json?> __extensions?;\n" + "   OperationResponse ...;\n" + "|}")));

        return createTypeDefinitionNode(null, null, createToken(TYPE_KEYWORD), createIdentifierToken("DataResponse"),
                typeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Gets the path of the utils.bal template at the time of execution.
     *
     * @return Path to utils.bal file in the temporary directory created
     * @throws IOException When failed to get the templates/utils.bal file from resources
     */
    private Path getResourceFilePath() throws IOException {
        Path path = null;
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/utils_graphql.bal");
        if (inputStream != null) {
            String clientSyntaxTreeString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Path tmpDir = Files.createTempDirectory(".util-tmp" + System.nanoTime());
            path = tmpDir.resolve("utils.bal");
            try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
                writer.print(clientSyntaxTreeString);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(tmpDir.toFile());
                } catch (IOException ex) {
                    log.error("Unable to delete the temporary directory : " + tmpDir, ex);
                }
            }));
        }
        return path;
    }
}
