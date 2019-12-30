package org.gradle.docs.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class IOUtils {
    public static String toString(InputStream inStream, Charset encoding) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int d;
        while ((d = inStream.read()) != -1) {
            outStream.write(d);
        }

        return outStream.toString(encoding.name());
    }
}
