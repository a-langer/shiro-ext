package com.github.alanger.shiroext.web;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StaticFilterTest extends AbstractShiroFilters {

    public StaticFilterTest() throws Throwable {
        super();
    }

    @Test
    public void test01_Content() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/resources/file.txt");
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertEquals("content", response.getContentAsString());
    }

    @Test
    public void test02_Directory() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/resources/");
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertEquals("text/html;charset=UTF-8", response.getContentType());
        assertEquals("UTF-8", response.getCharacterEncoding());
    }

    @Test
    public void test03_NotFound() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/resources/file_not_exist.txt");
        filter.doFilter(request, response, chain);
        assertEquals(404, response.getStatus());
        assertEquals("UTF-8", response.getCharacterEncoding());
    }

    @Test
    public void test04_ContentPath() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/not_exist/file_not_exist.txt");
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertEquals("content", response.getContentAsString());
    }

    @Test
    public void test05_DirectoryPath() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/not_exist/");
        filter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertEquals("content", response.getContentAsString());
    }
    
}
