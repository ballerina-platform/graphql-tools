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

package io.ballerina.graphql.schema.generator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.schema.diagnostic.DiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.stdlib.graphql.commons.types.Schema;
import io.ballerina.stdlib.graphql.commons.utils.SdlSchemaStringGenerator;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.schema.Constants.EMPTY_STRING;
import static io.ballerina.graphql.schema.Constants.PERIOD;
import static io.ballerina.graphql.schema.utils.Utils.createOutputDirectory;
import static io.ballerina.graphql.schema.utils.Utils.formatBasePath;
import static io.ballerina.graphql.schema.utils.Utils.getDecodedSchema;
import static io.ballerina.graphql.schema.utils.Utils.getSchemaString;
import static io.ballerina.graphql.schema.utils.Utils.getSdlFileName;
import static io.ballerina.graphql.schema.utils.Utils.getServiceBasePath;
import static io.ballerina.graphql.schema.utils.Utils.isGraphqlService;
import static io.ballerina.graphql.schema.utils.Utils.resolveSchemaFileName;
import static io.ballerina.graphql.schema.utils.Utils.writeFile;
import static io.ballerina.stdlib.graphql.commons.utils.Utils.isGraphQLServiceObjectDeclaration;

/**
 * This class implements the GraphQL SDL schema generation.
 */
public class SdlSchemaGenerator {

    private static PrintStream outStream = System.out;

    /**
     * Export the SDL schema for given Ballerina GraphQL services.
     */
    public static void generate(Path filePath, Path outPath, String serviceName)
            throws SchemaGenerationException {
        Project project = ProjectLoader.loadProject(filePath);
        PackageCompilation compilation = getPackageCompilation(project);
        Package packageName = project.currentPackage();
        DocumentId docId;
        Document doc;
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(filePath);
            ModuleId moduleId = docId.moduleId();
            doc = project.currentPackage().module(moduleId).document(docId);
        } else {
            Module currentModule = packageName.getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
            docId = documentIterator.next();
            doc = currentModule.document(docId);
        }

        SyntaxTree syntaxTree = doc.syntaxTree();
        SemanticModel semanticModel = compilation.getSemanticModel(docId.moduleId());
        List<SdlSchema> schemaDefinitions = generateSdlSchema(syntaxTree, semanticModel, serviceName);
        List<String> fileNames = new ArrayList<>();
        for (SdlSchema definition : schemaDefinitions) {
            String fileName = resolveSchemaFileName(outPath, definition.getName());
            createOutputDirectory(outPath);
            writeFile(outPath.resolve(fileName), definition.getSchema());
            fileNames.add(fileName);
        }
        if (fileNames.isEmpty()) {
            return;
        }
        outStream.println("SDL Schema(s) generated successfully and copy to :");
        Iterator<String> iterator = fileNames.iterator();
        while (iterator.hasNext()) {
            outStream.println("-- " + iterator.next());
        }
    }

    /**
     * Generate a List of SdlSchema objects for given GraphQL services.
     */
    private static List<SdlSchema> generateSdlSchema(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                     String serviceName) throws SchemaGenerationException {
        Map<String, String> servicesToGenerate = new HashMap<>();
        List<String> availableServices = new ArrayList<>();
        List<SdlSchema> outputs = new ArrayList<>();

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        extractSchemaStringsFromServices(serviceName, modulePartNode, semanticModel, availableServices,
                servicesToGenerate);
        // If there are no services found for a given service name.
        if (serviceName != null && servicesToGenerate.isEmpty()) {
            throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_101, null, serviceName,
                    availableServices.toString());
        }
        // Generating schema for selected services
        for (Map.Entry<String, String> schema : servicesToGenerate.entrySet()) {
            String sdlFileName = getSdlFileName(syntaxTree.filePath(), schema.getKey());
            Schema schemaObject = getDecodedSchema(schema.getValue());
            String sdlSchemaString = SdlSchemaStringGenerator.generate(schemaObject);
            SdlSchema sdlSchema = new SdlSchema(sdlFileName, sdlSchemaString);
            outputs.add(sdlSchema);
        }
        return outputs;
    }

    /**
     * Filter the GraphQL schemas from the service node.
     * This method not filter services declared as module-level variables,
     * since the service base path info is not included in node.
     * Hence, extract schemas for all the GraphQL services with variable declaration.
     */
    public static void extractSchemaStringsFromServices(String serviceName, ModulePartNode modulePartNode,
                                                        SemanticModel semanticModel, List<String> availableServices,
                                                        Map<String, String> schemasToGenerate)
                                                        throws SchemaGenerationException {

        int duplicateCount = 0;
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
                if (isGraphqlService(serviceNode, semanticModel)) {
                    String service = getServiceBasePath(serviceNode);
                    String schema = getSchemaString(serviceNode);
                    String updatedServiceName = service;
                    if (schemasToGenerate.containsKey(service)) {
                        duplicateCount += 1;
                        updatedServiceName = getUpdatedServiceName(service, duplicateCount);
                    }
                    addToList(serviceName, service, updatedServiceName, schema, availableServices, schemasToGenerate);
                }
            } else if (syntaxKind.equals(SyntaxKind.MODULE_VAR_DECL)) {
                ModuleVariableDeclarationNode moduleVariableNode = (ModuleVariableDeclarationNode) node;
                if (!isGraphQLServiceObjectDeclaration(moduleVariableNode)) {
                    continue;
                }
                if (moduleVariableNode.initializer().isEmpty()) {
                    continue;
                }
                ExpressionNode expressionNode = moduleVariableNode.initializer().get();
                if (expressionNode.kind() == SyntaxKind.OBJECT_CONSTRUCTOR) {
                    ObjectConstructorExpressionNode graphqlServiceObject =
                            (ObjectConstructorExpressionNode) moduleVariableNode.initializer().get();
                    String schema = getSchemaString(graphqlServiceObject);
                    String service = EMPTY_STRING;
                    if (schemasToGenerate.containsKey(service)) {
                        duplicateCount += 1;
                        service = getUpdatedServiceName(service, duplicateCount);
                    }
                    schemasToGenerate.put(service, schema);
                }
            }
        }
    }

    /**
     * Filter schemas by given base path.
     */
    private static void addToList(String serviceName, String service, String updateServiceName, String schema,
                                  List<String> availableServices, Map<String, String> schemasToGenerate) {
        if (serviceName != null) {
            availableServices.add(service);
            if (formatBasePath(serviceName).equals(service)) {
                schemasToGenerate.put(updateServiceName, schema);
            }
        } else {
            schemasToGenerate.put(updateServiceName, schema);
        }
    }

    /**
     * Update the duplicate service names.
     */
    private static String getUpdatedServiceName(String serviceName, int duplicateCount) {
        if (serviceName.isBlank()) {
            return PERIOD + duplicateCount;
        } else {
            return serviceName + PERIOD + duplicateCount;
        }
    }

    /**
     * Get the compilation of given Ballerina source.
     */
    private static PackageCompilation getPackageCompilation(Project project) throws SchemaGenerationException {
        DiagnosticResult diagnosticResult = project.currentPackage().runCodeGenAndModifyPlugins();
        boolean hasErrors = diagnosticResult
                .diagnostics().stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        if (!hasErrors) {
            PackageCompilation compilation = project.currentPackage().getCompilation();
            hasErrors = compilation.diagnosticResult()
                    .diagnostics().stream()
                    .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
            if (!hasErrors) {
                return compilation;
            }
        }
        throw new SchemaGenerationException(DiagnosticMessages.SDL_SCHEMA_100, null);
    }
}
