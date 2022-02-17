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

package io.ballerina.graphql.generator.model;

import io.ballerina.graphql.generator.CodeGeneratorConstants;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Model class representing authentication configuration information.
 */
public class AuthConfig {
    private final Set<CodeGeneratorConstants.AuthConfigType> authConfigTypes = new LinkedHashSet<>();
    private final Set<String> apiHeaders = new LinkedHashSet<>();

    /**
     * Returns `true` if authentication mechanism is API key.
     *
     * @return {@link boolean}    value of the flag isApiKeysConfig
     */
    public boolean isApiKeysConfig() {
        if (getAuthConfigTypes().size() != 0) {
            if (getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigType.API_KEY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns `true` if authentication mechanism is OAuth.
     *
     * @return {@link boolean}    value of the flag isClientConfig
     */
    public boolean isClientConfig() {
        if (getAuthConfigTypes().size() != 0) {
            if (getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigType.BASIC) ||
                    getAuthConfigTypes().contains(CodeGeneratorConstants.AuthConfigType.BEARER)) {
                return true;
            }
        }
        return false;
    }

    public void addAuthConfigType(CodeGeneratorConstants.AuthConfigType authConfigType) {
        this.authConfigTypes.add(authConfigType);
    }

    public Set<CodeGeneratorConstants.AuthConfigType> getAuthConfigTypes() {
        return authConfigTypes;
    }

    public void addApiHeader(String apiHeader) {
        this.apiHeaders.add(apiHeader);
    }

    public Set<String> getApiHeaders() {
        return apiHeaders;
    }
}
