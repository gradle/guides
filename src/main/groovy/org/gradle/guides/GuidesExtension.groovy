/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.guides

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.util.CollectionUtils

/**
 *
 * @since 0.1
 */
@CompileStatic
class GuidesExtension {

    GuidesExtension(Project project) {
    }

    /** Path of repository relative to {@code https://github.com}.
     *
     */
    String repoPath

    /** Name of main author
     *
     * @since 0.5
     */
    String mainAuthor

    /** Get list of supporting authors
     *
     * @return List of strings
     * @since 0.5
     */
    List<String> getSupAuthors() {
        this.supAuthors
    }

    /** Add supporting authors.
     *
     * @param a List of authors
     * @since 0.5
     */
    void supAuthors( Iterable<String> a ) {
        supAuthors.addAll(a)
    }

    /** Add supporting authors.
     *
     * @param a List of authors
     * @since 0.5
     */
    void supAuthors( String... a ) {
        supAuthors.addAll(a as List)
    }

    /** Returns all collaborators
     *
     * @return List of authors & collaborators
     * @since 0.5
     */
    List<String> getAllAuthors() {
        List<String> ret = [ this.mainAuthor ]
        ret + getSupAuthors()
    }

    /** An Asciidoc-formatted section that can be inserted when
     * {@code include::contribute[]} is encountered.
     *
     * @param msg Asciidoc-formatted code.
     */
    void setContributeMessage(Object msg) {
        this.contributeMessage = msg
    }

    /** Message that is printed at the end of the guide.
     *
     * @return Message in Asciidoc markup
     */
    String getContributeMessage() {
        CollectionUtils.stringize([this.contributeMessage])[0]
    }

    private List<String> supAuthors = []

    private Object contributeMessage = """

[.contribute]
== Help improve this guide

Have feedback or a question? Found a typo? Like all Gradle guides, help is just a GitHub issue away. Please add an issue or pull request to https://github.com/{repo-path}/[{repo-path}] and we'll get back to you.
"""

}
