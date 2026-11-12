/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd.generator;

import io.ballerina.graphql.exception.ExitCodes;
import io.ballerina.graphql.exception.ToolException;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The license header the user gives with --license, added at the top of every generated Ballerina file (IO-2). The
 * file is read and checked before anything is generated (IO-3, FF-4 phase 3), so a header that would stop the
 * generated sources from compiling fails the run up front.
 */
final class LicenseHeader {

    private static final String ERROR_CANNOT_READ =
            "Cannot read the license file \"%s\". Check that it exists and is readable.";
    private static final String ERROR_NOT_TEXT = "The license file \"%s\" is not a UTF-8 text file.";
    private static final String ERROR_NOT_A_COMMENT = "The license file \"%s\" can only contain comment lines and "
            + "blank lines. Line %d is not a comment: \"%s\".";
    private static final String COMMENT_PREFIX = "//";
    private static final String BYTE_ORDER_MARK = "\uFEFF";
    private static final String WINDOWS_LINE_BREAK = "\r\n";
    private static final String OLD_MAC_LINE_BREAK = "\r";
    private static final String LINE_BREAK = "\n";
    private static final String NO_HEADER = "";
    private static final char SPACE = ' ';
    private static final char TAB = '\t';
    private static final char FORM_FEED = '\f';
    private static final char LINE_BREAK_CHARACTER = '\n';
    private static final String CHARACTER_CODE_FORMAT = "\\u%04X";

    private LicenseHeader() {
    }

    /**
     * Reads the license file and returns the header to add: the file's content with LF line breaks (GEN-2), followed
     * by one blank line.
     *
     * @param licensePath   the path of the license file as given, or {@code null} if --license was not given
     * @param executionPath the directory a relative license path is resolved against, as -o is, or {@code null}
     *                      to use the current directory
     * @return the header, or an empty string if no license was given or the file has no comments
     * @throws ToolException with exit code 1 if the file cannot be read, is not text, or has a line that is not a
     *                       comment
     */
    static String read(String licensePath, Path executionPath) {
        if (licensePath == null) {
            return NO_HEADER;
        }
        String content = readText(licensePath, executionPath);
        if (content.startsWith(BYTE_ORDER_MARK)) {
            content = content.substring(BYTE_ORDER_MARK.length());
        }
        content = content.replace(WINDOWS_LINE_BREAK, LINE_BREAK).replace(OLD_MAC_LINE_BREAK, LINE_BREAK);
        String[] lines = content.split(LINE_BREAK, -1);
        for (int index = 0; index < lines.length; index++) {
            String line = stripWhitespace(lines[index]);
            if (!line.isEmpty() && !line.startsWith(COMMENT_PREFIX)) {
                throw new ToolException(ExitCodes.RUNTIME,
                        String.format(ERROR_NOT_A_COMMENT, licensePath, index + 1, showInvisible(line)));
            }
        }
        String header = stripTrailingBlankLines(content);
        return header.isEmpty() ? NO_HEADER : header + LINE_BREAK + LINE_BREAK;
    }

    /**
     * Adds the header at the top of each generated file. Client and service generation only generate Ballerina
     * files.
     *
     * @param sources the generated files
     * @param header  the header from {@link #read(String, Path)}
     * @return the generated files with the header
     */
    static List<SrcFilePojo> addTo(List<SrcFilePojo> sources, String header) {
        if (header.isEmpty()) {
            return sources;
        }
        return sources.stream()
                .map(source -> new SrcFilePojo(source.getType(), source.getModuleName(), source.getFileName(),
                        header + source.getContent()))
                .toList();
    }

    // Only a space, a tab or a form feed is whitespace in Ballerina source, so a line indented with another kind,
    // such as an em space copied from a word processor, is not a comment line and is reported instead of breaking
    // the build.
    private static String stripWhitespace(String line) {
        int start = 0;
        int end = line.length();
        while (start < end && isWhitespace(line.charAt(start))) {
            start++;
        }
        while (end > start && isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(start, end);
    }

    // The blank lines and whitespace at the end are removed by the same rule as the line check, so the header is
    // kept as written and only the end is trimmed, before the one blank line that follows it.
    private static String stripTrailingBlankLines(String content) {
        int end = content.length();
        while (end > 0 && (content.charAt(end - 1) == LINE_BREAK_CHARACTER || isWhitespace(content.charAt(end - 1)))) {
            end--;
        }
        return content.substring(0, end);
    }

    private static boolean isWhitespace(char character) {
        return character == SPACE || character == TAB || character == FORM_FEED;
    }

    // A space other than a space, a tab or a form feed, or a control or format character, is shown as its code,
    // e.g. \u2003, so a line that looks blank in the message still shows what is wrong with it.
    private static String showInvisible(String line) {
        StringBuilder shown = new StringBuilder();
        for (char character : line.toCharArray()) {
            boolean invisible = !isWhitespace(character) && (Character.isWhitespace(character)
                    || Character.isSpaceChar(character) || Character.isISOControl(character)
                    || Character.getType(character) == Character.FORMAT);
            shown.append(invisible ? String.format(CHARACTER_CODE_FORMAT, (int) character) : character);
        }
        return shown.toString();
    }

    private static String readText(String licensePath, Path executionPath) {
        Path path;
        try {
            path = executionPath == null ? Paths.get(licensePath) : executionPath.resolve(licensePath);
        } catch (InvalidPathException e) {
            // A path the file system cannot name, such as one with "<" on Windows, cannot be read either.
            throw new ToolException(ExitCodes.RUNTIME, String.format(ERROR_CANNOT_READ, licensePath));
        }
        if (!Files.isRegularFile(path)) {
            throw new ToolException(ExitCodes.RUNTIME, String.format(ERROR_CANNOT_READ, licensePath));
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            throw new ToolException(ExitCodes.RUNTIME, String.format(ERROR_NOT_TEXT, licensePath));
        } catch (IOException e) {
            throw new ToolException(ExitCodes.RUNTIME, String.format(ERROR_CANNOT_READ, licensePath));
        }
    }
}
