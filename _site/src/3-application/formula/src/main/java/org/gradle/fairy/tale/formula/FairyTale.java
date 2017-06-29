package org.gradle.fairy.tale.formula;

import org.gradle.actors.Actor;
import org.gradle.actors.Group;
import org.gradle.fairy.tale.Tale;
import org.gradle.fairy.tale.formula.events.Event;
import org.gradle.fairy.tale.formula.events.IntransativeEvent;
import org.gradle.fairy.tale.formula.events.TransitiveEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a fairy tale.
 */
public class FairyTale implements Tale {
    private final List<Actor> actors;
    private final List<Event> events;

    private FairyTale(List<Actor> actors, List<Event> events) {
        this.actors = actors;
        this.events = events;
    }

    @Override
    public void tell() {
        StringBuilder builder = new StringBuilder("Once upon a time, there lived ");
        for (int i=0; i < actors.size(); i++) {
            if (i == actors.size() - 1 && i != 0) {
                builder.append("and ");
            }
            builder.append(actors.get(i));
            if (i != actors.size() - 1 && actors.size() > 1) {
                builder.append(", ");
            }
        }
        builder.append(".").append(System.getProperty("line.separator"));

        for (Event event : events) {
            builder.append(event).append(System.getProperty("line.separator"));
        }
        builder.append("And they all lived happily ever after.")
                .append(System.getProperty("line.separator"))
                .append(System.getProperty("line.separator"));
        System.out.print(builder.toString());
    }

    public static Weaver getWeaver() {
        return new Weaver(new HashSet<>(), new HashSet<>(), new ArrayList<>());
    }

    public static class Weaver {
        private final Set<Actor> actorSet;
        private final Set<Actor> groupActors;
        private final List<Event> events;

        private Weaver(Set<Actor> actorSet, Set<Actor> groupActors, List<Event> events) {
            this.actorSet = actorSet;
            this.groupActors = groupActors;
            this.events = events;
        }

        public Tale weave() {
            List<Actor> actors = new ArrayList<>();
            actors.addAll(actorSet);
            return new FairyTale(actors, events);
        }

        public Weaver record(Actor actor, String action) {
            addActorOrGroup(actor);
            events.add(new IntransativeEvent(actor, action));
            return this;
        }

        public Weaver record(Actor actor, String action, Actor object) {
            addActorOrGroup(actor);
            addActorOrGroup(object);
            events.add(new TransitiveEvent(actor, action, object));
            return this;
        }

        private void addActorOrGroup(Actor actor) {
            if (actor instanceof Group) {
                for(Actor a : (Group) actor) {
                    actorSet.remove(a);
                    groupActors.add(a);
                }
            }
            if (!groupActors.contains(actor)) {
                actorSet.add(actor);
            }
        }
    }
}
