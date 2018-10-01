package org.gradle.plugins.site.utils

import java.io.*

object FileUtils {

    @Throws(IOException::class)
    fun createDirectory(dir: File) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Unable to create directory $dir")
        }
    }

    @Throws(IOException::class)
    fun copyFile(source: File, target: File) {
        copyFile(FileInputStream(source), target)
    }

    @Throws(IOException::class)
    fun copyFile(source: InputStream, target: File) {
        var out: OutputStream? = null

        try {
            out = FileOutputStream(target)

            val buf = ByteArray(1024)
            var len: Int = source.read(buf)
            while (len > 0) {
                out.write(buf, 0, len)
                len = source.read(buf)
            }
        } finally {
            source.close()
            out?.close()
        }
    }
}
