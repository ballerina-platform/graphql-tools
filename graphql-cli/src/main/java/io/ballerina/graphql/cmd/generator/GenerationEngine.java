/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.cmd.generator;

import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.exception.ClientCodeGenerationException;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.schema.exception.SchemaFileGenerationException;

import java.io.IOException;

/**
 * Runs a GraphQL generation operation.
 */
public class GenerationEngine {

    private GenerationEngine() {}

    public static void run(GenerationContext context)
            throws CmdException, ParseException, IOException, ValidationException, ClientCodeGenerationException,
            SchemaFileGenerationException, ServiceGenerationException {
        run(GeneratorFactory.getGenerator(context));
    }

    public static void run(Generator generator)
            throws ParseException, IOException, ValidationException, ClientCodeGenerationException,
            SchemaFileGenerationException, ServiceGenerationException {
        generator.validate();
        generator.generate();
        generator.write();
    }
}
