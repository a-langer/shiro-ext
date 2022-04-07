package com.github.alanger.shiroext.servlets;

import static com.github.alanger.shiroext.web.Utils.getRealPath;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    protected Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    protected static final String CTX_PREFIX = "shiroext-";
    protected static final String ENGINE_NAME = "engine-name";
    protected static final String ENGINE_CLASS = "engine-class";
    protected static final String TEXT_POSTFIX = "-text";
    protected static final String INIT_SCRIPT = "init-script";
    protected static final String INVOKE_SCRIPT = "invoke-script";
    protected static final String DESTROY_SCRIPT = "destroy-script";
    protected static final String CLASS_SCRIPT = "class-script";
    protected static final String LOGGER_NAME = "logger-name";

    protected String engineName = "nashorn";
    protected String initScript = null;
    protected String initScriptPath = null;
    protected String invokeScript = null;
    protected String invokeScriptPath = null;
    protected String destroyScript = null;
    protected String destroyScriptPath = null;
    protected String classScript = null;
    protected String classScriptPath = null;

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

    public String getClassScript() {
        return classScript;
    }

    public void setClassScript(String classScript) {
        this.classScript = classScript;
    }

    public String getInitScriptPath() {
        return initScriptPath;
    }

    public void setInitScriptPath(String initScriptPath) {
        this.initScriptPath = initScriptPath;
    }

    public String getInvokeScriptPath() {
        return invokeScriptPath;
    }

    public void setInvokeScriptPath(String invokeScriptPath) {
        this.invokeScriptPath = invokeScriptPath;
    }

    public String getDestroyScriptPath() {
        return destroyScriptPath;
    }

    public void setDestroyScriptPath(String destroyScriptPath) {
        this.destroyScriptPath = destroyScriptPath;
    }

    public String getClassScriptPath() {
        return classScriptPath;
    }

    public void setClassScriptPath(String classScriptPath) {
        this.classScriptPath = classScriptPath;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    protected static String getInitParameter(ServletConfig config, String key, String defValue) {
        String value = config.getInitParameter(key) != null ? config.getInitParameter(key)
                : config.getServletContext().getInitParameter(CTX_PREFIX + key);
        return value != null ? value : defValue;
    }

    protected String getScript(String key, String defValue) throws ServletException {
        String value = getInitParameter(key) != null ? getInitParameter(key) : defValue;
        if (value != null) {
            String path = getRealPath(getServletContext(), value);
            try {
                value = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
                if (CLASS_SCRIPT.equals(key)) {
                    engine.getContext().setAttribute("__FILE__", path, ScriptContext.ENGINE_SCOPE);
                    engine.getContext().setAttribute("__DIR__", path.substring(0, path.lastIndexOf("/")), ScriptContext.ENGINE_SCOPE);
                    engine.getContext().setAttribute(ScriptEngine.FILENAME, path, ScriptContext.ENGINE_SCOPE);
                    classScriptPath = path;
                } else if (INIT_SCRIPT.equals(key)) {
                    initScriptPath = path;
                } else if (INVOKE_SCRIPT.equals(key)) {
                    invokeScriptPath = path;
                } else if (DESTROY_SCRIPT.equals(key)) {
                    destroyScriptPath = path;
                }
            } catch (IOException e) {
                throw new ServletException(e);
            }
        } else {
            value = getInitParameter(key + TEXT_POSTFIX);
        }
        return value;
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
        logger = getInitParameter(LOGGER_NAME) != null ? Logger.getLogger(getInitParameter(LOGGER_NAME)) : logger;
        engine.getContext().setAttribute("logger", logger, ScriptContext.ENGINE_SCOPE);

        initScript = getScript(INIT_SCRIPT, initScriptPath);
        invokeScript = getScript(INVOKE_SCRIPT, invokeScriptPath);
        destroyScript = getScript(DESTROY_SCRIPT, destroyScriptPath);
        classScript = getScript(CLASS_SCRIPT, classScriptPath);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        initPatameter(config);

        try {
            if (classScript != null) {
                engine.eval(classScript);
                invocable = (Invocable) engine;
                invocable.invokeFunction("init", getServletConfig());
            } else if (initScript != null) {
                engine.getContext().setAttribute(ScriptEngine.FILENAME, initScriptPath, ScriptContext.ENGINE_SCOPE);
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
            if (classScript != null) {
                invocable.invokeFunction("service", request, response);
            } else if (invokeScript != null) {
                engine.getContext().setAttribute(ScriptEngine.FILENAME, invokeScriptPath, ScriptContext.ENGINE_SCOPE);
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
            if (classScript != null && invocable != null) {
                invocable.invokeFunction("destroy");
            } else if (destroyScript != null) {
                engine.getContext().setAttribute(ScriptEngine.FILENAME, destroyScriptPath, ScriptContext.ENGINE_SCOPE);
                engine.eval(destroyScript);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Script error during servlet destroyetion", e);
        }
    }

}
