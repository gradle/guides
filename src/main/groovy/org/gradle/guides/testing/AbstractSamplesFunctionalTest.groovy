package org.gradle.guides.testing

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils

@CompileStatic
class AbstractSamplesFunctionalTest extends AbstractFunctionalTest {

    void copySampleCode(String path) {
        copySampleDirRecursively("code/$path")
    }

    private void copySampleDirRecursively(String path) {
        FileUtils.copyDirectory(new File(System.getProperty('samplesDir'), path), testDirectory)
    }
}
