package org.gradle.guides;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileFixture {
    private final TestFile file;
    private final Set<String> entries;

    ZipFileFixture(TestFile file) {
        this.file = file;
        this.entries = new HashSet<>();

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                if (!entry.isDirectory()) {
                    this.entries.add(entry.getName());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(file + " is not a zip file?", e);
        }
    }

    public void assertContainsContent(String... expectedContent) {
        assert entries.containsAll(Arrays.asList(expectedContent));
    }

    public void assertHasContent(String... expectedContent) {
        assert entries.size() == expectedContent.length;
        Set<String> unexpectedEntries = new HashSet<>(entries);
        unexpectedEntries.removeAll(Arrays.asList(expectedContent));
        assert unexpectedEntries.isEmpty();
    }
}
