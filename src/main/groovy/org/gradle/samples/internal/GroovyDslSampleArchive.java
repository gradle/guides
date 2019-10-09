package org.gradle.samples.internal;

import org.gradle.api.file.Directory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class GroovyDslSampleArchive extends DslSampleArchive {
    @Inject
    public GroovyDslSampleArchive(String name) {
        super(name + "GroovyDsl", "groovy", "groovy-dsl", "settings.gradle");
    }

    public static boolean hasSource(Directory sampleDirectory) {
        // TODO: Support `groovy-dsl` convention
        return sampleDirectory.dir("groovy").getAsFile().exists();
    }
}
