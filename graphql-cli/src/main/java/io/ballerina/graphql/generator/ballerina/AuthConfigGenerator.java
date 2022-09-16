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

package io.ballerina.graphql.generator.ballerina;

import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.model.AuthConfig;

import java.util.Map;

/**
 * This class is used to generate authentication configuration in the ballerina client file.
 */
public class AuthConfigGenerator {
    private static AuthConfigGenerator authConfigGenerator = null;

    public static AuthConfigGenerator getInstance() {
        if (authConfigGenerator == null) {
            authConfigGenerator = new AuthConfigGenerator();
        }
        return authConfigGenerator;
    }

    /**
     * Populates the authentication types extracting information from the extensions.
     *
     * @param extensions                the extensions value of the Graphql config file
     * @param authConfig                the object instance representing authentication configuration information
     */
    public void populateAuthConfigTypes(Extension extensions, AuthConfig authConfig) {
        if (extensions != null && extensions.getEndpoints() != null &&
                extensions.getEndpoints().getDefaultName() != null &&
                extensions.getEndpoints().getDefaultName().getHeaders().size() != 0) {
            Map<String, String> headers = extensions.getEndpoints().getDefaultName().getHeaders();
            for (String headerName : headers.keySet()) {
                if (headerName.equals("Authorization")) {
                    if (headers.get(headerName).startsWith("Basic")) {
                        authConfig.addAuthConfigType(CodeGeneratorConstants.AuthConfigType.BASIC);
                    } else if (headers.get(headerName).startsWith("Bearer")) {
                        authConfig.addAuthConfigType(CodeGeneratorConstants.AuthConfigType.BEARER);
                    } else {
                        authConfig.addAuthConfigType(CodeGeneratorConstants.AuthConfigType.API_KEY);
                    }
                } else {
                    authConfig.addAuthConfigType(CodeGeneratorConstants.AuthConfigType.API_KEY);
                }
            }
        }
    }

    /**
     * Populates the API headers if present extracting information from the extensions.
     *
     * @param extensions                the extensions value of the Graphql config file
     * @param authConfig                the object instance representing authentication configuration information
     */
    public void populateApiHeaders(Extension extensions, AuthConfig authConfig) {
        if (extensions != null && extensions.getEndpoints() != null &&
                extensions.getEndpoints().getDefaultName() != null &&
                extensions.getEndpoints().getDefaultName().getHeaders().size() != 0) {
            Map<String, String> headers = extensions.getEndpoints().getDefaultName().getHeaders();
            for (String headerName : headers.keySet()) {
                if (headerName.equals("Authorization")) {
                    if (!headers.get(headerName).startsWith("Basic") &&
                            !headers.get(headerName).startsWith("Bearer")) {
                        authConfig.addApiHeader(headerName);
                    }
                } else {
                    authConfig.addApiHeader(headerName);
                }
            }
        }
    }
}
