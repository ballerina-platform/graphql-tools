package io.ballerina.graphql.generator.gateway.generator;

/**
 * Class to store constants used in gateway generation.
 */
public class Constants {
    public static final String QUERY_PLACEHOLDER = "@\\{query}";
    public static final String RESPONSE_TYPE_PLACEHOLDER = "@\\{responseType}";
    public static final String BASIC_RESPONSE_TYPE_PLACEHOLDER = "@\\{basicResponseType}";
    public static final String CLIENT_NAME_PLACEHOLDER = "@\\{clientName}";
    public static final String QUERY_ARGS_PLACEHOLDER = "@\\{queryArgs}";
    public static final String URL_PLACEHOLDER = "@\\{url}";

    public static final String CLIENT_NOT_FOUND_PANIC_BLOCK = "{ panic error(\"Client not found\"); }";
    public static final String CONFIGURABLE_PORT_STATEMENT = "configurable int PORT = 9000;";
    public static final String BALLERINA_GRAPHQL_IMPORT_STATEMENT = "import ballerina/graphql;";
    public static final String GRAPHQL_CLIENT_DECLARATION_STATEMENT =
            "final graphql:Client " + CLIENT_NAME_PLACEHOLDER +
                    "_CLIENT = check new graphql:Client(\"" + URL_PLACEHOLDER + "\");";
}
