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

package io.ballerina.graphql.generator.service.mapper;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import io.ballerina.graphql.generator.common.mapper.NameEscaper;
import io.ballerina.graphql.generator.common.mapper.TypeMapper;
import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.EnumValueModel;
import io.ballerina.graphql.generator.common.model.FieldModel;
import io.ballerina.graphql.generator.common.model.InputTypeModel;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.model.ParameterModel;
import io.ballerina.graphql.generator.service.model.ResolverKind;
import io.ballerina.graphql.generator.service.model.ResolverModel;
import io.ballerina.graphql.generator.service.model.ServiceModel;
import io.ballerina.graphql.generator.service.model.TypeDefinitionModel;
import io.ballerina.graphql.generator.service.model.TypeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the {@link ServiceModel} for a GraphQL schema. Every decision about the generated service is made here.
 */
public final class ServiceModelMapper {

    private static final String INTROSPECTION_TYPE_PREFIX = "__";
    private static final String NIL_LITERAL = "()";
    private static final String STRING_LITERAL_FORMAT = "\"%s\"";
    private static final String MAPPING_FIELD_FORMAT = "%s: %s";
    private static final String VALUE_SEPARATOR = ", ";
    private static final String LIST_START = "[";
    private static final String LIST_END = "]";
    private static final String MAPPING_START = "{";
    private static final String MAPPING_END = "}";
    private static final String ERROR_UNSUPPORTED_DEFAULT_VALUE = "Default value \"%s\" is not supported.";

    private final GraphQLSchema schema;
    private final boolean useRecordsForObjects;
    private final Set<String> unionMembers;

    private ServiceModelMapper(GraphQLSchema schema, boolean useRecordsForObjects) {
        this.schema = schema;
        this.useRecordsForObjects = useRecordsForObjects;
        this.unionMembers = schema.getAllTypesAsList().stream()
                .filter(GraphQLUnionType.class::isInstance)
                .flatMap(union -> ((GraphQLUnionType) union).getTypes().stream())
                .map(GraphQLNamedType::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Builds the service model for a schema.
     *
     * @param schema               the GraphQL schema
     * @param useRecordsForObjects whether object types are generated as records where possible
     * @return the service model
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent
     */
    public static ServiceModel map(GraphQLSchema schema, boolean useRecordsForObjects)
            throws ServiceGenerationException {
        try {
            return new ServiceModelMapper(schema, useRecordsForObjects).map();
        } catch (IllegalArgumentException e) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_102, null,
                    e.getMessage());
        }
    }

    private ServiceModel map() {
        List<GraphQLNamedType> types = schema.getAllTypesAsList().stream()
                .filter(type -> !type.getName().startsWith(INTROSPECTION_TYPE_PREFIX))
                .filter(type -> !isRootType(type))
                .toList();
        return new ServiceModel(
                resolvers(schema.getQueryType(), ResolverKind.RESOURCE_GET),
                resolvers(schema.getMutationType(), ResolverKind.REMOTE),
                resolvers(schema.getSubscriptionType(), ResolverKind.RESOURCE_SUBSCRIBE),
                typeDefinitions(types), inputTypes(types), enums(types));
    }

    private boolean isRootType(GraphQLNamedType type) {
        return Stream.of(schema.getQueryType(), schema.getMutationType(), schema.getSubscriptionType())
                .anyMatch(rootType -> rootType != null && rootType.getName().equals(type.getName()));
    }

    private static List<ResolverModel> resolvers(GraphQLObjectType rootType, ResolverKind kind) {
        if (rootType == null) {
            return List.of();
        }
        return resolvers(rootType.getFields(), kind);
    }

    private static List<ResolverModel> resolvers(List<GraphQLFieldDefinition> fields, ResolverKind kind) {
        return fields.stream()
                .map(field -> new ResolverModel(kind, NameEscaper.escape(field.getName()),
                        parameters(field.getArguments()), TypeMapper.map(field.getType()), field.getDescription(),
                        field.isDeprecated() ? field.getDeprecationReason() : null))
                .toList();
    }

    private static List<ParameterModel> parameters(List<GraphQLArgument> arguments) {
        return arguments.stream()
                .map(argument -> new ParameterModel(NameEscaper.escape(argument.getName()),
                        TypeMapper.map(argument.getType()),
                        argument.hasSetDefaultValue() ? literal(argument.getArgumentDefaultValue().getValue()) : null,
                        argument.getDescription()))
                .toList();
    }

    private List<TypeDefinitionModel> typeDefinitions(List<GraphQLNamedType> types) {
        List<TypeDefinitionModel> definitions = new ArrayList<>();
        for (GraphQLNamedType type : types) {
            if (type instanceof GraphQLObjectType objectType) {
                definitions.add(new TypeDefinitionModel(NameEscaper.escape(objectType.getName()),
                        isRecord(objectType) ? TypeKind.RECORD : TypeKind.SERVICE_CLASS,
                        resolvers(objectType.getFields(), ResolverKind.RESOURCE_GET),
                        names(objectType.getInterfaces()), objectType.getDescription()));
            } else if (type instanceof GraphQLInterfaceType interfaceType) {
                definitions.add(new TypeDefinitionModel(NameEscaper.escape(interfaceType.getName()),
                        TypeKind.INTERFACE, resolvers(interfaceType.getFields(), ResolverKind.RESOURCE_GET),
                        names(interfaceType.getInterfaces()), interfaceType.getDescription()));
            } else if (type instanceof GraphQLUnionType unionType) {
                definitions.add(new TypeDefinitionModel(NameEscaper.escape(unionType.getName()), TypeKind.UNION,
                        List.of(), names(unionType.getTypes()), unionType.getDescription()));
            }
        }
        return definitions;
    }

    private boolean isRecord(GraphQLObjectType objectType) {
        return useRecordsForObjects
                && objectType.getInterfaces().isEmpty()
                && !unionMembers.contains(objectType.getName())
                && objectType.getFields().stream().allMatch(field -> field.getArguments().isEmpty());
    }

    private static List<String> names(List<? extends GraphQLNamedType> types) {
        return types.stream().map(type -> NameEscaper.escape(type.getName())).toList();
    }

    private static List<InputTypeModel> inputTypes(List<GraphQLNamedType> types) {
        return types.stream()
                .filter(GraphQLInputObjectType.class::isInstance)
                .map(GraphQLInputObjectType.class::cast)
                .map(inputType -> new InputTypeModel(NameEscaper.escape(inputType.getName()),
                        inputType.getFields().stream().map(ServiceModelMapper::inputField).toList(),
                        inputType.getDescription()))
                .toList();
    }

    private static FieldModel inputField(GraphQLInputObjectField field) {
        return new FieldModel(NameEscaper.escape(field.getName()), TypeMapper.map(field.getType()),
                field.hasSetDefaultValue() ? literal(field.getInputFieldDefaultValue().getValue()) : null,
                field.getDescription(), field.isDeprecated() ? field.getDeprecationReason() : null);
    }

    private static List<EnumModel> enums(List<GraphQLNamedType> types) {
        return types.stream()
                .filter(GraphQLEnumType.class::isInstance)
                .map(GraphQLEnumType.class::cast)
                .map(enumType -> new EnumModel(NameEscaper.escape(enumType.getName()),
                        enumType.getValues().stream().map(ServiceModelMapper::enumValue).toList(),
                        enumType.getDescription()))
                .toList();
    }

    private static EnumValueModel enumValue(GraphQLEnumValueDefinition value) {
        return new EnumValueModel(NameEscaper.escape(value.getName()), value.getDescription(),
                value.isDeprecated() ? value.getDeprecationReason() : null);
    }

    private static String literal(Object value) {
        if (value instanceof IntValue intValue) {
            return intValue.getValue().toString();
        }
        if (value instanceof FloatValue floatValue) {
            return floatValue.getValue().toString();
        }
        if (value instanceof StringValue stringValue) {
            return String.format(STRING_LITERAL_FORMAT, stringValue.getValue());
        }
        if (value instanceof BooleanValue booleanValue) {
            return String.valueOf(booleanValue.isValue());
        }
        if (value instanceof EnumValue enumValue) {
            return NameEscaper.escape(enumValue.getName());
        }
        if (value instanceof NullValue) {
            return NIL_LITERAL;
        }
        if (value instanceof ArrayValue arrayValue) {
            return arrayValue.getValues().stream()
                    .map(ServiceModelMapper::literal)
                    .collect(Collectors.joining(VALUE_SEPARATOR, LIST_START, LIST_END));
        }
        if (value instanceof ObjectValue objectValue) {
            return objectValue.getObjectFields().stream()
                    .map(field -> String.format(MAPPING_FIELD_FORMAT, NameEscaper.escape(field.getName()),
                            literal(field.getValue())))
                    .collect(Collectors.joining(VALUE_SEPARATOR, MAPPING_START, MAPPING_END));
        }
        throw new IllegalArgumentException(String.format(ERROR_UNSUPPORTED_DEFAULT_VALUE, value));
    }
}
