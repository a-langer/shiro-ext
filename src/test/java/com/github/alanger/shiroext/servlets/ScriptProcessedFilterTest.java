package com.github.alanger.shiroext.servlets;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptProcessedFilterTest {

    static MockFilterConfig config;
    static MockServletContext context;
    static ScriptProcessedFilter filter;

    @BeforeClass
    public static void before() throws Throwable {
        config = new MockFilterConfig();
        context = (MockServletContext) config.getServletContext();
        context.addInitParameter("shiroext-engine-name", System.getProperty("shiroext-engine-name"));
        config.addInitParameter("engine-name", System.getProperty("engine-name"));
        config.addInitParameter("init-script-text", System.getProperty("init-script-text"));
        config.addInitParameter("invoke-script-text", System.getProperty("invoke-script-text"));
        config.addInitParameter("destroy-script-text", System.getProperty("destroy-script-text"));
        config.addInitParameter("is-filter", System.getProperty("is-filter", "false"));
        filter = new ScriptProcessedFilter();
    }

    @Test
    public void test01_initScript() throws Throwable {
        filter.init(config);
        assertEquals("init-value", config.getInitParameter("init-parameter"));
    }

    @Test
    public void test02_scriptEngine() throws Throwable {
        assertEquals(System.getProperty("engine-name", "nashorn"), filter.getEngineName());
    }

    @Test
    public void test03_invokeScript() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        request.setMethod("GET");
        request.setRequestURI("/");
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertEquals("text1", response.getContentAsString());
    }

    @Test
    public void test04_destroyScript() throws Throwable {
        filter.destroy();
        assertEquals("destroy-value", config.getInitParameter("destroy-parameter"));
    }
}
