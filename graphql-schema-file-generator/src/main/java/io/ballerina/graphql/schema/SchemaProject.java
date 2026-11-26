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
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The compiled Ballerina source that the SDL schemas are generated from: a standalone Ballerina file, the default
 * module of a package, or one module of a package.
 *
 * @param documents the Ballerina files of the source, in file name order
 */
public record SchemaProject(List<SchemaDocument> documents) {

    private static final String ERROR_MISSING_FILE = "\"%s\" does not exist.";
    private static final String ERROR_INVALID_BALLERINA_TOML = "\"%s\" is not valid: %s";
    private static final String ERROR_SEPARATOR = "; ";
    private static final String ERROR_CANNOT_READ_FILE = "Cannot read provided Ballerina file (Permission denied)";
    private static final String ERROR_CANNOT_READ = "Cannot read \"%s\".";
    private static final String PARENT_DIRECTORY = "..";
    private static final String CURRENT_DIRECTORY = ".";
    private static final String ERROR_FILE_IN_PACKAGE =
            "\"%s\" is part of the package at \"%s\". Pass the package directory or a module directory instead.";
    private static final String ERROR_NOT_PACKAGE_OR_MODULE = "\"%s\" is not a Ballerina package or module directory.";
    private static final String ERROR_WORKSPACE_ROOT =
            "\"%s\" is a Ballerina workspace. Pass the directory of one of its packages instead.";
    private static final String ERROR_NO_PACKAGE_TABLE =
            "\"%s\" is not a Ballerina package. Its Ballerina.toml has no [package] table.";
    private static final String ERROR_DEPENDENCIES_NOT_LOCKED = "Dependencies are not locked for the package "
            + "\"%s\". Run 'bal build' to lock the dependencies, then re-run. Try: bal build \"%s\"";
    private static final String ERROR_LINKED_MODULE = "\"%s\" is a symbolic link, and the Ballerina compiler does "
            + "not read a module directory that is a symbolic link. Replace the link with the module directory.";
    private static final String ERROR_TEST_FILE =
            "\"%s\" is a test file. Schema generation does not support test services.";
    private static final String PACKAGE_TABLE = "package";
    // The versions in Dependencies.toml are used as they are, and are never updated (CMP-1).
    private static final BuildOptions BUILD_OPTIONS =
            BuildOptions.builder().setLockingMode(PackageLockingMode.HARD).build();

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
     * default module, and a directory under the modules directory of a package gives that module. A package that
     * belongs to a workspace is loaded through the workspace.
     *
     * @param inputPath      the path of a standalone Ballerina file, a package directory or a module directory
     * @param warningHandler receives the warnings about the dependencies, such as a Dependencies.toml written by an
     *                       older distribution
     * @return the compiled source
     * @throws SchemaGenerationException if the input cannot be read, is a file inside a package, is a workspace root,
     *                                   is a directory that is not a package or module, is a package whose
     *                                   dependencies are not locked, or has compilation errors
     */
    public static SchemaProject load(Path inputPath, Consumer<String> warningHandler)
            throws SchemaGenerationException {
        Path path = toReadablePath(inputPath);
        Project project = loadProject(inputPath, path);
        return switch (project.kind()) {
            // A standalone file has no Dependencies.toml, so it uses the packages in the distribution.
            case SINGLE_FILE_PROJECT -> fromModule(project, compile(project, warningHandler),
                    project.currentPackage().getDefaultModule().moduleId());
            case BUILD_PROJECT -> fromPackage(inputPath, path, project, warningHandler);
            case WORKSPACE_PROJECT -> fromPackage(inputPath, path, findWorkspaceMember(inputPath, path,
                    (WorkspaceProject) project), warningHandler);
            default -> throw invalidTarget(String.format(ERROR_NOT_PACKAGE_OR_MODULE, inputPath));
        };
    }

    private static SchemaProject fromPackage(Path inputPath, Path path, Project project,
                                             Consumer<String> warningHandler) throws SchemaGenerationException {
        ModuleId moduleId = findTargetModule(inputPath, path, project);
        // The HARD locking mode takes the latest compatible versions when nothing is locked, so the result could
        // differ between runs without a Dependencies.toml.
        if (project.currentPackage().dependenciesToml().isEmpty()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null,
                    String.format(ERROR_DEPENDENCIES_NOT_LOCKED, project.currentPackage().packageName().value(),
                            project.sourceRoot()));
        }
        // In a workspace, the sibling packages the package imports are compiled with it, so their test sources are
        // removed too, or an error in them would stop the generation.
        if (project.workspaceProject().isPresent()) {
            project.workspaceProject().get().projects().forEach(SchemaProject::removeTestDocuments);
        } else {
            removeTestDocuments(project);
        }
        return fromModule(project, compile(project, warningHandler), moduleId);
    }

    // Test services are not supported, so the test sources are removed from the loaded package before compiling, and
    // an error in them does not stop the generation. Only the loaded package changes, the files stay as they are.
    private static void removeTestDocuments(Project project) {
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            if (module.testDocumentIds().isEmpty()) {
                continue;
            }
            Module.Modifier modifier = module.modify();
            for (DocumentId testDocumentId : module.testDocumentIds()) {
                modifier = modifier.removeDocument(testDocumentId);
            }
            modifier.apply();
        }
    }

    private static boolean isTestDocument(Project project, Path filePath) {
        try {
            DocumentId documentId = project.documentId(filePath);
            return project.currentPackage().moduleIds().stream()
                    .anyMatch(moduleId -> project.currentPackage().module(moduleId).testDocumentIds()
                            .contains(documentId));
        } catch (ProjectException e) {
            return false;
        }
    }

    // The loader gives the same package for its root, its modules directory, a module and a file inside it, so the
    // given path is compared with the package root to tell them apart.
    private static ModuleId findTargetModule(Path inputPath, Path path, Project project)
            throws SchemaGenerationException {
        // The errors in Ballerina.toml are reported first, since they also cause the missing [package] table and the
        // missing lock file errors below.
        List<String> tomlErrors = project.currentPackage().manifest().diagnostics().errors().stream()
                .map(Diagnostic::message)
                .toList();
        if (!tomlErrors.isEmpty()) {
            throw invalidTarget(String.format(ERROR_INVALID_BALLERINA_TOML,
                    project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML), String.join(ERROR_SEPARATOR,
                            tomlErrors)));
        }
        if (!hasPackageTable(project)) {
            throw invalidTarget(String.format(ERROR_NO_PACKAGE_TABLE, inputPath));
        }
        Path packageRoot = project.sourceRoot();
        if (!Files.isDirectory(path)) {
            if (isTestDocument(project, path)) {
                throw invalidTarget(String.format(ERROR_TEST_FILE, inputPath));
            }
            throw invalidTarget(String.format(ERROR_FILE_IN_PACKAGE, inputPath, packageRoot));
        }
        if (path.equals(packageRoot)) {
            return project.currentPackage().getDefaultModule().moduleId();
        }
        if (packageRoot.resolve(ProjectConstants.MODULES_ROOT).equals(path.getParent())) {
            Optional<ModuleId> moduleId = findModule(project, path.getFileName().toString());
            // The compiler does not read the files of a module directory that is a link, so the module would be empty.
            if (moduleId.isPresent() && Files.isSymbolicLink(path)) {
                throw invalidTarget(String.format(ERROR_LINKED_MODULE, inputPath));
            }
            if (moduleId.isPresent()) {
                return moduleId.get();
            }
        }
        throw invalidTarget(String.format(ERROR_NOT_PACKAGE_OR_MODULE, inputPath));
    }

    // The loader gives the workspace for its root and for anything inside one of its packages, so the package is
    // taken from the workspace, which lets the package API resolve the sibling packages it imports.
    private static Project findWorkspaceMember(Path inputPath, Path path, WorkspaceProject workspace)
            throws SchemaGenerationException {
        for (BuildProject member : workspace.projects()) {
            if (path.startsWith(member.sourceRoot())) {
                return member;
            }
        }
        if (path.equals(workspace.sourceRoot())) {
            throw invalidTarget(String.format(ERROR_WORKSPACE_ROOT, inputPath));
        }
        throw invalidTarget(String.format(ERROR_NOT_PACKAGE_OR_MODULE, inputPath));
    }

    private static boolean hasPackageTable(Project project) {
        return project.currentPackage().ballerinaToml()
                .map(ballerinaToml -> ballerinaToml.tomlAstNode().entries().containsKey(PACKAGE_TABLE))
                .orElse(false);
    }

    private static SchemaGenerationException invalidTarget(String message) {
        return new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_104, null, message);
    }

    // The parent is resolved to its real path, so links and ".." in it are followed the way the file system follows
    // them. The last part is kept as given only when it is a linked module directory, so its modules/<name> layout
    // can be detected and reported. Any other link, such as a linked package directory, is resolved, since the
    // compiler does not read the files of a linked directory.
    private static Path toReadablePath(Path inputPath) throws SchemaGenerationException {
        File input = inputPath.toFile();
        if (!input.exists()) {
            throw invalidTarget(String.format(ERROR_MISSING_FILE, inputPath));
        }
        if (!input.canRead()) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null,
                    ERROR_CANNOT_READ_FILE);
        }
        try {
            Path absolutePath = inputPath.toAbsolutePath();
            Path fileName = absolutePath.getFileName();
            if (absolutePath.getParent() == null || fileName == null || PARENT_DIRECTORY.equals(fileName.toString())
                    || CURRENT_DIRECTORY.equals(fileName.toString())) {
                return absolutePath.toRealPath();
            }
            Path path = absolutePath.getParent().toRealPath().resolve(fileName);
            if (Files.isSymbolicLink(path) && !isInModulesDirectory(path)) {
                return path.toRealPath();
            }
            return path;
        } catch (IOException e) {
            throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null, e.toString());
        }
    }

    private static boolean isInModulesDirectory(Path path) {
        Path parent = path.getParent();
        return parent != null && ProjectConstants.MODULES_ROOT.equals(String.valueOf(parent.getFileName()));
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

    private static Project loadProject(Path inputPath, Path path) throws SchemaGenerationException {
        try {
            return ProjectLoader.load(path, BUILD_OPTIONS).project();
        } catch (ProjectException e) {
            if (!Files.isDirectory(path)) {
                throw invalidTarget(e.getMessage());
            }
            Optional<Path> packageRoot = findPackageRoot(path);
            if (packageRoot.isEmpty()) {
                throw invalidTarget(String.format(ERROR_NOT_PACKAGE_OR_MODULE, inputPath));
            }
            // The loader gives up on a package that has a file it cannot read, and reports the directory as an
            // invalid source file, so the file is looked up to report the actual problem.
            Optional<Path> unreadable = findUnreadable(packageRoot.get());
            if (unreadable.isPresent()) {
                throw new SchemaGenerationException(SchemaDiagnosticMessages.SDL_SCHEMA_103, null,
                        String.format(ERROR_CANNOT_READ, unreadable.get()));
            }
            throw invalidTarget(e.getMessage());
        }
    }

    // A package directory has a Ballerina.toml, and a module directory is modules/<name> of such a directory.
    private static Optional<Path> findPackageRoot(Path directory) {
        if (Files.exists(directory.resolve(ProjectConstants.BALLERINA_TOML))) {
            return Optional.of(directory);
        }
        Path modulesDirectory = directory.getParent();
        if (modulesDirectory != null && modulesDirectory.getParent() != null
                && ProjectConstants.MODULES_ROOT.equals(String.valueOf(modulesDirectory.getFileName()))
                && Files.exists(modulesDirectory.getParent().resolve(ProjectConstants.BALLERINA_TOML))) {
            return Optional.of(modulesDirectory.getParent());
        }
        return Optional.empty();
    }

    private static Optional<Path> findUnreadable(Path packageRoot) {
        try (Stream<Path> paths = Files.walk(packageRoot)) {
            return paths.filter(path -> !Files.isReadable(path)).findFirst();
        } catch (IOException | UncheckedIOException e) {
            return Optional.empty();
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

    // The dependency warnings are passed on before compiling, so they are shown even when the compilation fails
    // (CMP-4).
    private static PackageCompilation compile(Project project, Consumer<String> warningHandler)
            throws SchemaGenerationException {
        project.currentPackage().getResolution().diagnosticResult().warnings()
                .forEach(warning -> warningHandler.accept(warning.toString()));
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
