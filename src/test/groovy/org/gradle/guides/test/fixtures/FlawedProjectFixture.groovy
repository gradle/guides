package org.gradle.guides.test.fixtures

final class FlawedProjectFixture {

    private FlawedProjectFixture() {}

    static String deprecatedGradleApiInSuccessfulBuild() {
        """
            task helloWorld << {
                println 'Hello World!'
            }
        """
    }

    static String deprecatedGradleApiInFailingBuild() {
        """
            task byeWorld << {
                throw new GradleException('Expected')
            }
        """
    }

    static String unexpectedStackTraceInSuccessfulBuild() {
        """
            task helloWorld {
                doLast {
                    new Exception().printStackTrace()
                }
            }
        """
    }

    static String unexpectedStackTraceInFailingBuild() {
        """
            task byeWorld {
                doLast {
                    new Exception().printStackTrace()
                    throw new GradleException('Expected')
                }
            }
        """
    }
}
