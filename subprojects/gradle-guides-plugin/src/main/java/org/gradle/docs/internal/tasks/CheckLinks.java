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
import org.cyberneko.html.parsers.SAXParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.docs.internal.IOUtils;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashSet;
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

    @Inject
    public abstract WorkerExecutor getWorkerExecuter();

    @TaskAction
    private void exec() {
        WorkQueue queue = getWorkerExecuter().noIsolation();
        queue.submit(CheckLinksAction.class, params -> {
            params.getIndexDocument().set(getIndexDocument());
        });
    }

    public interface CheckLinksParameters extends WorkParameters {
        RegularFileProperty getIndexDocument();
    }

    public static abstract class CheckLinksAction implements WorkAction<CheckLinksParameters> {
        private static final Logger logger = Logging.getLogger(CheckLinksAction.class);

        @Override
        public void execute() {
            try {
                Set<URI> failures = new HashSet<>();
                getAnchors(getParameters().getIndexDocument().get().getAsFile().toURI()).forEach(anchor -> {
                    if (anchor.isAbsolute()) {
                        if (anchor.getScheme().startsWith("http")) {
                            if (!isValid(anchor)) {
                                failures.add(anchor);
                            }
                        } else {
                            logger.debug("SKIPPED (Not http/s): " + anchor);
                        }
                    } else {
                        logger.debug("SKIPPED (relative): " + anchor);
                    }
                });

                if (!failures.isEmpty()) {
                    throw new GradleException("The following links are broken:\n " + failures.stream().map(URI::toString).collect(Collectors.joining("\n")) + "\n");
                }
            } catch (IOException | SAXException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isValid(URI anchor) {
            for (int i=0; i<3; i++) {
                try {
                    HttpURLConnection con = (HttpURLConnection) anchor.toURL().openConnection();
                    con.setInstanceFollowRedirects(true);
                    con.setRequestMethod("HEAD");
                    // Fake being a browser
                    con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
                    // timeout in 5 seconds
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    int responseCode = con.getResponseCode();
                    logger.info("RESPONSE: {} = {}", anchor, responseCode);
                    return true;
                } catch (IOException e) {
                    logger.error("FAILED: {}", anchor, e);
                    // https://github.com/gradle/gradle-private/issues/3109
                    // Server is accessible, but we don't keep sessions
                    if(e.getMessage().contains("Server redirected too many")) {
                        return true;
                    }
                }
            }
            return false;
        }

        private Set<URI> getAnchors(URI uri) throws IOException, SAXException {
            SAXParser parser = new SAXParser();
            URLConnection connection = uri.toURL().openConnection();
            connection.addRequestProperty("User-Agent", "Non empty");

            GPathResult page = new XmlSlurper(parser).parseText(IOUtils.toString(connection.getInputStream(), Charset.defaultCharset()));

            Spliterator<GPathResult> it = Spliterators.spliteratorUnknownSize(page.depthFirst(), Spliterator.NONNULL);
            return StreamSupport.stream(it, false).filter(e -> e.name().equals("A") && ((NodeChild) e).attributes().get("href") != null).map(NodeChild.class::cast).map(e -> {
                try {
                    return new URI(e.attributes().get("href").toString());
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.toSet());
        }
    }
}
