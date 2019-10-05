package org.gradle.samples.internal;

import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class KotlinDslSampleArchive extends DslSampleArchive {
    @Inject
    public KotlinDslSampleArchive(String name) {
        super(GUtil.toCamelCase(name) + "KotlinDsl", "kotlin", "kotlin-dsl");
    }
}
