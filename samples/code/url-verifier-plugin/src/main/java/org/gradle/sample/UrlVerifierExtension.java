package org.gradle.sample;

import org.gradle.api.Project;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;

public class UrlVerifierExtension {
    private final PropertyState<String> url;

    public UrlVerifierExtension(Project project) {
        this.url = project.property(String.class);
    }

    public String getUrl() {
        return url.get();
    }

    public Provider<String> getUrlProvider() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }
}
