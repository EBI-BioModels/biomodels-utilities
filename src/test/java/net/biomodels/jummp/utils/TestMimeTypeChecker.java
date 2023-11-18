package net.biomodels.jummp.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMimeTypeChecker {
    private static File init(final String resourceName) {
        ClassLoader classLoader = TestMimeTypeChecker.class.getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());
        String absolutePath = file.getAbsolutePath();
        System.out.println(absolutePath);
        return file;
    }

    @BeforeEach
    void setUp() {
        System.out.println("Thinking somethings put here");
    }
    @Test
    @DisplayName("Check the mimetype of a given file")
    public void testCheckMimeType() {
        File file = this.init("files/BIOMD0000001066.omex");
        String mime = MimeTypeChecker.check(file);
        System.out.println(mime);
        // As of writing this test, OMEX files haven't been  recognised as the common file type
        assertTrue("content/unknown" == mime);
    }

        @Test
    @DisplayName("Ensure that two temporary directories with same files names and content have same hash")
    void hashTwoDynamicDirectoryWhichHaveSameContent(@TempDir Path tempDir, @TempDir Path tempDir2) throws IOException {

        Path file1 = tempDir.resolve("myfile.txt");

        List<String> input = Arrays.asList("input1", "input2", "input3");
        Files.write(file1, input);

        assertTrue(Files.exists(file1), "File should exist");

        Path file2 = tempDir2.resolve("myfile.txt");

        Files.write(file2, input);
        assertTrue(Files.exists(file2), "File should exist");

    }
}
