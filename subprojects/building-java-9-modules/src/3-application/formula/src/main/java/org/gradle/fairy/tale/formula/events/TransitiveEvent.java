package org.gradle.fairy.tale.formula.events;

import org.gradle.actors.Actor;

/**
 * Something some actor did to some other actor.
 */
public class TransitiveEvent implements Event {
    private final Actor actor;
    private final String action;
    private final Actor object;

    public TransitiveEvent(Actor actor, String action, Actor object) {
        this.actor = actor;
        this.action = action;
        this.object = object;
    }

    @Override
    public String toString() {
        return actor + " " + action + " " + object + ".";
    }
}
