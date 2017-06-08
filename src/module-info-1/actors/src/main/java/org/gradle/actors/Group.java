package org.gradle.actors;

import java.util.Set;

/**
 * Represents some {@link Actor}s.
 */
public interface Group extends Set<Actor>, Actor {
    /**
     * Returns the size of the {@link Group}
     */
    int size();
}
