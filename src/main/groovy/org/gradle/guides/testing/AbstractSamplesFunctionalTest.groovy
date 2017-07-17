package org.gradle.guides.testing

import org.apache.commons.io.FileUtils

class AbstractSamplesFunctionalTest extends AbstractFunctionalTest {

    void copySampleCode(String path) {
        copySampleDirRecursively("code/$path")
    }

    private void copySampleDirRecursively(String path) {
        FileUtils.copyDirectory(new File(System.getProperty('samplesDir'), path), testDirectory)
    }
}
