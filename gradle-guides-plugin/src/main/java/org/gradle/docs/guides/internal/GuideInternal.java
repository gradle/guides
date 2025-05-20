package org.gradle.docs.guides.internal;

import org.gradle.docs.guides.Guide;

import javax.inject.Inject;

public abstract class GuideInternal implements Guide {
    private final String name;

    @Inject
    public GuideInternal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
