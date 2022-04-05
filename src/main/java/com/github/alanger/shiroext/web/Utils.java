package com.github.alanger.shiroext.web;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class Utils {

    private Utils() {
    }

    public static boolean isXMLHttpRequest(ServletRequest request) {
        String xhrHeader = ((HttpServletRequest) request).getHeader("X-Requested-With");
        return (xhrHeader != null && xhrHeader.equals("XMLHttpRequest"));
    }

    public static String normalizePath(String path) {
        if (path != null) {
            path = path.replace("\\\\", "/");
            if (!path.endsWith("/"))
                path = path + "/";
        }
        return path;
    }

}
