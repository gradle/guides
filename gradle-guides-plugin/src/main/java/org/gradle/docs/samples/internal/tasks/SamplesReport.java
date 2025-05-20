package org.gradle.docs.samples.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.docs.samples.Sample;
import org.gradle.docs.samples.Samples;
import org.gradle.docs.samples.Template;
import org.gradle.internal.UncheckedException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Generates report with list of samples and the resolved attributes of the sample.
 */
public abstract class SamplesReport extends DefaultTask {
    @Internal
    public abstract Property<Samples> getSamples();

    @TaskAction
    public void generate() {
        getLogger().lifecycle(new Renderer().render(getSamples().get()));
    }

    private class Renderer {
        private final StringWriter sw = new StringWriter();
        private final PrintWriter pw = new PrintWriter(sw);
        private int indent = 0;

        String render(Samples samples) {
            title("Information about generated samples");
            hline();
            path("samples root", samples.getSamplesRoot());
            path("sample index", samples.getSampleIndexFile());
            newline();
            indent();
            for (Sample sample : samples.getPublishedSamples()) {
                title("Sample " + sample.getName());
                attribute("display name", sample.getDisplayName());
                attribute("dsls", sample.getDsls());
                attribute("category", sample.getCategory());
                attribute("description", sample.getDescription());
                attribute("doc ref", sample.getSampleDocName());
                path("src", sample.getSampleDirectory());

                listContent("common", sample.getCommonContent().getFrom());
                listContent("groovy", sample.getGroovyContent().getFrom());
                listContent("kotlin", sample.getKotlinContent().getFrom());
                listContent("tests", sample.getTestsContent().getFrom());

                newline();
            }
            unindent();
            hline();

            // TODO: We may want the list of sample binaries too

            path("templates root", samples.getTemplatesRoot());
            newline();
            indent();
            for (Template template : samples.getTemplates()) {
                title("Template " + template.getName());
                path("src", template.getSourceDirectory());
                path("dest", template.getTemplateDirectory());
                attribute("target", template.getTarget());
                newline();
            }
            unindent();
            hline();
            return sw.toString();
        }

        private void newline() {
            pw.println();
        }

        private void indent() {
            indent++;
        }

        private void unindent() {
            indent--;
        }

        private void title(String title) {
            pw.println(title);
        }

        private void listContent(String contentTitle, Set<Object> contentFiles) {
            id();
            title(contentTitle);
            indent();
            for (Object content : contentFiles) {
                id();
                pw.append("- ");
                if (content instanceof Template) {
                    pw.append("template '").append(((Template)content).getName()).append("' from ");
                    pw.append(String.valueOf(((Template) content).getSourceDirectory().getOrNull()));
                } else if (content instanceof Provider) {
                    pw.append(String.valueOf(((Provider) content).getOrNull()));
                } else {
                    pw.append(String.valueOf(content));
                }
                newline();
            }
            unindent();
        }

        private void path(String label, Provider<? extends FileSystemLocation> path) {
            File file = path.get().getAsFile();
            attribute(label, asClickableFileUrl(file));
        }

        private void attribute(String key, Provider<?> value) {
            String valueAsString = String.valueOf(value.getOrNull());
            attribute(key, valueAsString);
        }

        private void attribute(String key, String value) {
            id();
            pw.append(key).append(" = ").append(value);
            newline();
        }

        private void id() {
            for (int i=0; i<indent; i++) {
                pw.append('\t');
            }
        }

        private void hline() {
            pw.println("----------------------------------------------------------------");
        }
    }

    private String asClickableFileUrl(File path) {
        // File.toURI().toString() leads to an URL like this on Mac: file:/reports/index.html
        // This URL is not recognized by the Mac console (too few leading slashes). We solve
        // this be creating an URI with an empty authority.
        try {
            return new URI("file", "", path.toURI().getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }
}
