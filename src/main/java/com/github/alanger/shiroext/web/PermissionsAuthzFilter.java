package com.github.alanger.shiroext.web;

import static com.github.alanger.shiroext.web.Utils.isXMLHttpRequest;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionsAuthzFilter extends PermissionsAuthorizationFilter implements ISilent {

    private final Logger log = LoggerFactory.getLogger(PermissionsAuthzFilter.class);

    protected Logical logicalGlobal = Logical.AND;

    protected static final String PERMS = RequiresPermissions.class.getCanonicalName();

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
        String[] perms = (String[]) mappedValue;
        boolean isPermitted = isRequiresPermissions(logicalGlobal, perms);
        if (!isPermitted)
            request.setAttribute(PERMS, perms);
        if (log.isTraceEnabled())
            log.trace("isAccessAllowed isPermitted: {} , logical: {} , path: {} , values: {}", isPermitted,
                    logicalGlobal, WebUtils.toHttp(request).getPathInfo(), Arrays.toString(perms));
        return isPermitted;
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
                        getErrorMessage(logicalGlobal, request.getAttribute(PERMS)));
            } else {
                WebUtils.issueRedirect(request, response, unauthorizedUrl);
            }
        }

        if (log.isTraceEnabled())
            log.trace("onAccessDenied principal: {} , logical: {} , path: {}", subject.getPrincipal(), logicalGlobal,
                    WebUtils.toHttp(request).getPathInfo());

        return false;
    }

    public static boolean isRequiresPermissions(RequiresPermissions requiresPermissions) {
        if (requiresPermissions != null) {

            String[] perms = requiresPermissions.value(); // "right:itemRight, right:*"
            Logical logic = requiresPermissions.logical();

            return isRequiresPermissions(logic, perms);
        }
        return true;
    }

    public static boolean isRequiresPermissions(Logical logic, String[] perms) {
        Subject subject = SecurityUtils.getSubject();
        boolean access = false;

        if (Logical.OR.equals(logic)) {
            for (String permission : perms) {
                if (subject.isPermitted(permission)) {
                    access = true;
                    break;
                }
            }
        } else if (Logical.AND.equals(logic)) {
            access = subject.isPermittedAll(perms);
        }

        return access;
    }

    public static String getErrorMessage(RequiresPermissions requiresPermissions) {
        return getErrorMessage(requiresPermissions.logical(), requiresPermissions.value());
    }

    public static String getErrorMessage(Logical logic, Object perms) {
        return getErrorMessage(logic) + (perms != null ? Arrays.toString((String[]) perms) : "");
    }

    public static String getErrorMessage(Logical logic) {
        return "Forbidden - Subject " + SecurityUtils.getSubject().getPrincipal() + " does not have required "
                + (Logical.OR.equals(logic) ? "permission " : "permissions ");
    }
}
