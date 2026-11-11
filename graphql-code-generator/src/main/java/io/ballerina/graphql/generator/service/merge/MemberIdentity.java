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

package io.ballerina.graphql.generator.service.merge;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Says what identifies a member of a source file, a service or a class, so that an existing member and a generated
 * one are matched the same way by every merger. A resolver is identified by its key, and any other member by its
 * name, so a member is still matched after the developer moves or edits it.
 */
final class MemberIdentity {

    private static final String KEY_SEPARATOR = " ";
    private static final String REMOTE_KEY_PREFIX = "remote ";
    private static final String PATH_PARAMETER_START = "[";
    private static final String PATH_PARAMETER_END = "]";

    private MemberIdentity() {
    }

    /**
     * Returns the key of a resolver: its accessor and path for a resource function, e.g. {@code get books}, or its
     * name for a remote function, e.g. {@code remote addBook}.
     *
     * @param member a member of a service or a class
     * @return the resolver key, or empty if the member is not a resolver
     */
    static Optional<String> resolverKey(Node member) {
        if (!(member instanceof FunctionDefinitionNode function)) {
            return Optional.empty();
        }
        if (hasQualifier(function, SyntaxKind.RESOURCE_KEYWORD)) {
            String path = function.relativeResourcePath().stream()
                    .map(MemberIdentity::pathSegment)
                    .collect(Collectors.joining());
            return Optional.of(function.functionName().text() + KEY_SEPARATOR + path);
        }
        if (hasQualifier(function, SyntaxKind.REMOTE_KEYWORD)) {
            return Optional.of(REMOTE_KEY_PREFIX + function.functionName().text());
        }
        return Optional.empty();
    }

    /**
     * Returns the name of a member that is not a resolver, such as a type, a class, a function, a field or a type
     * inclusion. Module-level declarations share one namespace, so a generated type replaces an existing declaration
     * of the same name whatever its kind.
     *
     * @param member a module-level declaration, or a member of a service or a class
     * @return the member's name, or its source when it has no name
     */
    static String name(Node member) {
        if (member instanceof FunctionDefinitionNode function) {
            return function.functionName().text();
        }
        if (member instanceof TypeDefinitionNode type) {
            return type.typeName().text();
        }
        if (member instanceof EnumDeclarationNode enumDeclaration) {
            return enumDeclaration.identifier().text();
        }
        if (member instanceof ClassDefinitionNode classDefinition) {
            return classDefinition.className().text();
        }
        if (member instanceof ConstantDeclarationNode constant) {
            return constant.variableName().text();
        }
        if (member instanceof ModuleVariableDeclarationNode variable) {
            return variable.typedBindingPattern().bindingPattern().toSourceCode().strip();
        }
        if (member instanceof ListenerDeclarationNode listener) {
            return listener.variableName().text();
        }
        if (member instanceof ObjectFieldNode field) {
            return field.fieldName().text();
        }
        if (member instanceof TypeReferenceNode inclusion) {
            return inclusion.typeName().toSourceCode().strip();
        }
        return member.toSourceCode().strip();
    }

    // A path parameter is keyed by its type only, e.g. "[string]", so renaming it does not make the resolver look new.
    private static String pathSegment(Node segment) {
        if (segment instanceof ResourcePathParameterNode parameter) {
            String rest = parameter.ellipsisToken().map(Token::text).orElse("");
            return PATH_PARAMETER_START + parameter.typeDescriptor().toSourceCode().strip() + rest
                    + PATH_PARAMETER_END;
        }
        return segment.toSourceCode().strip();
    }

    private static boolean hasQualifier(FunctionDefinitionNode function, SyntaxKind qualifier) {
        return function.qualifierList().stream().anyMatch(token -> token.kind() == qualifier);
    }
}
