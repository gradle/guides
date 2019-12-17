package org.gradle.samples.internal;

import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.samples.ExemplarExtension;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultExemplarExtension implements ExemplarExtension {
    final List<Action<? super CopySpec>> actions = new ArrayList<>();

    @Override
    public void source(Action<? super CopySpec> action) {
        actions.add(action);
    }
}
