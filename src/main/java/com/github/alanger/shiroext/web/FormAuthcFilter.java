package com.github.alanger.shiroext.web;

import static com.github.alanger.shiroext.web.Utils.isXMLHttpRequest;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormAuthcFilter extends FormAuthenticationFilter implements ISilent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean silent = false;

    private boolean base64 = false;

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(boolean base64) {
        this.base64 = base64;
    }

    @Override
    protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
        request.setAttribute(getFailureKeyAttribute(),
                ae.getCause() != null ? ae.getCause().getMessage() : ae.getMessage());
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
            ServletResponse response) throws Exception {
        if (isSilent()) {
            WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_OK);
            return true;
        } else if (!isSilent() && isXMLHttpRequest(request)) {
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_OK);
        } else {
            issueSuccessRedirect(request, response);
        }
        // we handled the success redirect directly, prevent the chain from continuing:
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request,
            ServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Authentication exception", e);
        }

        setFailureAttribute(request, e);

        if (!isSilent() && isXMLHttpRequest(request)) {
            try {
                WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            } catch (IOException e1) {
                WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            // we handled the success redirect directly, prevent the chain from continuing:
            return false;
        }

        // login failed, let request continue back to the login page:
        WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                if (log.isTraceEnabled()) {
                    log.trace("Login submission detected.  Attempting to execute login.");
                }
                return executeLogin(request, response);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Login page view.");
                }
                // allow them to see the login page ;)
                return true;
            }
        } else {
            if (log.isTraceEnabled())
                log.trace("Attempting to access a path which requires authentication.  Forwarding to the "
                        + "Authentication url [{}]", getLoginUrl());

            if (isSilent()) {
                WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return true;
            } else if (!isSilent() && isXMLHttpRequest(request)) {
                WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String username = getUsername(request);
        String password = getPassword(request);
        if (isBase64()) {
            username = Base64.decodeToString(username);
            password = Base64.decodeToString(password);
        }
        return createToken(username, password, request, response);
    }
}