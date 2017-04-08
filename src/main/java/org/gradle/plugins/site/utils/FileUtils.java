package org.gradle.plugins.site.utils;

import java.io.*;

public final class FileUtils {
    private FileUtils() {}

    public static void createDirectory(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdir()) {
            throw new IOException("Unable to create directory " + dir);
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(target);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
