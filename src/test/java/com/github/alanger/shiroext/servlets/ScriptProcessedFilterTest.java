package com.github.alanger.shiroext.servlets;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptProcessedFilterTest extends ScriptProcessed {

    private MockFilterConfig config;
    private ScriptProcessedFilter filter;

    public ScriptProcessedFilterTest() throws Throwable {
        config = new MockFilterConfig();
        context = (MockServletContext) config.getServletContext();

        context.addInitParameter(CTX_PREFIX + ENGINE_NAME, propEngineName);
        config.addInitParameter(ENGINE_NAME, propEngineName);

        context.addInitParameter(CTX_PREFIX + ENGINE_CLASS, propEngineClass);
        config.addInitParameter(ENGINE_CLASS, propEngineClass);

        config.addInitParameter(INIT_SCRIPT, System.getProperty(INIT_SCRIPT));
        config.addInitParameter(INVOKE_SCRIPT, System.getProperty(INVOKE_SCRIPT));
        config.addInitParameter(DESTROY_SCRIPT, System.getProperty(DESTROY_SCRIPT));
        config.addInitParameter(CLASS_SCRIPT, System.getProperty(CLASS_SCRIPT));
        filter = new ScriptProcessedFilter();
        filter.init(config);
    }

    @Test
    public void test01_initScript() throws Throwable {
        assertEquals("init-value", config.getInitParameter("init-parameter"));
    }

    @Test
    public void test02_initParameters() throws Throwable {
        assertEquals(propEngineName, getInitParameter(new FilterServletConfig(config), ENGINE_NAME, null));
        assertEquals(propEngineClass, getInitParameter(new FilterServletConfig(config), ENGINE_CLASS, null));
    }

    @Test
    public void test03_scriptEngine() throws Throwable {
        propEngineName = propEngineClass != null ? filter.engine.getFactory().getEngineName() : propEngineName;
        assertEquals(propEngineName, filter.getEngineName());
    }

    @Test
    public void test04_invokeScript() throws Throwable {
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
    public void test05_destroyScript() throws Throwable {
        filter.destroy();
        assertEquals("destroy-value", config.getInitParameter("destroy-parameter"));
    }
}
