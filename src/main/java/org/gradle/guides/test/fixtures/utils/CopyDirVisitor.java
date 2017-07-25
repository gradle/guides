package org.gradle.guides.test.fixtures.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyDirVisitor extends SimpleFileVisitor<Path> {
    private final Path sourceDir;
    private final Path targetDir;

    public CopyDirVisitor(Path sourceDir, Path targetDir) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path targetPath = targetDir.resolve(sourceDir.relativize(dir));

        if(!Files.exists(targetPath)){
            Files.createDirectory(targetPath);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, targetDir.resolve(sourceDir.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }
}
