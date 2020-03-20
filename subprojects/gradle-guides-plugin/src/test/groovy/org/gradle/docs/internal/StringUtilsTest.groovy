package org.gradle.docs.internal

import spock.lang.Specification

class StringUtilsTest extends Specification {
    def "capitalize works"() {
        expect:
        StringUtils.capitalize("abcdef") == "Abcdef"
        StringUtils.capitalize("ABCDEF") == "ABCDEF"
        StringUtils.capitalize("Abcdef") == "Abcdef"
        StringUtils.capitalize("12345") == "12345"
    }

    def "uncapitalize works"() {
        expect:
        StringUtils.uncapitalize("abcdef") == "abcdef"
        StringUtils.uncapitalize("ABCDEF") == "aBCDEF"
        StringUtils.uncapitalize("Abcdef") == "abcdef"
        StringUtils.uncapitalize("12345") == "12345"
    }

    def "toTitleCase works"() {
        expect:
        StringUtils.toTitleCase("this is an example") == "This Is An Example"
        StringUtils.toTitleCase("abcdef") == "Abcdef"
        StringUtils.toTitleCase("ABCDEF") == "Abcdef"
        StringUtils.toTitleCase("Abcdef") == "Abcdef"
        StringUtils.toTitleCase("12345") == "12345"
    }

    def "toLowerCamelCase works"() {
        expect:
        StringUtils.toLowerCamelCase("ThisIsAnExample") == "thisIsAnExample"
        StringUtils.toLowerCamelCase("abcdef") == "abcdef"
        StringUtils.toLowerCamelCase("ABCDEF") == "abcdef"
        StringUtils.toLowerCamelCase("Abcdef") == "abcdef"
        StringUtils.toLowerCamelCase("12345") == "12345"
    }

    def "toSnakeCase works"() {
        expect:
        StringUtils.toSnakeCase("ThisIsAnExample") == "this_is_an_example"
        StringUtils.toSnakeCase("abcdef") == "abcdef"
        StringUtils.toSnakeCase("ABCDEF") == "a_bc_de_f"
        StringUtils.toSnakeCase("Abcdef") == "abcdef"
        StringUtils.toSnakeCase("12345") == "12345"
        StringUtils.toSnakeCase("javaJunit4IntegrationTestForListLibrary") == "java_junit4_integration_test_for_list_library"
    }

    def "toKebabCase works"() {
        expect:
        StringUtils.toKebabCase("ThisIsAnExample") == "This-is-an-example"
        StringUtils.toKebabCase("abcdef") == "abcdef"
        StringUtils.toKebabCase("ABCDEF") == "ABCDEF"
        StringUtils.toKebabCase("Abcdef") == "Abcdef"
        StringUtils.toKebabCase("12345") == "12345"
        StringUtils.toKebabCase("javaJunit4IntegrationTestForListLibrary") == "java-junit4-integration-test-for-list-library"
    }
}
