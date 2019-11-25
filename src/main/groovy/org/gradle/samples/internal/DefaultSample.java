package org.gradle.samples.internal;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.samples.Dsl;
import org.gradle.samples.Sample;

import javax.inject.Inject;

public abstract class DefaultSample implements Sample {
    private final String name;

    @Inject
    public DefaultSample(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public abstract DirectoryProperty getSampleDirectory();
    @Override
    public abstract RegularFileProperty getReadMeFile();
    @Override
    public abstract RegularFileProperty getLicenseFile();
    @Override
    public abstract Property<String> getDescription();
    @Override
    public abstract Property<String> getDisplayName();
    @Override
    public abstract ConfigurableFileCollection getCommonContent();
    @Override
    public abstract ConfigurableFileCollection getGroovyContent();
    @Override
    public abstract ConfigurableFileCollection getKotlinContent();
    @Override
    public abstract ListProperty<Dsl> getDsls();
    @Override
    public abstract DirectoryProperty getInstallDirectory();
    @Override
    public abstract RegularFileProperty getSamplePageFile();

    @Override
    public void common(Action<? super ConfigurableFileCollection> action) {
        action.execute(getCommonContent());
    }
    @Override
    public void groovy(Action<? super ConfigurableFileCollection> action) {
        action.execute(getGroovyContent());
    }
    @Override
    public void kotlin(Action<? super ConfigurableFileCollection> action) {
        action.execute(getKotlinContent());
    }
}
