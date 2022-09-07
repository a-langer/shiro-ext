package com.github.alanger.shiroext.servlets;

import java.io.IOException;
import java.util.logging.Level;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ScriptProcessedFilter extends ScriptProcessedServlet implements Filter {

    private static final long serialVersionUID = 1L;

    protected static final String FORCED_CHAIN = "forced-chain";

    protected boolean forcedChain = false;

    public boolean isForcedChain() {
        return forcedChain;
    }

    public void setForcedChain(boolean forcedChain) {
        this.forcedChain = forcedChain;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.initPatameter(new FilterServletConfig(filterConfig));

        engine.getContext().setAttribute("filterConfig", filterConfig, ScriptContext.ENGINE_SCOPE);
        forcedChain = getInitParameter(FORCED_CHAIN) != null ? Boolean.valueOf(getInitParameter(FORCED_CHAIN))
                : forcedChain;

        try {
            if (classScript != null) {
                engine.eval(classScript);
                invocable = (Invocable) engine;
                invocable.invokeFunction("init", filterConfig);
            } else if (initScript != null) {
                engine.getContext().setAttribute(ScriptEngine.FILENAME, initScriptPath, ScriptContext.ENGINE_SCOPE);
                engine.eval(initScript);
            }
            initialized = true;
        } catch (NoSuchMethodException | ScriptException e) {
            logger.log(Level.SEVERE, "Script error during filter initialization", e);
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (classScript != null) {
                invocable.invokeFunction("doFilter", request, response, chain);
            } else if (invokeScript != null) {
                engine.getContext().setAttribute(ScriptEngine.FILENAME, invokeScriptPath, ScriptContext.ENGINE_SCOPE);
                engine.getContext().setAttribute("request", request, ScriptContext.ENGINE_SCOPE);
                engine.getContext().setAttribute("response", response, ScriptContext.ENGINE_SCOPE);
                engine.getContext().setAttribute("chain", chain, ScriptContext.ENGINE_SCOPE);
                engine.eval(invokeScript);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Script error during filter invocation", e);
            throw new ServletException(e);
        } finally {
            if (!response.isCommitted() || isForcedChain())
                chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        try {
            if (classScript != null && invocable != null) {
                invocable.invokeFunction("destroy");
            } else if (destroyScript != null) {
                engine.getContext().setAttribute(ScriptEngine.FILENAME, destroyScriptPath, ScriptContext.ENGINE_SCOPE);
                engine.eval(destroyScript);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Script error during filter destroyetion", e);
        }
    }

}
