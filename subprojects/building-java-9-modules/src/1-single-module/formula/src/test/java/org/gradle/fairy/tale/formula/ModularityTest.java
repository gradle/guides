package org.gradle.fairy.tale.formula;

import org.gradle.actors.Actor;
import org.gradle.actors.Imagination;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the visibility of objects to the formula module.
 */
@SuppressWarnings("unchecked")
public class ModularityTest {
    @Test
    public void canReachActor() {
        Actor actor = Imagination.createActor("Sean Connery");
        assertEquals("Sean Connery", actor.toString());
    }

    @Test
    public void canDynamicallyReachDefaultActor() throws Exception {
        Class clazz = ModularityTest
            .class.getClassLoader()
            .loadClass("org.gradle.actors.impl.DefaultActor");
        Actor actor = (Actor) clazz.getConstructor(String.class)
            .newInstance("Kevin Costner");
        assertEquals("Kevin Costner", actor.toString());
    }

    @Test
    public void canReachDefaultActor() {
        Actor actor = new org.gradle.actors.impl.DefaultActor("Kevin Costner");
        assertEquals("Kevin Costner", actor.toString());
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
