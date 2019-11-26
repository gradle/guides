package org.gradle.samples.internal;

import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
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
