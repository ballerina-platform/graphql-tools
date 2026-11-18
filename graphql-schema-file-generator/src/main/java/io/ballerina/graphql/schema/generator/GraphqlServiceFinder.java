/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.graphql.schema.generator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.graphql.schema.SchemaProject;
import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.stdlib.graphql.commons.utils.TypeUtils.removeEscapeCharacter;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.PACKAGE_NAME;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.hasGraphqlListener;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.isGraphQLServiceObjectDeclaration;

/**
 * Finds the GraphQL services in a compiled Ballerina file and reads the encoded schema of each one.
 */
final class GraphqlServiceFinder {

    private static final String ROOT_BASE_PATH = "/";
    private static final String NO_BASE_PATH = "";
    private static final String SCHEMA_STRING_FIELD = "schemaString";
    private static final String SERVICE_CONFIG_IDENTIFIER = "ServiceConfig";
    private static final String ERROR_MISSING_ANNOTATION = "Annotation is missing in GraphQL service";
    private static final String ERROR_MISSING_SERVICE_CONFIG = "GraphQL SchemaConfig annotation is missing";
    private static final String ERROR_MISSING_SCHEMA_STRING =
            "'schemaString' field is missing in GraphQL ServiceConfig";

    /**
     * A GraphQL service found in the file.
     *
     * @param basePath      the base path, empty for the root path and for a service object
     * @param encodedSchema the encoded schema the GraphQL compiler plugin added to the service
     * @param serviceObject whether the service is a service object assigned to a module-level variable
     */
    record GraphqlService(String basePath, String encodedSchema, boolean serviceObject) {
    }

    private GraphqlServiceFinder() {
    }

    /**
     * Finds the GraphQL services in the order they are declared in the file.
     *
     * @param project the compiled Ballerina file
     * @return the GraphQL services
     * @throws SchemaGenerationException if a service has no encoded schema
     */
    static List<GraphqlService> find(SchemaProject project) throws SchemaGenerationException {
        List<GraphqlService> services = new ArrayList<>();
        ModulePartNode modulePartNode = project.syntaxTree().rootNode();
        for (Node member : modulePartNode.members()) {
            if (member.kind() == SyntaxKind.SERVICE_DECLARATION) {
                ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) member;
                if (isGraphqlService(serviceNode, project.semanticModel())) {
                    services.add(new GraphqlService(getBasePath(serviceNode), getSchemaString(serviceNode), false));
                }
            } else if (member.kind() == SyntaxKind.MODULE_VAR_DECL) {
                Optional<ObjectConstructorExpressionNode> serviceObject =
                        getServiceObject((ModuleVariableDeclarationNode) member);
                if (serviceObject.isPresent()) {
                    services.add(new GraphqlService(NO_BASE_PATH, getSchemaString(serviceObject.get()), true));
                }
            }
        }
        return services;
    }

    /**
     * Formats a base path the same way the base paths of the found services are formatted.
     *
     * @param basePath the base path
     * @return an empty string for the root path, otherwise the base path as it is
     */
    static String formatBasePath(String basePath) {
        if (ROOT_BASE_PATH.equals(basePath)) {
            return NO_BASE_PATH;
        }
        return basePath;
    }

    private static boolean isGraphqlService(ServiceDeclarationNode serviceNode, SemanticModel semanticModel) {
        Optional<Symbol> symbol = semanticModel.symbol(serviceNode);
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.SERVICE_DECLARATION) {
            return false;
        }
        return hasGraphqlListener((ServiceDeclarationSymbol) symbol.get());
    }

    private static Optional<ObjectConstructorExpressionNode> getServiceObject(
            ModuleVariableDeclarationNode variableNode) {
        if (!isGraphQLServiceObjectDeclaration(variableNode) || variableNode.initializer().isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode initializer = variableNode.initializer().get();
        if (initializer.kind() != SyntaxKind.OBJECT_CONSTRUCTOR) {
            return Optional.empty();
        }
        return Optional.of((ObjectConstructorExpressionNode) initializer);
    }

    private static String getBasePath(ServiceDeclarationNode serviceNode) {
        StringBuilder basePath = new StringBuilder();
        for (Node pathNode : serviceNode.absoluteResourcePath()) {
            basePath.append(removeEscapeCharacter(pathNode.toString()));
        }
        return formatBasePath(basePath.toString().trim());
    }

    private static String getSchemaString(ServiceDeclarationNode serviceNode) throws SchemaGenerationException {
        Optional<MetadataNode> metadata = serviceNode.metadata();
        if (metadata.isPresent() && !metadata.get().annotations().isEmpty()) {
            for (AnnotationNode annotationNode : metadata.get().annotations()) {
                if (isGraphqlServiceConfig(annotationNode) && annotationNode.annotValue().isPresent()) {
                    return getSchemaStringField(annotationNode.annotValue().get());
                }
            }
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null,
                    ERROR_MISSING_SERVICE_CONFIG);
        }
        throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null, ERROR_MISSING_ANNOTATION);
    }

    private static String getSchemaString(ObjectConstructorExpressionNode serviceObject)
            throws SchemaGenerationException {
        for (AnnotationNode annotationNode : serviceObject.annotations()) {
            if (isGraphqlServiceConfig(annotationNode) && annotationNode.annotValue().isPresent()) {
                return getSchemaStringField(annotationNode.annotValue().get());
            }
        }
        throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null, ERROR_MISSING_ANNOTATION);
    }

    private static String getSchemaStringField(MappingConstructorExpressionNode annotationValue)
            throws SchemaGenerationException {
        for (MappingFieldNode field : annotationValue.fields()) {
            if (field.children().get(0).toString().contains(SCHEMA_STRING_FIELD)) {
                String schemaString = field.children().get(2).toString();
                return schemaString.substring(1, schemaString.length() - 1);
            }
        }
        throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_102, null,
                ERROR_MISSING_SCHEMA_STRING);
    }

    private static boolean isGraphqlServiceConfig(AnnotationNode annotationNode) {
        if (annotationNode.annotReference().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return false;
        }
        QualifiedNameReferenceNode referenceNode = (QualifiedNameReferenceNode) annotationNode.annotReference();
        return PACKAGE_NAME.equals(referenceNode.modulePrefix().text())
                && SERVICE_CONFIG_IDENTIFIER.equals(referenceNode.identifier().text());
    }
}
