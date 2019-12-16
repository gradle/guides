package org.gradle.fairy.tale.pigs;

import org.gradle.actors.Actor;
import org.gradle.actors.Imagination;
import org.gradle.fairy.tale.Tale;
import org.gradle.fairy.tale.formula.FairyTale;

/**
 * The classic tale of "The Three Little Pigs"
 */
public class ThreeLittlePigs implements Tale {
    private Tale fairyTale;

    @Override
    public void tell() {
        if (fairyTale == null) {
            Actor strawPig = Imagination.createActor("the first little pig");
            Actor woodPig = Imagination.createActor("the second little pig");
            Actor brickPig = Imagination.createActor("the third little pig");
            Actor pigs = Imagination.createGroup("little pigs", strawPig, woodPig, brickPig);
            Actor wolf = Imagination.createActor("the big bad wolf");
            fairyTale = FairyTale.getWeaver()
                    .record(strawPig, "was lazy and built his house of straw.")
                    .record(woodPig, "was common and built his house of wood.")
                    .record(brickPig, "was smart and built his house of brick.")
                    .record(wolf, "wanted to eat", pigs)
                    .record(wolf, "knocked on the door of", strawPig)
                    .record(wolf, "said, 'Little pig, little pig, let me in.'")
                    .record(strawPig, "said 'Not by the hair on my chinny, chin, chin.")
                    .record(wolf, "huffed, and he puffed, and he blew in the house of", strawPig)
                    .record(strawPig, "ran over to join his neighbor in the wood house of", woodPig)
                    .record(wolf, "knocked on the door of", woodPig)
                    .record(wolf, "said, 'Little pig, little pig, let me in.'")
                    .record(woodPig, "said 'Not by the hair on my chinny, chin, chin.")
                    .record(wolf, "huffed, and he puffed, and he blew in the house of", woodPig)
                    .record(brickPig, "welcomed his frightened neighbors to his brick house.")
                    .record(wolf, "knocked on the door of", brickPig)
                    .record(wolf, "said, 'Little pig, little pig, let me in.'")
                    .record(brickPig, "said 'Not by the hair on my chinny, chin, chin.")
                    .record(wolf, "huffed, and he puffed, and he could NOT blow in the house of", brickPig)
                    .record(wolf, "hyper-ventilated and died.")
                    .weave();
        }
        fairyTale.tell();
    }
}
