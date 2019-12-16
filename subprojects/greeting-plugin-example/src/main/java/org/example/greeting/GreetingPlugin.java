package org.example.greeting;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GreetingPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getTasks().create("hello", Greeting.class, (task) -> { // <1>
            task.setMessage("Hello");
            task.setRecipient("World");                                // <2>
        });
    }
}
