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

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Merges a newly generated service file into the one the developer already has. The schema is the source of truth
 * for the resolvers: each one is matched to its schema field by key, and kept as written while its signature still
 * matches. The developer's other code in the file, such as imports, the port, the context init function and batch
 * functions, is kept, and what the schema newly needs is added.
 */
public final class ServiceMerger {

    private static final String ERROR_SYNTAX =
            "The existing service has a syntax error at line %d: %s. Fix it before merging.";
    private static final String ERROR_NOT_IN_SCHEMA = "Resolvers not in the schema: %s. Update or delete them.";
    private static final String ERROR_MERGED_SOURCE = "The merged service could not be formatted: %s";
    private static final String CONTEXT_INIT_FUNCTION = "initContext";
    private static final Pattern DATALOADER_REGISTRATION = Pattern.compile("registerDataLoader\\(\\s*\"([^\"]+)\"");
    private static final String KEY_SEPARATOR = " ";
    private static final String REMOTE_KEY_PREFIX = "remote ";
    private static final String FUNCTION_KEY_PREFIX = "function ";
    private static final String VARIABLE_KEY_PREFIX = "variable ";
    private static final String MODULE_SEPARATOR = ".";
    private static final String ORG_SEPARATOR = "/";
    private static final String LINE_BREAK = "\n";
    private static final String MEMBER_SEPARATOR = "\n\n";
    private static final String CLOSE_BRACE = "}";
    private static final String QUOTE = "\"";
    private static final String LIST_SEPARATOR = ", ";

    private ServiceMerger() {
    }

    /**
     * Merges the generated service file into the existing one. Each resolver of the generated service is kept from
     * the existing service when its signature is unchanged, regenerated when its signature changed, or added when it
     * is new. The existing imports, module-level code and service annotations are kept, and the imports, functions
     * and DataLoader registrations the generated file newly needs are added.
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
        ModulePartNode existingModule = existingTree.rootNode();
        ModulePartNode generatedModule = SyntaxTree.from(TextDocuments.from(generatedSource)).rootNode();
        ServiceDeclarationNode generatedService = service(generatedModule).orElseThrow();
        Optional<ServiceDeclarationNode> existingService = service(existingModule);

        List<MergedResolver> resolvers = new ArrayList<>();
        String mergedService = mergeService(existingService, generatedService, existingSource, generatedSource,
                resolvers);
        String merged = String.join(LINE_BREAK, mergeImports(existingModule, generatedModule)) + MEMBER_SEPARATOR
                + String.join(MEMBER_SEPARATOR, mergeModuleMembers(existingModule, generatedModule, existingService,
                mergedService, existingSource)) + LINE_BREAK;
        try {
            return new MergeResult(Formatter.format(merged), resolvers);
        } catch (FormatterException e) {
            throw error(String.format(ERROR_MERGED_SOURCE, e.getMessage()));
        }
    }

    // The existing imports are kept, and an import the generated file needs is added when its module is not imported.
    private static List<String> mergeImports(ModulePartNode existingModule, ModulePartNode generatedModule) {
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

    // The existing module members are kept in their order, with the service merged and any missing DataLoader
    // registrations added to the context init function. A generated member that is not in the existing file is added
    // before the service when it comes before the service in the generated file, and at the end otherwise.
    private static List<String> mergeModuleMembers(ModulePartNode existingModule, ModulePartNode generatedModule,
                                                   Optional<ServiceDeclarationNode> existingService,
                                                   String mergedService, String existingSource) {
        Optional<FunctionDefinitionNode> generatedContextInit = contextInit(generatedModule);
        List<String> members = new ArrayList<>();
        Set<String> existingKeys = new HashSet<>();
        int serviceIndex = -1;
        for (ModuleMemberDeclarationNode member : existingModule.members()) {
            if (existingService.isPresent() && member == existingService.get()) {
                serviceIndex = members.size();
                members.add(mergedService);
                continue;
            }
            String key = memberKey(member);
            existingKeys.add(key);
            if (member instanceof FunctionDefinitionNode function
                    && function.functionName().text().equals(CONTEXT_INIT_FUNCTION)
                    && generatedContextInit.isPresent()) {
                members.add(addRegistrations(function, generatedContextInit.get(), existingSource));
            } else {
                members.add(member.toSourceCode().strip());
            }
        }

        boolean beforeService = true;
        for (ModuleMemberDeclarationNode member : generatedModule.members()) {
            if (member instanceof ServiceDeclarationNode) {
                beforeService = false;
                if (serviceIndex < 0) {
                    members.add(mergedService);
                }
                continue;
            }
            if (existingKeys.contains(memberKey(member))) {
                continue;
            }
            if (beforeService && serviceIndex >= 0) {
                members.add(serviceIndex++, member.toSourceCode().strip());
            } else {
                members.add(member.toSourceCode().strip());
            }
        }
        return members;
    }

    // The developer's context init function is kept, and a registration for each DataLoader the generated one
    // registers is added before its return statement when it is not registered yet.
    private static String addRegistrations(FunctionDefinitionNode existing, FunctionDefinitionNode generated,
                                           String existingSource) {
        int start = existing.textRange().startOffset();
        String function = existingSource.substring(start, existing.textRange().endOffset());
        Set<String> registered = registeredDataLoaders(existing);
        List<String> missing = new ArrayList<>();
        for (StatementNode statement : statements(generated)) {
            Matcher matcher = DATALOADER_REGISTRATION.matcher(statement.toSourceCode());
            if (matcher.find() && !registered.contains(matcher.group(1))) {
                missing.add(statement.toSourceCode().strip());
            }
        }
        if (missing.isEmpty()) {
            return function;
        }
        // Without a return statement, the registrations go before the closing brace of the body.
        int insertAt = statements(existing).stream()
                .filter(statement -> statement.kind() == SyntaxKind.RETURN_STATEMENT)
                .map(ReturnStatementNode.class::cast)
                .reduce((first, second) -> second)
                .map(statement -> statement.textRange().startOffset())
                .orElse(((FunctionBodyBlockNode) existing.functionBody()).closeBraceToken().textRange()
                        .startOffset()) - start;
        return function.substring(0, insertAt) + String.join(LINE_BREAK, missing) + LINE_BREAK
                + function.substring(insertAt);
    }

    private static Set<String> registeredDataLoaders(FunctionDefinitionNode function) {
        Set<String> names = new HashSet<>();
        Matcher matcher = DATALOADER_REGISTRATION.matcher(function.toSourceCode());
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static List<StatementNode> statements(FunctionDefinitionNode function) {
        return function.functionBody() instanceof FunctionBodyBlockNode body ?
                body.statements().stream().toList() : List.of();
    }

    // The resolvers are merged inside the service. The existing service keeps its name, listener and annotations;
    // when it has no annotations, the generated ones are used, such as the context init config for DataLoaders.
    private static String mergeService(Optional<ServiceDeclarationNode> existingService,
                                       ServiceDeclarationNode generatedService, String existingSource,
                                       String generatedSource, List<MergedResolver> resolvers)
            throws ServiceGenerationException {
        Map<String, FunctionDefinitionNode> existingResolvers = new LinkedHashMap<>();
        Map<String, Node> otherMembers = new LinkedHashMap<>();
        existingService.ifPresent(service -> service.members().forEach(member -> resolverKey(member).ifPresentOrElse(
                key -> existingResolvers.put(key, (FunctionDefinitionNode) member),
                () -> otherMembers.put(memberName(member), member))));

        List<String> members = new ArrayList<>();
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

        String header = existingService.map(service -> serviceHeader(service, existingSource))
                .orElseGet(() -> serviceHeader(generatedService, generatedSource));
        if (existingService.isPresent() && existingService.get().metadata().isEmpty()) {
            header = generatedService.metadata().map(metadata -> metadata.toSourceCode().strip() + LINE_BREAK)
                    .orElse("") + header;
        }
        String body = members.isEmpty() ? "" : String.join(MEMBER_SEPARATOR, members) + LINE_BREAK;
        return header + LINE_BREAK + body + CLOSE_BRACE;
    }

    // The service up to and including its opening brace: its annotations, name and listener.
    private static String serviceHeader(ServiceDeclarationNode service, String source) {
        return source.substring(service.textRange().startOffset(), service.openBraceToken().textRange().endOffset());
    }

    private static Optional<ServiceDeclarationNode> service(ModulePartNode modulePart) {
        return modulePart.members().stream()
                .filter(ServiceDeclarationNode.class::isInstance)
                .map(ServiceDeclarationNode.class::cast)
                .findFirst();
    }

    private static Optional<FunctionDefinitionNode> contextInit(ModulePartNode modulePart) {
        return modulePart.members().stream()
                .filter(FunctionDefinitionNode.class::isInstance)
                .map(FunctionDefinitionNode.class::cast)
                .filter(function -> function.functionName().text().equals(CONTEXT_INIT_FUNCTION))
                .findFirst();
    }

    // An import is identified by its module, e.g. "ballerina/graphql.dataloader", whatever prefix it uses.
    private static String moduleName(ImportDeclarationNode importNode) {
        String module = importNode.moduleName().stream().map(Token::text)
                .collect(Collectors.joining(MODULE_SEPARATOR));
        return importNode.orgName().map(org -> org.orgName().text() + ORG_SEPARATOR + module).orElse(module);
    }

    // Module-level functions and variables are matched by name, so they are found again even if they moved.
    private static String memberKey(ModuleMemberDeclarationNode member) {
        if (member instanceof FunctionDefinitionNode function) {
            return FUNCTION_KEY_PREFIX + function.functionName().text();
        }
        if (member instanceof ModuleVariableDeclarationNode variable) {
            return VARIABLE_KEY_PREFIX + variable.typedBindingPattern().bindingPattern().toSourceCode().strip();
        }
        return member.toSourceCode().strip();
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
