package org.gradle.docs.guides

import org.gradle.docs.AbstractWellBehavePluginFunctionalTest

class GuidesWellBehavePluginFunctionalTest extends AbstractWellBehavePluginFunctionalTest {
    @Override
    protected String getPluginIdUnderTest() {
        return 'org.gradle.guides'
    }
}
