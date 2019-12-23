package org.gradle.guides.test.fixtures

import static org.gradle.guides.test.fixtures.JavaProjectFixture.basicTestableJavaProject
import static org.gradle.guides.test.fixtures.JavaProjectFixture.simpleJavaClass
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class BasicFunctionalTest extends AbstractFunctionalTest {

    def "can successfully execute project"() {
        given:
        gradleRunner.forwardOutput()
        buildFile << basicTestableJavaProject()
        settingsFile << """
            rootProject.name = 'my-proj'
        """
        file('src/main/java/com/company/MyClass.java') << simpleJavaClass()

        when:
        def result = succeeds('compileJava')

        then:
        result.task(':compileJava').outcome == SUCCESS
        dir('build', 'classes', 'java', 'main', 'com', 'company').isDirectory()
        file('build/classes/java/main/com/company/MyClass.class').isFile()
    }
}
