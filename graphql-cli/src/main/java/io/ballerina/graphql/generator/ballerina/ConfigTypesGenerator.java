/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.ballerina;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.exception.ConfigTypesGernerationException;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.model.AuthConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.AuthConfigType;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.DISPLAY_ANNOTATION_KIND_FIELD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.DISPLAY_ANNOTATION_KIND_PASSWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.DISPLAY_ANNOTATION_LABEL_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.DISPLAY_ANNOTATION_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorUtils.getMetadataNode;

/**
 * This class is used to generate connection config related types in the ballerina config types file.
 */
public class ConfigTypesGenerator {
    private static ConfigTypesGenerator configTypesGenerator = null;
    private static final String CONNECTION_CONFIG = "ConnectionConfig";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTypesGenerator.class);

    public static ConfigTypesGenerator getInstance() {
        if (configTypesGenerator == null) {
            configTypesGenerator = new ConfigTypesGenerator();
        }
        return configTypesGenerator;
    }

    /**
     * Generates the config types file content.
     *
     * @param authConfig the object instance representing authentication config information
     * @return the config types file content
     * @throws ConfigTypesGernerationException when a config types code generation error occurs
     */
    public String generateSrc(AuthConfig authConfig) throws ConfigTypesGernerationException {
        try {
            return Formatter.format(generateSyntaxTree(authConfig)).toString();
        } catch (FormatterException | IOException e) {
            throw new ConfigTypesGernerationException(e.getMessage());
        }
    }

    /**
     * Generates the config types syntax tree.
     *
     * @param authConfig the object instance representing authentication config information
     * @return syntax tree for the Ballerina config types file code
     * @throws IOException if an I/O error occurs
     */
    private SyntaxTree generateSyntaxTree(AuthConfig authConfig) throws IOException {

        List<ModuleMemberDeclarationNode> memberDeclarationNodes = new ArrayList<>();
        Path path = getResourceFilePath();

        Project project = ProjectLoader.loadProject(path);
        Package currentPackage = project.currentPackage();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree syntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();

        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        for (ModuleMemberDeclarationNode node : members) {
            if (authConfig.isClientConfig()) {
                if (node.kind().equals(SyntaxKind.TYPE_DEFINITION) && ((TypeDefinitionNode) node).typeName().text()
                        .equals(CONNECTION_CONFIG)) {
                    node = constructConnectionConfig(node, authConfig);
                }
                memberDeclarationNodes.add(node);
            } else {
                memberDeclarationNodes.add(node);
            }
        }

        if (authConfig.isApiKeysConfig()) {
            Token typeName = AbstractNodeFactory.createIdentifierToken("ApiKeysConfig");
            NodeList<Node> nodeList = createNodeList(generateApiKeysConfigRecordFields(authConfig));
            RecordTypeDescriptorNode recordTypeDescriptorNode =
                    NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                            createToken(OPEN_BRACE_PIPE_TOKEN), nodeList, null,
                            createToken(CLOSE_BRACE_PIPE_TOKEN));
            TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(metadataNode,
                    createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                    recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
            memberDeclarationNodes.add(typeDefinitionNode);
        }

        ModulePartNode moduleNode = createModulePartNode(imports, createNodeList(memberDeclarationNodes),
                createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree configTypeSyntaxTree = SyntaxTree.from(textDocument);
        return configTypeSyntaxTree.modifyWith(moduleNode);
    }

    /**
     * Creates updated `ConnectionConfig` record.
     *
     * @param node       the 'ConnectionConfig` node
     * @param authConfig the object instance representing authentication configuration information
     * @return updated `ConnectionConfig` type
     */
    private TypeDefinitionNode constructConnectionConfig(ModuleMemberDeclarationNode node, AuthConfig authConfig) {
        RecordTypeDescriptorNode connectionConfigNode = (RecordTypeDescriptorNode)
                ((TypeDefinitionNode) node).typeDescriptor();
        MetadataNode authMetadataNode = getMetadataNode("Configurations related to client authentication");
        String authName;
        if (authConfig.getAuthConfigTypes().contains(AuthConfigType.BEARER)) {
            authName = AuthConfigType.BEARER.getValue();
        } else {
            authName = AuthConfigType.BASIC.getValue();
        }
        IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(authName);

        Node authConfigNode = createRecordFieldNode(authMetadataNode, null, authFieldName,
                createIdentifierToken("auth"), null, createToken(SEMICOLON_TOKEN));

        List<Node> tokens = new ArrayList<>();
        NodeList<Node> fields = connectionConfigNode.fields();
        tokens.add(authConfigNode);
        for (Node n : fields) {
            tokens.add(n);
        }
        NodeList<Node> nodeList = createNodeList(tokens);
        RecordTypeDescriptorNode.RecordTypeDescriptorNodeModifier recordTypeDescriptorNodeModifier =
                connectionConfigNode.modify().withFields(nodeList);
        connectionConfigNode = recordTypeDescriptorNodeModifier.apply();

        return createTypeDefinitionNode(((TypeDefinitionNode) node).metadata().get(),
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), ((TypeDefinitionNode) node).typeName(),
                connectionConfigNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Gets the path of the config_types.bal template at the time of execution.
     *
     * @return Path to config_types.bal file in the temporary directory created
     * @throws IOException When failed to get the templates/config_types.bal file from resources
     */
    private Path getResourceFilePath() throws IOException {
        Path path = null;
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/config_types_graphql.bal");
        if (inputStream != null) {
            String clientSyntaxTreeString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Path tmpDir = Files.createTempDirectory(".config_types_graphql-tmp" + System.nanoTime());
            path = tmpDir.resolve("config_types_graphql.bal");
            try (PrintWriter writer = new PrintWriter(path.toString(), StandardCharsets.UTF_8)) {
                writer.print(clientSyntaxTreeString);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(tmpDir.toFile());
                } catch (IOException ex) {
                    LOGGER.error("Unable to delete the temporary directory : " + tmpDir, ex);
                }
            }));
        }
        return path;
    }

    /**
     * Generates API keys config record fields.
     *
     * @param authConfig the object instance representing authentication configuration information
     * @return list of nodes in API Keys Config
     */
    private List<Node> generateApiKeysConfigRecordFields(AuthConfig authConfig) {
        List<Node> ballerinaApiKeysConfigRecords = new ArrayList<>();
        for (String headerName : authConfig.getApiHeaders()) {
            MetadataNode metadataNode = createMetadataNode(null, generateSensitiveFieldsAnnotation());
            TypeDescriptorNode typeName = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
            IdentifierToken apiKeyFieldName =
                    createIdentifierToken(CodeGeneratorUtils.getValidName(headerName));
            ballerinaApiKeysConfigRecords.add(
                    createRecordFieldNode(metadataNode, null, typeName,
                            apiKeyFieldName, null, createToken(SEMICOLON_TOKEN)));
        }
        return ballerinaApiKeysConfigRecords;
    }

    /**
     * Generates annotations for sensitive fields.
     *
     * @return list of annotations
     */
    private NodeList<AnnotationNode> generateSensitiveFieldsAnnotation() {
        List<Node> annotFields = new ArrayList<>();
        Map<String, String> extFields = new LinkedHashMap<>();
        extFields.put(DISPLAY_ANNOTATION_LABEL_NAME, EMPTY_STRING);
        extFields.put(DISPLAY_ANNOTATION_KIND_FIELD, DISPLAY_ANNOTATION_KIND_PASSWORD);
        for (Map.Entry<String, String> field : extFields.entrySet()) {
            BasicLiteralNode valueExpr = createBasicLiteralNode(STRING_LITERAL,
                    createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, '"' + field.getValue().trim() + '"',
                            createEmptyMinutiaeList(), createEmptyMinutiaeList()));
            SpecificFieldNode fields = createSpecificFieldNode(null,
                    createIdentifierToken(field.getKey().trim()), createToken(COLON_TOKEN), valueExpr);
            annotFields.add(fields);
            annotFields.add(createToken(COMMA_TOKEN));
        }
        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(annotFields),
                createToken(CLOSE_BRACE_TOKEN));

        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken(DISPLAY_ANNOTATION_NAME));
        AnnotationNode annotationNode = createAnnotationNode(createToken(AT_TOKEN), annotateReference, annotValue);
        return createNodeList(annotationNode);
    }
}
