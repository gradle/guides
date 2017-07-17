package org.gradle.guides.testing

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.After
import org.junit.Before
import org.junit.rules.TemporaryFolder

/**
 * Provides functional testing support with GradleRunner.
 */
trait FunctionalTestingSupport {

    final TemporaryFolder temporaryFolder = new TemporaryFolder()
    GradleRunner gradleRunner

    @Before
    def setupTestingSupport() {
        temporaryFolder.create()
        gradleRunner = GradleRunner.create().withProjectDir(temporaryFolder.root)
    }

    @After
    def cleanupTestingSupport() {
        temporaryFolder.delete()
    }

    /**
     * Executes build for provided arguments and expects it finish successfully.
     *
     * @param arguments Arguments
     * @return Build result
     */
    BuildResult succeeds(String... arguments) {
        withArguments(arguments).build()
    }

    /**
     * Executes build for provided arguments and expects it fail.
     *
     * @param arguments Arguments
     * @return Build result
     */
    BuildResult fails(String... arguments) {
        withArguments(arguments).buildAndFail()
    }

    private GradleRunner withArguments(String... arguments) {
        gradleRunner.withArguments(arguments)
        gradleRunner
    }

    /**
     * Returns the root directory for a test.
     *
     * @return Root test directory
     */
    File getTestDirectory() {
        temporaryFolder.root
    }

    /**
     * Creates build file if it doesn't exist yet and returns it.
     *
     * @return The build file
     */
    File getBuildFile() {
        temporaryFolder.newFile('build.gradle')
    }

    /**
     * Creates settings file if it doesn't exist yet and returns it.
     *
     * @return The build file
     */
    File getSettingsFile() {
        temporaryFolder.newFile('settings.gradle')
    }

    /**
     * Create a new file with the given path.
     *
     * @param path Path
     * @return The created file
     */
    File file(String path) {
        temporaryFolder.newFile(path)
    }

    /**
     * Create a new directory with the given path.
     *
     * @param path Path
     * @return The created directory
     */
    File dir(String... path) {
        temporaryFolder.newFolder(path)
    }
}
