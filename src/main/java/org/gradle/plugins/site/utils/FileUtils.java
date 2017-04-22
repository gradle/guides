package org.gradle.plugins.site.utils;

import java.io.*;

public final class FileUtils {
    private FileUtils() {}

    public static void createDirectory(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create directory " + dir);
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        copyFile(new FileInputStream(source), target);
    }

    public static void copyFile(InputStream source, File target) throws IOException {
        OutputStream out = null;

        try {
            out = new FileOutputStream(target);

            byte[] buf = new byte[1024];
            int len;
            while ((len = source.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (source != null) {
                source.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
