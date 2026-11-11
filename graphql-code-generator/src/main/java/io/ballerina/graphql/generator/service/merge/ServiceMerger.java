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

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.merge.MergeResult.MergedResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.graphql.generator.service.merge.SourceMerger.LINE_BREAK;
import static io.ballerina.graphql.generator.service.merge.SourceMerger.MEMBER_SEPARATOR;

/**
 * Merges a newly generated service file into the one the developer already has. The schema is the source of truth
 * for the resolvers: each one is matched to its schema field by key and kept as written, and reported when its
 * signature no longer matches. The developer's other code in the file, such as imports, the port, the context init
 * function and batch functions, is kept, and what the schema newly needs is added.
 */
public final class ServiceMerger {

    private static final String FILE_LABEL = "service";
    private static final String CONTEXT_INIT_FUNCTION = "initContext";
    private static final String REGISTER_DATALOADER = "registerDataLoader";
    private static final String NO_KEY_PREFIX = "";

    private ServiceMerger() {
    }

    /**
     * Merges the generated service file into the existing one. Each resolver of the generated service is kept from
     * the existing service, reported as outdated when its signature changed, or added when it is new. The existing
     * imports, module-level code and service annotations are kept, and the imports, functions and DataLoader
     * registrations the generated file newly needs are added.
     *
     * @param existingSource  the source the developer already has
     * @param generatedSource the newly generated source
     * @return the merged source and what happened to each resolver
     * @throws ServiceGenerationException if the existing source has a syntax error, or has a resolver that is no
     *                                    longer in the schema
     */
    public static MergeResult merge(String existingSource, String generatedSource)
            throws ServiceGenerationException {
        ModulePartNode existingModule = SourceMerger.parseExisting(existingSource, FILE_LABEL);
        ModulePartNode generatedModule = SourceMerger.parse(generatedSource);
        ServiceDeclarationNode generatedService = service(generatedModule).orElseThrow();
        Optional<ServiceDeclarationNode> existingService = service(existingModule);

        List<MergedResolver> resolvers = new ArrayList<>();
        String mergedService = mergeService(existingService, generatedService, existingSource, generatedSource,
                resolvers);
        String merged = String.join(LINE_BREAK, SourceMerger.mergeImports(existingModule, generatedModule))
                + MEMBER_SEPARATOR + String.join(MEMBER_SEPARATOR, mergeModuleMembers(existingModule,
                generatedModule, existingService, mergedService, existingSource)) + LINE_BREAK;
        return SourceMerger.format(merged, FILE_LABEL, resolvers);
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
            existingKeys.add(MemberIdentity.name(member));
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
            if (existingKeys.contains(MemberIdentity.name(member))) {
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
            Set<String> registers = registeredDataLoaders(statement);
            if (!registers.isEmpty() && !registered.containsAll(registers)) {
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

    // The DataLoaders registered by registerDataLoader("name", ...) calls in the code. The syntax tree is read
    // instead of the text, so a call in a comment does not count.
    private static Set<String> registeredDataLoaders(Node code) {
        Set<String> names = new HashSet<>();
        code.accept(new NodeVisitor() {
            @Override
            public void visit(MethodCallExpressionNode call) {
                dataLoaderName(call).ifPresent(names::add);
                super.visit(call);
            }
        });
        return names;
    }

    private static Optional<String> dataLoaderName(MethodCallExpressionNode call) {
        if (!call.methodName().toSourceCode().strip().equals(REGISTER_DATALOADER) || call.arguments().isEmpty()
                || !(call.arguments().get(0) instanceof PositionalArgumentNode argument)
                || !(argument.expression() instanceof BasicLiteralNode literal)
                || literal.kind() != SyntaxKind.STRING_LITERAL) {
            return Optional.empty();
        }
        String quoted = literal.literalToken().text();
        return Optional.of(quoted.substring(1, quoted.length() - 1));
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
        List<String> notInSchema = new ArrayList<>();
        List<Node> existingMembers = existingService.map(service -> service.members().stream().toList())
                .orElse(List.of());
        List<String> members = SourceMerger.mergeMembers(existingMembers, generatedService.members().stream().toList(),
                NO_KEY_PREFIX, resolvers, notInSchema);
        SourceMerger.checkResolversInSchema(notInSchema);

        String header = existingService
                .map(service -> SourceMerger.header(service, service.openBraceToken(), existingSource))
                .orElseGet(() -> SourceMerger.header(generatedService, generatedService.openBraceToken(),
                        generatedSource));
        if (existingService.isPresent() && existingService.get().metadata().isEmpty()) {
            header = generatedService.metadata().map(metadata -> metadata.toSourceCode().strip() + LINE_BREAK)
                    .orElse("") + header;
        }
        return SourceMerger.block(header, members);
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
}
