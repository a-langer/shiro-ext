package com.github.alanger.shiroext.servlets;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptProcessedServletTest extends ScriptProcessed {

    private MockServletConfig config;
    private ScriptProcessedServlet servlet;

    public ScriptProcessedServletTest() throws Throwable {
        config = new MockServletConfig();
        context = (MockServletContext) config.getServletContext();

        System.getProperties().forEach((key, value) -> {
            config.addInitParameter(key.toString(), value.toString());
            context.addInitParameter(key.toString(), value.toString());
        });

        servlet = new ScriptProcessedServlet();
        servlet.init(config);
    }

    @Test
    public void test01_initScript() throws Throwable {
        assertEquals("init-value", config.getInitParameter("init-parameter"));
    }

    @Test
    public void test02_initParameters() throws Throwable {
        assertEquals(propEngineName, getInitParameter(config, ENGINE_NAME, null));
        assertEquals(propEngineClass, getInitParameter(config, ENGINE_CLASS, null));
    }

    @Test
    public void test03_scriptEngine() throws Throwable {
        propEngineName = propEngineClass != null ? servlet.engine.getFactory().getEngineName() : propEngineName;
        assertEquals(propEngineName, servlet.getEngineName());
    }

    @Test
    public void test04_invokeScript() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setRequestURI("/");
        servlet.service(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text1", response.getContentAsString());
    }

    @Test
    public void test05_destroyScript() throws Throwable {
        servlet.destroy();
        assertEquals("destroy-value", config.getInitParameter("destroy-parameter"));
    }
}
