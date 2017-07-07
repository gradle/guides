package org.myorg;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ServerPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ServerExtension extension = project.getExtensions().create("server", ServerExtension.class, project);
        Deploy deployTask = project.getTasks().create("deploy", Deploy.class);
        deployTask.setUrl(extension.getUrlProvider());
    }
}