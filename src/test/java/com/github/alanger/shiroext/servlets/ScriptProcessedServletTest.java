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

        context.addInitParameter(CTX_PREFIX + ENGINE_NAME, propEngineName);
        config.addInitParameter(ENGINE_NAME, propEngineName);

        context.addInitParameter(CTX_PREFIX + ENGINE_CLASS, propEngineClass);
        config.addInitParameter(ENGINE_CLASS, propEngineClass);

        config.addInitParameter(INIT_SCRIPT, System.getProperty(INIT_SCRIPT));
        config.addInitParameter(INVOKE_SCRIPT, System.getProperty(INVOKE_SCRIPT));
        config.addInitParameter(DESTROY_SCRIPT, System.getProperty(DESTROY_SCRIPT));
        config.addInitParameter(IS_SERVLET, System.getProperty(IS_SERVLET, "false"));
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
