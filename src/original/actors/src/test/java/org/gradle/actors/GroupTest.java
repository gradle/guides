package org.gradle.actors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
        assertTrue(EMPTY.isEmpty());
    }

    @Test
    public void containsActors() {
        Group beatles = Imagination.createGroup(BAND_NAME, JOHN, PAUL, GEORGE, RINGO);
        
        assertEquals(4, beatles.size());
        assertTrue(beatles.contains(JOHN));
        assertTrue(beatles.contains(PAUL));
        assertTrue(beatles.contains(GEORGE));
        assertTrue(beatles.contains(RINGO));
    }

    @Test
    public void accurateToString() {
        Group beatles = Imagination.createGroup(BAND_NAME, JOHN, PAUL, GEORGE, RINGO);
        assertEquals("the 4 Beatles", beatles.toString());
        assertEquals("the 0 nobodies", EMPTY.toString());
    }
}
