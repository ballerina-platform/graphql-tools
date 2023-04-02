/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

module io.ballerina.graphql {
    requires io.ballerina.tools.api;
    requires org.yaml.snakeyaml;
    requires io.ballerina.cli;
    requires info.picocli;
    requires java.net.http;
    requires io.ballerina.parser;
    requires com.graphqljava;
    requires io.ballerina.formatter.core;
    requires org.apache.commons.io;
    requires commons.logging;
    requires io.ballerina.lang;
    requires org.slf4j;
    requires io.ballerina.graphql.generator;
    requires io.ballerina.graphql.schema;
    exports io.ballerina.graphql.cmd;
    exports io.ballerina.graphql.cmd.pojo;
    exports io.ballerina.graphql.exception;
//    exports io.ballerina.graphql.generator.client.generator.model;
    exports io.ballerina.graphql.validator;
//    exports io.ballerina.graphql.generator.ballerina;
//    exports io.ballerina.graphql.generator.client.generator.graphql.components;
//    exports io.ballerina.graphql.generator.client.generator.graphql;
}
