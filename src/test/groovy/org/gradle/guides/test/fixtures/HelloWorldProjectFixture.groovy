package org.gradle.guides.test.fixtures

final class HelloWorldProjectFixture {

    private HelloWorldProjectFixture() {}

    static String successfulHelloWorldTask() {
        """
            task helloWorld {
                doLast {
                    println 'Hello World!'
                }
            }
        """
    }

    static String failingHelloWorldTask() {
        """
            task helloWorld {
                doLast {
                    throw new GradleException('expected failure')
                }
            }
        """
    }
}
