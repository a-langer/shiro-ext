package com.github.alanger.shiroext.servlets;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptProcessedServletTest {

    static MockServletConfig config;
    static MockServletContext context;
    static ScriptProcessedServlet servlet;

    @BeforeClass
    public static void before() throws Throwable {
        config = new MockServletConfig();
        context = (MockServletContext) config.getServletContext();
        config.addInitParameter("init-script-text", System.getProperty("init-script-text"));
        config.addInitParameter("invoke-script-text", System.getProperty("invoke-script-text"));
        config.addInitParameter("destroy-script-text", System.getProperty("destroy-script-text"));
        config.addInitParameter("is-servlet", System.getProperty("is-servlet", "false"));
        servlet = new ScriptProcessedServlet();
    }

    @Test
    public void test01_initScript() throws Throwable {
        servlet.init(config);
        assertEquals("init-value", config.getInitParameter("init-parameter"));
    }

    @Test
    public void test02_invokeScript() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setRequestURI("/");
        servlet.service(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text1", response.getContentAsString());
    }

    @Test
    public void test03_destroyScript() throws Throwable {
        servlet.destroy();
        assertEquals("destroy-value", config.getInitParameter("destroy-parameter"));
    }
}
