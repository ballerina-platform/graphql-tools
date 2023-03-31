/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.graphql.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class represents GraphQL client code generation related constants.
 */
public class CodeGeneratorConstants {
    public static final String GRAPHQL_API = "GraphqlApi";
    public static final String ROOT_PROJECT_NAME = "root";
    public static final String MODULES_PATH = "/modules/";
    public static final String CLIENT_FILE_NAME = "client.bal";
    public static final String SERVICE_FILE_NAME = "service.bal";
    public static final String TYPES_FILE_NAME = "types.bal";
    public static final String UTILS_FILE_NAME = "utils.bal";
    public static final String CONFIG_TYPES_FILE_NAME = "config_types.bal";
    public static final String SCHEMA_PATTERN = "schema";
    public static final String DOCUMENTS_PATTERN = "documents";
    public static final String GET = "get";
    public static final String SUBSCRIBE = "subscribe";
    public static final String ID = "id";

    public static final String SLASH = "/";
    public static final String SEMICOLON = ";";
    public static final String WHITESPACE = " ";
    public static final String EMPTY_STRING = "";
    public static final String EQUAL = "=";
    public static final String COMMA = ",";
    public static final String NEW_LINE = "\n";
    public static final String QUESTION_MARK = "?";
    public static final String EMPTY_EXPRESSION = "{}";
    public static final String NULLABLE_EXPRESSION = "()";
    public static final String IMPORT = "import";
    public static final String BALLERINA = "ballerina";
    public static final String BALLERINAX = "ballerinax";
    public static final String HTTP = "http";
    public static final String GRAPHQL = "graphql";
    public static final String CLIENT_CLASS_PREFIX = "Graphql";
    public static final String IDL_PLUGIN_CLIENT = "'client";
    public static final String CLIENT = "Client";
    public static final String SCHEMA = "schema";
    public static final String LISTENER = "Listener";
    public static final String GRAPHQL_CLIENT = "graphqlClient";
    public static final String INIT = "init";
    public static final String INIT_RETURN_TYPE = "graphql:ClientError";
    public static final String SELF = "self";
    public static final String CLIENT_EP = "clientEp";
    public static final String QUERY_VAR_NAME = "query";
    public static final String GRAPHQL_RESPONSE_VAR_NAME = "graphqlResponse";
    public static final String CLONE_READ_ONLY = "cloneReadOnly";

    public static final String FRAGMENT = "Fragment";
    public static final String MUTATION = "Mutation";
    public static final String QUERY = "Query";
    public static final String SUBSCRIPTION = "Subscription";
    public static final String REPRESENTS = "# Represents ";
    public static final String RESPONSE = "Response";
    public static final String HTTP_CLIENT_CONFIG_TYPE_NAME = "graphql:ClientConfiguration";
    public static final String CONNECTION_CONFIG_PARAM_NAME = "connectionConfig";
    public static final String SERVICE_URL_TYPE_NAME = "string";
    public static final String SERVICE_URL_PARAM_NAME = "serviceUrl";
    public static final String GRAPHQL_RESPONSE_TYPE_NAME = "json";
    public static final String GRAPHQL_RESPONSE_PARAM_NAME = "graphqlResponse";
    public static final String TARGET_TYPE_PARAM_TYPE_NAME = "typedesc<graphql:DataResponse>";
    public static final String TARGET_TYPE_PARAM_NAME = "targetType";
    public static final String GRAPHQL_CLIENT_TYPE_NAME = "graphql:Client";
    public static final String GRAPHQL_SERVICE_TYPE_NAME = "graphql:Service";
    public static final String GRAPHQL_LISTENER_TYPE_NAME = "graphql:Listener";
    public static final String GRAPHQL_CLIENT_VAR_NAME = "clientEp";
    public static final String GRAPHQL_CLIENT_CONFIGURATION_VAR_NAME = "graphqlClientConfig";
    public static final String GRAPHQL_VARIABLES_TYPE_NAME = "map<anydata>";
    public static final String GRAPHQL_VARIABLES_VAR_NAME = "variables";
    public static final String HEADER_VALUES_VARIABLES_TYPE_NAME = "map<any>";
    public static final String HEADER_VALUES_VARIABLES_VAR_NAME = "headerValues";
    public static final String HTTP_HEADERS_VARIABLES_TYPE_NAME = "map<string|string[]>";
    public static final String HTTP_HEADERS_VARIABLES_VAR_NAME = "httpHeaders";
    public static final String API_KEY_CONFIG_PARAM = "apiKeysConfig";
    public static final String CONNECTION_CONFIG_TYPE_NAME = "ConnectionConfig";
    public static final String CONFIG_PARAM_NAME = "config";

    public static final String API_KEYS_CONFIG_TYPE_NAME = "ApiKeysConfig";
    public static final String API_KEYS_CONFIG_PARAM_NAME = "apiKeysConfig";

    public static final String DISPLAY_ANNOTATION_NAME = "display";
    public static final String DISPLAY_ANNOTATION_LABEL_NAME = "label";
    public static final String DISPLAY_ANNOTATION_KIND_FIELD = "kind";
    public static final String DISPLAY_ANNOTATION_KIND_PASSWORD = "password";

    public static final String PORT = "port";
    public static final String PORT_NUMBER_DEFAULT = "9090";
    public static final String DEPRECATED = "deprecated";

    // OS specific line separator
    public static final String LINE_SEPARATOR = System.lineSeparator();

    // TODO: Update keywords if Ballerina Grammer changes
    private static final String[] KEYWORDS = new String[]{"abort", "aborted", "abstract", "all", "annotation",
            "any", "anydata", "boolean", "break", "byte", "catch", "channel", "check", "checkpanic", "client",
            "committed", "const", "continue", "decimal", "else", "error", "external", "fail", "final", "finally",
            "float", "flush", "fork", "function", "future", "handle", "if", "import", "in", "int", "is", "join",
            "json", "listener", "lock", "match", "new", "object", "OBJECT_INIT", "onretry", "parameter", "panic",
            "private", "public", "record", "remote", "resource", "retries", "retry", "return", "returns", "service",
            "source", "start", "stream", "string", "table", "transaction", "try", "type", "typedesc", "typeof",
            "trap", "throw", "wait", "while", "with", "worker", "var", "version", "xml", "xmlns", "BOOLEAN_LITERAL",
            "NULL_LITERAL", "ascending", "descending", "foreach", "map", "group", "from", "default", "field",
            "limit", "as", "on", "isolated", "readonly", "distinct", "where", "select", "do", "transactional"
            , "commit", "enum", "base16", "base64", "rollback", "configurable", "class", "module", "never",
            "outer", "order", "null", "key", "let", "by", "equals"};

    private static final String[] TYPES = new String[]{"int", "any", "anydata", "boolean", "byte", "float", "int",
            "json", "string", "table", "var", "xml"};

    public static final List<String> BAL_KEYWORDS;
    public static final List<String> BAL_TYPES;

    static {
        BAL_KEYWORDS = Collections.unmodifiableList(Arrays.asList(KEYWORDS));
        BAL_TYPES = Collections.unmodifiableList(Arrays.asList(TYPES));
    }

    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~`*\\-=^+();:\\/\\_{}\\s|.$])";
    public static final String SPECIAL_CHAR_REGEX = "([\\[\\]\\\\?!<>@#&~`*\\-=^+();:\\/\\_{}\\s|.$])";

    /**
     * Enum to select the relevant ballerina http auth record.
     */
    public enum AuthConfigType {
        NONE("None"),
        API_KEY("ApiKeysConfig"),
        BASIC("graphql:CredentialsConfig"),
        BEARER("graphql:BearerTokenConfig"),
        JWT_ISSUER("JwtIssuerConfig"),
        OAUTH2_CLIENT_CREDENTIAL("OAuth2ClientCredentialsGrantConfig"),
        OAUTH2_PASSWORD("OAuth2PasswordGrantConfig"),
        OAUTH2_REFRESH_TOKEN("OAuth2RefreshTokenGrantConfig"),
        OAUTH2_JWT_BEARER("OAuth2JwtBearerGrantConfig");

        private final String authType;

        AuthConfigType(String authType) {
            this.authType = authType;
        }

        public String getValue() {
            return authType;
        }
    }
}
