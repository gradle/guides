package org.gradle.docs.guides.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public abstract class GuideContentBinary extends GuideBinary {
    @Inject
    public GuideContentBinary(String name) {
        super(name);
    }

    public abstract Property<String> getGradleVersion();

    public abstract Property<String> getRepositoryPath();

    public abstract Property<String> getDisplayName();

    public abstract RegularFileProperty getIndexPageFile();

    public abstract DirectoryProperty getGuideDirectory();

    public abstract Property<String> getBaseDirectory();

    public abstract Property<String> getPermalink();

    public abstract RegularFileProperty getRenderedIndexPageFile();

    public abstract ConfigurableFileCollection getSourceFiles();
}
