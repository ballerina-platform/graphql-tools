/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generators.ballerina;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.cmd.mappers.Extension;
import io.ballerina.graphql.generators.CodeGeneratorConstants;
import io.ballerina.graphql.generators.CodeGeneratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DECIMAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

/**
 * This class is used to generate authentication configuration in the ballerina client file.
 */
public class AuthConfigGenerator {
    private static final Log log = LogFactory.getLog(AuthConfigGenerator.class);
    private final Set<CodeGeneratorConstants.AuthConfigTypes> authConfigTypes = new LinkedHashSet<>();
    private final Set<String> apiHeaders = new LinkedHashSet<>();
    private final List<Node> apiKeysConfigRecordFields = new ArrayList<>();

    /**
     * Returns `true` if authentication mechanism is API key.
     *
     * @return {@link boolean}    value of the flag isApiKeysConfig
     */
    public boolean isApiKeysConfig() {
        if (getAuthConfigTypes().size() != 0) {
            if (getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigTypes.API_KEY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns `true` if authentication mechanism is OAuth.
     *
     * @return {@link boolean}    value of the flag isClientConfig
     */
    public boolean isClientConfig() {
        if (getAuthConfigTypes().size() != 0) {
            if (getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigTypes.BASIC) ||
                    getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigTypes.BEARER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the authentication types extracting information from the extensions.
     *
     * @param extensions     the extensions value of the Graphql config file
     */
    public void setAuthConfigTypes(Extension extensions) {
        if (extensions != null && extensions.getEndpoints() != null &&
                extensions.getEndpoints().getDefaultName() != null &&
                extensions.getEndpoints().getDefaultName().getHeaders().size() != 0) {
            Map<String, String> headers = extensions.getEndpoints().getDefaultName().getHeaders();
            for (String headerName : headers.keySet()) {
                if (headerName.equals("Authorization")) {
                    if (headers.get(headerName).startsWith("Basic")) {
                        authConfigTypes.add(CodeGeneratorConstants.AuthConfigTypes.BASIC);
                    } else if (headers.get(headerName).startsWith("Bearer")) {
                        authConfigTypes.add(CodeGeneratorConstants.AuthConfigTypes.BEARER);
                    } else {
                        authConfigTypes.add(CodeGeneratorConstants.AuthConfigTypes.API_KEY);
                    }
                } else {
                    authConfigTypes.add(CodeGeneratorConstants.AuthConfigTypes.API_KEY);
                }
            }
        }
    }

    /**
     * Returns the authentication types.
     *
     * @return               the authentication types
     */
    public Set<CodeGeneratorConstants.AuthConfigTypes> getAuthConfigTypes() {
        return authConfigTypes;
    }

    /**
     * Sets the API headers if present extracting information from the extensions.
     *
     * @param extensions     the extensions value of the Graphql config file
     */
    public void setApiHeaders(Extension extensions) {
        if (extensions != null && extensions.getEndpoints() != null &&
                extensions.getEndpoints().getDefaultName() != null &&
                extensions.getEndpoints().getDefaultName().getHeaders().size() != 0) {
            Map<String, String> headers = extensions.getEndpoints().getDefaultName().getHeaders();
            for (String headerName : headers.keySet()) {
                if (headerName.equals("Authorization")) {
                    if (!headers.get(headerName).startsWith("Basic") &&
                            !headers.get(headerName).startsWith("Bearer")) {
                        apiHeaders.add(headerName);
                    }
                } else {
                    apiHeaders.add(headerName);
                }
            }
        }
    }

    /**
     * Returns the API headers.
     *
     * @return               the API headers
     */
    public Set<String> getApiHeaders() {
        return apiHeaders;
    }

    /**
     * Sets the API key config record fields extracting information from the extensions.
     *
     * @param extensions     the extensions value of the Graphql config file
     */
    public void setApiKeysConfigRecordFields(Extension extensions) {
        if (extensions != null && extensions.getEndpoints() != null &&
                extensions.getEndpoints().getDefaultName() != null &&
                extensions.getEndpoints().getDefaultName().getHeaders().size() != 0) {
            Map<String, String> headers = extensions.getEndpoints().getDefaultName().getHeaders();
            for (String headerName : headers.keySet()) {
                if (headerName.equals("Authorization")) {
                    if (!headers.get(headerName).startsWith("Basic") &&
                            !headers.get(headerName).startsWith("Bearer")) {
                        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
                        TypeDescriptorNode typeName = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
                        IdentifierToken apiKeyFieldName =
                                createIdentifierToken(CodeGeneratorUtils.getValidName(headerName));
                        apiKeysConfigRecordFields.add(createRecordFieldNode(metadataNode, null, typeName,
                                apiKeyFieldName, null, createToken(SEMICOLON_TOKEN)));
                    }
                } else {
                    MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
                    TypeDescriptorNode typeName = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
                    IdentifierToken apiKeyFieldName =
                            createIdentifierToken(CodeGeneratorUtils.getValidName(headerName));
                    apiKeysConfigRecordFields.add(createRecordFieldNode(metadataNode, null, typeName,
                            apiKeyFieldName, null, createToken(SEMICOLON_TOKEN)));
                }
            }
        }
    }

    /**
     * Returns the API key config record fields.
     *
     * @return               the API key config record fields
     */
    private List<Node> getApiKeysConfigRecordFields() {
        return apiKeysConfigRecordFields;
    }

    /**
     * Adds the authentication related record nodes.
     *
     * @param members     the list of module member declaration nodes
     */
    public void addAuthRelatedRecords(List<ModuleMemberDeclarationNode> members) {
        if (getAuthConfigTypes().size() != 0) {
            if (getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigTypes.API_KEY)) {
                members.add(getAuthRelatedRecord("ApiKeysConfig"));
            }
            if (getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigTypes.BASIC) ||
            getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigTypes.BEARER)) {
                members.add(getAuthRelatedRecord("ClientConfig"));
            }
        }
    }

    /**
     * Returns the authentication related record node.
     *
     * @param configRecordType     the configuration record type
     * @return                     the authentication related record node
     */
    public TypeDefinitionNode getAuthRelatedRecord(String configRecordType) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

        Token typeName = createIdentifierToken("");
        NodeList<Node> recordFieldList = createEmptyNodeList();
        if (configRecordType.equals("ApiKeysConfig")) {
            typeName = AbstractNodeFactory.createIdentifierToken("ApiKeysConfig");
            recordFieldList = createNodeList(getApiKeysConfigRecordFields());
        } else if (configRecordType.equals("ClientConfig")) {
            typeName = AbstractNodeFactory.createIdentifierToken("ClientConfig");
            recordFieldList = createNodeList(getClientConfigRecordFields());
        }
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));

        return NodeFactory.createTypeDefinitionNode(metadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Returns fields in ClientConfig record.
     * <pre>
     *     # Configurations related to client authentication
     *     http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;
     *     # The HTTP version understood by the client
     *     string httpVersion = "1.1";
     *     # Configurations related to HTTP/1.x protocol
     *     http:ClientHttp1Settings http1Settings = {};
     *     # Configurations related to HTTP/2 protocol
     *     http:ClientHttp2Settings http2Settings = {};
     *     # The maximum time to wait (in seconds) for a response before closing the connection
     *     decimal timeout = 60;
     *     # The choice of setting `forwarded`/`x-forwarded` header
     *     string forwarded = "disable";
     *     # Configurations associated with Redirection
     *     http:FollowRedirects? followRedirects = ();
     *     # Configurations associated with request pooling
     *     http:PoolConfiguration? poolConfig = ();
     *     # HTTP caching related configurations
     *     http:CacheConfig cache = {};
     *     # Specifies the way of handling compression (`accept-encoding`) header
     *     http:Compression compression = http:COMPRESSION_AUTO;
     *     # Configurations associated with the behaviour of the Circuit Breaker
     *     http:CircuitBreakerConfig? circuitBreaker = ();
     *     # Configurations associated with retrying
     *     http:RetryConfig? retryConfig = ();
     *     # Configurations associated with cookies
     *     http:CookieConfig? cookieConfig = ();
     *     # Configurations associated with inbound response size limits
     *     http:ResponseLimitConfigs responseLimits = {};
     *     #SSL/TLS-related options
     *     http:ClientSecureSocket? secureSocket = ();
     * </pre>
     *
     * @return {@link List <Node>}   ClientConfig record fields' node list
     */
    private List<Node> getClientConfigRecordFields() {
        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);
        Token equalToken = createToken(EQUAL_TOKEN);
        ExpressionNode emptyExpression = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(), createToken(CLOSE_BRACE_TOKEN));
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));

        // add auth field
        MetadataNode authMetadataNode =
                CodeGeneratorUtils.getMetadataNode("Configurations related to client authentication");
        IdentifierToken authFieldName =
                AbstractNodeFactory.createIdentifierToken(CodeGeneratorUtils.escapeIdentifier("auth"));
        TypeDescriptorNode authFieldTypeNode =
                createSimpleNameReferenceNode(createIdentifierToken(getAuthFieldTypeName()));
        RecordFieldNode authFieldNode = createRecordFieldNode(authMetadataNode, null,
                authFieldTypeNode, authFieldName, null, semicolonToken);
        recordFieldNodes.add(authFieldNode);

        // add httpVersion field
        MetadataNode httpVersionMetadata =
                CodeGeneratorUtils.getMetadataNode("The HTTP version understood by the client");
        TypeDescriptorNode httpVersionFieldType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        IdentifierToken httpVersionFieldName = createIdentifierToken("httpVersion");
        RequiredExpressionNode httpVersionExpression =
                createRequiredExpressionNode(createIdentifierToken("\"1.1\""));
        RecordFieldWithDefaultValueNode httpVersionFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                httpVersionMetadata, null, httpVersionFieldType, httpVersionFieldName,
                equalToken, httpVersionExpression, semicolonToken);
        recordFieldNodes.add(httpVersionFieldNode);

        // add http1Settings field
        MetadataNode http1SettingsMetadata =
                CodeGeneratorUtils.getMetadataNode("Configurations related to HTTP/1.x protocol");
        IdentifierToken http1SettingsFieldName = createIdentifierToken("http1Settings");
        TypeDescriptorNode http1SettingsFieldType =
                createSimpleNameReferenceNode(createIdentifierToken("http:ClientHttp1Settings"));
        RecordFieldWithDefaultValueNode http1SettingsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                http1SettingsMetadata, null, http1SettingsFieldType, http1SettingsFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(http1SettingsFieldNode);

        // add http2Settings fields
        MetadataNode http2SettingsMetadata =
                CodeGeneratorUtils.getMetadataNode("Configurations related to HTTP/2 protocol");
        TypeDescriptorNode http2SettingsFieldType =
                createSimpleNameReferenceNode(createIdentifierToken("http:ClientHttp2Settings"));
        IdentifierToken http2SettingsFieldName = createIdentifierToken("http2Settings");
        RecordFieldWithDefaultValueNode http2SettingsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                http2SettingsMetadata, null, http2SettingsFieldType, http2SettingsFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(http2SettingsFieldNode);

        // add timeout field
        MetadataNode timeoutMetadata = CodeGeneratorUtils.getMetadataNode(
                "The maximum time to wait (in seconds) for a response before closing the connection");
        IdentifierToken timeoutFieldName = createIdentifierToken("timeout");
        TypeDescriptorNode timeoutFieldType = createSimpleNameReferenceNode(createToken(DECIMAL_KEYWORD));
        ExpressionNode decimalLiteralNode = createRequiredExpressionNode(createIdentifierToken("60"));
        RecordFieldWithDefaultValueNode timeoutFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                timeoutMetadata, null, timeoutFieldType, timeoutFieldName,
                equalToken, decimalLiteralNode, semicolonToken);
        recordFieldNodes.add(timeoutFieldNode);

        // add forwarded field
        MetadataNode forwardedMetadata = CodeGeneratorUtils.getMetadataNode(
                "The choice of setting `forwarded`/`x-forwarded` header");
        IdentifierToken forwardedFieldName = createIdentifierToken("forwarded");
        TypeDescriptorNode forwardedFieldType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        ExpressionNode forwardedDefaultValue = createRequiredExpressionNode(createIdentifierToken("\"disable\""));
        RecordFieldWithDefaultValueNode forwardedFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                forwardedMetadata, null, forwardedFieldType, forwardedFieldName,
                equalToken, forwardedDefaultValue, semicolonToken);
        recordFieldNodes.add(forwardedFieldNode);

        // add followRedirects field
        MetadataNode followRedirectsMetadata =
                CodeGeneratorUtils.getMetadataNode("Configurations associated with Redirection");
        IdentifierToken followRedirectsFieldName = AbstractNodeFactory.createIdentifierToken("followRedirects");
        TypeDescriptorNode followRedirectsFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:FollowRedirects"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode followRedirectsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                followRedirectsMetadata, null, followRedirectsFieldType,
                followRedirectsFieldName, equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(followRedirectsFieldNode);

        // add poolConfig field
        MetadataNode poolConfigMetaData =
                CodeGeneratorUtils.getMetadataNode("Configurations associated with request pooling");
        IdentifierToken poolConfigFieldName = AbstractNodeFactory.createIdentifierToken("poolConfig");
        TypeDescriptorNode poolConfigFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:PoolConfiguration"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode poolConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                poolConfigMetaData, null, poolConfigFieldType, poolConfigFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(poolConfigFieldNode);

        // add cache field
        MetadataNode cachMetadata =
                CodeGeneratorUtils.getMetadataNode("HTTP caching related configurations");
        IdentifierToken cacheFieldName = createIdentifierToken("cache");
        TypeDescriptorNode cacheFieldType =
                createSimpleNameReferenceNode(createIdentifierToken("http:CacheConfig"));
        RecordFieldWithDefaultValueNode cachFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                cachMetadata, null, cacheFieldType, cacheFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(cachFieldNode);

        // add compression field
        MetadataNode compressionMetadata = CodeGeneratorUtils.getMetadataNode(
                "Specifies the way of handling compression (`accept-encoding`) header");
        IdentifierToken compressionFieldName = createIdentifierToken("compression");
        TypeDescriptorNode compressionFieldType = createSimpleNameReferenceNode(
                createIdentifierToken("http:Compression"));
        ExpressionNode compressionDefaultValue = createRequiredExpressionNode(
                createIdentifierToken("http:COMPRESSION_AUTO"));
        RecordFieldWithDefaultValueNode compressionFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                compressionMetadata, null, compressionFieldType, compressionFieldName,
                equalToken, compressionDefaultValue, semicolonToken);
        recordFieldNodes.add(compressionFieldNode);

        // add circuitBreaker field
        MetadataNode circuitBreakerMetadata = CodeGeneratorUtils.getMetadataNode(
                "Configurations associated with the behaviour of the Circuit Breaker");
        IdentifierToken circuitBreakerFieldName = AbstractNodeFactory.createIdentifierToken("circuitBreaker");
        TypeDescriptorNode circuitBreakerFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:CircuitBreakerConfig"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode circuitBreakerFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                circuitBreakerMetadata, null, circuitBreakerFieldType, circuitBreakerFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(circuitBreakerFieldNode);

        // add retryConfig field
        MetadataNode retryConfigMetadata =
                CodeGeneratorUtils.getMetadataNode("Configurations associated with retrying");
        IdentifierToken retryConfigFieldName = AbstractNodeFactory.createIdentifierToken("retryConfig");
        TypeDescriptorNode returConfigFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:RetryConfig"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode retryConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                retryConfigMetadata, null, returConfigFieldType, retryConfigFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(retryConfigFieldNode);

        // add cookieConfig field
        MetadataNode cookieConfigMetadata =
                CodeGeneratorUtils.getMetadataNode("Configurations associated with cookies");
        IdentifierToken cookieConfigFieldName = AbstractNodeFactory.createIdentifierToken("cookieConfig");
        TypeDescriptorNode cookieConfigFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:CookieConfig"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode cookieConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                cookieConfigMetadata, null, cookieConfigFieldType, cookieConfigFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(cookieConfigFieldNode);

        // add responseLimits field
        MetadataNode responseLimitsMetadata = CodeGeneratorUtils.getMetadataNode(
                "Configurations associated with inbound response size limits");
        IdentifierToken responseLimitsFieldName = createIdentifierToken("responseLimits");
        TypeDescriptorNode responseLimitsFieldType = createSimpleNameReferenceNode(
                createIdentifierToken("http:ResponseLimitConfigs"));
        RecordFieldWithDefaultValueNode responseLimitsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                responseLimitsMetadata, null, responseLimitsFieldType, responseLimitsFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(responseLimitsFieldNode);

        // add secureSocket field
        MetadataNode secureSocketMetadata = CodeGeneratorUtils.getMetadataNode("SSL/TLS-related options");
        IdentifierToken secureSocketFieldName = AbstractNodeFactory.createIdentifierToken("secureSocket");
        TypeDescriptorNode secureSocketfieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:ClientSecureSocket"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode secureSocketFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                secureSocketMetadata, null, secureSocketfieldType, secureSocketFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(secureSocketFieldNode);

        return recordFieldNodes;
    }

    /**
     * Travers through the authTypes and generate the field type name of auth field in ClientConfig record.
     *
     * @return {@link String}   Field type name of auth field
     *                          Ex: {@code http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig}
     */
    public String getAuthFieldTypeName() {
        Set<String> httpFieldTypeNames = new HashSet<>();
        for (CodeGeneratorConstants.AuthConfigTypes authType : authConfigTypes) {
            switch (authType) {
                case BEARER:
                    httpFieldTypeNames.add(CodeGeneratorConstants.AuthConfigTypes.BEARER.getValue());
                    break;
                case BASIC:
                    httpFieldTypeNames.add(CodeGeneratorConstants.AuthConfigTypes.BASIC.getValue());
                    break;
                default:
                    break;
            }
        }
        return buildConfigRecordFieldTypes(httpFieldTypeNames).toString();
    }

    /**
     * This method is used concat the config record authConfig field type.
     *
     * @param fieldtypes        Type name set from {@link (Map)} method.
     * @return {@link String}   Pipe concatenated list of type names
     */
    private static StringBuilder buildConfigRecordFieldTypes(Set<String> fieldtypes) {
        StringBuilder httpAuthFieldTypes = new StringBuilder();
        if (!fieldtypes.isEmpty()) {
            for (String fieldType : fieldtypes) {
                if (httpAuthFieldTypes.length() != 0) {
                    httpAuthFieldTypes.append("|").append(fieldType);
                } else {
                    httpAuthFieldTypes.append(fieldType);
                }
            }
        }
        return httpAuthFieldTypes;
    }
}
