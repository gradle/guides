package org.gradle.actors;

/**
 * Represents some {@link Actor}s.
 */
public interface Group extends Iterable<Actor>, Actor {
    /**
     * Returns the size of the {@link Group}
     */
    int size();
}
