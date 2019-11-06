package org.gradle.samples.internal.tasks;

import org.apache.tools.ant.filters.LineContainsRegExp;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.FilterReader;
import java.io.Reader;

public abstract class InstallSampleZipContentTask extends DefaultTask {
    private static final String WRAPPER_FILES_PATTERN = "gradle/**/*";

    @InputFiles
    public abstract ConfigurableFileCollection getSource();

    @OutputDirectory
    public abstract DirectoryProperty getInstallDirectory();

    @TaskAction
    private void doInstall() {
        getProject().sync(spec -> {
            FileTree nonWrapperFiles = getSource().getAsFileTree().matching(it -> it.exclude(WRAPPER_FILES_PATTERN));
            spec.from(nonWrapperFiles, it -> it.filter(AsciidoctorTagsFilter.class));

            FileTree wrapperFiles = getSource().getAsFileTree().matching(it -> it.include(WRAPPER_FILES_PATTERN));
            spec.from(wrapperFiles);

            spec.exclude("**/build/**", "**/.gradle/**");

            spec.into(getInstallDirectory());
        });
    }

    public static class AsciidoctorTagsFilter extends FilterReader {
        public AsciidoctorTagsFilter(Reader in) {
            super(configure(new LineContainsRegExp(in)));
        }

        private static LineContainsRegExp configure(LineContainsRegExp reader) {
            reader.setNegate(true);
            reader.setCaseSensitive(true);
            reader.setRegexp("^// (tag|end)::.+\\[\\]");
            return reader;
        }
    }
}
