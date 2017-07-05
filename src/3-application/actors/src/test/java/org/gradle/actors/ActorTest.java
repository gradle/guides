package org.gradle.actors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActorTest {
    private static final String NAME = "Sean Connery";
    private static final Actor SEAN = Imagination.createActor(NAME);

    @Test
    public void nameMatches() {
        assertEquals(NAME, SEAN.name());
    }

    @Test
    public void accurateToString() {
        assertEquals(NAME, SEAN.toString());
    }

    @Test
    public void canReachDefaultActor() {
        Actor actor = new org.gradle.actors.impl.DefaultActor("Kevin Costner");
        assertEquals("Kevin Costner", actor.toString());
    }
}
