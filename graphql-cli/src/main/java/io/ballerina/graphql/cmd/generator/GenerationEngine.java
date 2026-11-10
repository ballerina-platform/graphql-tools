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

import io.ballerina.graphql.exception.ExitCodes;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ToolException;

import java.nio.file.Path;
import java.util.List;

/**
 * Runs a GraphQL generation operation.
 */
public class GenerationEngine {

    private static final String ERROR_MERGE_NOT_SUPPORTED =
            "The --merge flag only applies to service generation. It cannot be used with \"%s\".";

    private GenerationEngine() {}

    public static void run(GenerationContext context) throws GenerationException {
        Generator generator = GeneratorFactory.getGenerator(context);
        if (context.isMerge() && !generator.canMerge()) {
            throw new ToolException(ExitCodes.USAGE, String.format(ERROR_MERGE_NOT_SUPPORTED,
                    context.getInputPath()));
        }
        generator.validate();
        generator.generate();
        // The output files are checked once all of them are known and before any is written (FF-4).
        List<Path> outputFiles = generator.outputFiles();
        OutputFileGuard.checkWritable(outputFiles);
        if (context.isDryRun()) {
            OutputFileGuard.addToDryRunReport(outputFiles, context, generator.canMerge());
            return;
        }
        OutputFileGuard.confirmOverwrite(outputFiles, context, generator.canMerge());
        generator.write();
    }
}
