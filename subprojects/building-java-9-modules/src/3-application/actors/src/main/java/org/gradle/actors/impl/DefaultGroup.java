package org.gradle.actors.impl;

import com.google.common.collect.ImmutableSet;
import org.gradle.actors.Actor;
import org.gradle.actors.Group;

import java.util.Collection;
import java.util.Iterator;

/**
 * Default Implementation of {@link Group}
 */
public class DefaultGroup implements Group {
    private final String name;
    private final ImmutableSet<Actor> actors;

    public DefaultGroup(String name, ImmutableSet<Actor> actors) {
        super();
        this.name = name;
        this.actors = actors;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("the %d %s", size(), name());
    }

    @Override
    public boolean isEmpty() {
        return actors.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return actors.contains(o);
    }

    @Override
    public Object[] toArray() {
        return actors.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return actors.toArray(a);
    }

    @Override
    @Deprecated
    public boolean add(Actor actor) {
        return actors.add(actor);
    }

    @Override
    @Deprecated
    public boolean remove(Object o) {
        return actors.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return actors.containsAll(c);
    }

    @Override
    @Deprecated
    public boolean addAll(Collection<? extends Actor> c) {
        return actors.addAll(c);
    }

    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        return actors.retainAll(c);
    }

    @Override
    @Deprecated
    public boolean removeAll(Collection<?> c) {
        return actors.removeAll(c);
    }

    @Override
    @Deprecated
    public void clear() {
        actors.clear();
    }

    @Override
    public int size() {
        return actors.size();
    }

    @Override
    public Iterator<Actor> iterator() {
        return actors.iterator();
    }
}
