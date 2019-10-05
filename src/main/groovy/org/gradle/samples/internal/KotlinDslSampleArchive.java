package org.gradle.samples.internal;

import org.gradle.api.file.Directory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class KotlinDslSampleArchive extends DslSampleArchive {
    @Inject
    public KotlinDslSampleArchive(String name) {
        super(name + "KotlinDsl", "kotlin", "kotlin-dsl", "settings.gradle.kts");
    }

    public static boolean hasSource(Directory sampleDirectory) {
        // TODO: Support `kotlin-dsl` convention
        return sampleDirectory.dir("kotlin").getAsFile().exists();
    }
}
