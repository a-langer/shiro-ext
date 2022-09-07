package com.github.alanger.shiroext.servlets;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class FilterServletConfig implements ServletConfig {

    private final FilterConfig filterConfig;

    public FilterServletConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public String getServletName() {
        return filterConfig.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return filterConfig.getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        return filterConfig.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return filterConfig.getInitParameterNames();
    }
}
