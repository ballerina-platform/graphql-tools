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

package io.ballerina.graphql.cmd;

import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Default;
import io.ballerina.graphql.cmd.pojo.Endpoints;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.exception.IntospectionException;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static io.ballerina.graphql.cmd.Constants.URL_RECOGNIZER;

/**
 * Utility class for GraphQL client generation command line tool.
 */
public class Utils {

    /**
     * Returns the Snakeyaml `Constructor` instance processing unsupported keywords in Java.
     *
     * @return               the `GraphQLSchema` instance
     */
    public static Constructor getProcessedConstructor() {
        Constructor constructor = new Constructor(Config.class);

        TypeDescription endpointsDesc = new TypeDescription(Endpoints.class);
        endpointsDesc.substituteProperty("default", Default.class,
                "getDefaultName", "setDefaultName");
        constructor.addTypeDescription(endpointsDesc);

        return constructor;
    }

    /**
     * Returns the `GraphQLSchema` instance for a given GraphQL schema file or schema URL.
     *
     * @param schema                                the schema value of the Graphql config file
     * @param extensions                            the extensions value of the Graphql config file
     * @return                                      the `GraphQLSchema` instance
     * @throws IntospectionException                If an error occurs during introspection of the GraphQL API
     * @throws SchemaProblem                        If a GraphQL schema related error occurs
     * @throws IOException                          If an I/O error occurs
     */
    public static GraphQLSchema getGraphQLSchemaDocument(String schema, Extension extensions)
            throws IntospectionException, SchemaProblem, IOException {
        Document introspectSchema = null;
        if (schema.startsWith(URL_RECOGNIZER)) {
            Map<String, Object> introspectionResult =
                    Introspector.getInstance().getIntrospectionResult(schema, extensions);
            IntrospectionResultToSchema introspectionResultToSchema = new IntrospectionResultToSchema();
            introspectSchema = introspectionResultToSchema.createSchemaDefinition(introspectionResult);
        }

        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        TypeDefinitionRegistry typeRegistry;
        if (introspectSchema != null) {
            typeRegistry = schemaParser.buildRegistry(introspectSchema);
        } else {
            String sdlInput = extractSchemaContent(schema);
            typeRegistry = schemaParser.parse(sdlInput);
        }
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry,
                RuntimeWiring.MOCKED_WIRING);
        return graphQLSchema;
    }

    /**
     * Extracts the schema content.
     *
     * @param schema                                the schema value of the Graphql config file
     * @return                                      the schema content
     * @throws IOException                          If an I/O error occurs
     */
    public static String extractSchemaContent(String schema) throws IOException {
        File schemaFile = new File(schema);
        Path schemaPath = Paths.get(schemaFile.getCanonicalPath());
        return Files.readString(schemaPath);
    }

    /**
     * Returns the `Document` instance for a given GraphQL queries file.
     *
     * @param document                              the document value of the Graphql config file
     * @return                                      the `GraphQLSchema` instance
     * @throws IOException                          If an I/O error occurs
     */
    public static Document getGraphQLQueryDocument(String document) throws IOException {
        Parser parser = new Parser();
        String queriesInput = extractDocumentContent(document);
        Document parsedDocument = parser.parseDocument(queriesInput);
        return parsedDocument;
    }

    /**
     * Extracts the document content.
     *
     * @param document                              the document value of the Graphql config file
     * @return                                      the document content
     * @throws IOException                          If an I/O error occurs
     */
    public static String extractDocumentContent(String document) throws IOException {
        File documentFile = new File(document);
        Path documentPath = Paths.get(documentFile.getCanonicalPath());
        return Files.readString(documentPath);
    }

    /**
     * Checks whether the schema is a valid URL.
     *
     * @param schema         the schema value
     */
    public static boolean isValidURL(String schema) {
        try {
            new URL(schema).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    /**
     * This util method is used to generate {@code Diagnostic} for graphql command errors.
     */
    public static GraphqlDiagnostic constructGraphqlDiagnostic(String code, String message, DiagnosticSeverity severity,
                                                               Location location, Object... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, message, severity);
        if (location == null) {
            location = new NullLocation();
        }
        return new GraphqlDiagnostic(diagnosticInfo, location, Collections.emptyList(), args);
    }

    /**
     * This {@code NullLocation} represents the null location allocation for scenarios which has no location.
     */
    public static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
