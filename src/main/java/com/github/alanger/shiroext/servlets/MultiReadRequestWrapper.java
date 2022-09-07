package com.github.alanger.shiroext.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class MultiReadRequestWrapper extends MutableRequestWrapper {

    public MultiReadRequestWrapper(HttpServletRequest wrapped) {
        super(wrapped);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (content == null) {
            cacheInputStream();
            updateContentLengthHeader();
        }
        return new ContentServletInputStream(content.toByteArray());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    private void cacheInputStream() throws IOException {
        try (ServletInputStream is = super.getInputStream()) {
            if (is != null) {
                content = new ByteArrayOutputStream();
                byte[] buffer = new byte[10240];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    content.write(buffer, 0, len);
                }
            }
        }
    }

}