package org.gradle.docs.guides.internal;

import org.gradle.api.Named;

public abstract class GuideBinary implements Named {
    private final String name;

    public GuideBinary(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
