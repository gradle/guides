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

package org.gradle.docs.internal.tasks;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import groovyx.net.http.HttpBuilder;
import org.cyberneko.html.parsers.SAXParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.docs.internal.IOUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
public abstract class CheckLinks extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getIndexDocument();

    @TaskAction
    private void exec() throws IOException, SAXException {

        Set<URI> failures = new HashSet<>();

        getAnchors(getIndexDocument().get().getAsFile().toURI()).forEach(anchor -> {
            if (anchor.isAbsolute()) {
                if (anchor.getScheme().startsWith("http")) {
                    if (!Blacklist.isBlacklisted(anchor)) {
                        HttpBuilder client = HttpBuilder.configure(config -> {
                            config.getRequest().setUri(anchor.toString());

                            Map<String, String> headers = new HashMap<>();
                            headers.put("User-Agent", "gradle-guides-plugin/0.0.0.1");
                            config.getRequest().setHeaders(headers);
                        });
                        try {
                            client.head();
                            getLogger().info("PASSED: " + anchor);
                        } catch (java.lang.RuntimeException e) {
                            failures.add(anchor);
                            getLogger().info("FAILED: " + anchor);
                        }
                    } else {
                        getLogger().debug("SKIPPED (blacklisted): " + anchor);
                    }
                } else {
                    getLogger().debug("SKIPPED (Not http/s): " + anchor);
                }
            } else {
                getLogger().debug("SKIPPED (relative): " + anchor);
            }
        });

        if (!failures.isEmpty()) {
            throw new GradleException("The following links are broken:\n " + failures.stream().map(URI::toString).collect(Collectors.joining("\n")) + "\n");
        }
    }

    private Set<URI> getAnchors(URI uri) throws IOException, SAXException {
        SAXParser parser = new SAXParser();
        URLConnection connection = uri.toURL().openConnection();
        connection.addRequestProperty("User-Agent", "Non empty");

        GPathResult page = new XmlSlurper(parser).parseText(IOUtils.toString(connection.getInputStream(), Charset.defaultCharset()));

        Spliterator<GPathResult> it = Spliterators.spliteratorUnknownSize(page.depthFirst(), Spliterator.NONNULL);
        return StreamSupport.stream(it, false).filter(e -> e.name().equals("A") && ((NodeChild)e).attributes().get("href") != null).map(NodeChild.class::cast).map(e -> {
            try {
                return new URI(e.attributes().get("href").toString());
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toSet());
    }

    private static class Blacklist {
        // These hosts are blocking web scrapers.
        private static final List<String> BLACKLISTED_HOSTS = Arrays.asList("bugs.java.com", "youtrack.jetbrains.com");

        private static boolean isBlacklisted(URI uri) {
            return BLACKLISTED_HOSTS.contains(uri.getHost());
        }
    }
}
