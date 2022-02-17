/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;

import java.text.MessageFormat;
import java.util.List;

/**
 * Represents a {@code Diagnostic} related to graphql command.
 */
public class GraphqlDiagnostic extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final List<DiagnosticProperty<?>> properties;
    private final String message;

    public GraphqlDiagnostic(DiagnosticInfo diagnosticInfo, Location location, List<DiagnosticProperty<?>> properties,
                             Object[] args) {
        this.diagnosticInfo = diagnosticInfo;
        this.location = location;
        this.properties = properties;
        this.message = MessageFormat.format(diagnosticInfo.messageFormat(), args);
    }

    @Override
    public Location location() {
        return this.location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return this.diagnosticInfo;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return this.properties;
    }

    @Override
    public  String toString() {
        LineRange lineRange = this.location().lineRange();
        String var10000 = this.diagnosticInfo().severity().toString();
        return var10000 + " [" + lineRange.filePath() + ":" + lineRange + "] " + this.message();
    }
}
