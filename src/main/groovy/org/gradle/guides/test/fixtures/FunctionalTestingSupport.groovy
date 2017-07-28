package org.gradle.guides.test.fixtures

import groovy.transform.CompileStatic
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.After
import org.junit.Before

/**
 * Provides functional testing support with GradleRunner.
 */
@CompileStatic
trait FunctionalTestingSupport implements FunctionalTestFixture {

    private final FunctionalTestFixture delegate = new DefaultFunctionalTestFixture()

    /**
     * {@inheritDoc}
     */
    @Before
    @Override
    void initialize() {
        delegate.initialize()
    }

    /**
     * {@inheritDoc}
     */
    @After
    @Override
    void tearDown() {
        delegate.tearDown()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GradleRunner getGradleRunner() {
        delegate.gradleRunner
    }

    /**
     * {@inheritDoc}
     */
    @Override
    BuildResult succeeds(List<String> arguments) {
        delegate.succeeds(arguments)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    BuildResult succeeds(String... arguments) {
        delegate.succeeds(arguments)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    BuildResult fails(List<String> arguments) {
        delegate.fails(arguments)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    BuildResult fails(String... arguments) {
        delegate.fails(arguments)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File getTestDirectory() {
        delegate.testDirectory
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File file(String path) {
        delegate.file(path)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File dir(String... path) {
        delegate.dir(path)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File getBuildFile() {
        delegate.buildFile
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File getSettingsFile() {
        delegate.settingsFile
    }
}
