package com.github.alanger.shiroext.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.alanger.shiroext.util.Assert;
import com.github.alanger.shiroext.web.Utils;

public class MockAsyncContext implements AsyncContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final List<AsyncListener> listeners = new ArrayList<>();

    private String dispatchedPath;

    private long timeout = 10 * 1000L; // 10 seconds is Tomcat's default

    private final List<Runnable> dispatchHandlers = new ArrayList<>();

    public MockAsyncContext(ServletRequest request, ServletResponse response) {
        this.request = (HttpServletRequest) request;
        this.response = (HttpServletResponse) response;
    }

    public void addDispatchHandler(Runnable handler) {
        Assert.notNull(handler, "Dispatch handler must not be null");
        this.dispatchHandlers.add(handler);
    }

    @Override
    public ServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ServletResponse getResponse() {
        return this.response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return (this.request instanceof MockHttpServletRequest && this.response instanceof MockHttpServletResponse);
    }

    @Override
    public void dispatch() {
        dispatch(this.request.getRequestURI());
    }

    @Override
    public void dispatch(String path) {
        dispatch(null, path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        this.dispatchedPath = path;
        for (Runnable r : this.dispatchHandlers) {
            r.run();
        }
    }

    public String getDispatchedPath() {
        return this.dispatchedPath;
    }

    @Override
    public void complete() {
        MockHttpServletRequest mockRequest = Utils.getNativeRequest(request, MockHttpServletRequest.class);
        if (mockRequest != null) {
            mockRequest.setAsyncStarted(false);
        }
        for (AsyncListener listener : this.listeners) {
            try {
                listener.onComplete(new AsyncEvent(this, this.request, this.response));
            } catch (IOException ex) {
                throw new IllegalStateException("AsyncListener failure", ex);
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void addListener(AsyncListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        this.listeners.add(listener);
    }

    public List<AsyncListener> getListeners() {
        return this.listeners;
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

}
