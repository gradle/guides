package org.gradle.guides.test.fixtures.validation

import spock.lang.Specification

class DefaultOutputValidatorTest extends Specification {

    def outputValidator = new DefaultOutputValidator()

    def "does not throw exception for valid output"() {
        when:
        outputValidator.validate(""":helloWorld
Hello world
""")

        then:
        noExceptionThrown()
    }

    def "throws exception if deprecation message is discovered"() {
        when:
        outputValidator.validate(""":helloWorld
Hello world
 
The Task.leftShift(Closure) method has been deprecated and is scheduled to be removed in Gradle 5.0. Please use Task.doLast(Action) instead.
""")

        then:
        Throwable t = thrown(AssertionError)
        t.message == """Line 4 contains a deprecation warning: The Task.leftShift(Closure) method has been deprecated and is scheduled to be removed in Gradle 5.0. Please use Task.doLast(Action) instead.
=====
:helloWorld
Hello world
 
The Task.leftShift(Closure) method has been deprecated and is scheduled to be removed in Gradle 5.0. Please use Task.doLast(Action) instead.

=====
"""
    }

    def "throws exception if stack trace is discovered"() {
        when:
        outputValidator.validate(""":helloWorld
Hello world

Stacktrace converted to String: java.lang.NumberFormatException: For input string: "Not a number"
        at java.lang.NumberFormatException.forInputString(Unknown Source)
        at java.lang.Integer.parseInt(Unknown Source)
        at java.lang.Integer.parseInt(Unknown Source)
        at StackTraceToStringExample.main(StackTraceToStringExample.java:16)
""")

        then:
        Throwable t = thrown(AssertionError)
        t.message == """Line 5 contains an unexpected stack trace:         at java.lang.NumberFormatException.forInputString(Unknown Source)
=====
:helloWorld
Hello world

Stacktrace converted to String: java.lang.NumberFormatException: For input string: "Not a number"
        at java.lang.NumberFormatException.forInputString(Unknown Source)
        at java.lang.Integer.parseInt(Unknown Source)
        at java.lang.Integer.parseInt(Unknown Source)
        at StackTraceToStringExample.main(StackTraceToStringExample.java:16)

=====
"""
    }
}
