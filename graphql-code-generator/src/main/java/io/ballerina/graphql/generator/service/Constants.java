/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.service;

/**
 * This class represents GraphQL service generation related constants.
 */
public class Constants {
    public static final String UNSUPPORTED_DEFAULT_ARGUMENT_VALUE =
            "Type \"%s\" is an unsupported default argument value type.";
    public static final String UNSUPPORTED_TYPE = "Type \"%s\" is not supported.";
    public static final String ONLY_SCALAR_TYPE_ALLOWED = "Should be a scalar type but found \"%s\".";
    public static final String NOT_ALLOWED_UNION_SUB_TYPE =
            "Union type can only have object types as members. But found: \"%s\" with different type.";
    public static final String MESSAGE_FOR_COMBINE_INTO_EMPTY_SERVICE_TYPES_FILE = "Service types file combination " +
            "failed. The service types file \"%s\" available in \"%s\" output location is empty. It should be deleted" +
            " or it should be a GraphQL service types file.";
    public static final String MESSAGE_FOR_COMBINE_INTO_EMPTY_SERVICE_FILE = "Service file combination failed. The " +
            "service file \"%s\" available in \"%s\" output location is empty. It should be deleted or it should be a" +
            " GraphQL service file.";

    public static final String RESOURCE = "resource";
    public static final String REMOTE = "remote";
    public static final String NEW_LINE = "\n";
}
