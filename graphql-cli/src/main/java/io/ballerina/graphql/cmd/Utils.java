/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.ballerina.graphql.cmd.mappers.Default;
import io.ballerina.graphql.cmd.mappers.Endpoints;
import io.ballerina.graphql.cmd.mappers.Extension;
import io.ballerina.graphql.cmd.mappers.GraphqlConfig;
import io.ballerina.graphql.exceptions.BallerinaGraphqlDocumentPathValidationException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlIntospectionException;
import io.ballerina.graphql.exceptions.BallerinaGraphqlSchemaPathValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * Utility class for GraphQL client generation command line tool.
 */
public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    /**
     * Returns the Snakeyaml `Constructor` instance processing unsupported keywords in Java.
     *
     * @return               the `GraphQLSchema` instance
     */
    public static Constructor getProcessedConstructor() {
        Constructor constructor = new Constructor(GraphqlConfig.class);

        TypeDescription endpointsDesc = new TypeDescription(Endpoints.class);
        endpointsDesc.substituteProperty("default", Default.class,
                "getDefaultName", "setDefaultName");
        constructor.addTypeDescription(endpointsDesc);

        return constructor;
    }

    /**
     * Returns the `GraphQLSchema` instance for a given GraphQL schema file or schema URL.
     *
     * @param schema         the schema value of the Graphql config file
     * @param extensions     the extensions value of the Graphql config file
     * @return               the `GraphQLSchema` instance
     */
    public static GraphQLSchema getGraphQLSchemaDocument(String schema, Extension extensions)
            throws BallerinaGraphqlIntospectionException, BallerinaGraphqlSchemaPathValidationException, IOException {
        Document introspectSchema = null;
        if (isValidURL(schema)) {
            // TODO: Implement introspection logic
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
     * Returns the `Document` instance for a given GraphQL queries file.
     *
     * @param document       the document value of the Graphql config file
     * @return               the `GraphQLSchema` instance
     */
    public static Document getGraphQLQueriesDocument(String document)
            throws BallerinaGraphqlDocumentPathValidationException, IOException {
        Parser parser = new Parser();
        String queriesInput = extractDocumentContent(document);
        Document parsedDocument = parser.parseDocument(queriesInput);
        return parsedDocument;
    }

    /**
     * Extracts the schema content.
     *
     * @param schema         the schema value of the Graphql config file
     * @return               the schema content
     */
    public static String extractSchemaContent(String schema)
            throws IOException, BallerinaGraphqlSchemaPathValidationException {
        File schemaFile = new File(schema);
        Path schemaPath = Paths.get(schemaFile.getCanonicalPath());
        validateSchemaPath(schemaPath);
        return Files.readString(schemaPath);
    }

    /**
     * Extracts the document content.
     *
     * @param document         the document value of the Graphql config file
     * @return                 the document content
     */
    public static String extractDocumentContent(String document)
            throws IOException, BallerinaGraphqlDocumentPathValidationException {
        File documentFile = new File(document);
        Path documentPath = Paths.get(documentFile.getCanonicalPath());
        validateDocumentPath(documentPath);
        return Files.readString(documentPath);
    }

    /**
     * Validates the schema path.
     *
     * @param schemaPath         the path to the schema
     */
    public static void validateSchemaPath(Path schemaPath) throws BallerinaGraphqlSchemaPathValidationException {
        if (!Files.exists(schemaPath)) {
            throw new BallerinaGraphqlSchemaPathValidationException("Schema file " + schemaPath + " doesn't exist.");
        }
    }

    /**
     * Validates the documents' path.
     *
     * @param documentPath         the path to the document
     */
    public static void validateDocumentPath(Path documentPath) throws BallerinaGraphqlDocumentPathValidationException {
        if (!Files.exists(documentPath)) {
            throw new BallerinaGraphqlDocumentPathValidationException("Queries file " + documentPath +
                    " doesn't exist.");
        }
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
}
