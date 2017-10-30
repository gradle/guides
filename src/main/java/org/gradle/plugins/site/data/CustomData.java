package org.gradle.plugins.site.data;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * The data descriptor for user-provided information.
 */
public class CustomData {
    private final Property<String> websiteUrl;
    private final Property<String> vcsUrl;

    public CustomData(Project project) {
        this.websiteUrl = project.getObjects().property(String.class);
        this.vcsUrl = project.getObjects().property(String.class);
    }

    @Input
    @Optional
    public String getWebsiteUrl() {
        return websiteUrl.getOrNull();
    }

    public void setWebsiteUrl(Provider<String> websiteUrl) {
        this.websiteUrl.set(websiteUrl);
    }

    @Input
    @Optional
    public String getVcsUrl() {
        return vcsUrl.getOrNull();
    }

    public void setVcsUrl(Provider<String> vcsUrl) {
        this.vcsUrl.set(vcsUrl);
    }
}
