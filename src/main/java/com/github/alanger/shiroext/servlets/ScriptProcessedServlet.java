package com.github.alanger.shiroext.servlets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ScriptProcessedServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    protected static final String CTX_PREFIX = "shiroext-";
    protected static final String ENGINE_NAME = "engine-name";
    protected static final String ENGINE_CLASS = "engine-class";
    protected static final String INIT_SCRIPT = "init-script-text";
    protected static final String INVOKE_SCRIPT = "invoke-script-text";
    protected static final String DESTROY_SCRIPT = "destroy-script-text";
    protected static final String IS_SERVLET = "is-servlet";

    protected String engineName = "nashorn";
    protected String initScript = null;
    protected String invokeScript = null;
    protected String destroyScript = null;
    protected boolean isServlet = false;
    protected boolean initialized = false;

    protected Invocable invocable;
    protected ScriptEngine engine;

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    public String getInvokeScript() {
        return invokeScript;
    }

    public void setInvokeScript(String invokeScript) {
        this.invokeScript = invokeScript;
    }

    public String getDestroyScript() {
        return destroyScript;
    }

    public void setDestroyScript(String destroyScript) {
        this.destroyScript = destroyScript;
    }

    public boolean isServlet() {
        return isServlet;
    }

    public void setServlet(boolean isServlet) {
        this.isServlet = isServlet;
    }

    protected static String getInitParameter(ServletConfig config, String name, String defValue) {
        String value = config.getInitParameter(name) != null ? config.getInitParameter(name)
                : config.getServletContext().getInitParameter(CTX_PREFIX + name);
        return value != null ? value : defValue;
    }

    protected void initPatameter(ServletConfig config) throws ServletException {
        super.init(config);

        String engineClass = getInitParameter(config, ENGINE_CLASS, null);
        if (engineClass != null) {
            try {
                Class<?> clazz = Class.forName(engineClass);
                engine = (ScriptEngine) clazz.getDeclaredConstructor().newInstance();
                engineName = engine.getFactory().getEngineName();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new ServletException(e);
            }
        } else {
            engineName = getInitParameter(config, ENGINE_NAME, engineName);
            ScriptEngineManager manager = new ScriptEngineManager(getClass().getClassLoader());
            engine = manager.getEngineByName(engineName);
            if (engine == null)
                throw new ServletException("Script engine '" + engineName + "' is null");
        }

        engine.getContext().setAttribute("servletConfig", config, ScriptContext.ENGINE_SCOPE);
        engine.getContext().setAttribute("servletContext", config.getServletContext(), ScriptContext.ENGINE_SCOPE);
        engine.getContext().setAttribute("logger", logger, ScriptContext.ENGINE_SCOPE);

        initScript = getInitParameter(INIT_SCRIPT) != null ? getInitParameter(INIT_SCRIPT) : initScript;
        invokeScript = getInitParameter(INVOKE_SCRIPT) != null ? getInitParameter(INVOKE_SCRIPT) : invokeScript;
        destroyScript = getInitParameter(DESTROY_SCRIPT) != null ? getInitParameter(DESTROY_SCRIPT) : destroyScript;
        isServlet = getInitParameter(IS_SERVLET) != null ? Boolean.valueOf(getInitParameter(IS_SERVLET)) : isServlet;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        initPatameter(config);

        try {
            if (isServlet) {
                engine.eval(invokeScript);
                invocable = (Invocable) engine;
                invocable.invokeFunction("init", getServletConfig());
            } else if (initScript != null) {
                engine.eval(initScript);
            }
            initialized = true;
        } catch (NoSuchMethodException | ScriptException e) {
            logger.log(Level.SEVERE, "Script error during servlet initialization", e);
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (isServlet) {
                invocable.invokeFunction("service", request, response);
            } else if (invokeScript != null) {
                engine.getContext().setAttribute("request", request, ScriptContext.ENGINE_SCOPE);
                engine.getContext().setAttribute("response", response, ScriptContext.ENGINE_SCOPE);
                engine.eval(invokeScript);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Script error during servlet invocation", e);
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            if (isServlet && invocable != null) {
                invocable.invokeFunction("destroy");
            } else if (destroyScript != null) {
                engine.eval(destroyScript);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Script error during servlet destroyetion", e);
        }
    }

}
