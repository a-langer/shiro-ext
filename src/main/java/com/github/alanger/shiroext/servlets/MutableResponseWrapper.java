package com.github.alanger.shiroext.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.github.alanger.shiroext.http.DelegatingServletOutputStream;
import com.github.alanger.shiroext.util.Assert;

public class MutableResponseWrapper extends HttpServletResponseWrapper {

    private boolean outputStreamAccessAllowed = true;

    private boolean writerAccessAllowed = true;

    private String characterEncoding = "UTF-8";

    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

    private final ServletOutputStream outputStream = new ResponseServletOutputStream(this.content);

    private PrintWriter writer;

    private boolean committed;

    public MutableResponseWrapper(HttpServletResponse response) {
        super(response);
        this.characterEncoding = response.getCharacterEncoding() != null ? response.getCharacterEncoding()
                : characterEncoding;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        Assert.state(this.outputStreamAccessAllowed, "OutputStream access not allowed");
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        Assert.state(this.writerAccessAllowed, "Writer access not allowed");
        if (this.writer == null) {
            Writer targetWriter = (this.characterEncoding != null
                    ? new OutputStreamWriter(this.content, this.characterEncoding)
                    : new OutputStreamWriter(this.content));
            this.writer = new ResponsePrintWriter(targetWriter);
        }
        return this.writer;
    }

    public byte[] getContentAsByteArray() {
        flushBuffer();
        return this.content.toByteArray();
    }

    public String getContentAsString() throws UnsupportedEncodingException {
        flushBuffer();
        return (this.characterEncoding != null ? this.content.toString(this.characterEncoding)
                : this.content.toString());
    }

    public void setOutputStreamAccessAllowed(boolean outputStreamAccessAllowed) {
        this.outputStreamAccessAllowed = outputStreamAccessAllowed;
    }

    public boolean isOutputStreamAccessAllowed() {
        return this.outputStreamAccessAllowed;
    }

    public void setWriterAccessAllowed(boolean writerAccessAllowed) {
        this.writerAccessAllowed = writerAccessAllowed;
    }

    public boolean isWriterAccessAllowed() {
        return this.writerAccessAllowed;
    }

    @Override
    public void flushBuffer() {
        setCommitted(true);
    }

    @Override
    public void resetBuffer() {
        Assert.state(!isCommitted(), "Cannot reset buffer - response is already committed");
        this.content.reset();
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    @Override
    public boolean isCommitted() {
        return this.committed ? this.committed : super.isCommitted();
    }

    private void setCommittedIfBufferSizeExceeded() {
        int bufSize = getBufferSize();
        if (bufSize > 0 && this.content.size() > bufSize) {
            setCommitted(true);
        }
    }

    /**
     * Inner class that adapts the ServletOutputStream to mark the
     * response as committed once the buffer size is exceeded.
     */
    private class ResponseServletOutputStream extends DelegatingServletOutputStream {

        public ResponseServletOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            setCommitted(true);
        }
    }

    /**
     * Inner class that adapts the PrintWriter to mark the
     * response as committed once the buffer size is exceeded.
     */
    private class ResponsePrintWriter extends PrintWriter {

        public ResponsePrintWriter(Writer out) {
            super(out, true);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void flush() {
            super.flush();
            setCommitted(true);
        }
    }

}
