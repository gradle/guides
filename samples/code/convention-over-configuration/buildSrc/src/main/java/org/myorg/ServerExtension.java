package org.myorg;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.PropertyState;

public class ServerExtension {
    private final PropertyState<String> url;
    
    public ServerExtension(Project project) {
        this.url = project.property(String.class);
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