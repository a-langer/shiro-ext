package com.github.alanger.shiroext.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StaticFilterTest extends StaticServletTest {

    protected MockFilterConfig config;
    protected MockFilterChain chain;

    public StaticFilterTest() throws Throwable {
        super();
    }

    @Override
    protected void initService() throws Throwable {
        config = new MockFilterConfig();
        config.addInitParameter(DIR_KEY, "src/test");
        config.addInitParameter(SHOW_DIR_KEY, "true");
        chain = new MockFilterChain();
        init(config);
    }
    
    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        doFilter(request, response, chain);
    }
}
