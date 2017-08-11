package org.gradle.sample.http;

public class HttpResponse {
    private String message;
    private int code;
    
    public HttpResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "HTTP " + code + ", Reason: " + message;
    }
}