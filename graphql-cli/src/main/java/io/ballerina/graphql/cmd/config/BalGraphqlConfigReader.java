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


/**
 * Reads a balGraphQL.toml configuration file.
 */
public class BalGraphqlConfigReader {

    private static final String HTTPS_PREFIX = "https://";
    private static final String MESSAGE_FOR_MISSING_SCHEMA_SECTION =
            "The balGraphQL.toml file is missing the [schema] section.";
    private static final String MESSAGE_FOR_INVALID_SCHEMA_SOURCE =
            "\"%s\" is not a supported value for the schema source. It should be one of \"file\", \"url\" "
                    + "or \"introspection\".";
    private static final String MESSAGE_FOR_MISSING_SCHEMA_FIELD =
            "The balGraphQL.toml file is configured with \"source = \\\"%s\\\"\" "
                    + "but the \"%s\" field is missing.";
    private static final String MESSAGE_FOR_INSECURE_SCHEMA_URL =
            "Warning: The \"%s\" field (\"%s\") uses HTTP instead of HTTPS. If this endpoint requires headers such "
                    + "as an authorization token, they will be sent unencrypted over the network. Using HTTPS is "
                    + "strongly recommended whenever the endpoint supports it.";
    private static final String MESSAGE_FOR_INVALID_CONFIG_FILE =
            "The balGraphQL.toml file could not be read. %s";
    private static final String MESSAGE_FOR_UNPARSABLE_CONFIG_FILE =
            "The balGraphQL.toml file could not be parsed. Check it for unclosed brackets, unclosed quotes or "
                    + "incomplete entries.";
    private static final String MESSAGE_FOR_NON_STRING_FIELD =
            "The \"%s\" field should be a string. Found \"%s\".";
    private static final String MESSAGE_FOR_NON_LIST_FIELD =
            "The \"%s\" field should be a list of strings. Found \"%s\".";

    private BalGraphqlConfigReader() {}

    public static BalGraphqlConfig read(Path configPath, PrintStream outStream) throws IOException, ParseException {
        Map<String, Object> content = readContent(configPath);
        return new BalGraphqlConfig(readSchemaConfig(content, outStream), readDocuments(content),
                readStringTable(content, "id-types"), readStringTable(content, "dataloaders"));
    }

    /**
     * Parses the configuration file, reporting the syntax errors the TOML parser found. The parser reports a
     * malformed file through its diagnostics rather than by failing, and can itself fail on a file it cannot
     * position an error in, so both are surfaced as a parse error against the configuration file.
     */
    private static Map<String, Object> readContent(Path configPath) throws IOException, ParseException {
        Toml toml;
        try {
            toml = Toml.read(configPath);
        } catch (RuntimeException e) {
            throw new ParseException(MESSAGE_FOR_UNPARSABLE_CONFIG_FILE);
        }
        List<Diagnostic> diagnostics = toml.diagnostics();
        if (!diagnostics.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_FOR_INVALID_CONFIG_FILE, diagnostics.get(0).toString()));
        }
        return toml.toMap();
    }

    private static SchemaConfig readSchemaConfig(Map<String, Object> content, PrintStream outStream)
            throws ParseException {
        Object schemaValue = content.get("schema");
        if (!(schemaValue instanceof Map)) {
            throw new ParseException(MESSAGE_FOR_MISSING_SCHEMA_SECTION);
        }
        Map<String, Object> schema = castToMap(schemaValue);
        SchemaConfig schemaConfig = new SchemaConfig(readSource(schema), readString(schema, "path"),
                readString(schema, "url"), readString(schema, "endpoint"), readStringTable(schema, "headers"));
        validateSchemaConfig(schemaConfig, outStream);
        return schemaConfig;
    }

    private static SchemaSource readSource(Map<String, Object> schema) throws ParseException {
        String source = readString(schema, "source");
        return SchemaSource.fromValue(source).orElseThrow(
                () -> new ParseException(String.format(MESSAGE_FOR_INVALID_SCHEMA_SOURCE, source)));
    }

    private static void validateSchemaConfig(SchemaConfig schemaConfig, PrintStream outStream)
            throws ParseException {
        switch (schemaConfig.getSource()) {
            case FILE:
                requireField(schemaConfig.getPath(), SchemaSource.FILE, "path");
                break;
            case URL:
                requireField(schemaConfig.getUrl(), SchemaSource.URL, "url");
                warnIfInsecure(schemaConfig.getUrl(), "url", outStream);
                break;
            case INTROSPECTION:
                requireField(schemaConfig.getEndpoint(), SchemaSource.INTROSPECTION, "endpoint");
                warnIfInsecure(schemaConfig.getEndpoint(), "endpoint", outStream);
                break;
            default:
                break;
        }
    }

    private static void requireField(String value, SchemaSource source, String field) throws ParseException {
        if (value == null || value.isBlank()) {
            throw new ParseException(String.format(MESSAGE_FOR_MISSING_SCHEMA_FIELD, source.getValue(), field));
        }
    }

    /**
     * Warns, rather than rejects, when a schema URL does not use HTTPS. Using plain HTTP is the responsibility of
     * the user configuring the endpoint; the tool surfaces the risk instead of blocking generation.
     */
    private static void warnIfInsecure(String value, String field, PrintStream outStream) {
        if (!value.startsWith(HTTPS_PREFIX)) {
            outStream.println(String.format(MESSAGE_FOR_INSECURE_SCHEMA_URL, field, value));
        }
    }

    private static List<String> readDocuments(Map<String, Object> content) throws ParseException {
        Object documents = content.get("documents");
        if (documents == null) {
            return null;
        }
        if (!(documents instanceof List)) {
            throw new ParseException(String.format(MESSAGE_FOR_NON_LIST_FIELD, "documents", documents));
        }
        List<String> paths = new ArrayList<>();
        for (Object document : (List<?>) documents) {
            if (!(document instanceof String)) {
                throw new ParseException(String.format(MESSAGE_FOR_NON_STRING_FIELD, "documents", document));
            }
            paths.add((String) document);
        }
        return paths;
    }

    private static Map<String, String> readStringTable(Map<String, Object> content, String key) {
        Object table = content.get(key);
        if (!(table instanceof Map)) {
            return null;
        }
        Map<String, String> values = new LinkedHashMap<>();
        flattenTable(castToMap(table), "", values);
        return values;
    }

    /**
     * Flattens a nested table into dotted keys. A GraphQL field notation such as "Profile.id" is read by the TOML
     * parser as a nested table, so it is flattened back into the notation used in the configuration file.
     */
    private static void flattenTable(Map<String, Object> table, String prefix, Map<String, String> values) {
        for (Map.Entry<String, Object> entry : table.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenTable(castToMap(value), key, values);
            } else if (value != null) {
                values.put(key, value.toString());
            }
        }
    }

    private static String readString(Map<String, Object> content, String key) throws ParseException {
        Object value = content.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new ParseException(String.format(MESSAGE_FOR_NON_STRING_FIELD, key, value));
        }
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToMap(Object value) {
        return (Map<String, Object>) value;
    }
}
