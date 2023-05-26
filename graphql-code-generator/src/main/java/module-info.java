/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
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

module io.ballerina.graphql.generator {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.cli;
    requires com.graphqljava;
    requires org.json;
    requires java.net.http;
    requires io.ballerina.tools.api;
    requires org.apache.commons.io;
    requires io.ballerina.stdlib.graphql.commons;
    requires io.ballerina.formatter.core;
    requires org.slf4j;
    requires commons.logging;
    exports io.ballerina.graphql.generator;
    exports io.ballerina.graphql.generator.client;
    exports io.ballerina.graphql.generator.service;
    exports io.ballerina.graphql.generator.utils;
    exports io.ballerina.graphql.generator.client.pojo;
    exports io.ballerina.graphql.generator.client.exception;
    exports io.ballerina.graphql.generator.service.exception;
    exports io.ballerina.graphql.generator.service.generator;
    exports io.ballerina.graphql.generator.client.generator;
    exports io.ballerina.graphql.generator.gateway;
    exports io.ballerina.graphql.generator.gateway.exception;
    exports io.ballerina.graphql.generator.gateway.generator;
    exports io.ballerina.graphql.generator.gateway.generator.common;
}
