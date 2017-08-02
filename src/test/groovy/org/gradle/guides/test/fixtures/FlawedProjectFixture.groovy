package org.gradle.guides.test.fixtures

final class FlawedProjectFixture {

    private FlawedProjectFixture() {}

    static String deprecatedGradleAPI() {
        """
            task helloWorld << {
                println 'Hello World!'
            }
        """
    }

    static String unexpectedStackTrace() {
        """
            task helloWorld {
                doLast {
                    println '''
Exception in thread "main" java.lang.NullPointerException
        at com.example.myproject.Book.getTitle(Book.java:16)
        at com.example.myproject.Author.getBookTitles(Author.java:25)
        at com.example.myproject.Bootstrap.main(Bootstrap.java:14)'''
                }
            }
        """
    }
}
