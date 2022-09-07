package com.github.alanger.shiroext.servlets;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class MultiReadRequestWrapperTest {

    private String toText(Reader reader) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader r = new BufferedReader(reader)) {
            int c = 0;
            while ((c = r.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    private String toText(InputStream inputStream) throws IOException {
        return toText(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())));
    }

    @Test
    public void test01_InputStream() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent("hello".getBytes());

        MultiReadRequestWrapper wrapper = new MultiReadRequestWrapper(request);
        assertEquals(request.getContentLength(), wrapper.getContentLength());
        assertEquals("hello", toText(wrapper.getInputStream()));
        assertEquals("hello", toText(wrapper.getInputStream()));
        assertEquals(5, wrapper.getContentLength());

        wrapper.setContent("hello2".getBytes());
        assertEquals("hello2", toText(wrapper.getInputStream()));
        assertEquals("hello2", toText(wrapper.getInputStream()));
        assertEquals(6, wrapper.getContentLength());
    }

    @Test
    public void test02_Reader() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent("hello".getBytes());

        MultiReadRequestWrapper wrapper = new MultiReadRequestWrapper(request);
        assertEquals(request.getContentLength(), wrapper.getContentLength());
        assertEquals("hello", toText(wrapper.getReader()));
        assertEquals("hello", toText(wrapper.getReader()));
        assertEquals(5, wrapper.getContentLength());

        wrapper.setContent("hello2".getBytes());
        assertEquals("hello2", toText(wrapper.getReader()));
        assertEquals("hello2", toText(wrapper.getReader()));
        assertEquals(6, wrapper.getContentLength());
    }

}
