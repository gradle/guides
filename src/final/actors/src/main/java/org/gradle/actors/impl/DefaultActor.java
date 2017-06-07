package org.gradle.actors.impl;

import org.gradle.actors.Actor;

/**
 * Default implementation of {@link Actor}
 */
public class DefaultActor implements Actor {
    private final String name;

    public DefaultActor(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name();
    }
}
