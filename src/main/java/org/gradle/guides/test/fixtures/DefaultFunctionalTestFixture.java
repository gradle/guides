package org.gradle.guides.test.fixtures;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DefaultFunctionalTestFixture implements FunctionalTestFixture {

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
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
        return withArguments(arguments).build();
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
        return withArguments(arguments).buildAndFail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildResult fails(String... arguments) {
        return fails(Arrays.asList(arguments));
    }

    private GradleRunner withArguments(List<String> arguments) {
        gradleRunner.withArguments(arguments);
        return gradleRunner;
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

    private String join(String[] array, String separator) {
        StringBuilder joinedString = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                joinedString.append(separator);
            }

            joinedString.append(array[i]);
        }

        return joinedString.toString();
    }
}
