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

package io.ballerina.graphql.schema;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.schema.diagnostic.SchemaDiagnosticMessages;
import io.ballerina.graphql.schema.exception.SchemaGenerationException;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A compiled Ballerina file that the SDL schema is generated from.
 *
 * @param syntaxTree    the syntax tree of the file
 * @param semanticModel the semantic model of the module that holds the file
 */
public record SchemaProject(SyntaxTree syntaxTree, SemanticModel semanticModel) {

    private static final String ERROR_MISSING_FILE = "Provided Ballerina file path does not exist";
    private static final String ERROR_CANNOT_READ_FILE = "Cannot read provided Ballerina file (Permission denied)";

    /**
     * Checks that the given Ballerina file can be read, then loads and compiles it.
     *
     * @param filePath the path of the Ballerina file
     * @return the compiled file
     * @throws SchemaGenerationException if the file cannot be read or has compilation errors
     */
    public static SchemaProject load(Path filePath) throws SchemaGenerationException {
        Path balFilePath = toReadablePath(filePath);
        Project project = ProjectLoader.loadProject(balFilePath);
        PackageCompilation compilation = compile(project);
        DocumentId documentId = findDocumentId(project, balFilePath);
        Document document = project.currentPackage().module(documentId.moduleId()).document(documentId);
        return new SchemaProject(document.syntaxTree(), compilation.getSemanticModel(documentId.moduleId()));
    }

    private static Path toReadablePath(Path filePath) throws SchemaGenerationException {
        File balFile = filePath.toFile();
        if (!balFile.exists()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, ERROR_MISSING_FILE);
        }
        if (!balFile.canRead()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null,
                    ERROR_CANNOT_READ_FILE);
        }
        try {
            return Paths.get(balFile.getCanonicalPath());
        } catch (IOException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, e.toString());
        }
    }

    private static DocumentId findDocumentId(Project project, Path balFilePath) {
        if (project.kind() == ProjectKind.BUILD_PROJECT) {
            return project.documentId(balFilePath);
        }
        Module defaultModule = project.currentPackage().getDefaultModule();
        return defaultModule.documentIds().iterator().next();
    }

    private static PackageCompilation compile(Project project) throws SchemaGenerationException {
        DiagnosticResult codeGenResult = project.currentPackage().runCodeGenAndModifyPlugins();
        if (!hasErrors(codeGenResult)) {
            PackageCompilation compilation = project.currentPackage().getCompilation();
            if (!hasErrors(compilation.diagnosticResult())) {
                return compilation;
            }
        }
        throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_100, null);
    }

    private static boolean hasErrors(DiagnosticResult diagnosticResult) {
        return diagnosticResult.diagnostics().stream()
                .anyMatch(diagnostic -> DiagnosticSeverity.ERROR.equals(diagnostic.diagnosticInfo().severity()));
    }
}
