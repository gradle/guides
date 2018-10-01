package org.gradle.plugins.site.utils

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object FileUtilsTest : Spek({
    describe("given parent directory") {
        val temporaryFolder = Files.createTempDirectory("fileutilstest")

        val dir = File(temporaryFolder.toFile(), "nested/subdir")
        it("can create nested directories") {
            assertFalse(dir.isDirectory)
            FileUtils.createDirectory(dir)
            assertTrue(dir.isDirectory)
        }

        describe("given source files") {
            val sourceFile = Files.createFile(temporaryFolder.resolve("a.txt")).toFile()
            val targetFile = temporaryFolder.resolve("b.txt").toFile()
            it("can copy a file") {
                assertTrue(sourceFile.isFile)
                assertFalse(targetFile.isFile)

                FileUtils.copyFile(sourceFile, targetFile)

                assertTrue(sourceFile.isFile)
                assertTrue(targetFile.isFile)
            }
        }
    }
})
