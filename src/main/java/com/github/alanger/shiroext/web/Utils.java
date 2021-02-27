package com.github.alanger.shiroext.web;

import javax.servlet.ServletRequest;

import org.apache.shiro.web.util.WebUtils;

public class Utils {

    private Utils() {
    }

    public static boolean isXMLHttpRequest(ServletRequest request) {
        String xhrHeader = WebUtils.toHttp(request).getHeader("X-Requested-With");
        return (xhrHeader != null && xhrHeader.equals("XMLHttpRequest"));
    }

}
