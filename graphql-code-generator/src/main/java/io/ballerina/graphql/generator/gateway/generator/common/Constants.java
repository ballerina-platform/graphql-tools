package io.ballerina.graphql.generator.gateway.generator.common;

/**
 * Class to store constants used in gateway generation.
 */
public class Constants {
    public static final String QUERY_PLACEHOLDER = "@\\{query}";
    public static final String FUNCTION_PARAM_PLACEHOLDER = "@\\{params}";
    public static final String RESPONSE_TYPE_PLACEHOLDER = "@\\{responseType}";
    public static final String BASIC_RESPONSE_TYPE_PLACEHOLDER = "@\\{basicResponseType}";
    public static final String CLIENT_NAME_PLACEHOLDER = "@\\{clientName}";
    public static final String CLIENT_NAME_VALUE_PLACEHOLDER = "@\\{clientNameValue}";
    public static final String QUERY_ARGS_PLACEHOLDER = "@\\{queryArgs}";
    public static final String URL_PLACEHOLDER = "@\\{url}";
    public static final String RESOURCE_FUNCTIONS_PLACEHOLDER = "@\\{resourceFunctions}";

    public static final String MATCH_CLIENT_STATEMENTS_PLACEHOLDER = "@\\{matchClientStatements}";
    // Constants for the gateway service generation.
    public static final String CONFIGURABLE_PORT_STATEMENT = "configurable int PORT = 9000;";
    public static final String BALLERINA_GRAPHQL_IMPORT_STATEMENT = "import ballerina/graphql;";
    public static final String BALLERINA_LOG_IMPORT_STATEMENT = "import ballerina/log;";
    public static final String GRAPHQL_CLIENT_DECLARATION_STATEMENT =
            "final graphql:Client " + CLIENT_NAME_PLACEHOLDER +
                    "_CLIENT = check new graphql:Client(\"" + URL_PLACEHOLDER + "\");";

    // Constants for the gateway query plan generation.
    public static final String CLIENT_NAME_DECLARATION = "public const string " + CLIENT_NAME_PLACEHOLDER
            + " = \"" + CLIENT_NAME_VALUE_PLACEHOLDER + "\";";

    public static final String ISOLATED_SERVICE_TEMPLATE = "isolated service on new graphql:Listener(PORT) {" +
            RESOURCE_FUNCTIONS_PLACEHOLDER +
            "}";
    public static final String MATCH_CLIENT_STATEMENT_TEMPLATE =
            "\"" + CLIENT_NAME_VALUE_PLACEHOLDER + "\" => {return " + CLIENT_NAME_PLACEHOLDER + "_CLIENT;}";

    // File names for templates
    public static final String RESOURCE_FUNCTION_TEMPLATE_FILE = "resource_function.bal.partial";
    public static final String GET_CLIENT_FUNCTION_TEMPLATE_FILE = "get_client_function.bal.partial";
    public static final String SERVICE_DECLARATION_TEMPLATE_FILE = "service_declaration.bal.partial";
    public static final String GATEWAY_PROJECT_TEMPLATE_DIRECTORY = "gateway";
    public static final String[] GATEWAY_PROJECT_TEMPLATE_FILES = {
            "Ballerina.toml",
            "resolver.bal",
            "utils.bal",
            "records.bal",
            "queryFieldClassifier.bal"
    };
    public static final String GATEWAY_TEMPLATE_FILES_DIRECTORY = "gateway_templates";

}
