package org.gradle.docs;

import org.gradle.docs.TestFile;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileFixture {
    private final Map<String, String> entries;

    ZipFileFixture(TestFile file) {
        this.entries = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                if (!entry.isDirectory()) {
                    String content = getContentForEntry(zipFile, entry);
                    this.entries.put(entry.getName(), content);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(file + " is not a zip file?", e);
        }
    }

    private String getContentForEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
        // Only keep around content that's <1KB
        if (entry.getSize() < 1024*1024) {
            InputStream inputStream = zipFile.getInputStream(entry);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            return new String(byteArray, StandardCharsets.UTF_8);
        }
        return "File too long";
    }

    public void assertContainsDescendants(String... expectedContent) {
        assert entries.keySet().containsAll(Arrays.asList(expectedContent));
    }

    public void assertDescendantHasContent(String descendant, Matcher<String> matcher) {
        String actualContent = entries.get(descendant);
        assert actualContent != null;
        assert matcher.matches(actualContent);
    }

    public void assertHasDescendants(String... descendants) {
        Assert.assertEquals(descendants.length, entries.size());
        Set<String> expectedEntries = new HashSet<>(Arrays.asList(descendants));
        Set<String> actualEntries = new HashSet<>(entries.keySet());
        Assert.assertEquals(expectedEntries, actualEntries);
    }
}
