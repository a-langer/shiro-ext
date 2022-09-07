package com.github.alanger.shiroext.http;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.Cookie;

import com.github.alanger.shiroext.util.Assert;
import com.github.alanger.shiroext.util.StringUtils;

// org.springframework.mock.web.MockCookie
public class MockCookie extends Cookie {

    private static final long serialVersionUID = -1L;

    private ZonedDateTime expires;

    private String sameSite;

    /**
     * Construct a new {@link MockCookie} with the supplied name and value.
     * 
     * @param name
     *            the name
     * @param value
     *            the value
     * @see Cookie#Cookie(String, String)
     */
    public MockCookie(String name, String value) {
        super(name, value);
    }

    /**
     * Set the "Expires" attribute for this cookie.
     * 
     * @since 5.1.11
     */
    public void setExpires(ZonedDateTime expires) {
        this.expires = expires;
    }

    /**
     * Get the "Expires" attribute for this cookie.
     * 
     * @return the "Expires" attribute for this cookie, or {@code null} if not set
     * @since 5.1.11
     */
    public ZonedDateTime getExpires() {
        return this.expires;
    }

    /**
     * Set the "SameSite" attribute for this cookie.
     * <p>
     * This limits the scope of the cookie such that it will only be attached
     * to same-site requests if the supplied value is {@code "Strict"} or cross-site
     * requests if the supplied value is {@code "Lax"}.
     * 
     * @see <a href=
     *      "https://tools.ietf.org/html/draft-ietf-httpbis-rfc6265bis#section-4.1.2.7">RFC6265
     *      bis</a>
     */
    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    /**
     * Get the "SameSite" attribute for this cookie.
     * 
     * @return the "SameSite" attribute for this cookie, or {@code null} if not set
     */
    public String getSameSite() {
        return this.sameSite;
    }

    /**
     * Factory method that parses the value of the supplied "Set-Cookie" header.
     * 
     * @param setCookieHeader
     *            the "Set-Cookie" value; never {@code null} or empty
     * @return the created cookie
     */
    public static MockCookie parse(String setCookieHeader) {
        Assert.notNull(setCookieHeader, "Set-Cookie header must not be null");
        String[] cookieParts = setCookieHeader.split("\\s*=\\s*", 2);
        Assert.isTrue(cookieParts.length == 2, () -> "Invalid Set-Cookie header '" + setCookieHeader + "'");

        String name = cookieParts[0];
        String[] valueAndAttributes = cookieParts[1].split("\\s*;\\s*", 2);
        String value = valueAndAttributes[0];
        String[] attributes = (valueAndAttributes.length > 1 ? valueAndAttributes[1].split("\\s*;\\s*")
                : new String[0]);

        MockCookie cookie = new MockCookie(name, value);
        for (String attribute : attributes) {
            if (StringUtils.startsWithIgnoreCase(attribute, "Domain")) {
                cookie.setDomain(extractAttributeValue(attribute, setCookieHeader));
            } else if (StringUtils.startsWithIgnoreCase(attribute, "Max-Age")) {
                cookie.setMaxAge(Integer.parseInt(extractAttributeValue(attribute, setCookieHeader)));
            } else if (StringUtils.startsWithIgnoreCase(attribute, "Expires")) {
                try {
                    cookie.setExpires(ZonedDateTime.parse(extractAttributeValue(attribute, setCookieHeader),
                            DateTimeFormatter.RFC_1123_DATE_TIME));
                } catch (DateTimeException ex) {
                    // ignore invalid date formats
                }
            } else if (StringUtils.startsWithIgnoreCase(attribute, "Path")) {
                cookie.setPath(extractAttributeValue(attribute, setCookieHeader));
            } else if (StringUtils.startsWithIgnoreCase(attribute, "Secure")) {
                cookie.setSecure(true);
            } else if (StringUtils.startsWithIgnoreCase(attribute, "HttpOnly")) {
                cookie.setHttpOnly(true);
            } else if (StringUtils.startsWithIgnoreCase(attribute, "SameSite")) {
                cookie.setSameSite(extractAttributeValue(attribute, setCookieHeader));
            }
        }
        return cookie;
    }

    private static String extractAttributeValue(String attribute, String header) {
        String[] nameAndValue = attribute.split("=");
        Assert.isTrue(nameAndValue.length == 2,
                () -> "No value in attribute '" + nameAndValue[0] + "' for Set-Cookie header '" + header + "'");
        return nameAndValue[1];
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("name: " + getName())
                .append("value: " + getValue())
                .append("Path: " + getPath())
                .append("Domain: " + getDomain())
                .append("Version: " + getVersion())
                .append("Comment: " + getComment())
                .append("Secure: " + getSecure())
                .append("HttpOnly: " + isHttpOnly())
                .append("SameSite: " + this.sameSite)
                .append("Max-Age: " + getMaxAge())
                .append("Expires: "
                        + (this.expires != null ? DateTimeFormatter.RFC_1123_DATE_TIME.format(this.expires) : null))
                .toString();
    }

}
