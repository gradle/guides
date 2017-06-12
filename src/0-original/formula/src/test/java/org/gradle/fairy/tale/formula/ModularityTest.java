package org.gradle.fairy.tale.formula;

import org.gradle.actors.Actor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the visibility of objects to the formula module.
 */
public class ModularityTest {
    @Test
    public void canReachDefaultActor() {
        Actor actor = new org.gradle.actors.impl.DefaultActor("Elliot Ness");
        assertEquals("Elliot Ness", actor.toString());
    }

    /*
    @Test
    public void canReachGuavaClasses() {
        // This line would throw a compiler error because gradle has kept the implementation dependency "guava"
        // from leaking into the formula project.
        Set<String> strings = com.google.common.collect.ImmutableSet.of("Hello", "Goodbye");
        assertTrue(strings.contains("Hello"));
        assertTrue(strings.contains("Goodbye"));
    }
    */
}
