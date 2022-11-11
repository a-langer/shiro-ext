package com.github.alanger.shiroext.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class MutableResponseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing
    }

    protected ServletResponse getResponseWrapper(ServletResponse response) {
        if (response instanceof HttpServletResponse) {
            return new MutableResponseWrapper((HttpServletResponse) response);
        } else {
            return response;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, getResponseWrapper(response));
    }

    @Override
    public void destroy() {
        // nothing
    }
}
