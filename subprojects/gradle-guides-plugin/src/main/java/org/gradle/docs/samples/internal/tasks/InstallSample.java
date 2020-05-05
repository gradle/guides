package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.docs.samples.Dsl;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Installs the sample's zip to the given directory.
 */
public abstract class InstallSample extends DefaultTask {
    @InputFiles
    @SkipWhenEmpty
    protected FileTree getSourceAsTree() {
        return getSource().getAsFileTree();
    }

    @Internal
    public abstract ConfigurableFileCollection getSource();

    @OutputDirectory
    public abstract DirectoryProperty getInstallDirectory();

    @Input
    public abstract Property<Dsl> getDsl();

    @TaskAction
    private void doInstall() {
        getProject().sync(spec -> {
            spec.from(getSource(), copySpec -> getFilteredExtensions().forEach(ext -> copySpec.exclude(ext) ));
            spec.into(getInstallDirectory());
        });
    }

    private List<String> getFilteredExtensions() {
        Dsl dsl = getDsl().get();
        EnumSet<Dsl> otherDsls = EnumSet.complementOf(EnumSet.of(dsl));
        return otherDsls.stream().map(d -> "**/*." +  d.getDisplayName().toLowerCase() + ".sample.conf").collect(Collectors.toList());
    }
}
