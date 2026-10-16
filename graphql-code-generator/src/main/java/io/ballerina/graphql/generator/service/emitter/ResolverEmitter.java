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

package io.ballerina.graphql.generator.service.emitter;

import io.ballerina.graphql.generator.common.emitter.DocEmitter;
import io.ballerina.graphql.generator.common.emitter.TemplateLoader;
import io.ballerina.graphql.generator.common.emitter.TypeEmitter;
import io.ballerina.graphql.generator.service.model.ParameterModel;
import io.ballerina.graphql.generator.service.model.ResolverKind;
import io.ballerina.graphql.generator.service.model.ResolverModel;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes resolver functions, shared by the service, the service classes and the interfaces.
 */
final class ResolverEmitter {

    private static final String TEMPLATE = "templates/service/resolver.bal";
    private static final String DECLARATION_TEMPLATE = "templates/service/resolver_declaration.bal";
    private static final String DOCS = "docs";
    private static final String SIGNATURE = "signature";
    private static final String RESOURCE_GET = "resource function get";
    private static final String REMOTE = "remote function";
    private static final String RESOURCE_SUBSCRIBE = "resource function subscribe";
    private static final String SIGNATURE_FORMAT = "%s %s(%s) returns %s";
    private static final String RETURN_FORMAT = "%s|error";
    private static final String STREAM_RETURN_FORMAT = "stream<%s>|error";
    private static final String PARAMETER_FORMAT = "%s %s";
    private static final String PARAMETER_WITH_DEFAULT_FORMAT = "%s %s = %s";
    private static final String PARAMETER_SEPARATOR = ", ";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String BLANK_LINE = NEW_LINE + NEW_LINE;

    private ResolverEmitter() {
    }

    static String emit(List<ResolverModel> resolvers) {
        return resolvers.stream()
                .map(resolver -> TemplateLoader.fill(TEMPLATE, values(resolver)))
                .collect(Collectors.joining(BLANK_LINE));
    }

    static String emitDeclarations(List<ResolverModel> resolvers) {
        return resolvers.stream()
                .map(resolver -> TemplateLoader.fill(DECLARATION_TEMPLATE, values(resolver)))
                .collect(Collectors.joining(NEW_LINE));
    }

    private static Map<String, String> values(ResolverModel resolver) {
        List<ParameterModel> parameters = requiredFirst(resolver.parameters());
        Map<String, String> parameterDescriptions = new LinkedHashMap<>();
        parameters.forEach(parameter -> parameterDescriptions.put(parameter.name(), parameter.description()));
        return Map.of(
                DOCS, DocEmitter.emit(resolver.description(), parameterDescriptions, resolver.deprecationReason()),
                SIGNATURE, String.format(SIGNATURE_FORMAT, prefix(resolver.kind()), resolver.name(),
                        parameters.stream().map(ResolverEmitter::parameter)
                                .collect(Collectors.joining(PARAMETER_SEPARATOR)),
                        returnType(resolver)));
    }

    private static List<ParameterModel> requiredFirst(List<ParameterModel> parameters) {
        return parameters.stream()
                .sorted(Comparator.comparing(parameter -> parameter.defaultValue() != null))
                .toList();
    }

    private static String prefix(ResolverKind kind) {
        return switch (kind) {
            case RESOURCE_GET -> RESOURCE_GET;
            case REMOTE -> REMOTE;
            case RESOURCE_SUBSCRIBE -> RESOURCE_SUBSCRIBE;
        };
    }

    private static String returnType(ResolverModel resolver) {
        return resolver.kind() == ResolverKind.RESOURCE_SUBSCRIBE
                ? String.format(STREAM_RETURN_FORMAT, TypeEmitter.emit(resolver.returnType()))
                : String.format(RETURN_FORMAT, TypeEmitter.emitAnnotated(resolver.returnType()));
    }

    private static String parameter(ParameterModel parameter) {
        String type = TypeEmitter.emitAnnotated(parameter.type());
        return parameter.defaultValue() == null
                ? String.format(PARAMETER_FORMAT, type, parameter.name())
                : String.format(PARAMETER_WITH_DEFAULT_FORMAT, type, parameter.name(), parameter.defaultValue());
    }
}
