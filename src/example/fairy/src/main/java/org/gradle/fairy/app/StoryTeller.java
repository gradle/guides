package org.gradle.fairy.app;

import org.gradle.fairy.tale.Tale;

import java.util.ServiceLoader;

class StoryTeller {

    public static void main(String[] args) {
        ServiceLoader<Tale> loader = ServiceLoader.load(Tale.class);
        for (Tale tale : loader) {
            tale.tell();
        }
    }
}
