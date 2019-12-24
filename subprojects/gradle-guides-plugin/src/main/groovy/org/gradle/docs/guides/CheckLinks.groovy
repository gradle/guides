/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.docs.guides

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.net.http.HttpBuilder
import org.cyberneko.html.parsers.SAXParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

@CompileStatic
class CheckLinks extends DefaultTask {

    @InputFile
    File getIndexDocument() {
        project.file(indexDoc)
    }

    void setIndexDocument(Object index) {
        indexDoc = index
    }

    @TaskAction
    void exec() {

        Set<URI> failures = []

        getAnchors(indexDocument.toURI()).each { anchor ->

            if (anchor.absolute) {
                if (anchor.scheme.startsWith('http')) {
                    if (!Blacklist.isBlacklisted(anchor)) {
                        def client = HttpBuilder.configure {
                            request.uri = anchor.toString()
                            request.headers = ['User-Agent': 'gradle-guides-plugin/0.0.0.1']
                        }
                        try {
                            client.head()
                            logger.info "PASSED: ${anchor}"
                        } catch (java.lang.RuntimeException e) {
                            failures.add(anchor)
                            logger.info "FAILED: ${anchor}"
                        }
                    } else {
                        logger.debug "SKIPPED (blacklisted): ${anchor}"
                    }
                } else {
                    logger.debug "SKIPPED (Not http/s): ${anchor}"
                }
            } else {
                logger.debug "SKIPPED (relative): ${anchor}"
            }
        }

        if (!failures.empty) {
            throw new GradleException("The following links are broken:\n " + failures.join("\n ") + "\n")
        }
    }

    @CompileDynamic
    private Set<URI> getAnchors(URI uri) {
        def parser = new SAXParser()
        def page = new XmlSlurper(parser).parseText(
                uri.toURL().getText(requestProperties: ['User-Agent': 'Non empty'])
        )

        def anchors = page.'**'.findAll {
            it.name() == 'A' && it.@href != null
        }.collect { "${it.@href}".toURI() }
    }

    private Object indexDoc

    @CompileStatic
    private static class Blacklist {

        // These hosts are blocking web scrapers.
        private static final List<String> BLACKLISTED_HOSTS = ["bugs.java.com", "youtrack.jetbrains.com"]

        private static boolean isBlacklisted(URI uri) {
            return BLACKLISTED_HOSTS.contains(uri.host)
        }
    }
}
