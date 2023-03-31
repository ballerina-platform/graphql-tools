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

package io.ballerina.graphql.generator.client;

import graphql.language.Document;
import graphql.parser.Parser;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

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
