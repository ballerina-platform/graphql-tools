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

package io.ballerina.graphql.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generators.model.GenSrcFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOCUMENTATION_DESCRIPTION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.HASH_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MARKDOWN_DOCUMENTATION_LINE;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.BAL_KEYWORDS;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.ESCAPE_PATTERN;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.IMPORT;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.LINE_SEPARATOR;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.MODULES_PATH;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.ROOT_PROJECT_NAME;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.SEMICOLON;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.SLASH;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.SPECIAL_CHAR_REGEX;
import static io.ballerina.graphql.generators.CodeGeneratorConstants.WHITESPACE;

/**
 * Utility class for GraphQL client code generation.
 */
public class CodeGeneratorUtils {

    /**
     * Gets the document name for a given document.
     *
     * @param documentFile     the queries document file
     * @return                 the document name
     */
    public static String getDocumentName(File documentFile) {
        return documentFile.getName().split(".graphql")[0]
                .replaceAll("[-+.^:,]", "").substring(0, 1).toUpperCase() +
                documentFile.getName().split(".graphql")[0]
                        .replaceAll("[-+.^:,]", "").substring(1);
    }

    /**
     * Gets the client file name of the client file to be generated for a given document.
     *
     * @param documentFile     the queries document file
     * @return                 the client file name of the client file to be generated
     */
    public static String getClientFileName(File documentFile) {
        return documentFile.getName().split(".graphql")[0]
                .replaceAll("[-+.^:,]", "_").concat("_") + CLIENT_FILE_NAME;
    }

    /**
     * Gets the absolute file path of a given source file for code generation.
     *
     * @param file             the source file
     * @param outputPath       the target output path
     * @return                 the client file name of the client file to be generated
     */
    public static Path getAbsoluteFilePath(GenSrcFile file, Path outputPath) {
        Path filePath;
        if (file.getPkgName().equals(ROOT_PROJECT_NAME)) {
            File theDir = new File(outputPath.toString());
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            filePath = outputPath.resolve(file.getFileName());
        } else {
            File theDir = new File(outputPath + MODULES_PATH + file.getPkgName());
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            filePath = outputPath.resolve(outputPath + MODULES_PATH + file.getPkgName() + "/" + file.getFileName());
        }
        return filePath;
    }

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    public static final MinutiaeList SINGLE_WS_MINUTIAE = getSingleWSMinutiae();

    /**
     * Gets the `ImportDeclarationNode` instance for a given organization name & module name.
     *
     * @param orgName          the organization name
     * @param moduleName       the module name
     * @return                 the `ImportDeclarationNode` instance
     */
    public static ImportDeclarationNode getImportDeclarationNode(String orgName, String moduleName) {
        Token importKeyword = AbstractNodeFactory.createIdentifierToken(IMPORT, SINGLE_WS_MINUTIAE,
                SINGLE_WS_MINUTIAE);

        Token orgNameToken = AbstractNodeFactory.createIdentifierToken(orgName);
        Token slashToken = AbstractNodeFactory.createIdentifierToken(SLASH);
        ImportOrgNameNode importOrgNameNode = NodeFactory.createImportOrgNameNode(orgNameToken, slashToken);

        Token moduleNameToken = AbstractNodeFactory.createIdentifierToken(moduleName);
        SeparatedNodeList<IdentifierToken> moduleNodeList = AbstractNodeFactory.createSeparatedNodeList(
                moduleNameToken);

        Token semicolon = AbstractNodeFactory.createIdentifierToken(SEMICOLON);

        return NodeFactory.createImportDeclarationNode(importKeyword, importOrgNameNode,
                moduleNodeList, null, semicolon);
    }

    private static MinutiaeList getSingleWSMinutiae() {
        Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(WHITESPACE);
        MinutiaeList leading = AbstractNodeFactory.createMinutiaeList(whitespace);
        return leading;
    }

    /**
     * Gets the client class name for a given document.
     *
     * @param documentName     the name of the document
     * @return                 the client class name
     */
    public static String getClientClassName(String documentName) {
        return documentName + CodeGeneratorConstants.CLIENT;
    }

    /**
     * Gets the remote function signature return type name.
     *
     * @param operationName    the name of the operation
     * @return                 the remote function return type name
     */
    public static String getRemoteFunctionSignatureReturnTypeName(String operationName) {
        return operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1).concat("Response|graphql:Error");
    }

    /**
     * Gets the remote function body return type name.
     *
     * @param operationName    the name of the operation
     * @return                 the remote function return type name
     */
    public static String getRemoteFunctionBodyReturnTypeName(String operationName) {
        return "<" + operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1) + "Response> check self.graphqlClient->" +
                "execute(" + operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1) + "Response, query, variables)";
    }

    /**
     * Gets the remote function body return type name with Http headers.
     *
     * @param operationName    the name of the operation
     * @return                 the remote function return type name
     */
    public static String getRemoteFunctionBodyReturnTypeNameWithHeaders(String operationName) {
        return "<" + operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1) + "Response> check self.graphqlClient->" +
                "execute(" + operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1) + "Response, query, variables, httpHeaders)";
    }

    public static MetadataNode getMetadataNode(String comment) {
        List<Node> docs = new ArrayList<>(CodeGeneratorUtils.createAPIDescriptionDoc(comment, false));
        MarkdownDocumentationNode authDocumentationNode = createMarkdownDocumentationNode(
                createNodeList(docs));
        return createMetadataNode(authDocumentationNode, createEmptyNodeList());
    }

    private static final MinutiaeList SINGLE_END_OF_LINE_MINUTIAE = getEndOfLineMinutiae();

    private static MinutiaeList getEndOfLineMinutiae() {
        Minutiae endOfLineMinutiae = AbstractNodeFactory.createEndOfLineMinutiae(LINE_SEPARATOR);
        MinutiaeList leading = AbstractNodeFactory.createMinutiaeList(endOfLineMinutiae);
        return leading;
    }

    private static List<MarkdownDocumentationLineNode> createAPIDescriptionDoc(
            String description, boolean addExtraLine) {
        String[] descriptionLines = description.split("\n");
        List<MarkdownDocumentationLineNode> documentElements = new ArrayList<>();
        Token hashToken = createToken(HASH_TOKEN, createEmptyMinutiaeList(), SINGLE_WS_MINUTIAE);
        for (String line : descriptionLines) {
            MarkdownDocumentationLineNode documentationLineNode =
                    createMarkdownDocumentationLineNode(MARKDOWN_DOCUMENTATION_LINE, hashToken,
                            createNodeList(createLiteralValueToken(DOCUMENTATION_DESCRIPTION, line,
                                    createEmptyMinutiaeList(),
                                    SINGLE_END_OF_LINE_MINUTIAE)));
            documentElements.add(documentationLineNode);
        }
        if (addExtraLine) {
            MarkdownDocumentationLineNode newLine = createMarkdownDocumentationLineNode(MARKDOWN_DOCUMENTATION_LINE,
                    createToken(SyntaxKind.HASH_TOKEN), createEmptyNodeList());
            documentElements.add(newLine);
        }
        return documentElements;
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier - identifier or method name
     * @return - escaped string
     */
    public static String escapeIdentifier(String identifier) {

        if (identifier.matches("\\b[0-9]*\\b")) {
            return "'" + identifier;
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b")
                || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

            // TODO: Remove this `if`. Refer - https://github.com/ballerina-platform/ballerina-lang/issues/23045
            if (identifier.equals("error")) {
                identifier = "_error";
            } else {
                identifier = identifier.replaceAll(ESCAPE_PATTERN, "\\\\$1");
                if (identifier.endsWith("?")) {
                    if (identifier.charAt(identifier.length() - 2) == '\\') {
                        StringBuilder stringBuilder = new StringBuilder(identifier);
                        stringBuilder.deleteCharAt(identifier.length() - 2);
                        identifier = stringBuilder.toString();
                    }
                    if (BAL_KEYWORDS.stream().anyMatch(Optional.ofNullable(identifier)
                            .filter(sStr -> sStr.length() != 0)
                            .map(sStr -> sStr.substring(0, sStr.length() - 1))
                            .orElse(identifier)::equals)) {
                        identifier = "'" + identifier;
                    } else {
                        return identifier;
                    }
                } else {
                    identifier = "'" + identifier;
                }
            }
        }
        return identifier;
    }

    public static String getValidName(String identifier) {
        //For the flatten enable we need to remove first Part of valid name check
        // this - > !identifier.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(SPECIAL_CHAR_REGEX);
            StringBuilder validName = new StringBuilder();
            for (String part : split) {
                if (!part.isBlank()) {
                    if (split.length > 1) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                                part.substring(1).toLowerCase(Locale.ENGLISH);
                    }
                    validName.append(part);
                }
            }
            identifier = validName.toString();
        }
        return identifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + identifier.substring(1);
    }
}
