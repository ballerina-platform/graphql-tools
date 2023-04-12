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

package io.ballerina.graphql.cmd;

/**
 * This class represents GraphQL command line tool related constants.
 */
public class Constants {

    // GraphQL command line tool messages
    public static final String MESSAGE_FOR_MISSING_INPUT_ARGUMENT = "The input file path argument is missing. " +
            "Please provide the path of the GraphQL config file, Ballerina service file or GraphQL schema file " +
            "with -i or --input flag. \ne.g: bal graphql --input <GraphQL configuration file>";
    public static final String MESSAGE_FOR_INVALID_FILE_EXTENSION =
            "File \"%s\" is invalid. Supported files are,\nA GraphQL configuration file " +
            "with .yaml/.yml extension, a Ballerina service file with .bal or a GraphQL schema file with .graphql is " +
            "required as the input. \nPlease provide the path of the GraphQL config file with -i or --input flag." +
            "\ne.g: bal graphql --input <GraphQL configuration file>";
    public static final String MESSAGE_FOR_MISMATCH_MODE_AND_FILE_EXTENSION =
            "\"%s\" mode is not allowed to used with file \"%s\".\nThe mode flag value should be client, " +
                "service or schema. Input file should be a GraphQL configuration file with .yaml/.yml " +
                "extension, a Ballerina service file with .bal or a GraphQL schema file with .graphql respectively.";
    public static final String MESSAGE_FOR_USE_RECORDS_FOR_OBJECTS_FLAG_MISUSE =
            "The use-records-for-objects flag is incompatible with: \"%s\"";
    public static final String MESSAGE_FOR_EMPTY_CONFIGURATION_FILE =
            "The GraphQL " + "configuration YAML file is empty. \nPlease provide a valid content in the YAML file.";
    public static final String MESSAGE_FOR_INVALID_CONFIGURATION_FILE_CONTENT =
            "The GraphQL config file is not in " + "the specific config file format.\n";
    public static final String MESSAGE_FOR_EMPTY_PROJECT = "The GraphQL configuration YAML file is configured " +
            "with an empty project. \nPlease provide a valid project content in the YAML file.";
    public static final String MESSAGE_FOR_MISSING_SCHEMA_OR_DOCUMENTS = "The GraphQL configuration YAML file " +
            "project is configured with a missing schema or documents section. " +
            "\nPlease provide both the schema & documents section under each project in the YAML file.";
    public static final String MESSAGE_FOR_INVALID_SCHEMA_URL = "The GraphQL configuration YAML file " +
            "project is configured with an invalid web URL for schema location. " +
            "\nPlease provide a valid graphQL endpoint URL or file path for the schema section " +
            "under each project in the YAML file. \nInvalid URL ";
    public static final String MESSAGE_FOR_INVALID_SCHEMA_PATH = "The GraphQL configuration YAML file " +
            "project is configured with a file path for schema location which does not exist. " +
            "\nPlease provide a valid graphQL endpoint URL or file path for the schema section " +
            "under each project in the YAML file. \nInvalid file path ";
    public static final String MESSAGE_FOR_INVALID_DOCUMENT_PATH = "The GraphQL configuration YAML file " +
            "project is configured with a file path for queries file location which does not exist. " +
            "\nPlease provide a valid file path for the schema section " +
            "under each project in the YAML file. \nInvalid file path ";

    public static final String MESSAGE_MISSING_SCHEMA_FILE = "Provided Schema file \"%s\" does not exist.";
    public static final String MESSAGE_CAN_NOT_READ_SCHEMA_FILE =
            "Provided Schema file \"%s\" is not allowed to be read";

    public static final String MESSAGE_FOR_INVALID_MODE =
            "\"%s\" is not a supported argument for mode flag. The mode flag argument should be one of " +
                    "these (client, service, schema)";
    // GraphQL config file extensions supported
    public static final String YAML_EXTENSION = ".yaml";
    public static final String YML_EXTENSION = ".yml";
    public static final String BAL_EXTENSION = ".bal";
    public static final String GRAPHQL_EXTENSION = ".graphql";

    // GraphQL Introspection query
    public static final String INTROSPECTION_QUERY =
            "    query IntrospectionQuery {\n" + "      __schema {\n" + "        queryType { name }\n" +
                    "        mutationType { name }\n" + "        subscriptionType { name }\n" + "        types {\n" +
                    "          ...FullType\n" + "        }\n" + "        directives {\n" + "          name\n" +
                    "          description\n" + "          locations\n" + "          args {\n" +
                    "            ...InputValue\n" + "          }\n" + "        }\n" + "      }\n" + "    }\n" + "  \n" +
                    "    fragment FullType on __Type {\n" + "      kind\n" + "      name\n" + "      description\n" +
                    "      fields(includeDeprecated: true) {\n" + "        name\n" + "        description\n" +
                    "        args {\n" + "          ...InputValue\n" + "        }\n" + "        type {\n" +
                    "          ...TypeRef\n" + "        }\n" + "        isDeprecated\n" +
                    "        deprecationReason\n" + "      }\n" + "      inputFields {\n" + "        ...InputValue\n" +
                    "      }\n" + "      interfaces {\n" + "        ...TypeRef\n" + "      }\n" +
                    "      enumValues(includeDeprecated: true) {\n" + "        name\n" + "        description\n" +
                    "        isDeprecated\n" + "        deprecationReason\n" + "      }\n" + "      possibleTypes {\n" +
                    "        ...TypeRef\n" + "      }\n" + "    }\n" + "  \n" +
                    "    fragment InputValue on __InputValue {\n" + "      name\n" + "      description\n" +
                    "      type { ...TypeRef }\n" + "      defaultValue\n" + "    }\n" + "  \n" +
                    "    fragment TypeRef on __Type {\n" + "      kind\n" + "      name\n" + "      ofType {\n" +
                    "        kind\n" + "        name\n" + "        ofType {\n" + "          kind\n" +
                    "          name\n" + "          ofType {\n" + "            kind\n" + "            name\n" +
                    "            ofType {\n" + "              kind\n" + "              name\n" +
                    "              ofType {\n" + "                kind\n" + "                name\n" +
                    "                ofType {\n" + "                  kind\n" + "                  name\n" +
                    "                  ofType {\n" + "                    kind\n" + "                    name\n" +
                    "                  }\n" + "                }\n" + "              }\n" + "            }\n" +
                    "          }\n" + "        }\n" + "      }\n" + "    }\n";

    // Constants related to HTTP request
    public static final String QUERY = "query";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String URL_RECOGNIZER = "http";
    public static final String SERVICE = "service";
}
