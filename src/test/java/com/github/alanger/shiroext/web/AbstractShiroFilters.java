package com.github.alanger.shiroext.web;

import org.apache.shiro.web.servlet.IniShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;

@SuppressWarnings("deprecation")
public class AbstractShiroFilters {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected MockFilterConfig config;
    protected MockFilterChain chain;
    protected IniShiroFilter filter;

    protected String admin = "admin";
    protected String user = "user";

    public AbstractShiroFilters() throws Throwable {
        chain = new MockFilterChain();
        config = new MockFilterConfig();
        config.addInitParameter("config", System.getProperty("config"));
        filter = new IniShiroFilter();
        filter.init(config);
    }
}
