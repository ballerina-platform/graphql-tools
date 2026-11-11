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
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.merge.MergeResult.MergedResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.graphql.generator.service.merge.SourceMerger.LINE_BREAK;
import static io.ballerina.graphql.generator.service.merge.SourceMerger.MEMBER_SEPARATOR;

/**
 * Merges a newly generated types file into the one the developer already has. The resolvers of each service class
 * are merged as in the service, and the developer's other code in a class is kept. The types that only describe the
 * schema, such as records, enums, unions and the service object type, are regenerated, since the schema is the source
 * of truth. The developer's own code in the file is kept, and a type or enum the schema does not have is kept and
 * reported.
 */
public final class TypesMerger {

    private static final String FILE_LABEL = "types file";
    private static final String SERVICE_CLASSES = "Service classes";
    private static final String CLASS_KEY_SEPARATOR = ".";

    private TypesMerger() {
    }

    /**
     * Merges the generated types file into the existing one.
     *
     * @param existingSource  the source the developer already has
     * @param generatedSource the newly generated source
     * @return the merged source, what happened to each service class resolver, keyed like {@code Book.get title}, and
     * the types that are not in the schema
     * @throws ServiceGenerationException if the existing source has a syntax error, or has a service class or a
     *                                    resolver that is no longer in the schema
     */
    public static MergeResult merge(String existingSource, String generatedSource)
            throws ServiceGenerationException {
        ModulePartNode existingModule = SourceMerger.parseExisting(existingSource, FILE_LABEL);
        ModulePartNode generatedModule = SourceMerger.parse(generatedSource);
        Set<String> generatedClasses = generatedModule.members().stream()
                .filter(ClassDefinitionNode.class::isInstance)
                .map(MemberIdentity::name)
                .collect(Collectors.toSet());
        Map<String, ClassDefinitionNode> existingClasses = new LinkedHashMap<>();
        List<ModuleMemberDeclarationNode> otherExistingMembers = new ArrayList<>();
        for (ModuleMemberDeclarationNode member : existingModule.members()) {
            // A class the developer wrote, such as a cache, is kept like the rest of their code.
            if (member instanceof ClassDefinitionNode serviceClass
                    && (generatedClasses.contains(MemberIdentity.name(serviceClass)) || isServiceClass(serviceClass))) {
                existingClasses.put(MemberIdentity.name(serviceClass), serviceClass);
            } else {
                otherExistingMembers.add(member);
            }
        }

        List<String> members = new ArrayList<>();
        List<MergedResolver> resolvers = new ArrayList<>();
        List<String> notInSchema = new ArrayList<>();
        Set<String> generatedNames = new HashSet<>();
        for (ModuleMemberDeclarationNode member : generatedModule.members()) {
            generatedNames.add(MemberIdentity.name(member));
            if (member instanceof ClassDefinitionNode serviceClass) {
                String name = MemberIdentity.name(serviceClass);
                ClassDefinitionNode existing = existingClasses.remove(name);
                List<Node> existingMembers = existing != null ? existing.members().stream().toList() : List.of();
                members.add(SourceMerger.block(
                        SourceMerger.header(serviceClass, serviceClass.openBrace(), generatedSource),
                        SourceMerger.mergeMembers(existingMembers, serviceClass.members().stream().toList(),
                                name + CLASS_KEY_SEPARATOR, resolvers, notInSchema)));
            } else {
                // A type that only describes the schema is regenerated.
                members.add(member.toSourceCode().strip());
            }
        }
        SourceMerger.checkInSchema(SERVICE_CLASSES, existingClasses.keySet());
        SourceMerger.checkResolversInSchema(notInSchema);
        // The developer's own code, such as a helper function, is kept after the generated types. A type or enum the
        // schema does not have is kept too, since the tool cannot tell a stale generated type from one the developer
        // wrote, and it is reported so the developer can delete it.
        List<String> typesNotInSchema = new ArrayList<>();
        for (ModuleMemberDeclarationNode member : otherExistingMembers) {
            String name = MemberIdentity.name(member);
            if (generatedNames.contains(name)) {
                continue;
            }
            if (member instanceof TypeDefinitionNode || member instanceof EnumDeclarationNode) {
                typesNotInSchema.add(name);
            }
            members.add(member.toSourceCode().strip());
        }

        String merged = String.join(LINE_BREAK, SourceMerger.mergeImports(existingModule, generatedModule))
                + MEMBER_SEPARATOR + String.join(MEMBER_SEPARATOR, members) + LINE_BREAK;
        return SourceMerger.format(merged, FILE_LABEL, resolvers, typesNotInSchema);
    }

    // A class is a GraphQL service class when it is a distinct service class with resolvers, as the tool generates.
    private static boolean isServiceClass(ClassDefinitionNode classDefinition) {
        Set<SyntaxKind> qualifiers = classDefinition.classTypeQualifiers().stream()
                .map(Token::kind)
                .collect(Collectors.toSet());
        boolean hasResolvers = classDefinition.members().stream()
                .anyMatch(member -> MemberIdentity.resolverKey(member).isPresent());
        return qualifiers.contains(SyntaxKind.DISTINCT_KEYWORD) && qualifiers.contains(SyntaxKind.SERVICE_KEYWORD)
                && hasResolvers;
    }
}
