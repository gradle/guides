package org.gradle.guides.test.fixtures

import groovy.transform.CompileStatic
import org.gradle.guides.test.fixtures.utils.CopyDirVisitor

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
class AbstractSamplesFunctionalTest extends AbstractFunctionalTest {

    /**
     * Copies the given source path found in the samples directory to the test directory.
     *
     * @param sourcePath Source path
     */
    protected void copySampleCode(String sourcePath) {
        copySampleDirRecursively(new File(getSamplesCodeDir(), sourcePath), testDirectory)
    }

    /**
     * Copies the given source path found in the samples directory to a subdirectory in the test directory.
     * <p>
     * Executing the build requires reconfiguring the project directory of the {@link org.gradle.testkit.runner.GradleRunner}
     * instance.
     *
     * @param sourcePath Source path
     * @paran targetPath Target path
     */
    protected void copySampleCode(String sourcePath, String targetPath) {
        copySampleDirRecursively(new File(getSamplesCodeDir(), sourcePath), new File(testDirectory, targetPath))
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

    private void copySampleDirRecursively(File sourceDir, File targetDir) {
        Path sourcePath = Paths.get(sourceDir.toURI())
        Path targetPath = Paths.get(targetDir.toURI())
        Files.walkFileTree(sourcePath, new CopyDirVisitor(sourcePath, targetPath))
    }
}
