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

package io.ballerina.graphql.cmd.config;

import io.ballerina.graphql.exception.ParseException;
import io.ballerina.toml.api.Toml;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reads a balGraphQL.toml configuration file.
 */
public class BalGraphqlConfigReader {

    private static final String HTTPS_PREFIX = "https://";
    private static final String KEY_SEPARATOR = ".";
    private static final String SCHEMA_KEY = "schema";
    private static final String SOURCE_KEY = "source";
    private static final String PATH_KEY = "path";
    private static final String URL_KEY = "url";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String HEADERS_KEY = "headers";
    private static final String DOCUMENTS_KEY = "documents";
    private static final String ID_TYPES_KEY = "id-types";
    private static final String DATALOADERS_KEY = "dataloaders";
    private static final String FIELD_KEY = "field";
    private static final String TYPE_KEY = "type";
    private static final String LOADER_KEY = "loader";
    private static final String ID_TYPES_EXAMPLE = "[[id-types]]%nfield = \"Profile.id\"%ntype = \"int\"";
    private static final String DATALOADERS_EXAMPLE =
            "[[dataloaders]]%nfield = \"Query.profile\"%nloader = \"profileLoader\"";
    private static final String ERROR_MISSING_SCHEMA_SECTION =
            "The balGraphQL.toml file is missing the [schema] section.";
    private static final String ERROR_INVALID_SCHEMA_SOURCE =
            "\"%s\" is not a supported value for the schema source. It should be one of \"file\", \"url\" "
                    + "or \"introspection\".";
    private static final String ERROR_MISSING_REQUIRED_FIELD =
            "Required field \"%s\" is not provided in the config file.";
    private static final String WARNING_INSECURE_SCHEMA_URL =
            "Warning: The \"%s\" field (\"%s\") uses HTTP instead of HTTPS. If this endpoint requires headers such "
                    + "as an authorization token, they will be sent unencrypted over the network. Using HTTPS is "
                    + "strongly recommended whenever the endpoint supports it.";
    private static final String ERROR_INVALID_CONFIG_FILE =
            "The balGraphQL.toml file could not be read:%n%s";
    private static final String ERROR_UNPARSABLE_CONFIG_FILE =
            "The balGraphQL.toml file could not be parsed. Check it for unclosed brackets, unclosed quotes or "
                    + "incomplete entries.";
    private static final String ERROR_NON_STRING_FIELD =
            "The \"%s\" field should be a string. Found \"%s\".";
    private static final String ERROR_NON_LIST_FIELD =
            "The \"%s\" field should be a list of strings. Found \"%s\".";
    private static final String ERROR_NON_STRING_TABLE_VALUE = "The value of \"%s\" in [%s] must be a string.";
    private static final String ERROR_INVALID_MAPPING_SECTION =
            "The \"%s\" section should be a list of [[%s]] entries, each with \"field\" and \"%s\". "
                    + "For example:%n%s";
    private static final String ERROR_MISSING_MAPPING_FIELD =
            "Required field \"%s\" is not provided in [[%s]] entry %d.";
    private static final String ERROR_NON_STRING_MAPPING_FIELD =
            "The \"%s\" field in [[%s]] entry %d should be a string. Found \"%s\".";
    private static final String ERROR_DUPLICATE_MAPPING_FIELD = "\"%s\" is given more than once in [[%s]].";

    private BalGraphqlConfigReader() {}

    public static BalGraphqlConfig read(Path configPath, PrintStream outStream) throws IOException, ParseException {
        Map<String, Object> content = readContent(configPath);
        return new BalGraphqlConfig(readSchemaConfig(content, outStream), readDocuments(content),
                readMappings(content, ID_TYPES_KEY, TYPE_KEY, ID_TYPES_EXAMPLE),
                readMappings(content, DATALOADERS_KEY, LOADER_KEY, DATALOADERS_EXAMPLE));
    }

    private static Map<String, Object> readContent(Path configPath) throws IOException, ParseException {
        Toml toml;
        try {
            toml = Toml.read(configPath);
        } catch (RuntimeException e) {
            throw new ParseException(ERROR_UNPARSABLE_CONFIG_FILE);
        }
        List<Diagnostic> diagnostics = toml.diagnostics();
        if (!diagnostics.isEmpty()) {
            String errors = diagnostics.stream()
                    .map(Diagnostic::toString)
                    .collect(Collectors.joining(System.lineSeparator()));
            throw new ParseException(String.format(ERROR_INVALID_CONFIG_FILE, errors));
        }
        return toml.toMap();
    }

    private static SchemaConfig readSchemaConfig(Map<String, Object> content, PrintStream outStream)
            throws ParseException {
        Object schemaValue = content.get(SCHEMA_KEY);
        if (!(schemaValue instanceof Map)) {
            throw new ParseException(ERROR_MISSING_SCHEMA_SECTION);
        }
        Map<String, Object> schema = castToMap(schemaValue);
        SchemaConfig schemaConfig = new SchemaConfig(readSource(schema), readString(schema, PATH_KEY),
                readString(schema, URL_KEY), readString(schema, ENDPOINT_KEY),
                readHeaders(schema));
        validateSchemaConfig(schemaConfig, outStream);
        return schemaConfig;
    }

    private static SchemaSource readSource(Map<String, Object> schema) throws ParseException {
        String source = readString(schema, SOURCE_KEY);
        requireField(source, SOURCE_KEY);
        return SchemaSource.fromValue(source).orElseThrow(
                () -> new ParseException(String.format(ERROR_INVALID_SCHEMA_SOURCE, source)));
    }

    private static void validateSchemaConfig(SchemaConfig schemaConfig, PrintStream outStream)
            throws ParseException {
        switch (schemaConfig.source()) {
            case FILE:
                requireField(schemaConfig.path(), PATH_KEY);
                break;
            case URL:
                requireField(schemaConfig.url(), URL_KEY);
                warnIfInsecure(schemaConfig.url(), URL_KEY, outStream);
                break;
            case INTROSPECTION:
                requireField(schemaConfig.endpoint(), ENDPOINT_KEY);
                warnIfInsecure(schemaConfig.endpoint(), ENDPOINT_KEY, outStream);
                break;
            default:
                break;
        }
    }

    private static void requireField(String value, String field) throws ParseException {
        if (value == null || value.isBlank()) {
            throw new ParseException(String.format(ERROR_MISSING_REQUIRED_FIELD, field));
        }
    }

    private static void warnIfInsecure(String value, String field, PrintStream outStream) {
        if (!value.startsWith(HTTPS_PREFIX)) {
            outStream.println(String.format(WARNING_INSECURE_SCHEMA_URL, field, value));
        }
    }

    private static List<String> readDocuments(Map<String, Object> content) throws ParseException {
        Object documents = content.get(DOCUMENTS_KEY);
        if (documents == null) {
            return null;
        }
        if (!(documents instanceof List)) {
            throw new ParseException(String.format(ERROR_NON_LIST_FIELD, DOCUMENTS_KEY, documents));
        }
        List<String> paths = new ArrayList<>();
        for (Object document : (List<?>) documents) {
            if (!(document instanceof String)) {
                throw new ParseException(String.format(ERROR_NON_STRING_FIELD, DOCUMENTS_KEY, document));
            }
            paths.add((String) document);
        }
        return paths;
    }

    private static Map<String, String> readHeaders(Map<String, Object> schema) throws ParseException {
        Object table = schema.get(HEADERS_KEY);
        if (!(table instanceof Map)) {
            return null;
        }
        Map<String, String> headers = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        for (Map.Entry<String, Object> entry : castToMap(table).entrySet()) {
            if (entry.getValue() instanceof String value) {
                headers.put(entry.getKey(), value);
            } else {
                errors.add(String.format(ERROR_NON_STRING_TABLE_VALUE, entry.getKey(),
                        SCHEMA_KEY + KEY_SEPARATOR + HEADERS_KEY));
            }
        }
        throwIfAny(errors);
        return headers;
    }

    private static Map<String, String> readMappings(Map<String, Object> content, String section, String valueKey,
                                                    String example) throws ParseException {
        Object entries = content.get(section);
        if (entries == null) {
            return null;
        }
        if (!(entries instanceof List<?> list) || !list.stream().allMatch(Map.class::isInstance)) {
            throw new ParseException(String.format(ERROR_INVALID_MAPPING_SECTION, section, section, valueKey,
                    String.format(example)));
        }
        Map<String, String> mappings = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            Map<String, Object> entry = castToMap(list.get(index));
            int entryNumber = index + 1;
            Optional<String> field = readMappingField(entry, FIELD_KEY, section, entryNumber, errors);
            Optional<String> value = readMappingField(entry, valueKey, section, entryNumber, errors);
            if (field.isPresent() && value.isPresent()
                    && mappings.putIfAbsent(field.get(), value.get()) != null) {
                errors.add(String.format(ERROR_DUPLICATE_MAPPING_FIELD, field.get(), section));
            }
        }
        throwIfAny(errors);
        return mappings;
    }

    private static Optional<String> readMappingField(Map<String, Object> entry, String key, String section,
                                                     int entryNumber, List<String> errors) {
        Object value = entry.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return Optional.of(text);
        }
        errors.add(value == null || value instanceof String
                ? String.format(ERROR_MISSING_MAPPING_FIELD, key, section, entryNumber)
                : String.format(ERROR_NON_STRING_MAPPING_FIELD, key, section, entryNumber, value));
        return Optional.empty();
    }

    private static void throwIfAny(List<String> errors) throws ParseException {
        if (!errors.isEmpty()) {
            throw new ParseException(String.join(System.lineSeparator(), errors));
        }
    }

    private static String readString(Map<String, Object> content, String key) throws ParseException {
        Object value = content.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new ParseException(String.format(ERROR_NON_STRING_FIELD, key, value));
        }
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToMap(Object value) {
        return (Map<String, Object>) value;
    }
}
