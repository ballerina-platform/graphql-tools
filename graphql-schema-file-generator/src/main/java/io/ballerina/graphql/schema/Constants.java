/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.schema;

/**
 * This class represents GraphQL SDL schema generation related constants.
 */
public final class Constants {
    private Constants() {}

    public static final String SCHEMA_PREFIX = "schema";
    public static final String GRAPHQL_EXTENSION = ".graphql";
    public static final String SLASH = "/";
    public static final String UNDERSCORE = "_";
    public static final String PERIOD = ".";

    public static final String MESSAGE_CANNOT_READ_SCHEMA_STRING = "Cannot read decoded schema string";
    public static final String MESSAGE_INVALID_SCHEMA_STRING = "Invalid schema string found";
}
