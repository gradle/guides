package org.gradle.guides.test.fixtures;

import org.gradle.guides.test.fixtures.validation.DefaultOutputValidator;
import org.gradle.guides.test.fixtures.validation.OutputValidator;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.gradle.guides.test.fixtures.utils.StringUtils.join;

public class DefaultFunctionalTestFixture implements FunctionalTestFixture {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final OutputValidator outputValidator = new DefaultOutputValidator();
    private GradleRunner gradleRunner;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            temporaryFolder.create();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create temporary directory", e);
        }

        gradleRunner = GradleRunner.create().withProjectDir(getTestDirectory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() {
        temporaryFolder.delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GradleRunner getGradleRunner() {
        return gradleRunner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildResult succeeds(List<String> arguments) {
        BuildResult buildResult = withArguments(arguments).build();
        outputValidator.validate(buildResult.getOutput());
        return buildResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildResult succeeds(String... arguments) {
        return succeeds(Arrays.asList(arguments));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildResult fails(List<String> arguments) {
        BuildResult buildResult = withArguments(arguments).buildAndFail();
        outputValidator.validate(buildResult.getOutput());
        return buildResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildResult fails(String... arguments) {
        return fails(Arrays.asList(arguments));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getTestDirectory() {
        return temporaryFolder.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File file(String path) {
        return createNewFile(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File dir(String... path) {
        return createNewFolder(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getBuildFile() {
        return createNewFile("build.gradle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSettingsFile() {
        return createNewFile("settings.gradle");
    }

    private GradleRunner withArguments(List<String> arguments) {
        gradleRunner.withArguments(arguments);
        return gradleRunner;
    }

    private File createNewFile(String fileName) {
        File targetFile = new File(temporaryFolder.getRoot(), fileName);
        File parentDir = new File(temporaryFolder.getRoot(), fileName).getParentFile();

        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new RuntimeException(String.format("Unable to create directory '%s' in temporary directory", parentDir));
        }

        if (targetFile.isFile()) {
            return targetFile;
        }

        try {
            return temporaryFolder.newFile(fileName);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to create file '%s' in temporary directory", fileName), e);
        }
    }

    private File createNewFolder(String... folderNames) {
        File targetDir = new File(temporaryFolder.getRoot(), join(folderNames, "/"));

        if (targetDir.isDirectory()) {
            return targetDir;
        }

        try {
            return temporaryFolder.newFolder(folderNames);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to create directory '%s' in temporary directory", join(folderNames, ",")), e);
        }
    }
}
