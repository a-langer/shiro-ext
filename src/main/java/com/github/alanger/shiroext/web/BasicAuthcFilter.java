package com.github.alanger.shiroext.web;

import static com.github.alanger.shiroext.web.Utils.isXMLHttpRequest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthcFilter extends BasicHttpAuthenticationFilter implements ISilent {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean silent = false;

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        boolean loggedIn = false; // false by default or we wouldn't be in this method
        if (isLoginAttempt(request, response)) {
            loggedIn = executeLogin(request, response);
        }
        if (!loggedIn) {
            if (isSilent()) {
                WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return true;
            } else if (!isSilent() && isXMLHttpRequest(request)) {
                WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                sendChallenge(request, response);
            }
        }

        if (log.isTraceEnabled())
            log.trace("onAccessDenied loggedIn: {} , path: {}", loggedIn, WebUtils.toHttp(request).getPathInfo());

        return loggedIn;
    }

    public static boolean isRequiresAuthentication(RequiresAuthentication requiresAuthentication) {
        if (requiresAuthentication != null) {
            return SecurityUtils.getSubject().isAuthenticated();
        }
        return true;
    }

}