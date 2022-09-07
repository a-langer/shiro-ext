package com.github.alanger.shiroext.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogoutAuthcFilterTest extends AbstractShiroFilters {

    public LogoutAuthcFilterTest() throws Throwable {
        super();
    }

    @Test
    public void test01_Logout() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/logout");
        filter.doFilter(request, response, chain);
        assertEquals(302, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }

    @Test
    public void test02_LogoutXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/logout");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertNull(response.getHeader("Location"));
    }

}
