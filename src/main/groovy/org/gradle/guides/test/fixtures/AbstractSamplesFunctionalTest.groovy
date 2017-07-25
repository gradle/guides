package org.gradle.guides.test.fixtures

import groovy.transform.CompileStatic
import org.gradle.guides.test.fixtures.utils.CopyDirVisitor

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
class AbstractSamplesFunctionalTest extends AbstractFunctionalTest {

    void copySampleCode(String path) {
        copySampleDirRecursively("code/$path")
    }

    private void copySampleDirRecursively(String path) {
        Path sourceDir = Paths.get(new File(System.getProperty('samplesDir'), path).toURI())
        Path targetDir = Paths.get(testDirectory.toURI())
        Files.walkFileTree(sourceDir, new CopyDirVisitor(sourceDir, targetDir))
    }
}
