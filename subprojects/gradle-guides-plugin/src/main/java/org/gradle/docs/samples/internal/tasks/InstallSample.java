package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.*;
import org.gradle.docs.samples.Dsl;

import java.io.File;
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
        getSourceAsTree().getElements();

        getProject().sync(spec -> {
            spec.from(getSource(), copySpec -> getFilteredExtensions().forEach(ext -> copySpec.exclude(ext) ));
            spec.into(getInstallDirectory());
        });
    }

    private Spec<? super File> getFilteredFiles() {
        Dsl dsl = getDsl().getOrNull();
        if (dsl == null) {
            return Specs.satisfyAll();
        }
        EnumSet<Dsl> otherDsls = EnumSet.complementOf(EnumSet.of(dsl));
        List<String> excluded = otherDsls.stream().map(d -> d.getDisplayName().toLowerCase() + ".sample.conf").collect(Collectors.toList());
        return (Spec<File>) file -> !excluded.stream().anyMatch(ext -> file.getName().endsWith(ext));
    }

    private List<String> getFilteredExtensions() {
        Dsl dsl = getDsl().get();
        EnumSet<Dsl> otherDsls = EnumSet.complementOf(EnumSet.of(dsl));
        return otherDsls.stream().map(d -> "**/*." +  d.getDisplayName().toLowerCase() + ".sample.conf").collect(Collectors.toList());
    }
}
