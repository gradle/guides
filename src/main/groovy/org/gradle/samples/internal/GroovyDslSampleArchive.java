package org.gradle.samples.internal;

import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class GroovyDslSampleArchive extends DslSampleArchive {
    @Inject
    public GroovyDslSampleArchive(String name) {
        super(GUtil.toCamelCase(name) + "GroovyDsl", "groovy", "groovy-dsl");
    }
}
