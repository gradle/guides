package org.gradle.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zips a sample to the given location.
 *
 * Skips execution if there are no "main" content.  This is usually DSL-specific content.
 */
public abstract class ZipSample extends DefaultTask {
    @InputFiles
    protected FileTree getSourceAsTree() {
        return getSource().getAsFileTree();
    }
    @InputFiles
    @SkipWhenEmpty
    protected FileTree getMainSourceAsTree() {
        return getMainSource().getAsFileTree();
    }

    @Internal
    public abstract ConfigurableFileCollection getSource();

    @Internal
    public abstract ConfigurableFileCollection getMainSource();

    @Internal
    public abstract ListProperty<String> getExcludes();

    @OutputFile
    public abstract RegularFileProperty getArchiveFile();

    @TaskAction
    private void zip() {
        File zipFile = getArchiveFile().get().getAsFile();
        zipFile.delete();

        try (FileOutputStream fileStream = new FileOutputStream(zipFile);
             ZipOutputStream zipStream = new ZipOutputStream(fileStream)) {
            getFilteredSourceTree().visit(new FileVisitor() {
                @Override
                public void visitDir(FileVisitDetails dirDetails) {
                    try {
                        zipStream.putNextEntry(new ZipEntry(dirDetails.getRelativePath().getPathString() + "/"));
                        zipStream.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                @Override
                public void visitFile(FileVisitDetails fileDetails) {
                    try {
                        zipStream.putNextEntry(new ZipEntry(fileDetails.getRelativePath().getPathString()));
                        fileDetails.copyTo(zipStream);
                        zipStream.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileTree getFilteredSourceTree() {
        return getSourceAsTree().matching(f -> f.exclude(getExcludes().get()));
    }
}
