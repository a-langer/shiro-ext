package com.github.alanger.shiroext.servlets;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class MultiReadRequestFilter extends MutableRequestFilter {

    @Override
    protected ServletRequest getRequestWrapper(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            return new MultiReadRequestWrapper((HttpServletRequest) request);
        } else {
            return request;
        }
    }
}
