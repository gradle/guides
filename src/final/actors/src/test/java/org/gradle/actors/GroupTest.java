package org.gradle.actors;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupTest {
    private static final String NOBODY = "nobodies";
    private static final Group EMPTY = Imagination.createGroup(NOBODY);

    private static final String BAND_NAME = "Beatles";
    private static final Actor JOHN = Imagination.createActor("John Lennon");
    private static final Actor PAUL = Imagination.createActor("Paul McCartney");
    private static final Actor GEORGE = Imagination.createActor("George Harrison");
    private static final Actor RINGO = Imagination.createActor("Ringo Starr");

    @Test
    public void nameMatches() {
        assertEquals(NOBODY, EMPTY.name());
    }

    @Test
    public void noActorsByDefault() {
        assertEquals(0, EMPTY.size());
        assertFalse(EMPTY.iterator().hasNext());
    }

    @Test
    public void containsActors() {
        Group beatles = Imagination.createGroup(BAND_NAME, JOHN, PAUL, GEORGE, RINGO);
        
        assertEquals(4, beatles.size());
        Collection<Actor> collection = Lists.newArrayList(beatles);
        assertTrue(collection.contains(JOHN));
        assertTrue(collection.contains(PAUL));
        assertTrue(collection.contains(GEORGE));
        assertTrue(collection.contains(RINGO));
    }

    @Test
    public void accurateToString() {
        Group beatles = Imagination.createGroup(BAND_NAME, JOHN, PAUL, GEORGE, RINGO);
        assertEquals("the 4 Beatles", beatles.toString());
        assertEquals("the 0 nobodies", EMPTY.toString());
    }
}
