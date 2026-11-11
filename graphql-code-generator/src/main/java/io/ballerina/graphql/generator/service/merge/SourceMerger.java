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
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The parts of a merge that the service file and the types file share: parsing, imports, matching resolvers and
 * formatting the result.
 */
final class SourceMerger {

    private static final String ERROR_SYNTAX =
            "The existing %s has a syntax error at line %d: %s. Fix it before merging.";
    private static final String ERROR_NOT_IN_SCHEMA = "%s not in the schema: %s. Update or delete them.";
    private static final String ERROR_MERGED_SOURCE = "The merged %s could not be formatted: %s";
    private static final String RESOLVERS = "Resolvers";
    private static final String MODULE_SEPARATOR = ".";
    private static final String ORG_SEPARATOR = "/";
    private static final String QUOTE = "\"";
    private static final String LIST_SEPARATOR = ", ";
    static final String LINE_BREAK = "\n";
    static final String MEMBER_SEPARATOR = "\n\n";
    private static final String CLOSE_BRACE = "}";

    private SourceMerger() {
    }

    static ModulePartNode parseExisting(String source, String fileLabel) throws ServiceGenerationException {
        SyntaxTree tree = SyntaxTree.from(TextDocuments.from(source));
        Iterator<Diagnostic> syntaxErrors = tree.diagnostics().iterator();
        if (syntaxErrors.hasNext()) {
            Diagnostic syntaxError = syntaxErrors.next();
            throw error(String.format(ERROR_SYNTAX, fileLabel,
                    syntaxError.location().lineRange().startLine().line() + 1, syntaxError.message()));
        }
        return tree.rootNode();
    }

    static ModulePartNode parse(String source) {
        return SyntaxTree.from(TextDocuments.from(source)).rootNode();
    }

    // The existing imports are kept, and an import the generated file needs is added when its module is not imported.
    static List<String> mergeImports(ModulePartNode existingModule, ModulePartNode generatedModule) {
        List<String> imports = new ArrayList<>();
        Set<String> importedModules = new HashSet<>();
        for (ImportDeclarationNode importNode : existingModule.imports()) {
            imports.add(importNode.toSourceCode().strip());
            importedModules.add(moduleName(importNode));
        }
        for (ImportDeclarationNode importNode : generatedModule.imports()) {
            if (importedModules.add(moduleName(importNode))) {
                imports.add(importNode.toSourceCode().strip());
            }
        }
        return imports;
    }

    /**
     * Merges the members of a generated service or service class into the existing one, keeping the existing order.
     * A resolver is kept as written while its signature matches, and replaced in place when it changed. A generated
     * member that is not a resolver, such as a DataLoader prefetch function, is kept from the existing members when
     * it is there. The developer's other members, such as fields, init and helper methods, are kept. A type inclusion
     * the schema no longer has is removed. New type inclusions are added first, and other new members last.
     *
     * @param existingMembers  the members the developer already has
     * @param generatedMembers the newly generated members
     * @param keyPrefix        the prefix of each resolver key, e.g. {@code Book.} for a service class
     * @param resolvers        collects what happened to each generated resolver
     * @param notInSchema      collects the keys of existing resolvers that are no longer in the schema
     * @return the source of each merged member, in order
     */
    static List<String> mergeMembers(Collection<Node> existingMembers, Collection<Node> generatedMembers,
                                     String keyPrefix, List<MergedResolver> resolvers, List<String> notInSchema) {
        Map<String, FunctionDefinitionNode> generatedResolvers = new LinkedHashMap<>();
        Map<String, Node> generatedOthers = new LinkedHashMap<>();
        generatedMembers.forEach(member -> MemberIdentity.resolverKey(member).ifPresentOrElse(
                key -> generatedResolvers.put(key, (FunctionDefinitionNode) member),
                () -> generatedOthers.put(MemberIdentity.name(member), member)));

        List<String> members = new ArrayList<>();
        for (Node member : existingMembers) {
            Optional<String> key = MemberIdentity.resolverKey(member);
            if (key.isEmpty()) {
                // A type inclusion comes from the schema, so one the schema no longer has is not kept.
                boolean generated = generatedOthers.remove(MemberIdentity.name(member)) != null;
                if (generated || !(member instanceof TypeReferenceNode)) {
                    members.add(member.toSourceCode().strip());
                }
                continue;
            }
            FunctionDefinitionNode generated = generatedResolvers.remove(key.get());
            if (generated == null) {
                notInSchema.add(keyPrefix + key.get());
            } else if (tokens(((FunctionDefinitionNode) member).functionSignature())
                    .equals(tokens(generated.functionSignature()))) {
                members.add(member.toSourceCode().strip());
                resolvers.add(new MergedResolver(keyPrefix + key.get(), Outcome.KEPT));
            } else {
                members.add(generated.toSourceCode().strip());
                resolvers.add(new MergedResolver(keyPrefix + key.get(), Outcome.REPLACED));
            }
        }

        int inclusionIndex = 0;
        for (Node member : generatedMembers) {
            Optional<String> key = MemberIdentity.resolverKey(member);
            if (key.isPresent() && generatedResolvers.containsKey(key.get())) {
                members.add(member.toSourceCode().strip());
                resolvers.add(new MergedResolver(keyPrefix + key.get(), Outcome.ADDED));
            } else if (key.isEmpty() && generatedOthers.containsKey(MemberIdentity.name(member))) {
                if (member instanceof TypeReferenceNode) {
                    members.add(inclusionIndex++, member.toSourceCode().strip());
                } else {
                    members.add(member.toSourceCode().strip());
                }
            }
        }
        return members;
    }

    // A service or class: its header up to and including the opening brace, then its members, then the closing brace.
    static String block(String header, List<String> members) {
        String body = members.isEmpty() ? "" : String.join(MEMBER_SEPARATOR, members) + LINE_BREAK;
        return header + LINE_BREAK + body + CLOSE_BRACE;
    }

    static String header(Node node, Token openBrace, String source) {
        return source.substring(node.textRange().startOffset(), openBrace.textRange().endOffset());
    }

    static void checkInSchema(String kind, Collection<String> notInSchema) throws ServiceGenerationException {
        if (!notInSchema.isEmpty()) {
            throw error(String.format(ERROR_NOT_IN_SCHEMA, kind, notInSchema.stream()
                    .map(key -> QUOTE + key + QUOTE).collect(Collectors.joining(LIST_SEPARATOR))));
        }
    }

    static void checkResolversInSchema(Collection<String> notInSchema) throws ServiceGenerationException {
        checkInSchema(RESOLVERS, notInSchema);
    }

    static MergeResult format(String merged, String fileLabel, List<MergedResolver> resolvers)
            throws ServiceGenerationException {
        return format(merged, fileLabel, resolvers, List.of());
    }

    static MergeResult format(String merged, String fileLabel, List<MergedResolver> resolvers,
                              List<String> removedTypes) throws ServiceGenerationException {
        try {
            return new MergeResult(Formatter.format(merged), resolvers, removedTypes);
        } catch (FormatterException e) {
            throw error(String.format(ERROR_MERGED_SOURCE, fileLabel, e.getMessage()));
        }
    }

    static ServiceGenerationException error(String message) {
        return new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100,
                NullLocation.getInstance(), message);
    }

    // An import is identified by its module, e.g. "ballerina/graphql.dataloader", whatever prefix it uses.
    private static String moduleName(ImportDeclarationNode importNode) {
        String module = importNode.moduleName().stream().map(Token::text)
                .collect(Collectors.joining(MODULE_SEPARATOR));
        return importNode.orgName().map(org -> org.orgName().text() + ORG_SEPARATOR + module).orElse(module);
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
}
