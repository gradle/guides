package org.gradle.docs.samples.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.docs.samples.Template;

import javax.inject.Inject;
import java.util.concurrent.Callable;

public abstract class TemplateInternal implements Template, Callable<DirectoryProperty> {
    private final String name;

    @Inject
    public TemplateInternal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DirectoryProperty call() throws Exception {
        return getTemplateDirectory();
    }
}
