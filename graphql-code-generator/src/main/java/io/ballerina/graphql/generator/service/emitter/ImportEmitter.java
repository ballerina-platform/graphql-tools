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

package io.ballerina.graphql.generator.service.emitter;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.graphql.generator.common.model.TypeModel;
import io.ballerina.graphql.generator.service.model.ParameterModel;
import io.ballerina.graphql.generator.service.model.ResolverModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes the import lines of a generated file: {@code ballerina/graphql}, followed by every other module whose types
 * the file uses, e.g. {@code ballerina/uuid} for {@code uuid:Uuid}. A module is imported only where it is used, since
 * an unused import fails the build.
 */
final class ImportEmitter {

    private static final String IMPORT_FORMAT = "import ballerina/%s;";
    private static final String GRAPHQL_MODULE = "graphql";
    private static final String MODULE_SEPARATOR = SyntaxKind.COLON_TOKEN.stringValue();
    private static final String NEW_LINE = System.lineSeparator();

    private ImportEmitter() {
    }

    static String emit(Stream<TypeModel> types) {
        Stream<String> otherModules = types.flatMap(ImportEmitter::namedTypes)
                .map(TypeModel::name)
                .filter(name -> name.contains(MODULE_SEPARATOR))
                .map(name -> name.substring(0, name.indexOf(MODULE_SEPARATOR)))
                .filter(module -> !module.equals(GRAPHQL_MODULE))
                .distinct()
                .sorted();
        return Stream.concat(Stream.of(GRAPHQL_MODULE), otherModules)
                .map(module -> String.format(IMPORT_FORMAT, module))
                .collect(Collectors.joining(NEW_LINE));
    }

    static Stream<TypeModel> types(List<ResolverModel> resolvers) {
        return resolvers.stream().flatMap(resolver -> Stream.concat(Stream.of(resolver.returnType()),
                resolver.parameters().stream().map(ParameterModel::type)));
    }

    private static Stream<TypeModel> namedTypes(TypeModel type) {
        return type.kind() == TypeModel.Kind.LIST ? namedTypes(type.ofType()) : Stream.of(type);
    }
}
