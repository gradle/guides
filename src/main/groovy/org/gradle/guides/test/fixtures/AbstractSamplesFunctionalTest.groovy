package org.gradle.guides.test.fixtures

import groovy.transform.CompileStatic
import org.gradle.guides.test.fixtures.utils.CopyDirVisitor

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
class AbstractSamplesFunctionalTest extends AbstractFunctionalTest {

    protected void copySampleCode(String path) {
        copySampleDirRecursively(new File(getSamplesCodeDir(), path))
    }

    protected static File getSamplesDir() {
        new File(System.getProperty('samplesDir'))
    }

    protected static File getSamplesCodeDir() {
        new File(getSamplesDir(), 'code')
    }

    protected static File getSamplesOutputDir() {
        new File(getSamplesDir(), 'output')
    }

    private void copySampleDirRecursively(File dir) {
        Path sourceDir = Paths.get(dir.toURI())
        Path targetDir = Paths.get(testDirectory.toURI())
        Files.walkFileTree(sourceDir, new CopyDirVisitor(sourceDir, targetDir))
    }
}
