package com.github.alanger.shiroext.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.Part;

import com.github.alanger.shiroext.util.Assert;

// org.springframework.mock.web.MockPart
public class MockPart implements Part {

    private final String name;

    private final String filename;

    private final byte[] content;

    private final HttpHeaders headers = new HttpHeaders();

    /**
     * Constructor for a part with byte[] content only.
     * 
     * @see #getHeaders()
     */
    public MockPart(String name, byte[] content) {
        this(name, null, content);
    }

    /**
     * Constructor for a part with a filename and byte[] content.
     * 
     * @see #getHeaders()
     */
    public MockPart(String name, String filename, byte[] content) {
        Assert.hasLength(name, "'name' must not be empty");
        this.name = name;
        this.filename = filename;
        this.content = (content != null ? content : new byte[0]);
        this.headers.setContentDispositionFormData(name, filename);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSubmittedFileName() {
        return this.filename;
    }

    @Override
    public String getContentType() {
        MediaType contentType = this.headers.getContentType();
        return (contentType != null ? contentType.toString() : null);
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void write(String fileName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String name) {
        return this.headers.getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        Collection<String> headerValues = this.headers.get(name);
        return (headerValues != null ? headerValues : Collections.emptyList());
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    /**
     * Return the {@link HttpHeaders} backing header related accessor methods,
     * allowing for populating selected header entries.
     */
    public final HttpHeaders getHeaders() {
        return this.headers;
    }

}
