package com.github.alanger.shiroext.web;

import static com.github.alanger.shiroext.web.Utils.isXMLHttpRequest;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutAuthcFilter extends LogoutFilter implements ISilent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean silent = false;

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

        Subject subject = getSubject(request, response);

        // Check if POST only logout is enabled and current request's method is a POST
        if (isPostOnlyLogout() && !WebUtils.toHttp(request).getMethod().toUpperCase(Locale.ENGLISH).equals("POST")) {
            return onLogoutRequestNotAPost(request, response);
        }

        // try/catch added for SHIRO-298:
        try {
            subject.logout();
        } catch (SessionException ise) {
            log.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
        }

        if (isSilent()) {
            WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return true;
        } else if (!isSilent() && isXMLHttpRequest(request)) {
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            String redirectUrl = getRedirectUrl(request, response, subject);
            issueRedirect(request, response, redirectUrl);
        }

        return false;
    }
}
