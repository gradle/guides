package org.gradle.actors;

import org.gradle.actors.impl.DefaultActor;
import org.gradle.actors.impl.DefaultGroup;

import java.util.Arrays;

/**
 * The place where {@link Actor}s and {@link Group}s come from.
 */
public class Imagination {
    /**
     * Creates and returns an {@link Actor} with the given <code>name</code>.
     */
    public static Actor createActor(String name) {
        return new DefaultActor(name);
    }

    /**
     * Creates a {@link Group} with the given <code>name</code> containing the <code>actors</code>
     */
    public static Group createGroup(String name, Actor... actors) {
        DefaultGroup group = new DefaultGroup(name);
        group.addAll(Arrays.asList(actors));
        return group;
    }
}
