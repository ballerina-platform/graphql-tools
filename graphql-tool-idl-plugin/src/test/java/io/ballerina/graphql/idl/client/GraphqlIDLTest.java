package io.ballerina.graphql.idl.client;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterTest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class for storing the test utils for IDL tests.
 */
public class GraphqlIDLTest {
    public static final Path RESOURCE = Paths.get("src/test/resources/graphql-client-projects").toAbsolutePath();

    @AfterTest
    public void removeGeneratedFile() throws IOException {
        deleteGeneratedFiles();
    }

    // Delete generated bal files.
    public void deleteGeneratedFiles() throws IOException {
        File[] matchingGeneratedDirs = RESOURCE.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("generated");
            }
        });
        if (matchingGeneratedDirs != null) {
            for (File generatedDir : matchingGeneratedDirs) {
                FileUtils.deleteDirectory(generatedDir);
            }
        }
    }
}
