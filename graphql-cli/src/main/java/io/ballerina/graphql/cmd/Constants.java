/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    public static final String MESSAGE_FOR_MISSING_INPUT_ARGUMENT = "Missing the input file path," +
            " Please provide the path of the GraphQL config file with -i flag";
    public static final String MESSAGE_FOR_MISSING_GRAPHQL_CONFIGURATION_FILE = "A GraphQL configuration file " +
            "is required to generate the clients. \ne.g: bal graphql --input <GraphQL configuration file>";
    public static final String MESSAGE_FOR_EMPTY_CONFIGURATION_YAML = "Empty GraphQL " +
            "configuration YAML file provided. Please provide a valid content in the YAML file.";
    public static final String MESSAGE_FOR_INVALID_CONFIGURATION_YAML = "Invalid structure used for the GraphQL " +
            "configuration YAML file.";

    // GraphQL config file extensions supported
    public static final String YAML_EXTENSION = ".yaml";
    public static final String YML_EXTENSION = ".yml";

    public static final String URL_RECOGNIZER = "http";
}
