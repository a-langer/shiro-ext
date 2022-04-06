package com.github.alanger.shiroext.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class Utils {

    private Utils() {
    }

    public static boolean isXMLHttpRequest(ServletRequest request) {
        String xhrHeader = ((HttpServletRequest) request).getHeader("X-Requested-With");
        return (xhrHeader != null && xhrHeader.equals("XMLHttpRequest"));
    }

    public static String getRequestURI(HttpServletRequest request) {
        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        return path;
    }

    public static String normalizePath(String path) {
        if (path != null)
            path = path.replace("\\\\", "/");
        return path;
    }

    public static String normalizeDir(String path) {
        path = normalizePath(path);
        if (path != null && !path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    public static String getRealPath(ServletContext sc, String path) {
        if (isInternal(path)) {
            path = sc.getRealPath(path);
        }
        return normalizePath(path);
    }

    public static boolean isInternal(String path) {
        return (path == null || path.isEmpty()) || (path.startsWith("/WEB-INF/") || path.startsWith("/META-INF/"));
    }

}
