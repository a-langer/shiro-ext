package com.github.alanger.shiroext.http;

import java.util.HashMap;
import java.util.Map;

// org.springframework.http.HttpMethod
public enum HttpMethod {

    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    private static final Map<String, HttpMethod> mappings = new HashMap<>(16);

    static {
        for (HttpMethod httpMethod : values()) {
            mappings.put(httpMethod.name(), httpMethod);
        }
    }

    public static HttpMethod resolve(String method) {
        return (method != null ? mappings.get(method) : null);
    }

    public boolean matches(String method) {
        return name().equals(method);
    }

}