package org.gradle.docs.samples

import org.gradle.docs.AbstractWellBehavePluginFunctionalTest

class SamplesWellBehavePluginFunctionalTest extends AbstractWellBehavePluginFunctionalTest {
    @Override
    protected String getPluginIdUnderTest() {
        return 'org.gradle.samples'
    }
}
