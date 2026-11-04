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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.merge.MergeResult.MergedResolver;
import io.ballerina.graphql.generator.service.merge.MergeResult.Outcome;
import io.ballerina.graphql.generator.utils.NullLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Merges a newly generated service into the service the developer already has. The schema is the source of truth:
 * each resolver is matched to its schema field by key, and kept as written while its signature still matches.
 */
public final class ServiceMerger {

    private static final String ERROR_SYNTAX =
            "The existing service has a syntax error at line %d: %s. Fix it before merging.";
    private static final String ERROR_NOT_IN_SCHEMA = "Resolvers not in the schema: %s. Update or delete them.";
    private static final String ERROR_MERGED_SOURCE = "The merged service could not be formatted: %s";
    private static final String KEY_SEPARATOR = " ";
    private static final String REMOTE_KEY_PREFIX = "remote ";
    private static final String LINE_BREAK = "\n";
    private static final String MEMBER_SEPARATOR = "\n\n";
    private static final String QUOTE = "\"";
    private static final String LIST_SEPARATOR = ", ";

    private ServiceMerger() {
    }

    /**
     * Merges the generated service into the existing one. Each resolver of the generated service is kept from the
     * existing service when its signature is unchanged, regenerated when its signature changed, or added when it is
     * new. Any other code in the existing service is kept.
     *
     * @param existingSource  the source the developer already has
     * @param generatedSource the newly generated source
     * @return the merged source and what happened to each resolver
     * @throws ServiceGenerationException if the existing source has a syntax error, or has a resolver that is no
     *                                    longer in the schema
     */
    public static MergeResult merge(String existingSource, String generatedSource)
            throws ServiceGenerationException {
        SyntaxTree existingTree = SyntaxTree.from(TextDocuments.from(existingSource));
        Iterator<Diagnostic> syntaxErrors = existingTree.diagnostics().iterator();
        if (syntaxErrors.hasNext()) {
            Diagnostic syntaxError = syntaxErrors.next();
            throw error(String.format(ERROR_SYNTAX, syntaxError.location().lineRange().startLine().line() + 1,
                    syntaxError.message()));
        }
        ServiceDeclarationNode generatedService = service(SyntaxTree.from(TextDocuments.from(generatedSource))
                .rootNode()).orElseThrow();
        Map<String, FunctionDefinitionNode> existingResolvers = new LinkedHashMap<>();
        Map<String, Node> otherMembers = new LinkedHashMap<>();
        Optional<ServiceDeclarationNode> existingService = service(existingTree.rootNode());
        existingService.ifPresent(service -> service.members().forEach(member -> resolverKey(member).ifPresentOrElse(
                key -> existingResolvers.put(key, (FunctionDefinitionNode) member),
                () -> otherMembers.put(memberName(member), member))));

        List<String> members = new ArrayList<>();
        List<MergedResolver> resolvers = new ArrayList<>();
        for (Node member : generatedService.members()) {
            Optional<String> key = resolverKey(member);
            if (key.isEmpty()) {
                // Generated code that is not a resolver, such as a DataLoader prefetch function, is kept from the
                // existing service when it is already there, since the developer may have changed it.
                Node existing = otherMembers.remove(memberName(member));
                members.add((existing != null ? existing : member).toSourceCode().strip());
                continue;
            }
            FunctionDefinitionNode existing = existingResolvers.remove(key.get());
            if (existing == null) {
                members.add(member.toSourceCode().strip());
                resolvers.add(new MergedResolver(key.get(), Outcome.ADDED));
            } else if (tokens(existing.functionSignature())
                    .equals(tokens(((FunctionDefinitionNode) member).functionSignature()))) {
                members.add(existing.toSourceCode().strip());
                resolvers.add(new MergedResolver(key.get(), Outcome.KEPT));
            } else {
                members.add(member.toSourceCode().strip());
                resolvers.add(new MergedResolver(key.get(), Outcome.REPLACED));
            }
        }
        if (!existingResolvers.isEmpty()) {
            throw error(String.format(ERROR_NOT_IN_SCHEMA, existingResolvers.keySet().stream()
                    .map(key -> QUOTE + key + QUOTE).collect(Collectors.joining(LIST_SEPARATOR))));
        }
        otherMembers.values().forEach(member -> members.add(member.toSourceCode().strip()));

        String merged = generatedSource.substring(0, generatedService.openBraceToken().textRange().endOffset())
                + LINE_BREAK + String.join(MEMBER_SEPARATOR, members) + LINE_BREAK
                + generatedSource.substring(generatedService.closeBraceToken().textRange().startOffset());
        try {
            return new MergeResult(Formatter.format(merged), resolvers);
        } catch (FormatterException e) {
            throw error(String.format(ERROR_MERGED_SOURCE, e.getMessage()));
        }
    }

    private static Optional<ServiceDeclarationNode> service(ModulePartNode modulePart) {
        return modulePart.members().stream()
                .filter(ServiceDeclarationNode.class::isInstance)
                .map(ServiceDeclarationNode.class::cast)
                .findFirst();
    }

    // A resource resolver is keyed by its accessor and path, e.g. "get books", and a remote one by its name, e.g.
    // "remote addBook". Any other member is not a resolver.
    private static Optional<String> resolverKey(Node member) {
        if (!(member instanceof FunctionDefinitionNode function)) {
            return Optional.empty();
        }
        if (hasQualifier(function, SyntaxKind.RESOURCE_KEYWORD)) {
            String path = function.relativeResourcePath().stream()
                    .map(node -> node.toSourceCode().strip())
                    .collect(Collectors.joining());
            return Optional.of(function.functionName().text() + KEY_SEPARATOR + path);
        }
        if (hasQualifier(function, SyntaxKind.REMOTE_KEYWORD)) {
            return Optional.of(REMOTE_KEY_PREFIX + function.functionName().text());
        }
        return Optional.empty();
    }

    private static boolean hasQualifier(FunctionDefinitionNode function, SyntaxKind qualifier) {
        return function.qualifierList().stream().anyMatch(token -> token.kind() == qualifier);
    }

    // Members that are not resolvers are matched by name, so a function is found again even if it moved.
    private static String memberName(Node member) {
        return member instanceof FunctionDefinitionNode function ? function.functionName().text() :
                member.toSourceCode().strip();
    }

    // The signature as its tokens, so spacing, line breaks and comments do not count as a change.
    private static List<String> tokens(Node node) {
        List<String> tokens = new ArrayList<>();
        collectTokens(node, tokens);
        return tokens;
    }

    private static void collectTokens(Node node, List<String> tokens) {
        if (node instanceof Token token) {
            tokens.add(token.text());
            return;
        }
        if (node instanceof NonTerminalNode nonTerminal) {
            nonTerminal.children().forEach(child -> collectTokens(child, tokens));
        }
    }

    private static ServiceGenerationException error(String message) {
        return new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100,
                NullLocation.getInstance(), message);
    }
}
