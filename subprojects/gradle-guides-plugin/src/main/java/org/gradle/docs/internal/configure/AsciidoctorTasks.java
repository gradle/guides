package org.gradle.docs.internal.configure;

import org.asciidoctor.gradle.AsciidoctorTask;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.StandardOutputListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AsciidoctorTasks {
    public static void failTaskOnRenderingErrors(AsciidoctorTask task) {
        List<String> capturedOutput = new ArrayList<>();
        StandardOutputListener listener = it -> capturedOutput.add(it.toString());

        task.getLogging().addStandardErrorListener(listener);
        task.getLogging().addStandardOutputListener(listener);

        task.doLast(new Action<Task>() {
            @Override
            public void execute(Task t) {
                task.getLogging().removeStandardOutputListener(listener);
                task.getLogging().removeStandardErrorListener(listener);
                String output = capturedOutput.stream().collect(Collectors.joining());
                if (output.indexOf("include file not found:") > 0) {
                    throw new RuntimeException("Include file(s) not found.");
                }
            }
        });
    }
}
