package com.github.alanger.shiroext.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.shiro.codec.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicAuthcFilterTest extends AbstractShiroFilters {

    protected String path = "/basic";
    protected String pathSilent = "/basicSilent";
    protected String authorization = "Basic " + Base64.encodeToString((admin + ":" + admin).getBytes());

    public BasicAuthcFilterTest() throws Throwable {
        super();
    }

    @Test
    public void test01_Unauthorized() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(path);
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertNotNull(response.getHeader("WWW-Authenticate"));
    }

    @Test
    public void test02_UnauthorizedSilent() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(pathSilent);
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(302, response.getStatus());
        assertNull(response.getHeader("WWW-Authenticate"));
        assertNotNull(response.getHeader("Set-Cookie"));
    }

    @Test
    public void test03_UnauthorizedXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(path);
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertNull(response.getHeader("WWW-Authenticate"));
    }

    @Test
    public void test04_UnauthorizedSilentXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(pathSilent);
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertNull(response.getHeader("Set-Cookie"));
    }

    @Test
    public void test05_Login() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(path);
        request.addHeader("Authorization", authorization);
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeader("Set-Cookie"));
    }

    @Test
    public void test06_LoginXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(path);
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Authorization", authorization);
        filter.doFilter(request, response, chain);
        log.trace("status: {}, location: {}, cookie: {}, content: {}", response.getStatus(),
                response.getHeader("Location"), response.getHeader("Set-Cookie"), response.getContentAsString());
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeader("Set-Cookie"));
    }

}
