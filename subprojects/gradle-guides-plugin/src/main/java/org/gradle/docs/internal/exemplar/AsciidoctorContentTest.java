package org.gradle.docs.internal.exemplar;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AsciidoctorContentTest extends DefaultTask {
    private final List<AsciidoctorContentTestCase> testCases = new ArrayList<>();

    @Nested
    protected List<AsciidoctorContentTestCase> getTestCases() {
        return testCases;
    }

    public void testCase(Action<? super AsciidoctorContentTestCase> action) {
        AsciidoctorContentTestCase testCase = getObjectFactory().newInstance(AsciidoctorContentTestCase.class);
        action.execute(testCase);
        testCases.add(testCase);
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    /**
     * Note: The default console type doesn't affect the console choice requireing user interaction like `init` task or `--scan` flag
     * @return a property for configuring the console type to use as defined by {@link AsciidoctorContentTestConsoleType}
     */
    @Input @Optional
    public abstract Property<AsciidoctorContentTestConsoleType> getDefaultConsoleType();

    @Internal
    public abstract Property<String> getGradleVersion();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    private void doTest() throws IOException {
        WorkQueue workQueue = getWorkerExecutor().classLoaderIsolation(spec -> {
            spec.getClasspath().from(getClasspath());
        });

        workQueue.submit(AsciidoctorContentTestWorkerAction.class, parameter -> {
            parameter.getTestCases().set(getTestCases());
            parameter.getWorkspaceDirectory().set(getTemporaryDir());
            parameter.getGradleUserHomeDirectory().set(new File(getTemporaryDir(), "gradle-user-home"));
            parameter.getGradleVersion().set(getGradleVersion());
            parameter.getDefaultConsoleType().set(getDefaultConsoleType());
        });
    }
}
