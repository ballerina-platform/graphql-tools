/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.schema.utils;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaFileGenerationException;
import io.ballerina.stdlib.graphql.commons.types.Schema;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.ballerina.graphql.schema.Constants.EMPTY_STRING;
import static io.ballerina.graphql.schema.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.schema.Constants.MESSAGE_CANNOT_READ_SCHEMA_STRING;
import static io.ballerina.graphql.schema.Constants.MESSAGE_INVALID_SCHEMA_STRING;
import static io.ballerina.graphql.schema.Constants.MESSAGE_MISSING_ANNOTATION;
import static io.ballerina.graphql.schema.Constants.MESSAGE_MISSING_FIELD_SCHEMA_STRING;
import static io.ballerina.graphql.schema.Constants.MESSAGE_MISSING_SERVICE_CONFIG;
import static io.ballerina.graphql.schema.Constants.PERIOD;
import static io.ballerina.graphql.schema.Constants.SCHEMA_PREFIX;
import static io.ballerina.graphql.schema.Constants.SCHEMA_STRING_FIELD;
import static io.ballerina.graphql.schema.Constants.SERVICE_CONFIG_IDENTIFIER;
import static io.ballerina.graphql.schema.Constants.SLASH;
import static io.ballerina.graphql.schema.Constants.UNDERSCORE;
import static io.ballerina.stdlib.graphql.commons.utils.TypeUtils.removeEscapeCharacter;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.PACKAGE_NAME;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.hasGraphqlListener;

/**
 * Utility class for Ballerina GraphQL SDL schema generation.
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Check whether the given service declaration node is related to a GraphQL
     * service.
     */
    public static boolean isGraphqlService(ServiceDeclarationNode node, SemanticModel semanticModel) {
        if (semanticModel.symbol(node).isEmpty()) {
            return false;
        }
        if (semanticModel.symbol(node).get().kind() != SymbolKind.SERVICE_DECLARATION) {
            return false;
        }
        ServiceDeclarationSymbol symbol = (ServiceDeclarationSymbol) semanticModel.symbol(node).get();
        return hasGraphqlListener(symbol);
    }

    /**
     * Get service base path from the given service declaration node.
     */
    public static String getServiceBasePath(ServiceDeclarationNode serviceDefinition) {
        StringBuilder currentServiceName = new StringBuilder();
        NodeList<Node> serviceNameNodes = serviceDefinition.absoluteResourcePath();
        for (Node serviceBasedPathNode : serviceNameNodes) {
            currentServiceName.append(removeEscapeCharacter(serviceBasedPathNode.toString()));
        }
        return formatBasePath(currentServiceName.toString().trim());
    }

    /**
     * Get encoded schema string from the given node.
     */
    public static String getSchemaString(ServiceDeclarationNode node) throws SchemaFileGenerationException {
        if (node.metadata().isPresent()) {
            if (!node.metadata().get().annotations().isEmpty()) {
                MappingConstructorExpressionNode annotationValue = getAnnotationValue(node.metadata().get());
                return getSchemaStringFieldFromValue(annotationValue);
            }
        }
        throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MESSAGE_MISSING_ANNOTATION);
    }

    /**
     * Get encoded schema string from the given node.
     */
    public static String getSchemaString(ObjectConstructorExpressionNode node) throws SchemaFileGenerationException {
        if (!node.annotations().isEmpty()) {
            for (AnnotationNode annotationNode : node.annotations()) {
                if (isGraphqlServiceConfig(annotationNode) && annotationNode.annotValue().isPresent()) {
                    return getSchemaStringFieldFromValue(annotationNode.annotValue().get());
                }
            }
        }
        throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MESSAGE_MISSING_ANNOTATION);
    }

    /**
     * Get annotation value string from the given metadata node.
     */
    private static MappingConstructorExpressionNode getAnnotationValue(MetadataNode metadataNode)
            throws SchemaFileGenerationException {
        for (AnnotationNode annotationNode : metadataNode.annotations()) {
            if (isGraphqlServiceConfig(annotationNode) && annotationNode.annotValue().isPresent()) {
                return annotationNode.annotValue().get();
            }
        }
        throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null,
                MESSAGE_MISSING_SERVICE_CONFIG);
    }

    /**
     * Get schema string field from the given node.
     */
    private static String getSchemaStringFieldFromValue(MappingConstructorExpressionNode annotationValue)
            throws SchemaFileGenerationException {
        SeparatedNodeList<MappingFieldNode> existingFields = annotationValue.fields();
        for (MappingFieldNode field : existingFields) {
            if (field.children().get(0).toString().contains(SCHEMA_STRING_FIELD)) {
                String schemaString = field.children().get(2).toString();
                return schemaString.substring(1, schemaString.length() - 1);
            }
        }
        throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null,
                MESSAGE_MISSING_FIELD_SCHEMA_STRING);
    }

    /**
     * Check whether the given annotation is a GraphQL service config.
     *
     * @param annotationNode annotation node
     */
    private static boolean isGraphqlServiceConfig(AnnotationNode annotationNode) {
        if (annotationNode.annotReference().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return false;
        }
        QualifiedNameReferenceNode referenceNode = ((QualifiedNameReferenceNode) annotationNode.annotReference());
        if (!PACKAGE_NAME.equals(referenceNode.modulePrefix().text())) {
            return false;
        }
        return SERVICE_CONFIG_IDENTIFIER.equals(referenceNode.identifier().text());
    }

    /**
     * Generate file name with service basePath.
     */
    public static String getSdlFileName(String servicePath, String serviceName) {
        String sdlFileName;
        if (serviceName.isBlank()) {
            sdlFileName = FilenameUtils.removeExtension(servicePath);
        } else if (serviceName.startsWith(PERIOD)) {
            sdlFileName = FilenameUtils.removeExtension(servicePath) + serviceName;
        } else {
            if (serviceName.startsWith(SLASH)) {
                serviceName = serviceName.substring(1);
            }
            sdlFileName = serviceName.replaceAll(SLASH, "_");
        }
        sdlFileName = getNormalizedFileName(sdlFileName);
        return String.join("", SCHEMA_PREFIX, UNDERSCORE, sdlFileName, GRAPHQL_EXTENSION);
    }

    /**
     * Remove special characters from the given file name.
     */
    public static String getNormalizedFileName(String sdlFileName) {
        String[] splitNames = sdlFileName.split("[^a-zA-Z0-9]");
        if (splitNames.length > 0) {
            return Arrays.stream(splitNames)
                    .filter(namePart -> !namePart.isBlank())
                    .collect(Collectors.joining(UNDERSCORE));
        }
        return sdlFileName;
    }

    /**
     * This method use for format the base path.
     *
     * @param basePath service base path
     * @return formatted base path
     */
    public static String formatBasePath(String basePath) {
        if (basePath.equals(SLASH)) {
            return EMPTY_STRING;
        }
        return basePath;
    }

    /**
     * This method use for decode the encoded schema string.
     *
     * @param schemaString encoded schema string
     * @return GraphQL schema object
     */
    public static Schema getDecodedSchema(String schemaString) throws SchemaFileGenerationException {
        if (schemaString == null || schemaString.isBlank() || schemaString.isEmpty()) {
            throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null,
                    MESSAGE_INVALID_SCHEMA_STRING);
        }
        byte[] decodedString = Base64.getDecoder().decode(schemaString.getBytes(StandardCharsets.UTF_8));
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(decodedString);
            ObjectInputStream inputStream = new ObjectInputStream(byteStream);
            return (Schema) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null,
                    MESSAGE_CANNOT_READ_SCHEMA_STRING);
        }
    }

    /**
     * This method returns the schema file name as-is.
     * File conflict checking is now handled earlier in the process.
     *
     * @param outPath    output path for file generated (unused, kept for
     *                   compatibility)
     * @param schemaName given file name
     * @return the original schema name
     */
    public static String resolveSchemaFileName(Path outPath, String schemaName) {
        // File conflicts are now resolved early in SdlSchemaGenerator
        // This method now simply returns the name as-is
        return schemaName;
    }

    /**
     * This method use for write the generated SDL schema string.
     *
     * @param filePath output file path
     * @param content  SDL schema string
     */
    public static void writeFile(Path filePath, String content) throws SchemaFileGenerationException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_103, null, e.getMessage());
        }
    }

    /**
     * This method create the given output directory if not exist.
     *
     * @param outputPath output file path
     */
    public static void createOutputDirectory(Path outputPath) {
        File outputDir = new File(outputPath.toString());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    /**
     * Check if any of the files to be generated already exist and get user consent
     * for overwriting.
     * This method provides immediate feedback and allows early exit to prevent
     * wasted computation.
     *
     * @param outPath   the output directory path
     * @param fileNames list of file names that will be generated
     * @param outStream print stream for user interaction
     * @throws SchemaFileGenerationException if user chooses not to overwrite
     *                                       existing files
     */
    public static void checkFileOverwriteConsent(Path outPath, java.util.List<String> fileNames,
            java.io.PrintStream outStream) throws SchemaFileGenerationException {
        java.util.List<String> existingFiles = new java.util.ArrayList<>();

        for (String fileName : fileNames) {
            Path filePath = outPath.resolve(fileName);
            if (Files.exists(filePath)) {
                existingFiles.add(fileName);
            }
        }

        if (!existingFiles.isEmpty()) {
            outStream.println("The following schema file(s) already exist:");
            for (String existingFile : existingFiles) {
                outStream.println("-- " + existingFile);
            }
            outStream.print("Do you want to overwrite them? [Y/n]: ");

            try {
                String input = System.console() != null ? System.console().readLine()
                        : new java.util.Scanner(System.in).nextLine();

                // Default to Yes - only cancel if user explicitly says no
                if (input != null && input.trim().toLowerCase().equals("n")) {
                    throw new SchemaFileGenerationException(DiagnosticMessages.SDL_SCHEMA_104, null);
                }
                // Empty input or "y" or any other input defaults to overwrite
            } catch (Exception e) {
                if (e instanceof SchemaFileGenerationException) {
                    throw e;
                }
                // On error reading input, default to overwrite (fail-safe approach)
                outStream.println("Could not read input, proceeding with overwrite...");
            }
        }
    }
}
