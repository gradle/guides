package org.gradle.docs;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: Make this not dependent on Groovy methods
public class TestFile extends File {
    public TestFile(File file, Object... path) {
        super(join(file, path).getAbsolutePath());
    }

    public TestFile file(Object... path) {
        try {
            return new TestFile(this, path);
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("Could not locate file '%s' relative to '%s'.", Arrays.toString(path), this), e);
        }
    }

    public TestFile leftShift(Object content) {
        getParentFile().mkdirs();
        try {
            ResourceGroovyMethods.leftShift(this, content);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not append to test file '%s'", this), e);
        }
    }

    public TestFile setText(String content) {
        getParentFile().mkdirs();
        try {
            ResourceGroovyMethods.setText(this, content);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not append to test file '%s'", this), e);
        }
    }

    public String getText() {
        assertIsFile();
        try {
            return ResourceGroovyMethods.getText(this, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not read from test file '%s'", this), e);
        }
    }

    public TestFile assertExists() {
        assertTrue(exists(), () -> String.format("%s does not exist", this));
        return this;
    }

    public TestFile assertIsFile() {
        assertTrue(isFile(), () -> String.format("%s is not a file", this));
        return this;
    }

    public TestFile assertIsDir() {
        assertTrue(isDirectory(), () -> String.format("%s is not a directory.", this));
        return this;
    }

    public TestFile assertDoesNotExist() {
        assertFalse(exists(), () -> String.format("%s should not exist", this));
        return this;
    }

    private static File join(File file, Object[] path) {
        File current = file.getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }
        try {
            return current.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not canonicalise '%s'.", current), e);
        }
    }

    public ZipFileFixture asZip() {
        assertIsFile();
        return new ZipFileFixture(this);
    }
}
