package org.gradle.sample;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.sample.tasks.UrlVerify;

public class UrlVerifierPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        UrlVerifierExtension extension = project.getExtensions().create("verification", UrlVerifierExtension.class, project);
        UrlVerify verifyUrlTask = project.getTasks().create("verifyUrl", UrlVerify.class);
        verifyUrlTask.setUrl(extension.getUrlProvider());
    }
}
