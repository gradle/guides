/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.samples.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class SampleZipTask extends DefaultTask {
    @Internal
    public abstract DirectoryProperty getSampleDirectory();

    @InputFiles
    @SkipWhenEmpty
    protected FileTree getInputFiles() {
        return getSampleDirectory().getAsFileTree();
    }

    @OutputFile
    public abstract RegularFileProperty getSampleZipFile();

    @TaskAction
    private void doZip() throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(getSampleZipFile().get().getAsFile()))) {
            getSampleDirectory().getAsFileTree().visit(new FileVisitor() {
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
        }
    }
}
