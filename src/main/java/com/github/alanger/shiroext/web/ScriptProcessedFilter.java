package com.github.alanger.shiroext.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ScriptProcessedFilter extends ScriptProcessedServlet implements Filter {

    private static final long serialVersionUID = 1L;

    protected static final String IS_FILTER = "is-filter";
    protected static final String FORCED_CHAIN = "forced-chain";

    protected boolean isFilter = false;
    protected boolean forcedChain = false;
    protected FilterConfig filterConfig;

    public boolean isFilter() {
        return isFilter;
    }

    public void setFilter(boolean isFilter) {
        this.isFilter = isFilter;
    }

    public boolean isForcedChain() {
        return forcedChain;
    }

    public void setForcedChain(boolean forcedChain) {
        this.forcedChain = forcedChain;
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
        filterConfig = fc;
        super.init(new FilterServletConfig());
        engine.getContext().setAttribute("filterConfig", filterConfig, ScriptContext.ENGINE_SCOPE);
        forcedChain = fc.getInitParameter(FORCED_CHAIN) != null ? Boolean.valueOf(fc.getInitParameter(FORCED_CHAIN))
                : forcedChain;
        isFilter = fc.getInitParameter(IS_FILTER) != null ? Boolean.valueOf(fc.getInitParameter(IS_FILTER)) : isFilter;

        try {
            if (isFilter) {
                if (invocable == null) {
                    engine.eval(invokeScript);
                    invocable = (Invocable) engine;
                }
                invocable.invokeFunction("init", filterConfig);
            } else if (initScript != null && isServlet) {
                engine.eval(initScript);
            }
        } catch (NoSuchMethodException | ScriptException e) {
            logger.log(Level.SEVERE, "Script error during filter initialization", e);
            throw new ServletException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            if (isFilter) {
                invocable.invokeFunction("doFilter", request, response, chain);
            } else {
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
            if (isFilter && invocable != null) {
                invocable.invokeFunction("destroy");
            } else if (destroyScript != null) {
                engine.eval(destroyScript);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Script error during filter destroyetion", e);
        }
    }

    class FilterServletConfig implements ServletConfig {
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
}
