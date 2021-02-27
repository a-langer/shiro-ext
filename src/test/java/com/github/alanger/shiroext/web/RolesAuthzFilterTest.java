package com.github.alanger.shiroext.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.shiro.codec.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RolesAuthzFilterTest extends AbstractShiroFilters {

    protected String urlUnauthorized = "/rolesOnly";
    protected String urlForbidden = "/roles";
    protected String urlAllowed = "/role";

    public RolesAuthzFilterTest() throws Throwable {
        super();
    }

    @Test
    public void test01_Unauthorized() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(urlUnauthorized);
        filter.doFilter(request, response, chain);
        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getHeader("Location"));
    }

    @Test
    public void test02_UnauthorizedXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(urlUnauthorized);
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertNull(response.getHeader("Location"));
    }

    @Test
    public void test03_Forbidden() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(urlForbidden);
        request.addHeader("Authorization", "Basic " + Base64.encodeToString((user + ":" + user).getBytes()));
        filter.doFilter(request, response, chain);
        assertEquals(302, response.getStatus());
        assertEquals("/loginError", response.getHeader("Location"));
    }

    @Test
    public void test04_ForbiddenXHR() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(urlForbidden);
        request.addHeader("Authorization", "Basic " + Base64.encodeToString((user + ":" + user).getBytes()));
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        filter.doFilter(request, response, chain);
        assertEquals(403, response.getStatus());
        assertNull(response.getHeader("Location"));
    }

    @Test
    public void test05_Allowed() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo(urlAllowed);
        request.addHeader("Authorization", "Basic " + Base64.encodeToString((user + ":" + user).getBytes()));
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertNull(response.getHeader("Location"));
    }

}
