package org.gradle.sample.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.sample.http.DefaultHttpCaller;
import org.gradle.sample.http.HttpCallException;
import org.gradle.sample.http.HttpCaller;
import org.gradle.sample.http.HttpResponse;

public class UrlVerify extends DefaultTask {
    private HttpCaller httpCaller = new DefaultHttpCaller();
    private final Property<String> url;

    public UrlVerify() {
        this.url = getProject().getObjects().property(String.class);
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public void setUrl(Provider<String> url) {
        this.url.set(url);
    }
    
    @Input
    public String getUrl() {
        return url.get();
    }

    @TaskAction
    public void verify() {
        try {
            HttpResponse httpResponse = httpCaller.get(getUrl());

            if (httpResponse.getCode() != 200) {
                throw new GradleException(String.format("Failed to resolve url '%s' (%s)", getUrl(), httpResponse.toString()));
            }
        } catch (HttpCallException e) {
            throw new GradleException(String.format("Failed to resolve url '%s'", getUrl(), e));
        }

        getLogger().quiet(String.format("Successfully resolved URL '%s'", getUrl()));
    }

    void setHttpCaller(HttpCaller httpCaller) {
        this.httpCaller = httpCaller;
    }
}
