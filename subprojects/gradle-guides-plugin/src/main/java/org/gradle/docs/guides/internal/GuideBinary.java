package org.gradle.docs.guides.internal;

import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class GuideBinary implements Named {
    private final String name;

    @Inject
    public GuideBinary(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract Property<String> getGradleVersion();

    public abstract Property<String> getRepositoryPath();

    public abstract Property<String> getDisplayName();

    public abstract RegularFileProperty getIndexPageFile();

    public abstract DirectoryProperty getGuideDirectory();

    public abstract Property<String> getPermalink();
}
