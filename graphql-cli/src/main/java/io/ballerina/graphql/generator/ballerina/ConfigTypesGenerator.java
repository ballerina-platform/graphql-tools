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
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CONST_DECLARATION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ENUM_DECLARATION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.AuthConfigType;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorUtils.getMetadataNode;

/**
 * This class is used to generate connection config related types in the ballerina config types file.
 */
public class ConfigTypesGenerator {
    private static ConfigTypesGenerator configTypesGenerator = null;
    private static final String CONNECTION_CONFIG = "ConnectionConfig";
    private static final String CLIENT_HTTP1_SETTINGS = "ClientHttp1Settings";
    private static final String PROXY_CONFIG = "ProxyConfig";
    private static final String KEEP_ALIVE = "KeepAlive";
    private static final String CHUNKING = "Chunking";
    private static final String KEEPALIVE_AUTO = "KEEPALIVE_AUTO";
    private static final String KEEPALIVE_ALWAYS = "KEEPALIVE_ALWAYS";
    private static final String KEEPALIVE_NEVER = "KEEPALIVE_NEVER";
    private static final String CHUNKING_AUTO = "CHUNKING_AUTO";
    private static final String CHUNKING_ALWAYS = "CHUNKING_ALWAYS";
    private static final String CHUNKING_NEVER = "CHUNKING_NEVER";
    private static final String CLIENT_HTTP2_SETTINGS = "ClientHttp2Settings";
    private static final String CACHE_CONFIG = "CacheConfig";
    private static final String CACHING_POLICY = "CachingPolicy";
    private static final String CACHE_CONTROL_AND_VALIDATORS = "CACHE_CONTROL_AND_VALIDATORS";
    private static final String RFC_7234 = "RFC_7234";
    private static final String COMPRESSION = "Compression";
    private static final String COMPRESSION_AUTO = "COMPRESSION_AUTO";
    private static final String COMPRESSION_ALWAYS = "COMPRESSION_ALWAYS";
    private static final String COMPRESSION_NEVER = "COMPRESSION_NEVER";
    private static final String CIRCUIT_BREAKER_CONFIG = "CircuitBreakerConfig";
    private static final String ROLLING_WINDOW = "RollingWindow";
    private static final String RETRY_CONFIG = "RetryConfig";
    private static final String RESPONSE_LIMIT_CONFIGS = "ResponseLimitConfigs";
    private static final String CLIENT_SECURE_SOCKET = "ClientSecureSocket";
    private static final String TRUST_STORE = "TrustStore";
    private static final String KEY_STORE = "KeyStore";
    private static final String CERT_KEY = "CertKey";
    private static final String CERT_VALIDATION_TYPE = "CertValidationType";
    private static final String PROTOCOL = "Protocol";
    private static final String POOL_CONFIGURATION = "PoolConfiguration";
    private static final String HTTP_VERSION = "HttpVersion";
    private static final String BEARER_TOKEN_CONFIG = "BearerTokenConfig";
    private static final String CREDENTIALS_CONFIG = "CredentialsConfig";
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
        Set<String> commonTypesList = new LinkedHashSet<>(Arrays.asList(CONNECTION_CONFIG, CLIENT_HTTP1_SETTINGS,
                PROXY_CONFIG, KEEP_ALIVE, CHUNKING, KEEPALIVE_AUTO, KEEPALIVE_ALWAYS, KEEPALIVE_NEVER, CHUNKING_AUTO,
                CHUNKING_ALWAYS, CHUNKING_NEVER, CLIENT_HTTP2_SETTINGS, CACHE_CONFIG, CACHING_POLICY,
                CACHE_CONTROL_AND_VALIDATORS, RFC_7234, COMPRESSION, COMPRESSION_AUTO, COMPRESSION_ALWAYS,
                COMPRESSION_NEVER, CIRCUIT_BREAKER_CONFIG, ROLLING_WINDOW, RETRY_CONFIG, RESPONSE_LIMIT_CONFIGS,
                CLIENT_SECURE_SOCKET, TRUST_STORE, KEY_STORE, CERT_KEY, CERT_VALIDATION_TYPE, PROTOCOL,
                POOL_CONFIGURATION, HTTP_VERSION));

        Set<String> clientConfigTypesList = new LinkedHashSet<>();
        if (authConfig.getAuthConfigTypes().contains(AuthConfigType.BEARER)) {
            clientConfigTypesList.add(BEARER_TOKEN_CONFIG);
        }
        if (authConfig.getAuthConfigTypes().contains(AuthConfigType.BASIC)) {
            clientConfigTypesList.add(CREDENTIALS_CONFIG);
        }

        List<ModuleMemberDeclarationNode> memberDeclarationNodes = new ArrayList<>();
        Path path = getResourceFilePath();

        Project project = ProjectLoader.loadProject(path);
        Package currentPackage = project.currentPackage();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree syntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();

        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind().equals(SyntaxKind.TYPE_DEFINITION)) {
                if (commonTypesList.contains(((TypeDefinitionNode) node).typeName().text()) ||
                        clientConfigTypesList.contains(((TypeDefinitionNode) node).typeName().text())) {
                    if (authConfig.isClientConfig() && ((TypeDefinitionNode) node).typeName().text()
                            .equals(CONNECTION_CONFIG)) {
                        node = constructConnectionConfig(node, authConfig);
                    }
                    memberDeclarationNodes.add(node);
                }
            } else if (node.kind().equals(CONST_DECLARATION) && commonTypesList.contains(((ConstantDeclarationNode)
                    node).variableName().text())) {
                memberDeclarationNodes.add(node);
            } else if (node.kind().equals(ENUM_DECLARATION) && commonTypesList.contains(((EnumDeclarationNode)
                    node).identifier().text())) {
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

        ModulePartNode moduleNode = createModulePartNode(createEmptyNodeList(),
                createNodeList(memberDeclarationNodes), createToken(EOF_TOKEN));
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
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
            TypeDescriptorNode typeName = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
            IdentifierToken apiKeyFieldName =
                    createIdentifierToken(CodeGeneratorUtils.getValidName(headerName));
            ballerinaApiKeysConfigRecords.add(
                    createRecordFieldNode(metadataNode, null, typeName,
                            apiKeyFieldName, null, createToken(SEMICOLON_TOKEN)));
        }
        return ballerinaApiKeysConfigRecords;
    }
}
