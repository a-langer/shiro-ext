package com.github.alanger.shiroext.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class StaticFilter extends StaticServlet implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(new FilterServletConfig(filterConfig));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        super.service(request, response);
        if (!response.isCommitted())
            chain.doFilter(request, response);
    }

}
