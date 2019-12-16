package org.gradle.guides.test.fixtures.utils

import spock.lang.Specification

class StringUtilsTest extends Specification {

    def "an empty String array returns an empty String"() {
        when:
        def joinedStrings = StringUtils.join([] as String[], ', ')

        then:
        joinedStrings == ''
    }

    def "a single String does not use separator"() {
        when:
        def joinedStrings = StringUtils.join(['a'] as String[], ', ')

        then:
        joinedStrings == 'a'
    }

    def "can join multiple Strings"() {
        when:
        def joinedStrings = StringUtils.join(['a', 'b', 'c'] as String[], ', ')

        then:
        joinedStrings == 'a, b, c'
    }
}
