package org.gradle.sample.http;

public interface HttpCaller {
    HttpResponse get(String url);
}