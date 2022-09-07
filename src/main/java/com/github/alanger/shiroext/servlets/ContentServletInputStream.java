package com.github.alanger.shiroext.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ContentServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream input;
    private boolean finished = false;

    public ContentServletInputStream(byte[] content) {
        input = new ByteArrayInputStream(content);
    }

    @Override
    public int read() throws IOException {
        int data = input.read();
        if (data == -1) {
            finished = true;
        }
        return data;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            input.close();
        }
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

}
