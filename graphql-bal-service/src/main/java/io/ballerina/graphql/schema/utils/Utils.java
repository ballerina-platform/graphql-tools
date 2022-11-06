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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
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
import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.stdlib.graphql.commons.types.Schema;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
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

import static io.ballerina.graphql.schema.Constants.GRAPHQL_EXTENSION;
import static io.ballerina.graphql.schema.Constants.HYPHEN;
import static io.ballerina.graphql.schema.Constants.MSG_CANNOT_READ_SCHEMA_STR;
import static io.ballerina.graphql.schema.Constants.MSG_INVALID_SCHEMA_STR;
import static io.ballerina.graphql.schema.Constants.MSG_MISSING_ANNOT;
import static io.ballerina.graphql.schema.Constants.MSG_MISSING_FIELD_SCHEMA_STR;
import static io.ballerina.graphql.schema.Constants.MSG_MISSING_SERVICE_CONFIG;
import static io.ballerina.graphql.schema.Constants.SCHEMA_PREFIX;
import static io.ballerina.graphql.schema.Constants.SCHEMA_STRING_FIELD;
import static io.ballerina.graphql.schema.Constants.SERVICE_CONFIG_IDENTIFIER;
import static io.ballerina.graphql.schema.Constants.SLASH;
import static io.ballerina.graphql.schema.Constants.UNDERSCORE;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.PACKAGE_NAME;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.isGraphqlModuleSymbol;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.removeEscapeCharacter;

/**
 * Utility class for Ballerina GraphQL schema types.
 */
public class Utils {

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

    private static boolean hasGraphqlListener(ServiceDeclarationSymbol symbol) {
        for (TypeSymbol listener : symbol.listenerTypes()) {
            if (isGraphqlListener(listener)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGraphqlListener(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
            for (TypeSymbol member : unionTypeSymbol.memberTypeDescriptors()) {
                if (isGraphqlModuleSymbol(member)) {
                    return true;
                }
            }
        } else {
            return isGraphqlModuleSymbol(typeSymbol);
        }
        return false;
    }

    public static String getServiceBasePath(ServiceDeclarationNode serviceDefinition) {
        StringBuilder currentServiceName = new StringBuilder();
        NodeList<Node> serviceNameNodes = serviceDefinition.absoluteResourcePath();
        for (Node serviceBasedPathNode : serviceNameNodes) {
            currentServiceName.append(removeEscapeCharacter(serviceBasedPathNode.toString()));
        }
        return currentServiceName.toString().trim();
    }

    public static String getSchemaString(ServiceDeclarationNode node) throws SchemaGenerationException {
        if (node.metadata().isPresent()) {
            if (!node.metadata().get().annotations().isEmpty()) {
                MappingConstructorExpressionNode annotationValue = getAnnotationValue(node.metadata().get());
                return getSchemaStringFieldFromValue(annotationValue);
            }
        }
        throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MSG_MISSING_ANNOT);
    }

    public static String getSchemaString(ObjectConstructorExpressionNode node) throws SchemaGenerationException {
        if (!node.annotations().isEmpty()) {
            for (AnnotationNode annotationNode: node.annotations()) {
                if (isGraphqlServiceConfig(annotationNode) && annotationNode.annotValue().isPresent()) {
                    return getSchemaStringFieldFromValue(annotationNode.annotValue().get());
                }
            }
        }
        throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MSG_MISSING_ANNOT);
    }

    private static MappingConstructorExpressionNode getAnnotationValue(MetadataNode metadataNode)
            throws SchemaGenerationException {
        for (AnnotationNode annotationNode: metadataNode.annotations()) {
            if (isGraphqlServiceConfig(annotationNode) && annotationNode.annotValue().isPresent()) {
                return annotationNode.annotValue().get();
            }
        }
        throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MSG_MISSING_SERVICE_CONFIG);
    }

    private static String getSchemaStringFieldFromValue(MappingConstructorExpressionNode annotationValue)
            throws SchemaGenerationException {
        SeparatedNodeList<MappingFieldNode> existingFields = annotationValue.fields();
        for (MappingFieldNode field : existingFields) {
            if (field.children().get(0).toString().contains(SCHEMA_STRING_FIELD)) {
                String schemaString = field.children().get(2).toString();
                return schemaString.substring(1, schemaString.length() - 1);
            }
        }
        throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MSG_MISSING_FIELD_SCHEMA_STR);
    }

    private static boolean isGraphqlServiceConfig(AnnotationNode annotationNode) {
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
        //Can be use method used in http path validation
        if (serviceName.isBlank() || serviceName.equals(SLASH) || serviceName.startsWith(SLASH + HYPHEN)) {
            String[] fileName = serviceName.split(SLASH);
            // This condition is to handle `service on ep1 {} ` multiple scenarios
            if (fileName.length > 0 && !serviceName.isBlank()) {
                sdlFileName = FilenameUtils.removeExtension(servicePath) + fileName[1];
            } else {
                sdlFileName = FilenameUtils.removeExtension(servicePath);
            }
        } else if (serviceName.startsWith(HYPHEN)) {
            // serviceName -> service on ep1 {} has multiple service ex: "-33456"
            sdlFileName = FilenameUtils.removeExtension(servicePath) + serviceName;
        } else {
            // Remove starting path separate if exists
            if (serviceName.startsWith(SLASH)) {
                serviceName = serviceName.substring(1);
            }
            // Replace rest of the path separators with underscore
            sdlFileName = serviceName.replaceAll(SLASH, "_");
        }
        sdlFileName =  getNormalizedFileName(sdlFileName);
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

    public static Schema getDecodedSchema(String schemaString) throws SchemaGenerationException {
        if (schemaString == null || schemaString.isBlank() || schemaString.isEmpty()) {
            throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MSG_INVALID_SCHEMA_STR);
        }
        byte[] decodedString = Base64.getDecoder().decode(schemaString.getBytes(StandardCharsets.UTF_8));
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(decodedString);
            ObjectInputStream inputStream = new ObjectInputStream(byteStream);
            return (Schema) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_102, null, MSG_CANNOT_READ_SCHEMA_STR);
        }
    }

    /**
     * This method use for checking the duplicate files.
     *
     * @param outPath     output path for file generated
     * @param schemaName given file name
     * @return file name with duplicate number tag
     */
    public static String resolveSchemaFileName(Path outPath, String schemaName) {
        if (outPath != null && Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                schemaName = checkAvailabilityOfGivenName(schemaName, listFiles);
            }
        }
        return schemaName;
    }

    private static String checkAvailabilityOfGivenName(String schemaName, File[] listFiles) {
        for (File file : listFiles) {
            if (System.console() != null && file.getName().equals(schemaName)) {
                String userInput = System.console().readLine("There is already a file named ' " + file.getName() +
                        "' in the target location. Do you want to overwrite the file? [y/N] ");
                if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                    schemaName = setGeneratedFileName(listFiles, schemaName);
                }
            }
        }
        return schemaName;
    }

    /**
     * This method for setting the file name for generated file.
     *
     * @param listFiles      generated files
     * @param fileName       File name
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName) {
        int duplicateCount = 0;
        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2)
                    && (listFileName.split("\\.")[0]
                    .equals(fileName.split("\\.")[0]))) {
                duplicateCount++;
            }
        }
        return fileName.split("\\.")[0] + "_" + duplicateCount + GRAPHQL_EXTENSION;
    }

    public static void writeFile(Path filePath, String content) throws SchemaGenerationException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_103, null, e.toString());
        }
    }

    /**
     * This {@code NullLocation} represents the null location allocation for scenarios which has no location.
     */
    public static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
