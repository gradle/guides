package org.gradle.actors.impl;

import org.gradle.actors.Actor;
import org.gradle.actors.Group;

import java.util.HashSet;

/**
 * Default Implementation of {@link Group}
 */
public class DefaultGroup extends HashSet<Actor> implements Group {
    private final String name;

    public DefaultGroup(String name) {
        super();
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("the %d %s", size(), name());
    }
}
