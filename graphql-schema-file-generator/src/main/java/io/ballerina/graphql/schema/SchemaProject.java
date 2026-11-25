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
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The compiled Ballerina source that the SDL schemas are generated from: a standalone Ballerina file, the default
 * module of a package, or one module of a package.
 *
 * @param documents the Ballerina files of the source, in file name order
 */
public record SchemaProject(List<SchemaDocument> documents) {

    private static final String ERROR_MISSING_FILE = "Provided Ballerina file path does not exist";
    private static final String ERROR_CANNOT_READ_FILE = "Cannot read provided Ballerina file (Permission denied)";
    private static final String ERROR_FILE_IN_PACKAGE =
            "\"%s\" is part of the package at \"%s\". Pass the package directory or a module directory instead.";
    private static final String ERROR_NOT_PACKAGE_OR_MODULE = "\"%s\" is not a Ballerina package or module directory.";

    /**
     * A compiled Ballerina file of the source.
     *
     * @param syntaxTree    the syntax tree of the file
     * @param semanticModel the semantic model of the module that holds the file
     */
    public record SchemaDocument(SyntaxTree syntaxTree, SemanticModel semanticModel) {
    }

    /**
     * Loads and compiles the given input. A standalone Ballerina file is taken as it is, a package directory gives its
     * default module, and a directory under the modules directory of a package gives that module.
     *
     * @param inputPath the path of a standalone Ballerina file, a package directory or a module directory
     * @return the compiled source
     * @throws SchemaGenerationException if the input cannot be read, is a file inside a package, is a directory that is
     *                                   not a package or module, or has compilation errors
     */
    public static SchemaProject load(Path inputPath) throws SchemaGenerationException {
        Path path = toReadablePath(inputPath);
        if (!Files.isDirectory(path)) {
            return loadFile(inputPath, path);
        }
        if (Files.isRegularFile(path.resolve(ProjectConstants.BALLERINA_TOML))) {
            Project project = loadProject(path);
            return fromModule(project, compile(project), project.currentPackage().getDefaultModule().moduleId());
        }
        if (isModuleDirectory(path)) {
            Project project = loadProject(path.getParent().getParent());
            Optional<ModuleId> moduleId = findModule(project, path.getFileName().toString());
            if (moduleId.isPresent()) {
                return fromModule(project, compile(project), moduleId.get());
            }
        }
        throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_104, null,
                String.format(ERROR_NOT_PACKAGE_OR_MODULE, inputPath));
    }

    private static SchemaProject loadFile(Path inputPath, Path filePath) throws SchemaGenerationException {
        Project project = loadProject(filePath);
        if (project.kind() == ProjectKind.BUILD_PROJECT) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_104, null,
                    String.format(ERROR_FILE_IN_PACKAGE, inputPath, project.sourceRoot()));
        }
        return fromModule(project, compile(project), project.currentPackage().getDefaultModule().moduleId());
    }

    private static Path toReadablePath(Path inputPath) throws SchemaGenerationException {
        File input = inputPath.toFile();
        if (!input.exists()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, ERROR_MISSING_FILE);
        }
        if (!input.canRead()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null,
                    ERROR_CANNOT_READ_FILE);
        }
        try {
            return Paths.get(input.getCanonicalPath());
        } catch (IOException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, e.toString());
        }
    }

    private static boolean isModuleDirectory(Path directory) {
        Path modulesDirectory = directory.getParent();
        if (modulesDirectory == null || modulesDirectory.getParent() == null
                || !ProjectConstants.MODULES_ROOT.equals(modulesDirectory.getFileName().toString())) {
            return false;
        }
        return Files.isRegularFile(modulesDirectory.getParent().resolve(ProjectConstants.BALLERINA_TOML));
    }

    private static Optional<ModuleId> findModule(Project project, String moduleDirectoryName) {
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            if (moduleDirectoryName.equals(module.moduleName().moduleNamePart())) {
                return Optional.of(moduleId);
            }
        }
        return Optional.empty();
    }

    private static Project loadProject(Path path) throws SchemaGenerationException {
        try {
            return ProjectLoader.loadProject(path);
        } catch (ProjectException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, e.getMessage());
        }
    }

    // The module is read from the package after compiling, since the GraphQL compiler plugin adds the encoded
    // schemas to the syntax trees while compiling.
    private static SchemaProject fromModule(Project project, PackageCompilation compilation, ModuleId moduleId) {
        Module module = project.currentPackage().module(moduleId);
        SemanticModel semanticModel = compilation.getSemanticModel(moduleId);
        List<SchemaDocument> documents = module.documentIds().stream()
                .map(module::document)
                .sorted(Comparator.comparing(Document::name))
                .map(document -> new SchemaDocument(document.syntaxTree(), semanticModel))
                .toList();
        return new SchemaProject(documents);
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
