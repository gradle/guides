package org.myorg;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.Property;

public class ServerExtension {
    private final Property<String> url;
    
    public ServerExtension(Project project) {
        this.url = project.getObjects().property(String.class);
    }
    
    public void setUrl(String url) {
        this.url.set(url);
    }
    
    public String getUrl() {
        return url.get();
    }
    
    public Provider<String> getUrlProvider() {
        return url;
    }
}