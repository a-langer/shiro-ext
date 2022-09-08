package com.github.alanger.shiroext.http;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import com.github.alanger.shiroext.util.Assert;

// org.springframework.mock.web.MockFilterConfig
public class MockFilterConfig implements FilterConfig {

    private final ServletContext servletContext;

    private final String filterName;

    private final Map<String, String> initParameters = new LinkedHashMap<>();

    /**
     * Create a new MockFilterConfig with a default {@link MockServletContext}.
     */
    public MockFilterConfig() {
        this(null, "");
    }

    /**
     * Create a new MockFilterConfig with a default {@link MockServletContext}.
     * 
     * @param filterName
     *            the name of the filter
     */
    public MockFilterConfig(String filterName) {
        this(null, filterName);
    }

    /**
     * Create a new MockFilterConfig.
     * 
     * @param servletContext
     *            the ServletContext that the servlet runs in
     */
    public MockFilterConfig(ServletContext servletContext) {
        this(servletContext, "");
    }

    /**
     * Create a new MockFilterConfig.
     * 
     * @param servletContext
     *            the ServletContext that the servlet runs in
     * @param filterName
     *            the name of the filter
     */
    public MockFilterConfig(ServletContext servletContext, String filterName) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.filterName = filterName;
    }

    @Override
    public String getFilterName() {
        return this.filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void addInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    public String getInitParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

}
