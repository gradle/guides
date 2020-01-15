package org.gradle.docs.internal.tasks;

import org.apache.tools.zip.UnixStat;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Zips a sample to the given location.
 */
public abstract class ZipDocumentation extends DefaultTask {
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

    @Optional
    @InputFile
    public abstract RegularFileProperty getReadmeFile();

    @OutputFile
    public abstract RegularFileProperty getArchiveFile();

    @TaskAction
    private void zip() {
        File zipFile = getArchiveFile().get().getAsFile();
        zipFile.delete();

        try (FileOutputStream fileStream = new FileOutputStream(zipFile);
             ZipOutputStream zipStream = new ZipOutputStream(fileStream)) {
            zipStream.setMethod(ZipOutputStream.DEFLATED);
            getFilteredSourceTree().visit(new FileVisitor() {
                @Override
                public void visitDir(FileVisitDetails dirDetails) {
                    try {
                    ZipEntry entry = new ZipEntry(dirDetails.getRelativePath().getPathString() + "/");
                    entry.setUnixMode(UnixStat.DIR_FLAG | dirDetails.getMode());
                    zipStream.putNextEntry(entry);
                    zipStream.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                }

                @Override
                public void visitFile(FileVisitDetails fileDetails) {
                    try {
                        final ZipEntry entry;
                        if (getReadmeFile().isPresent() && fileDetails.getName().equals(getReadmeFile().get().getAsFile().getName())) {
                            entry = new ZipEntry("README");
                        } else {
                            entry = new ZipEntry(fileDetails.getRelativePath().getPathString());
                        }
                        entry.setSize(fileDetails.getSize());
                        entry.setUnixMode(UnixStat.FILE_FLAG | fileDetails.getMode());
                        zipStream.putNextEntry(entry);
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
