module org.gradle.fairy.tale.bears {
    requires org.gradle.actors;
    requires org.gradle.fairy.tale;
    requires org.gradle.fairy.tale.formula;

    exports org.gradle.fairy.tale.bears;
    provides org.gradle.fairy.tale.Tale
        with org.gradle.fairy.tale.bears.GoldilocksAndTheThreeBears;
}