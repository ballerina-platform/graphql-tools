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

import io.ballerina.graphql.generator.common.emitter.DocEmitter;
import io.ballerina.graphql.generator.common.emitter.EnumEmitter;
import io.ballerina.graphql.generator.common.emitter.RecordEmitter;
import io.ballerina.graphql.generator.common.emitter.TemplateLoader;
import io.ballerina.graphql.generator.common.emitter.TypeEmitter;
import io.ballerina.graphql.generator.common.model.FieldModel;
import io.ballerina.graphql.generator.common.model.TypeModel;
import io.ballerina.graphql.generator.service.model.ResolverModel;
import io.ballerina.graphql.generator.service.model.ServiceModel;
import io.ballerina.graphql.generator.service.model.TypeDefinitionModel;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes the {@code types.bal} file: the service object type, then the service classes, records, interfaces,
 * unions, input types and enums.
 */
public final class TypesEmitter {

    private static final String SERVICE_OBJECT_TEMPLATE = "templates/service/service_object.bal";
    private static final String SERVICE_CLASS_TEMPLATE = "templates/service/service_class.bal";
    private static final String INTERFACE_TEMPLATE = "templates/service/interface.bal";
    private static final String UNION_TEMPLATE = "templates/service/union.bal";
    private static final String DOCS = "docs";
    private static final String NAME = "name";
    private static final String MEMBERS = "members";
    private static final String RESOLVERS = "resolvers";
    private static final String NOT_DEPRECATED = null;
    private static final String NO_DEFAULT_VALUE = null;
    private static final String INCLUSION_FORMAT = "*%s;";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String BLANK_LINE = NEW_LINE + NEW_LINE;

    private TypesEmitter() {
    }

    /**
     * Writes the types file for a service.
     *
     * @param model the service model
     * @return the file content
     */
    public static String emit(ServiceModel model) {
        String serviceObject = TemplateLoader.fill(SERVICE_OBJECT_TEMPLATE, Map.of(
                NAME, model.serviceName(),
                RESOLVERS, ResolverEmitter.emitDeclarations(model.rootResolvers())));
        return Stream.of(
                        Stream.of(ImportEmitter.emit(usedTypes(model)), serviceObject),
                        model.typeDefinitions().stream().map(TypesEmitter::typeDefinition),
                        model.inputTypes().stream().map(inputType ->
                                RecordEmitter.emit(inputType.name(), inputType.fields(), inputType.description())),
                        model.enums().stream().map(EnumEmitter::emit))
                .flatMap(stream -> stream)
                .collect(Collectors.joining(BLANK_LINE)) + NEW_LINE;
    }

    private static Stream<TypeModel> usedTypes(ServiceModel model) {
        return Stream.of(ImportEmitter.types(model.rootResolvers()),
                        model.typeDefinitions().stream()
                                .flatMap(definition -> ImportEmitter.types(definition.fields())),
                        model.inputTypes().stream()
                                .flatMap(inputType -> inputType.fields().stream().map(FieldModel::type)))
                .flatMap(stream -> stream);
    }

    private static String typeDefinition(TypeDefinitionModel definition) {
        return switch (definition.kind()) {
            case SERVICE_CLASS -> fill(SERVICE_CLASS_TEMPLATE, definition,
                    members(definition, ResolverEmitter.emit(definition.fields()), BLANK_LINE));
            case RECORD -> RecordEmitter.emit(definition.name(),
                    definition.fields().stream().map(TypesEmitter::recordField).toList(), definition.description());
            case INTERFACE -> fill(INTERFACE_TEMPLATE, definition,
                    members(definition, ResolverEmitter.emitDeclarations(definition.fields()), NEW_LINE));
            case UNION -> fill(UNION_TEMPLATE, definition, TypeEmitter.emitUnion(definition.relatedTypes()));
        };
    }

    private static String fill(String template, TypeDefinitionModel definition, String members) {
        return TemplateLoader.fill(template, Map.of(
                DOCS, DocEmitter.emit(definition.description(), NOT_DEPRECATED),
                NAME, definition.name(),
                MEMBERS, members));
    }

    private static String members(TypeDefinitionModel definition, String resolvers, String separator) {
        String inclusions = definition.relatedTypes().stream()
                .map(name -> String.format(INCLUSION_FORMAT, name))
                .collect(Collectors.joining(NEW_LINE));
        return inclusions.isEmpty() ? resolvers : inclusions + separator + resolvers;
    }

    private static FieldModel recordField(ResolverModel field) {
        return new FieldModel(field.name(), field.returnType(), NO_DEFAULT_VALUE, field.description(),
                field.deprecationReason());
    }
}
