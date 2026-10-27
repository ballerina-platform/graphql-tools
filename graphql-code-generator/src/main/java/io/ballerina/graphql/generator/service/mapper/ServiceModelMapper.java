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
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLImplementingType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnionType;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.graphql.generator.common.mapper.Scalar;
import io.ballerina.graphql.generator.common.mapper.TypeMapper;
import io.ballerina.graphql.generator.common.model.EnumModel;
import io.ballerina.graphql.generator.common.model.EnumValueModel;
import io.ballerina.graphql.generator.common.model.FieldModel;
import io.ballerina.graphql.generator.common.model.InputTypeModel;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.model.DataLoaderModel;
import io.ballerina.graphql.generator.service.model.ParameterModel;
import io.ballerina.graphql.generator.service.model.ResolverKind;
import io.ballerina.graphql.generator.service.model.ResolverModel;
import io.ballerina.graphql.generator.service.model.ServiceModel;
import io.ballerina.graphql.generator.service.model.TypeDefinitionModel;
import io.ballerina.graphql.generator.service.model.TypeKind;
import io.ballerina.graphql.generator.utils.NullLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
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
    private static final String ERROR_SHARED_INTERFACE_FIELD =
            "Type \"%s\" cannot implement \"%s\" and \"%s\" as both have the field \"%s\". "
                    + "Do not implement multiple interfaces that have the same field.";
    private static final String ERROR_SHARED_PARENT_INTERFACE_FIELD =
            "Type \"%s\" cannot implement \"%s\" and \"%s\" as both inherit the field \"%s\" from \"%s\". "
                    + "Do not implement multiple interfaces that have the same field.";
    private static final String ERROR_UNKNOWN_ID_PATH =
            "\"%s\" in [[id-types]] does not match any field or argument in the schema.";
    private static final String ERROR_NOT_AN_ID = "\"%s\" in [[id-types]] is not an ID field or argument.";
    private static final String ERROR_UNSUPPORTED_ID_TYPE =
            "\"%s\" is not a supported ID type for \"%s\". Use string, int, float, decimal or uuid:Uuid.";
    private static final String ERROR_ID_TYPE_MISMATCH =
            "\"%s\" in [[id-types]] must use the same type as \"%s\" (\"%s\").";
    private static final Set<String> SUPPORTED_ID_TYPES = Set.of("string", "int", "float", "decimal", "uuid:Uuid");
    private static final String ERROR_UNKNOWN_DATALOADER_PATH =
            "\"%s\" in [[dataloaders]] does not match any object type field in the schema.";
    private static final String ERROR_DATALOADER_ON_MUTATION_OR_SUBSCRIPTION =
            "\"%s\" in [[dataloaders]] cannot use a DataLoader. Only Query fields and object type fields can.";
    private static final String ERROR_DATALOADER_ON_INTERFACE_FIELD =
            "\"%s\" in [[dataloaders]] cannot use a DataLoader, as the field comes from the interface \"%s\".";
    private static final String ERROR_DATALOADER_ON_RECORD_FIELD =
            "\"%s\" in [[dataloaders]] cannot use a DataLoader, as \"%s\" is generated as a record.";
    private static final String ERROR_INVALID_DATALOADER_NAME =
            "\"%s\" in [[dataloaders]] is not a valid Ballerina function name for \"%s\".";
    private static final Pattern DATALOADER_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String PREFETCH_METHOD_FORMAT = "pre%s%s";
    private static final String PATH_SEPARATOR = ".";
    private static final int TYPE_PART = 0;
    private static final int FIELD_PART = 1;
    private static final int ARGUMENT_PART = 2;
    private static final int FIELD_PATH_LENGTH = 2;
    private static final int ARGUMENT_PATH_LENGTH = 3;
    private static final int KEEP_EMPTY_PARTS = -1;

    private final GraphQLSchema schema;
    private final boolean useRecordsForObjects;
    private final Map<String, String> idTypes;
    private final Map<String, String> dataLoaders;
    private final Set<String> unionMembers;

    private ServiceModelMapper(GraphQLSchema schema, boolean useRecordsForObjects, Map<String, String> idTypes,
                               Map<String, String> dataLoaders) {
        this.schema = schema;
        this.useRecordsForObjects = useRecordsForObjects;
        this.idTypes = idTypes;
        this.dataLoaders = dataLoaders;
        this.unionMembers = schema.getAllTypesAsList().stream()
                .filter(GraphQLUnionType.class::isInstance)
                .flatMap(union -> ((GraphQLUnionType) union).getTypes().stream())
                .map(GraphQLNamedType::getName)
                .collect(Collectors.toSet());
        validate();
    }

    /**
     * Builds the service model for a schema, using {@code string} for every GraphQL {@code ID}.
     *
     * @param schema               the GraphQL schema
     * @param serviceName          the name of the service object type, e.g. the schema file name
     * @param useRecordsForObjects whether object types are generated as records where possible
     * @return the service model
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent
     */
    public static ServiceModel map(GraphQLSchema schema, String serviceName, boolean useRecordsForObjects)
            throws ServiceGenerationException {
        return map(schema, serviceName, useRecordsForObjects, Map.of());
    }

    /**
     * Builds the service model for a schema, with no DataLoaders.
     *
     * @param schema               the GraphQL schema
     * @param serviceName          the name of the service object type, e.g. the schema file name
     * @param useRecordsForObjects whether object types are generated as records where possible
     * @param idTypes              the Ballerina type of each GraphQL {@code ID} field or argument, keyed by its path,
     *                             e.g. {@code Profile.id} or {@code Query.profile.id}; others use {@code string}
     * @return the service model
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent, or an
     *                                    ID type mapping is invalid
     */
    public static ServiceModel map(GraphQLSchema schema, String serviceName, boolean useRecordsForObjects,
                                   Map<String, String> idTypes) throws ServiceGenerationException {
        return map(schema, serviceName, useRecordsForObjects, idTypes, Map.of());
    }

    /**
     * Builds the service model for a schema.
     *
     * @param schema               the GraphQL schema
     * @param serviceName          the name of the service object type, e.g. the schema file name
     * @param useRecordsForObjects whether object types are generated as records where possible
     * @param idTypes              the Ballerina type of each GraphQL {@code ID} field or argument, keyed by its path,
     *                             e.g. {@code Profile.id} or {@code Query.profile.id}; others use {@code string}
     * @param dataLoaders          the DataLoader name of each field that is wired to one, keyed by its path,
     *                             e.g. {@code Query.profile}
     * @return the service model
     * @throws ServiceGenerationException if the schema uses a type or value the service cannot represent, or an
     *                                    ID type or DataLoader mapping is invalid
     */
    public static ServiceModel map(GraphQLSchema schema, String serviceName, boolean useRecordsForObjects,
                                   Map<String, String> idTypes, Map<String, String> dataLoaders)
            throws ServiceGenerationException {
        try {
            return new ServiceModelMapper(schema, useRecordsForObjects, idTypes, dataLoaders).map(serviceName);
        } catch (IllegalArgumentException e) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_102,
                    NullLocation.getInstance(), e.getMessage());
        }
    }

    private ServiceModel map(String serviceName) {
        List<GraphQLNamedType> types = schema.getAllTypesAsList().stream()
                .filter(type -> !type.getName().startsWith(INTROSPECTION_TYPE_PREFIX))
                .filter(type -> !isRootType(type))
                .toList();
        return new ServiceModel(TypeMapper.escapeName(serviceName),
                resolvers(schema.getQueryType(), ResolverKind.RESOURCE_GET),
                resolvers(schema.getMutationType(), ResolverKind.REMOTE),
                resolvers(schema.getSubscriptionType(), ResolverKind.RESOURCE_SUBSCRIBE),
                typeDefinitions(types), inputTypes(types), enums(types));
    }

    private boolean isRootType(GraphQLNamedType type) {
        return Stream.of(schema.getQueryType(), schema.getMutationType(), schema.getSubscriptionType())
                .anyMatch(rootType -> rootType != null && rootType.getName().equals(type.getName()));
    }

    private List<ResolverModel> resolvers(GraphQLObjectType rootType, ResolverKind kind) {
        if (rootType == null) {
            return List.of();
        }
        return resolvers(rootType.getName(), rootType.getFields(), kind);
    }

    private List<ResolverModel> resolvers(String typeName, List<GraphQLFieldDefinition> fields, ResolverKind kind) {
        return fields.stream().map(field -> resolver(typeName, field, kind)).toList();
    }

    private ResolverModel resolver(String typeName, GraphQLFieldDefinition field, ResolverKind kind) {
        List<ParameterModel> parameters = parameters(typeName, field);
        return new ResolverModel(kind, TypeMapper.escapeName(field.getName()), parameters,
                TypeMapper.map(field.getType(), idType(typeName, field.getName())), field.getDescription(),
                field.isDeprecated() ? field.getDeprecationReason() : null,
                dataLoader(typeName, field, parameters));
    }

    private DataLoaderModel dataLoader(String typeName, GraphQLFieldDefinition field,
                                       List<ParameterModel> parameters) {
        String name = dataLoaders.get(path(typeName, field.getName()));
        if (name == null) {
            return null;
        }
        List<String> keys = parameters.stream().map(ParameterModel::name).toList();
        String fieldName = field.getName();
        return new DataLoaderModel(name,
                String.format(PREFETCH_METHOD_FORMAT, Character.toUpperCase(fieldName.charAt(0)),
                        fieldName.substring(1)),
                keys, !keys.isEmpty() && isPlainData(field.getType(), new HashSet<>()));
    }

    private boolean isPlainData(GraphQLType type, Set<String> visited) {
        GraphQLType namedType = GraphQLTypeUtil.unwrapAll(type);
        if (namedType instanceof GraphQLObjectType objectType) {
            return isRecord(objectType) && (!visited.add(objectType.getName())
                    || objectType.getFields().stream().allMatch(field -> isPlainData(field.getType(), visited)));
        }
        return !(namedType instanceof GraphQLInterfaceType) && !(namedType instanceof GraphQLUnionType);
    }

    private List<ParameterModel> parameters(String typeName, GraphQLFieldDefinition field) {
        return field.getArguments().stream()
                .map(argument -> new ParameterModel(TypeMapper.escapeName(argument.getName()),
                        TypeMapper.map(argument.getType(), idType(typeName, path(field.getName(), argument.getName()))),
                        argument.hasSetDefaultValue() ? literal(argument.getArgumentDefaultValue().getValue()) : null,
                        argument.getDescription()))
                .toList();
    }

    // A field or argument without its own mapping uses the mapping of the same member on an implemented interface.
    private String idType(String typeName, String member) {
        return Stream.concat(Stream.of(typeName), interfaceNames(typeName).stream())
                .map(name -> idTypes.get(path(name, member)))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(Scalar.ID.getBallerinaType());
    }

    private List<String> interfaceNames(String typeName) {
        if (schema.getType(typeName) instanceof GraphQLImplementingType implementingType) {
            return implementingType.getInterfaces().stream().map(GraphQLNamedType::getName).toList();
        }
        return List.of();
    }

    private static String path(String... parts) {
        return String.join(PATH_SEPARATOR, parts);
    }

    private void validate() {
        List<String> errors = Stream.concat(errors(idTypes, this::idTypeError),
                errors(dataLoaders, this::dataLoaderError)).toList();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(System.lineSeparator(), errors));
        }
    }

    private static Stream<String> errors(Map<String, String> mappings,
                                         BiFunction<String, String, Optional<String>> check) {
        return mappings.keySet().stream()
                .sorted()
                .map(path -> check.apply(path, mappings.get(path)))
                .flatMap(Optional::stream);
    }

    private Optional<String> dataLoaderError(String path, String name) {
        String[] parts = path.split(Pattern.quote(PATH_SEPARATOR), KEEP_EMPTY_PARTS);
        GraphQLObjectType objectType = parts.length == FIELD_PATH_LENGTH ? schema.getObjectType(parts[TYPE_PART])
                : null;
        if (objectType == null || objectType.getFieldDefinition(parts[FIELD_PART]) == null) {
            return Optional.of(String.format(ERROR_UNKNOWN_DATALOADER_PATH, path));
        }
        if (Stream.of(schema.getMutationType(), schema.getSubscriptionType())
                .anyMatch(rootType -> rootType != null && rootType.getName().equals(objectType.getName()))) {
            return Optional.of(String.format(ERROR_DATALOADER_ON_MUTATION_OR_SUBSCRIPTION, path));
        }
        Optional<String> interfaceName = objectType.getInterfaces().stream()
                .filter(interfaceType -> ((GraphQLInterfaceType) interfaceType)
                        .getFieldDefinition(parts[FIELD_PART]) != null)
                .map(GraphQLNamedType::getName)
                .findFirst();
        if (interfaceName.isPresent()) {
            return Optional.of(String.format(ERROR_DATALOADER_ON_INTERFACE_FIELD, path, interfaceName.get()));
        }
        if (isRecord(objectType)) {
            return Optional.of(String.format(ERROR_DATALOADER_ON_RECORD_FIELD, path, objectType.getName()));
        }
        if (!DATALOADER_NAME.matcher(name).matches() || SyntaxInfo.isKeyword(name)) {
            return Optional.of(String.format(ERROR_INVALID_DATALOADER_NAME, name, path));
        }
        return Optional.empty();
    }

    private Optional<String> idTypeError(String path, String idType) {
        String[] parts = path.split(Pattern.quote(PATH_SEPARATOR), KEEP_EMPTY_PARTS);
        Optional<GraphQLType> target = target(parts);
        if (target.isEmpty()) {
            return Optional.of(String.format(ERROR_UNKNOWN_ID_PATH, path));
        }
        if (!TypeMapper.isId(target.get())) {
            return Optional.of(String.format(ERROR_NOT_AN_ID, path));
        }
        if (!SUPPORTED_ID_TYPES.contains(idType)) {
            return Optional.of(String.format(ERROR_UNSUPPORTED_ID_TYPE, idType, path));
        }
        String member = path.substring(parts[TYPE_PART].length() + PATH_SEPARATOR.length());
        return interfaceNames(parts[TYPE_PART]).stream()
                .filter(interfaceName -> target(path(interfaceName, member).split(Pattern.quote(PATH_SEPARATOR)))
                        .isPresent())
                .filter(interfaceName -> !idType(interfaceName, member).equals(idType))
                .findFirst()
                .map(interfaceName -> String.format(ERROR_ID_TYPE_MISMATCH, path, path(interfaceName, member),
                        idType(interfaceName, member)));
    }

    private Optional<GraphQLType> target(String[] parts) {
        if (parts.length != FIELD_PATH_LENGTH && parts.length != ARGUMENT_PATH_LENGTH) {
            return Optional.empty();
        }
        GraphQLType type = schema.getType(parts[TYPE_PART]);
        if (type instanceof GraphQLInputObjectType inputType && parts.length == FIELD_PATH_LENGTH) {
            return Optional.ofNullable(inputType.getField(parts[FIELD_PART])).map(GraphQLInputObjectField::getType);
        }
        if (!(type instanceof GraphQLFieldsContainer container)) {
            return Optional.empty();
        }
        GraphQLFieldDefinition field = container.getFieldDefinition(parts[FIELD_PART]);
        if (field == null) {
            return Optional.empty();
        }
        if (parts.length == FIELD_PATH_LENGTH) {
            return Optional.of(field.getType());
        }
        return Optional.ofNullable(field.getArgument(parts[ARGUMENT_PART])).map(GraphQLArgument::getType);
    }

    private List<TypeDefinitionModel> typeDefinitions(List<GraphQLNamedType> types) {
        List<TypeDefinitionModel> definitions = new ArrayList<>();
        for (GraphQLNamedType type : types) {
            if (type instanceof GraphQLObjectType objectType) {
                List<GraphQLNamedOutputType> interfaces = directInterfaces(objectType.getInterfaces());
                checkSharedFields(objectType.getName(), interfaces);
                definitions.add(new TypeDefinitionModel(TypeMapper.escapeName(objectType.getName()),
                        isRecord(objectType) ? TypeKind.RECORD : TypeKind.SERVICE_CLASS,
                        resolvers(objectType.getName(), objectType.getFields(), ResolverKind.RESOURCE_GET),
                        names(interfaces), objectType.getDescription()));
            } else if (type instanceof GraphQLInterfaceType interfaceType) {
                List<GraphQLNamedOutputType> interfaces = directInterfaces(interfaceType.getInterfaces());
                checkSharedFields(interfaceType.getName(), interfaces);
                definitions.add(new TypeDefinitionModel(TypeMapper.escapeName(interfaceType.getName()),
                        TypeKind.INTERFACE,
                        resolvers(interfaceType.getName(), ownFields(interfaceType), ResolverKind.RESOURCE_GET),
                        names(interfaces), interfaceType.getDescription()));
            } else if (type instanceof GraphQLUnionType unionType) {
                definitions.add(new TypeDefinitionModel(TypeMapper.escapeName(unionType.getName()), TypeKind.UNION,
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

    private static List<GraphQLNamedOutputType> directInterfaces(List<GraphQLNamedOutputType> interfaces) {
        Set<String> inherited = interfaces.stream()
                .flatMap(interfaceType -> ((GraphQLInterfaceType) interfaceType).getInterfaces().stream())
                .map(GraphQLNamedType::getName)
                .collect(Collectors.toSet());
        return interfaces.stream().filter(interfaceType -> !inherited.contains(interfaceType.getName())).toList();
    }

    private static void checkSharedFields(String typeName, List<GraphQLNamedOutputType> interfaces) {
        Map<String, GraphQLInterfaceType> owners = new HashMap<>();
        for (GraphQLNamedOutputType namedType : interfaces) {
            GraphQLInterfaceType interfaceType = (GraphQLInterfaceType) namedType;
            for (GraphQLFieldDefinition field : interfaceType.getFields()) {
                GraphQLInterfaceType owner = owners.putIfAbsent(field.getName(), interfaceType);
                if (owner != null) {
                    throw new IllegalArgumentException(
                            sharedFieldError(typeName, owner, interfaceType, field.getName()));
                }
            }
        }
    }

    private static String sharedFieldError(String typeName, GraphQLInterfaceType first, GraphQLInterfaceType second,
                                           String fieldName) {
        String declaringInterface = declaringInterface(first, fieldName);
        if (declaringInterface.equals(declaringInterface(second, fieldName))) {
            return String.format(ERROR_SHARED_PARENT_INTERFACE_FIELD, typeName, first.getName(), second.getName(),
                    fieldName, declaringInterface);
        }
        return String.format(ERROR_SHARED_INTERFACE_FIELD, typeName, first.getName(), second.getName(), fieldName);
    }

    // Follows a field up through the parent interfaces to the interface that declares it.
    private static String declaringInterface(GraphQLInterfaceType interfaceType, String fieldName) {
        return interfaceType.getInterfaces().stream()
                .map(GraphQLInterfaceType.class::cast)
                .filter(parent -> parent.getFieldDefinition(fieldName) != null)
                .findFirst()
                .map(parent -> declaringInterface(parent, fieldName))
                .orElse(interfaceType.getName());
    }

    private static List<GraphQLFieldDefinition> ownFields(GraphQLInterfaceType interfaceType) {
        Set<String> inherited = interfaceType.getInterfaces().stream()
                .flatMap(parent -> ((GraphQLInterfaceType) parent).getFields().stream())
                .map(GraphQLFieldDefinition::getName)
                .collect(Collectors.toSet());
        return interfaceType.getFields().stream().filter(field -> !inherited.contains(field.getName())).toList();
    }

    private static List<String> names(List<? extends GraphQLNamedType> types) {
        return types.stream().map(type -> TypeMapper.escapeName(type.getName())).toList();
    }

    private List<InputTypeModel> inputTypes(List<GraphQLNamedType> types) {
        return types.stream()
                .filter(GraphQLInputObjectType.class::isInstance)
                .map(GraphQLInputObjectType.class::cast)
                .map(inputType -> new InputTypeModel(TypeMapper.escapeName(inputType.getName()),
                        inputType.getFields().stream().map(field -> inputField(inputType.getName(), field)).toList(),
                        inputType.getDescription()))
                .toList();
    }

    private FieldModel inputField(String typeName, GraphQLInputObjectField field) {
        return new FieldModel(TypeMapper.escapeName(field.getName()),
                TypeMapper.map(field.getType(), idType(typeName, field.getName())),
                field.hasSetDefaultValue() ? literal(field.getInputFieldDefaultValue().getValue()) : null,
                field.getDescription(), field.isDeprecated() ? field.getDeprecationReason() : null);
    }

    private static List<EnumModel> enums(List<GraphQLNamedType> types) {
        return types.stream()
                .filter(GraphQLEnumType.class::isInstance)
                .map(GraphQLEnumType.class::cast)
                .map(enumType -> new EnumModel(TypeMapper.escapeName(enumType.getName()),
                        enumType.getValues().stream().map(ServiceModelMapper::enumValue).toList(),
                        enumType.getDescription()))
                .toList();
    }

    private static EnumValueModel enumValue(GraphQLEnumValueDefinition value) {
        return new EnumValueModel(TypeMapper.escapeName(value.getName()), value.getDescription(),
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
            return TypeMapper.escapeName(enumValue.getName());
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
                    .map(field -> String.format(MAPPING_FIELD_FORMAT, TypeMapper.escapeName(field.getName()),
                            literal(field.getValue())))
                    .collect(Collectors.joining(VALUE_SEPARATOR, MAPPING_START, MAPPING_END));
        }
        throw new IllegalArgumentException(String.format(ERROR_UNSUPPORTED_DEFAULT_VALUE, value));
    }
}
