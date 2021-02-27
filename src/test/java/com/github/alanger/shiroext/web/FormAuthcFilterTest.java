package com.github.alanger.shiroext.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FormAuthcFilterTest extends AbstractShiroFilters {

    public FormAuthcFilterTest() throws Throwable {
        super();
    }

    @Test
    public void test01_Unauthorized() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/loginSuccess");
        filter.doFilter(request, response, chain);
        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getHeader("Location"));
    }

    @Test
    public void test02_UnauthorizedXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/loginSuccess");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertNull(response.getHeader("Location"));
    }

    @Test
    public void test03_Login() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setPathInfo("/login");
        request.setParameter("j_username", admin);
        request.setParameter("j_password", admin);
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(302, response.getStatus());
        assertEquals("/loginSuccess", response.getHeader("Location"));
    }

    @Test
    public void test04_LoginXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setPathInfo("/login");
        request.setParameter("j_username", admin);
        request.setParameter("j_password", admin);
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(200, response.getStatus());
        assertNull(response.getHeader("Location"));
        assertNotNull(response.getHeader("Set-Cookie"));
    }

    @Test
    public void test05_LoginFailure() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setPathInfo("/login");
        request.setParameter("j_username", "baduser");
        request.setParameter("j_password", "baduser");
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, failure: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"),
                request.getAttribute("shiroLoginFailure"));
        assertEquals(401, response.getStatus());
        assertEquals(false, response.isCommitted());
        assertNull(response.getHeader("Location"));
        assertNotNull(request.getAttribute("shiroLoginFailure"));
    }

    @Test
    public void test06_LoginFailureXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setPathInfo("/login");
        request.setParameter("j_username", "baduser");
        request.setParameter("j_password", "baduser");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, failure: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"),
                request.getAttribute("shiroLoginFailure"));
        assertEquals(401, response.getStatus());
        assertEquals(true, response.isCommitted());
        assertNull(response.getHeader("Location"));
        assertNotNull(request.getAttribute("shiroLoginFailure"));
    }
}
