package org.gradle.actors;

import com.google.common.collect.ImmutableSet;
import org.gradle.actors.impl.DefaultActor;
import org.gradle.actors.impl.DefaultGroup;

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
        return new DefaultGroup(name, ImmutableSet.copyOf(actors));
    }
}
