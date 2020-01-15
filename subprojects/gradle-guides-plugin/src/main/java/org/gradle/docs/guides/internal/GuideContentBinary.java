package org.gradle.docs.guides.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.docs.internal.components.RenderableContentComponent;
import org.gradle.docs.internal.components.TestableRenderedContentLinksComponent;
import org.gradle.docs.internal.components.ViewableContentComponent;
import org.gradle.docs.internal.components.NamedDocumentationComponent;

import javax.inject.Inject;

import static org.gradle.docs.internal.StringUtils.capitalize;

public abstract class GuideContentBinary extends NamedDocumentationComponent implements ViewableContentComponent, TestableRenderedContentLinksComponent, RenderableContentComponent {
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

    public abstract Property<String> getRenderedPermalink();

    public abstract Property<String> getSourcePermalink();

    public abstract RegularFileProperty getRenderedIndexPageFile();

    public abstract RegularFileProperty getInstalledIndexPageFile();

    @Override
    public String getViewTaskName() {
        return "view" + capitalize(getName()) + "Guide";
    }

    @Override
    public String getCheckLinksTaskName() {
        return "check" + capitalize(getName()) + "GuideLinks";
    }

    public abstract RegularFileProperty getSourcePageFile();

    public abstract Property<String> getGuideName();
}
