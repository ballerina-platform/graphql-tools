/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.graphql.generator.gateway.common;

import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.TestUtils;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.generator.GatewayCodeGenerator;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonUtilTest extends GraphqlTest {
    

    @Test(description = "Test successful compilation of a ballerina gateway projects",
            dataProvider = "GatewayProjectFilesProvider")
    public void testGetCompiledBallerinaProject(Path[] files, String folderName)
            throws GatewayGenerationException, IOException {
        Path projectDir = tmpDir.resolve(folderName);
        if (!projectDir.toFile().mkdir()) {
            throw new RuntimeException("Error while creating project directory");
        }
        GatewayCodeGenerator.copyTemplateFiles(projectDir);
        TestUtils.copyFilesToTarget(files, projectDir);
        File executable = CommonUtils.getCompiledBallerinaProject(projectDir, tmpDir, folderName);
        Assert.assertTrue(executable.exists());
    }

    @Test(description = "Test failure in compilation of a ballerina gateway projects",
            dataProvider = "InvalidGatewayProjectFilesProvider")
    public void testUnsuccessfulCompiledBallerinaProject(Path[] files, String folderName)
            throws GatewayGenerationException, IOException {
        Path projectDir = tmpDir.resolve(folderName);
        if (!projectDir.toFile().mkdir()) {
            throw new RuntimeException("Error while creating project directory");
        }
        GatewayCodeGenerator.copyTemplateFiles(projectDir);
        TestUtils.copyFilesToTarget(files, projectDir);
        try {
            CommonUtils.getCompiledBallerinaProject(projectDir, tmpDir, folderName);
        } catch (GatewayGenerationException e) {
            Assert.assertEquals(e.getMessage(), "Error while generating the executable.");
        }
    }

    @DataProvider(name = "GatewayProjectFilesProvider")
    public Object[][] getProjects() {
        Path GatewayResourceDir = Paths.get(resourceDir.toAbsolutePath().toString(), "federationGateway",
                "expectedResults");
        Path servicesDir = GatewayResourceDir.resolve("services");
        Path typesDir = GatewayResourceDir.resolve("types");
        Path queryPlans = GatewayResourceDir.resolve("queryPlans");
        return new Object[][] {
                {
                        new Path[] {
                                servicesDir.resolve("service.bal"),
                                typesDir.resolve("types.bal"),
                                queryPlans.resolve("queryPlan.bal")
                        },
                        "project"
                },
                {
                        new Path[] {
                                servicesDir.resolve("service01.bal"),
                                typesDir.resolve("types01.bal"),
                                queryPlans.resolve("queryPlan01.bal")
                        },
                        "project01"
                },
                {
                        new Path[] {
                                servicesDir.resolve("service02.bal"),
                                typesDir.resolve("types02.bal"),
                                queryPlans.resolve("queryPlan02.bal")
                        },
                        "project02"
                },
                {
                        new Path[] {
                                servicesDir.resolve("service03.bal"),
                                typesDir.resolve("types03.bal"),
                                queryPlans.resolve("queryPlan03.bal")
                        },
                        "project03"
                }
        };
    }

    @DataProvider(name = "InvalidGatewayProjectFilesProvider")
    public Object[][] getinvalidProjects() {
        Path GatewayResourceDir = Paths.get(resourceDir.toAbsolutePath().toString(), "federationGateway",
                "expectedResults");
        Path servicesDir = GatewayResourceDir.resolve("services");
        Path typesDir = GatewayResourceDir.resolve("types");
        Path queryPlans = GatewayResourceDir.resolve("queryPlans");
        return new Object[][] {
                {
                        new Path[] {},
                        "projectWithMissingFiles"
                },
                {
                        new Path[] {
                                servicesDir.resolve("service01.bal"),
                                typesDir.resolve("types01.bal")
                        },
                        "projectWithoutQueryPlan"
                },
                {
                        new Path[] {
                                servicesDir.resolve("service02.bal"),
                                queryPlans.resolve("queryPlan02.bal")
                        },
                        "projectWithoutTypes"
                },
                {
                        new Path[] {
                                servicesDir.resolve("service03.bal"),
                                typesDir.resolve("types02.bal"),
                                queryPlans.resolve("queryPlan03.bal")
                        },
                        "projectWithWrongTypeFile"
                }
        };
    }
}
