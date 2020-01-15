/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.docs.snippets

import org.gradle.docs.TestFile
import org.gradle.docs.Dsl

trait SnippetsTrait {
    static String createSnippet(String name) {
        return """
            documentation.snippets.publishedSnippets.create('$name')
        """
    }

    static String configureSnippetKotlinDsl(String name) {
        return """
            ${snippetDsl(name)}.dsls.add(${Dsl.canonicalName}.KOTLIN)
        """
    }

    static String configureSnippetGroovyDsl(String name) {
        return """
            ${snippetDsl(name)}.dsls.add(${Dsl.canonicalName}.GROOVY)
        """
    }

    static void writeGroovyDslSnippetTo(TestFile directory) {
        directory.file('build.gradle') << '''
            |// tag::println[]
            |println "Hello, world!"
            |// end:println[]
            |'''.stripMargin()
        directory.file('settings.gradle') << '''
            |// tag::root-project-name[]
            |rootProject.name = 'demo'
            |// end:root-project-name[]
            |'''.stripMargin()
    }

    static void writeKotlinDslSnippetTo(TestFile directory) {
        directory.file('build.gradle.kts') << '''
            |// tag::println[]
            |println("Hello, world!")
            |// end:println[]
            |'''.stripMargin()
        directory.file('settings.gradle.kts') << '''
            |// tag::root-project-name[]
            |rootProject.name = "demo"
            |// end:root-project-name[]
            |'''.stripMargin()
    }

    static TestFile groovyDslZipFile(TestFile buildDirectory, String name) {
        return buildDirectory.file("working/snippets/zips/snippet_${name}-groovy-dsl.zip")
    }

    static TestFile kotlinDslZipFile(TestFile buildDirectory, String name) {
        return buildDirectory.file("working/snippets/zips/snippet_${name}-kotlin-dsl.zip")
    }

    static String snippetDsl(String name) {
        return "documentation.snippets.publishedSnippets.${name}"
    }

    static List<String> allTaskToAssemble(String name) {
        return [":${assembleTaskName(name)}", ":generateWrapperForSnippets", ":zipSnippet${name.capitalize()}Groovy", ":zipSnippet${name.capitalize()}Kotlin"]
    }

    static String assembleTaskName(String name) {
        return "assemble${name.capitalize()}Snippet"
    }
}