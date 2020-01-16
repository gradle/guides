package org.gradle.docs.guides

import org.gradle.docs.AbstractWellBehavePluginFunctionalTest

class GuideWellBehavePluginFunctionalTest extends AbstractWellBehavePluginFunctionalTest {
    @Override
    protected String getPluginIdUnderTest() {
        return 'org.gradle.guide'
    }
}
