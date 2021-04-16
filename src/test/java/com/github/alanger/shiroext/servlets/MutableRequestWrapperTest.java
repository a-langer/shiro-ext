package com.github.alanger.shiroext.servlets;

import static java.util.Collections.list;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Locale;

import com.github.alanger.shiroext.realm.User;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockPart;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MutableRequestWrapperTest {

    @Test
    public void test01_EncodingAndContentType() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContentType("text/html");

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getCharacterEncoding(), wrapper.getCharacterEncoding());
        assertEquals(request.getContentType(), wrapper.getContentType());

        wrapper.setCharacterEncoding("windows-1251");
        assertEquals("windows-1251", wrapper.getCharacterEncoding());
        assertEquals("text/html;charset=windows-1251", wrapper.getHeader(MutableRequestWrapper.CONTENT_TYPE));

        wrapper.setContentType("application/xml");
        assertEquals("application/xml;charset=windows-1251", wrapper.getHeader(MutableRequestWrapper.CONTENT_TYPE));
    }

    @Test
    public void test02_InputStream() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent("hello".getBytes());

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getContentLength(), wrapper.getContentLength());
        assertEquals(request.getInputStream(), wrapper.getInputStream());
    }

    @Test
    public void test03_Reader() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent("hello".getBytes());

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getContentLength(), wrapper.getContentLength());
        assertEquals(request.getReader(), wrapper.getReader());
    }

    @Test
    public void test04_Content() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.addHeader(MutableRequestWrapper.CONTENT_LENGTH, "5");
        request.setContent("hello".getBytes());

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(null, wrapper.getContentAsString());
        assertNotEquals(request.getContentAsString(), wrapper.getContentAsString());

        wrapper.setContent("world".getBytes());
        assertEquals("world", wrapper.getContentAsString());
        assertEquals(5, wrapper.getContentLength());
        assertEquals("5", wrapper.getHeader(MutableRequestWrapper.CONTENT_LENGTH));

        wrapper.setContent("world2".getBytes());
        assertEquals("world2", wrapper.getContentAsString());
        assertEquals(6, wrapper.getContentLength());
        assertEquals("6", wrapper.getHeader(MutableRequestWrapper.CONTENT_LENGTH));
    }

    @Test
    public void test05_Parameter() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("param1", "value1");
        request.setParameter("param2", "value1");

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getParameter("param1"), wrapper.getParameter("param1"));
        assertEquals(request.getParameter("param2"), wrapper.getParameter("param2"));
        assertEquals(request.getParameterMap(), wrapper.getParameterMap());
        assertArrayEquals(list(request.getParameterNames()).toArray(), list(wrapper.getParameterNames()).toArray());
        assertArrayEquals(request.getParameterValues("param1"), wrapper.getParameterValues("param1"));

        wrapper.addParameter("param1", "new_value1");
        assertArrayEquals(new String[] { "new_value1" }, wrapper.getParameterValues("param1"));

        wrapper.addParameter("param1", "new_value2");
        assertEquals("new_value1", wrapper.getParameter("param1"));
        assertArrayEquals(new String[] { "new_value1", "new_value2" }, wrapper.getParameterValues("param1"));
        assertArrayEquals(new String[] { "param1", "param2" }, list(wrapper.getParameterNames()).toArray());

        wrapper.setParameter("param1", "set_value1");
        assertEquals("set_value1", wrapper.getParameter("param1"));
        assertArrayEquals(new String[] { "set_value1" }, wrapper.getParameterValues("param1"));

        wrapper.setParameter("param2", "set_" + wrapper.getParameter("param2"));
        assertEquals("set_value1", wrapper.getParameter("param2"));

        wrapper.addParameter("param3", "new_value1");
        assertEquals("new_value1", wrapper.getParameter("param3"));
        assertArrayEquals(new String[] { "param1", "param2", "param3" }, list(wrapper.getParameterNames()).toArray());

        wrapper.removeParameter("param3");
        assertEquals(null, wrapper.getParameter("param3"));
    }

    @Test
    public void test06_ServletRequestSimple() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.0.100");
        request.setRemoteHost("host1");
        request.setSecure(false);
        request.setRemotePort(81);
        request.setLocalName("localName1");
        request.setLocalAddr("addr1");
        request.setLocalPort(82);
        request.setAuthType("BASIC");
        request.setMethod("GET");
        request.setRemoteUser("remoteUser1");
        request.setRequestedSessionId("requestedSessionId1");

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getRemoteAddr(), wrapper.getRemoteAddr());
        assertEquals(request.getRemoteHost(), wrapper.getRemoteHost());
        assertEquals(request.isSecure(), wrapper.isSecure());
        assertEquals(request.getRemotePort(), wrapper.getRemotePort());
        assertEquals(request.getLocalName(), wrapper.getLocalName());
        assertEquals(request.getLocalAddr(), wrapper.getLocalAddr());
        assertEquals(request.getLocalPort(), wrapper.getLocalPort());
        assertEquals(request.getAuthType(), wrapper.getAuthType());
        assertEquals(request.getMethod(), wrapper.getMethod());
        assertEquals(request.getRemoteUser(), wrapper.getRemoteUser());
        assertEquals(request.getRequestedSessionId(), wrapper.getRequestedSessionId());

        wrapper.setRemoteAddr("192.168.0.101");
        wrapper.setRemoteHost("host2");
        wrapper.setSecure(true);
        wrapper.setRemotePort(444);
        wrapper.setLocalName("localName2");
        wrapper.setLocalAddr("addr2");
        wrapper.setLocalPort(445);
        wrapper.setAuthType("FORM");
        wrapper.setMethod("POST");
        wrapper.setRemoteUser("remoteUser2");
        wrapper.setRequestedSessionId("requestedSessionId2");
        assertEquals("192.168.0.101", wrapper.getRemoteAddr());
        assertEquals("host2", wrapper.getRemoteHost());
        assertEquals(true, wrapper.isSecure());
        assertEquals(444, wrapper.getRemotePort());
        assertEquals("localName2", wrapper.getLocalName());
        assertEquals("addr2", wrapper.getLocalAddr());
        assertEquals(445, wrapper.getLocalPort());
        assertEquals("FORM", wrapper.getAuthType());
        assertEquals("POST", wrapper.getMethod());
        assertEquals("remoteUser2", wrapper.getRemoteUser());
        assertEquals("requestedSessionId2", wrapper.getRequestedSessionId());
    }

    @Test
    public void test07_URL() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setProtocol("http");
        request.setScheme("http");
        request.setServerName("server1");
        request.setServerPort(80);
        request.setPathInfo("pathInfo1");
        request.setContextPath("contextPath1");
        request.setQueryString("queryString1");
        request.setRequestURI("/root1");

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getProtocol(), wrapper.getProtocol());
        assertEquals(request.getScheme(), wrapper.getScheme());
        assertEquals(request.getServerName(), wrapper.getServerName());
        assertEquals(request.getServerPort(), wrapper.getServerPort());
        assertEquals(request.getPathInfo(), wrapper.getPathInfo());
        assertEquals(request.getContextPath(), wrapper.getContextPath());
        assertEquals(request.getQueryString(), wrapper.getQueryString());
        assertEquals(request.getRequestURI(), wrapper.getRequestURI());
        assertEquals(request.getRequestURL().toString(), wrapper.getRequestURL().toString());

        wrapper.setProtocol("https");
        wrapper.setScheme("https");
        wrapper.setServerName("server2");
        wrapper.setServerPort(443);
        wrapper.setPathInfo("pathInfo2");
        wrapper.setContextPath("contextPath2");
        wrapper.setQueryString("queryString2");
        wrapper.setRequestURI("/root2");
        assertEquals("https", wrapper.getProtocol());
        assertEquals("https", wrapper.getScheme());
        assertEquals("server2", wrapper.getServerName());
        assertEquals(443, wrapper.getServerPort());
        assertEquals("pathInfo2", wrapper.getPathInfo());
        assertEquals("contextPath2", wrapper.getContextPath());
        assertEquals("queryString2", wrapper.getQueryString());
        assertEquals("/root2", wrapper.getRequestURI());
        assertEquals("https://server2/root2", wrapper.getRequestURL().toString());
    }

    @Test
    public void test08_Locale() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.ENGLISH);
        request.addPreferredLocale(Locale.FRANCE);

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getLocale(), wrapper.getLocale());
        assertArrayEquals(list(request.getLocales()).toArray(), list(wrapper.getLocales()).toArray());
        assertEquals("fr-fr, en, en", wrapper.getHeader(MutableRequestWrapper.ACCEPT_LANGUAGE));

        wrapper.addLocale(Locale.GERMANY);
        wrapper.addLocale(Locale.JAPAN);
        assertEquals(Locale.JAPAN, wrapper.getLocale());
        assertArrayEquals(new Locale[] { Locale.JAPAN, Locale.GERMANY }, list(wrapper.getLocales()).toArray());
        assertEquals("ja-jp, de-de", wrapper.getHeader(MutableRequestWrapper.ACCEPT_LANGUAGE));

        wrapper.setLocales(Arrays.asList(new Locale[] { Locale.CANADA, Locale.ITALIAN }));
        assertArrayEquals(new Locale[] { Locale.CANADA, Locale.ITALIAN }, list(wrapper.getLocales()).toArray());
        assertEquals("en-ca, it", wrapper.getHeader(MutableRequestWrapper.ACCEPT_LANGUAGE));
    }

    @Test
    public void test09_Cookies() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockCookie c1 = new MockCookie("JSESSIONID", "A1");
        request.setCookies(c1);

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertArrayEquals(request.getCookies(), wrapper.getCookies());
        request.setCookies(new MockCookie[] {});

        MockCookie c2 = new MockCookie("JSESSIONID", "A2");
        wrapper.setCookies(c2);
        assertArrayEquals(new MockCookie[] { c2 }, wrapper.getCookies());
        assertEquals("JSESSIONID=A2", wrapper.getHeader(MutableRequestWrapper.COOKIE));
        wrapper.setCookies(new MockCookie[] {});
        assertEquals(null, wrapper.getHeader(MutableRequestWrapper.COOKIE));
    }

    @Test
    public void test10_Header() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("name1", "value1");
        request.addHeader("name1", "value2");

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getHeader("name1"), wrapper.getHeader("name1"));
        assertArrayEquals(list(request.getHeaderNames()).toArray(), list(wrapper.getHeaderNames()).toArray());
        assertArrayEquals(list(request.getHeaders("name1")).toArray(), list(wrapper.getHeaders("name1")).toArray());
        assertArrayEquals(new String[] { "value1", "value2" }, list(wrapper.getHeaders("name1")).toArray());

        wrapper.addHeader("name1", "new_value1");
        assertArrayEquals(new String[] { "new_value1" }, list(wrapper.getHeaders("name1")).toArray());
        wrapper.addHeader("name1", "new_value2");
        assertArrayEquals(new String[] { "new_value1", "new_value2" }, list(wrapper.getHeaders("name1")).toArray());
        assertEquals("new_value1", wrapper.getHeader("name1"));

        wrapper.addHeader("name2", "value1");
        wrapper.addHeader("name2", "value2");
        assertArrayEquals(new String[] { "name1", "name2" }, list(wrapper.getHeaderNames()).toArray());
        assertArrayEquals(new String[] { "value1", "value2" }, list(wrapper.getHeaders("name2")).toArray());
        assertEquals("value1", wrapper.getHeader("name2"));

        wrapper.removeHeader("name2");
        assertEquals(null, wrapper.getHeader("name2"));

        wrapper.setHeader("name1", "only_value1");
        assertArrayEquals(new String[] { "only_value1" }, list(wrapper.getHeaders("name1")).toArray());
        assertEquals("only_value1", wrapper.getHeader("name1"));

        wrapper.setHeader("int", "999");
        assertEquals(999, wrapper.getIntHeader("int"));

        wrapper.setHeader("date", "Wed, 21 Oct 2015 07:28:00 GMT");
        assertEquals(1445412480000L, wrapper.getDateHeader("date"));
    }

    @Test
    public void test11_Role() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addUserRole("role1");

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.isUserInRole("role1"), wrapper.isUserInRole("role1"));

        wrapper.addUserRole("role2");
        assertEquals(true, wrapper.isUserInRole("role1"));
        assertEquals(true, wrapper.isUserInRole("role2"));
    }

    @Test
    public void test12_UserPrincipal() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new User("username1", "dn1", "password1", null, "userRoleId1"));

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getUserPrincipal(), wrapper.getUserPrincipal());

        User u2 = new User("username2", "dn2", "password2", null, "userRoleId2");
        wrapper.setUserPrincipal(u2);
        assertEquals(u2, wrapper.getUserPrincipal());
        wrapper.logout();
        assertEquals(null, wrapper.getUserPrincipal());
    }

    @Test
    public void test13_HttpSession() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.setRequestedSessionIdFromCookie(false);
        request.setRequestedSessionIdFromURL(false);

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getSession(), wrapper.getSession());
        assertNotEquals(request.changeSessionId(), wrapper.changeSessionId());
        assertEquals(request.isRequestedSessionIdFromCookie(), wrapper.isRequestedSessionIdFromCookie());
        assertEquals(request.isRequestedSessionIdFromURL(), wrapper.isRequestedSessionIdFromURL());

        MockHttpSession s2 = new MockHttpSession();
        assertNotEquals(s2, request.getSession());
        wrapper.setSession(s2);
        wrapper.setRequestedSessionIdFromCookie(true);
        wrapper.setRequestedSessionIdFromURL(true);
        assertEquals(s2, wrapper.getSession());
        assertEquals(s2.getId(), wrapper.changeSessionId());
        assertEquals(true, wrapper.isRequestedSessionIdFromCookie());
        assertEquals(true, wrapper.isRequestedSessionIdFromURL());
        wrapper.logout();
        assertNotEquals(s2, wrapper.getSession());
    }

    @Test
    public void test14_Part() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockPart p1 = new MockPart("name1", "data1".getBytes());
        request.addPart(p1);

        MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
        assertEquals(request.getPart("name1"), wrapper.getPart("name1"));
        assertArrayEquals(request.getParts().toArray(), wrapper.getParts().toArray());

        MockPart p2 = new MockPart("name2", "data2".getBytes());
        wrapper.addPart(p2);
        assertEquals(p2, wrapper.getPart("name2"));
        assertArrayEquals(new MockPart[] { p1, p2 }, wrapper.getParts().toArray());

        wrapper.removePart(p2.getName());
        assertEquals(null, wrapper.getPart("name2"));
    }

}
