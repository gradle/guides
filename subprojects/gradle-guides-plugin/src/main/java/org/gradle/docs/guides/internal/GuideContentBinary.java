package org.gradle.docs.guides.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.docs.internal.RenderableContentBinary;
import org.gradle.docs.internal.TestableContentBinary;
import org.gradle.docs.internal.ViewableContentBinary;

import javax.inject.Inject;

import static org.gradle.docs.internal.StringUtils.capitalize;

public abstract class GuideContentBinary extends GuideBinary implements ViewableContentBinary, TestableContentBinary, RenderableContentBinary {
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

    @Override
    public String getViewTaskName() {
        return "view" + capitalize(getName()) + "Guide";
    }

    @Override
    public String getCheckLinksTaskName() {
        return "check" + capitalize(getName()) + "GuideLinks";
    }

    public abstract RegularFileProperty getSourcePageFile();
}
