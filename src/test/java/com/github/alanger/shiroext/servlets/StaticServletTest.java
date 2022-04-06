package com.github.alanger.shiroext.servlets;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StaticServletTest extends StaticFilter {

    protected MockServletConfig config;

    public StaticServletTest() throws Throwable {
        initService();
    }

    protected void initService() throws Throwable {
        config = new MockServletConfig();
        config.addInitParameter(DIR_KEY, "src/test");
        config.addInitParameter(SHOW_DIR_KEY, "true");
        init(config);
    }

    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        service(request, response);
    }
    
    @Test
    public void test01_Content() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setPathInfo("/resources/file.txt");
        doService(request, response);
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
        doService(request, response);
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
        doService(request, response);
        assertEquals(404, response.getStatus());
        assertEquals("UTF-8", response.getCharacterEncoding());
    }

    @Test
    public void test04_ContentPath() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        setPathInfo("/resources/file.txt");
        request.setMethod("GET");
        request.setPathInfo("/not_exist/file_not_exist.txt");
        doService(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertEquals("content", response.getContentAsString());
    }

    @Test
    public void test05_DirectoryPath() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        setPathInfo("/resources/file.txt");
        request.setMethod("GET");
        request.setPathInfo("/not_exist/");
        doService(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertEquals("content", response.getContentAsString());
    }

    @Test
    public void test06_RootDirIsFile() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        setRootDir("src/test/resources/file.txt");
        request.setMethod("GET");
        request.setPathInfo("/");
        doService(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertEquals("content", response.getContentAsString());
    }

    @Test
    public void test07_RootDirIsFileWithPath() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        setRootDir("src/test/resources/file.txt");
        request.setMethod("GET");
        request.setPathInfo("/not_exist/");
        doService(request, response);
        assertEquals(404, response.getStatus());
    }
}
