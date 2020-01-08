package org.gradle.docs.samples.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.Property;
import org.gradle.docs.internal.RenderableContentBinary;
import org.gradle.docs.internal.TestableContentBinary;
import org.gradle.docs.internal.ViewableContentBinary;
import org.gradle.docs.samples.SampleSummary;

import javax.inject.Inject;

import static org.gradle.docs.internal.StringUtils.capitalize;

public abstract class SampleContentBinary extends SampleBinary implements ViewableContentBinary, TestableContentBinary, RenderableContentBinary {
    @Inject
    public SampleContentBinary(String name) {
        super(name);
    }

    public abstract Property<String> getDisplayName();

    public abstract DirectoryProperty getSampleDirectory();

    public abstract Property<String> getBaseName();

    public abstract Property<String> getPermalink();

    public abstract RegularFileProperty getIndexPageFile();

    public abstract RegularFileProperty getRenderedIndexPageFile();

    public abstract Property<SampleSummary> getSummary();

    public abstract ConfigurableFileCollection getSourceFiles();

    public abstract DirectoryProperty getSampleInstallDirectory();

    @Override
    public String getViewTaskName() {
        return "view" + capitalize(getName()) + "Sample";
    }

    @Override
    public String getCheckLinksTaskName() {
        return "check" + capitalize(getName()) + "SampleLinks";
    }
}
