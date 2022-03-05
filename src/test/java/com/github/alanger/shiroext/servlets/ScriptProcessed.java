package com.github.alanger.shiroext.servlets;

import org.springframework.mock.web.MockServletContext;

public abstract class ScriptProcessed extends ScriptProcessedFilter {

    protected MockServletContext context;
    protected String propEngineName;
    protected String propEngineClass;

    public ScriptProcessed() {
        propEngineName = System.getProperty(ENGINE_NAME, System.getProperty(CTX_PREFIX + ENGINE_NAME));
        propEngineClass = System.getProperty(ENGINE_CLASS, System.getProperty(CTX_PREFIX + ENGINE_CLASS));
    }

}
