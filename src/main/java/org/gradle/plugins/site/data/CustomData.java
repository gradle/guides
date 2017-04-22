package org.gradle.plugins.site.data;

import org.gradle.api.Project;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * The data descriptor for user-provided information.
 */
public class CustomData {
    private final PropertyState<String> websiteUrl;
    private final PropertyState<String> vcsUrl;

    public CustomData(Project project) {
        this.websiteUrl = project.property(String.class);
        this.vcsUrl = project.property(String.class);
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
