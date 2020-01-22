package org.gradle.docs.internal.configure;

import groovy.lang.Closure;
import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.docs.internal.RenderableContentBinary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.gradle.docs.internal.FileUtils.deleteDirectory;

public class AsciidoctorTasks {
    private static final Object IGNORED_CLOSURE_OWNER = new Object();

    public static void configureResources(AsciidoctorTask task, Collection<? extends RenderableContentBinary> binaries) {
        task.getInputs().files(binaries.stream().map(RenderableContentBinary::getResourceFiles).collect(Collectors.toList())).withPropertyName("resourceFiles").optional(true);
        task.resources(new Closure(IGNORED_CLOSURE_OWNER) {
            public Object doCall(Object ignore) {
                binaries.stream().map(RenderableContentBinary::getResourceSpec).forEach(spec -> ((CopySpec)this.getDelegate()).with(spec.get()));
                return null;
            }
        });
    }

    public static void configureSources(AsciidoctorTask task, Collection<? extends RenderableContentBinary> binaries) {
        task.sources(new Closure(IGNORED_CLOSURE_OWNER) {
            public Object doCall(Object ignore) {
                ((PatternSet)this.getDelegate()).setIncludes(binaries.stream().map(it -> it.getSourcePattern().get()).collect(Collectors.toList()));
                return null;
            }
        });
    }

    public static void cleanStaleFiles(AsciidoctorTask task) {
        // It seems Asciidoctor task is copying the resource as opposed to synching them. Let's delete the output folder first.
        task.doFirst(new Action<Task>() {
            @Override
            public void execute(Task t) {
                deleteDirectory(task.getOutputDir());
            }
        });
    }

    public static Map<String, Object> genericAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("doctype", "book");
        attributes.put("icons", "font");
        attributes.put("source-highlighter", "prettify");
        attributes.put("toc", "auto");
        attributes.put("toclevels", 1);
        attributes.put("toc-title", "Contents");
        return Collections.unmodifiableMap(attributes);
    }
}
