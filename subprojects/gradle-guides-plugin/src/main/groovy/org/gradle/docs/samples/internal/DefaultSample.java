package org.gradle.docs.samples.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.docs.samples.Sample;

import javax.inject.Inject;

import static org.gradle.docs.internal.StringUtils.capitalize;

public abstract class DefaultSample implements Sample {
    private final String name;
    private final TaskProvider<Task> assemble;
    private final TaskProvider<Task> check;

    @Inject
    public DefaultSample(String name, TaskContainer tasks) {
        this.name = name;
        this.assemble = tasks.register("assemble" + capitalize(name + "Sample"));
        this.check = tasks.register("check" + capitalize(name + "Sample"));
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
    @Override
    public void tests(Action<? super ConfigurableFileCollection> action) {
        action.execute(getTestsContent());
    }

    @Override
    public TaskProvider<Task> getAssembleTask() {
        return assemble;
    }

    @Override
    public TaskProvider<Task> getCheckTask() {
        return check;
    }
}
