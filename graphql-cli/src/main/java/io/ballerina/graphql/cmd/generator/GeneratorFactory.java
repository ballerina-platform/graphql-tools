/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.config.BalGraphqlConfigReader;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Creates the generator that handles a given generation context.
 */
public class GeneratorFactory {

    private static final String ERROR_UNRESOLVED_OPERATION_MODE =
            "The operation to perform could not be resolved from the input \"%s\".";
    private static final String ERROR_OBJECT_TYPE_FOR_CLIENT =
            "The --object-type flag only applies to service generation. \"%s\" resolves to client generation.";

    private GeneratorFactory() {}

    public static Generator getGenerator(GenerationContext context) throws GenerationException {
        String inputPath = context.getInputPath();
        if (inputPath.endsWith(BalGraphqlConfig.FILE_EXTENSION)) {
            BalGraphqlConfig config = readConfig(inputPath, context.getOutStream());
            OperationMode operationMode = resolveOperationMode(context, config);
            if (operationMode == OperationMode.CLIENT && context.isObjectTypeDeclared()) {
                throw new GenerationException(String.format(ERROR_OBJECT_TYPE_FOR_CLIENT, inputPath));
            }
            return createGenerator(operationMode, context, config);
        }
        OperationMode operationMode = OperationMode.fromInputPath(inputPath).orElseThrow(
                () -> new GenerationException(String.format(ERROR_UNRESOLVED_OPERATION_MODE, inputPath)));
        return createGenerator(operationMode, context, null);
    }

    private static OperationMode resolveOperationMode(GenerationContext context, BalGraphqlConfig config) {
        Optional<OperationMode> declaredOperationMode = context.getDeclaredOperationMode();
        return declaredOperationMode.orElseGet(() -> OperationMode.fromConfig(config));
    }

    private static Generator createGenerator(OperationMode operationMode, GenerationContext context,
                                             BalGraphqlConfig config) throws GenerationException {
        switch (operationMode) {
            case CLIENT:
                return new ClientGeneration(context, config);
            case SERVICE:
                return new ServiceGeneration(context, config);
            case SCHEMA:
                return new SchemaGeneration(context);
            default:
                throw new GenerationException("No generator is available for the \"" + operationMode
                        + "\" operation mode.");
        }
    }

    private static BalGraphqlConfig readConfig(String inputPath, PrintStream outStream) throws GenerationException {
        try {
            return BalGraphqlConfigReader.read(Paths.get(inputPath), outStream);
        } catch (IOException | ParseException e) {
            throw new GenerationException(e);
        }
    }
}
