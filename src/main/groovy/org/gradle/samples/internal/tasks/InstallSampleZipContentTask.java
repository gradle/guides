package org.gradle.samples.internal.tasks;

import org.apache.tools.ant.filters.LineContainsRegExp;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;

public abstract class InstallSampleZipContentTask extends DefaultTask {
    @InputFiles
    public abstract ConfigurableFileCollection getSource();

    @OutputDirectory
    public abstract DirectoryProperty getInstallDirectory();

    @TaskAction
    private void doInstall() {
        getProject().sync(spec -> {
            spec.from(getSource());

            spec.exclude("**/build/**", "**/.gradle/**");
            spec.eachFile(details -> {
                if (!isBinaryFile(details.getFile())) {
                    details.filter(AsciidoctorTagsFilter.class);
                }
            });

            spec.into(getInstallDirectory());
        });
    }

    private static boolean isBinaryFile(File file) {
        try (InputStream inStream = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int nread = inStream.read(buffer);
            for (int i = 0; i < nread; ++i) {
                if (buffer[i] == 0) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
