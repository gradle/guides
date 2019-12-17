package org.gradle.guides.test.fixtures

final class JavaProjectFixture {

    private JavaProjectFixture() {}

    static String basicTestableJavaProject() {
        """
            apply plugin: 'java'
            
            repositories {
                jcenter()
            }
            
            dependencies {
                testCompile 'junit:junit:4.12'
            }
        """
    }

    static String simpleJavaClass() {
        """
            package com.company;

            public class MyClass {}
        """
    }
}
