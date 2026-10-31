/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd.generator;

import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Reads the top-level names a module already defines, so generated code does not redefine them.
 */
final class ModuleDefinitions {

    private static final String BAL_EXTENSION = ".bal";
    private static final String QUOTED_IDENTIFIER_PREFIX = "'";
    private static final String ERROR_UNREADABLE_SOURCE = "Could not read \"%s\".";

    private ModuleDefinitions() {
    }

    static Map<String, String> read(Path moduleDirectory, Set<String> skippedFiles) throws GenerationException {
        Map<String, String> definitions = new HashMap<>();
        if (!Files.isDirectory(moduleDirectory)) {
            return definitions;
        }
        for (Path source : sources(moduleDirectory, skippedFiles)) {
            String content;
            try {
                content = Files.readString(source);
            } catch (IOException e) {
                throw new GenerationException(String.format(ERROR_UNREADABLE_SOURCE, source));
            }
            ModulePartNode modulePart = SyntaxTree.from(TextDocuments.from(content)).rootNode();
            for (ModuleMemberDeclarationNode member : modulePart.members()) {
                names(member).forEach(name -> definitions.putIfAbsent(unquote(name),
                        source.getFileName().toString()));
            }
        }
        return definitions;
    }

    private static List<Path> sources(Path moduleDirectory, Set<String> skippedFiles) throws GenerationException {
        try (Stream<Path> files = Files.list(moduleDirectory)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(BAL_EXTENSION))
                    .filter(file -> !skippedFiles.contains(file.getFileName().toString()))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new GenerationException(String.format(ERROR_UNREADABLE_SOURCE, moduleDirectory));
        }
    }

    private static List<String> names(ModuleMemberDeclarationNode member) {
        if (member instanceof TypeDefinitionNode type) {
            return List.of(type.typeName().text());
        }
        if (member instanceof ClassDefinitionNode classDefinition) {
            return List.of(classDefinition.className().text());
        }
        if (member instanceof FunctionDefinitionNode function) {
            return List.of(function.functionName().text());
        }
        if (member instanceof ConstantDeclarationNode constant) {
            return List.of(constant.variableName().text());
        }
        if (member instanceof ListenerDeclarationNode listener) {
            return List.of(listener.variableName().text());
        }
        if (member instanceof EnumDeclarationNode enumDeclaration) {
            // Enum members are module-level constants, so they are taken names too.
            return Stream.concat(Stream.of(enumDeclaration.identifier().text()),
                    enumDeclaration.enumMemberList().stream()
                            .filter(EnumMemberNode.class::isInstance)
                            .map(enumMember -> ((EnumMemberNode) enumMember).identifier().text()))
                    .toList();
        }
        if (member instanceof ModuleVariableDeclarationNode variable
                && variable.typedBindingPattern().bindingPattern() instanceof CaptureBindingPatternNode capture) {
            return List.of(capture.variableName().text());
        }
        return List.of();
    }

    private static String unquote(String name) {
        return name.startsWith(QUOTED_IDENTIFIER_PREFIX) ? name.substring(QUOTED_IDENTIFIER_PREFIX.length()) : name;
    }
}
