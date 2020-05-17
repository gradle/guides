package org.gradle.docs.samples.internal.tasks;

import org.apache.tools.zip.UnixStat;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.*;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.internal.work.WorkerLeaseService;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Zips a sample to the given location.
 * <p>
 * Skips execution if there are no "main" content.  This is usually DSL-specific content.
 * <p>
 * Removes references to the documentation (e.g. the lines starting with {@code // tag::}) and {@code // end::})
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

    @Input
    public abstract ListProperty<String> getExcludes();

    @OutputFile
    public abstract RegularFileProperty getArchiveFile();

    @Inject
    public abstract WorkerLeaseService getWorkerLeaseService();

    @TaskAction
    private void zip() {
        // TODO: Use the Worker API instead of releasing lock manually
        getWorkerLeaseService().withoutProjectLock(() -> {
            File zipFile = getArchiveFile().get().getAsFile();
            zipFile.delete();

            try (FileOutputStream fileStream = new FileOutputStream(zipFile);
                 ZipOutputStream zipStream = new ZipOutputStream(fileStream)) {
                zipStream.setMethod(ZipOutputStream.DEFLATED);
                Set<String> dirs = new HashSet<>();
                getFilteredSourceTree().visit(new FileVisitor() {
                    @Override
                    public void visitDir(FileVisitDetails dirDetails) {
                        try {
                            String dirPath = dirDetails.getRelativePath().getPathString();
                            if (!dirs.add(dirPath)) {
                                return;
                            }
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
                            final ZipEntry entry = new ZipEntry(fileDetails.getRelativePath().getPathString());
                            entry.setSize(fileDetails.getSize());
                            entry.setUnixMode(UnixStat.FILE_FLAG | fileDetails.getMode());
                            zipStream.putNextEntry(entry);

                            if (isTextFile(fileDetails)) {
                                byte[] content = readContent(fileDetails);
                                String contentString = new String(content, StandardCharsets.UTF_8);
                                if (containsUserGuideRefs(contentString)) {
                                    zipStream.write(filterUserGuideRefs(contentString).getBytes());
                                }
                                else  {
                                    zipStream.write(content);
                                }
                            } else {
                                fileDetails.copyTo(zipStream);
                            }

                            zipStream.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isTextFile(FileVisitDetails fileDetails) throws IOException {
        String fileName = fileDetails.getName();
        if (Stream.of(".java", ".groovy", ".kt", ".kts", ".gradle", ".out", ".conf").anyMatch(s -> fileName.endsWith(s))) {
            return true;
        }
        String type = Files.probeContentType(fileDetails.getFile().toPath());
        return type != null && type.startsWith("text");
    }

    private byte[] readContent(FileVisitDetails fileDetails) throws IOException {
        return Files.readAllBytes(fileDetails.getFile().toPath());
    }

    private boolean containsUserGuideRefs(String content) {
        return content.contains("// tag::") || content.contains("// end::");
    }

    private String filterUserGuideRefs(String content) throws IOException {
        return content.replaceAll("(// tag::|// end::).*\\R", "");
    }

    private FileTree getFilteredSourceTree() {
        return getSourceAsTree().matching(f -> f.exclude(getExcludes().get()));
    }
}
