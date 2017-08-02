package org.gradle.guides.test.fixtures

final class FlawedProjectFixture {

    private static final String STACK_TRACE = """
Exception in thread "main" java.lang.NullPointerException
        at com.example.myproject.Book.getTitle(Book.java:16)
        at com.example.myproject.Author.getBookTitles(Author.java:25)
        at com.example.myproject.Bootstrap.main(Bootstrap.java:14)"""

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
                    println '''$STACK_TRACE'''
                }
            }
        """
    }

    static String unexpectedStackTraceInFailingBuild() {
        """
            task byeWorld {
                doLast {
                    println '''$STACK_TRACE'''
                    throw new GradleException('Expected')
                }
            }
        """
    }
}
