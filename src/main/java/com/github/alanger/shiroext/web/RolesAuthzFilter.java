package com.github.alanger.shiroext.web;

import static com.github.alanger.shiroext.web.Utils.isXMLHttpRequest;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolesAuthzFilter extends RolesAuthorizationFilter implements ISilent {

    private final Logger log = LoggerFactory.getLogger(RolesAuthzFilter.class);

    protected Logical logicalGlobal = Logical.AND;

    protected static final String ROLES = RequiresRoles.class.getCanonicalName();

    private boolean silent = false;

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws IOException {
        String[] rolesArray = (String[]) mappedValue;
        if (rolesArray == null || rolesArray.length == 0) {
            // no roles specified, so nothing to check - allow access.
            return true;
        }
        boolean allow = isRequiresRoles(logicalGlobal, rolesArray);
        if (!allow)
            request.setAttribute(ROLES, rolesArray);
        if (log.isTraceEnabled())
            log.trace("isAccessAllowed allow: {} , logical: {} , path: {} , values: {}", allow, logicalGlobal,
                    WebUtils.toHttp(request).getPathInfo(), Arrays.toString(rolesArray));
        return allow;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {

        Subject subject = getSubject(request, response);
        boolean isXMLHttpRequest = isXMLHttpRequest(request);

        if (subject.getPrincipal() == null) {
            if (isSilent()) {
                WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else if (!isSilent() && isXMLHttpRequest) {
                WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                saveRequestAndRedirectToLogin(request, response);
            }
        } else {
            String unauthorizedUrl = getUnauthorizedUrl();
            if (isSilent()) {
                WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else if (!isSilent() && (isXMLHttpRequest || !StringUtils.hasText(unauthorizedUrl))) {
                WebUtils.toHttp(response).sendError(HttpServletResponse.SC_FORBIDDEN,
                        getErrorMessage(logicalGlobal, request.getAttribute(ROLES)));
            } else {
                WebUtils.issueRedirect(request, response, unauthorizedUrl);
            }
        }

        if (log.isTraceEnabled())
            log.trace("onAccessDenied principal: {} , logical: {} , path: {}", subject.getPrincipal(), logicalGlobal,
                    WebUtils.toHttp(request).getPathInfo());

        return false;
    }

    public static boolean isRequiresRoles(RequiresRoles requiresRoles) {
        if (requiresRoles != null) {
            String[] roles = requiresRoles.value(); // "admin, manager"
            Logical logic = requiresRoles.logical();

            return isRequiresRoles(logic, roles);
        }
        return true;
    }

    public static boolean isRequiresRoles(Logical logic, String[] roles) {

        Subject subject = SecurityUtils.getSubject();

        boolean access = false;

        if (Logical.OR.equals(logic)) {
            for (String role : roles) {
                if (subject.hasRole(role)) {
                    access = true;
                    break;
                }
            }
        } else if (Logical.AND.equals(logic)) {
            access = subject.hasAllRoles(CollectionUtils.asSet(roles));
        }

        return access;
    }

    public static String getErrorMessage(RequiresRoles requiresRoles) {
        return getErrorMessage(requiresRoles.logical(), requiresRoles.value());
    }

    public static String getErrorMessage(Logical logic, Object roles) {
        return getErrorMessage(logic) + (roles != null ? Arrays.toString((String[]) roles) : "");
    }

    public static String getErrorMessage(Logical logic) {
        return "Forbidden - Subject " + SecurityUtils.getSubject().getPrincipal() + " does not have required "
                + (Logical.OR.equals(logic) ? "role " : "roles ");
    }

}
