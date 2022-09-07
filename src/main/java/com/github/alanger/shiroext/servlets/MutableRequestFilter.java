package com.github.alanger.shiroext.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class MutableRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing
    }

    protected ServletRequest getRequestWrapper(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            return new MutableRequestWrapper((HttpServletRequest) request);
        } else {
            return request;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(getRequestWrapper(request), response);
    }

    @Override
    public void destroy() {
        // nothing
    }
}
