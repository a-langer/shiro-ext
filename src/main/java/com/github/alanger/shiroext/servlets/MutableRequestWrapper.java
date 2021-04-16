package com.github.alanger.shiroext.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class MutableRequestWrapper extends HttpServletRequestWrapper {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String CHARSET_PREFIX = "charset=";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.ENGLISH);

    private static final String[] DATE_FORMATS = new String[] { "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM dd HH:mm:ss yyyy" };

    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String COOKIE = "Cookie";

    // ---------------------------------------------------------------------
    // ServletRequest properties
    // ---------------------------------------------------------------------

    private String characterEncoding;

    protected ByteArrayOutputStream content;

    private String contentType;

    private ServletInputStream inputStream;

    private BufferedReader reader;

    private final Map<String, String[]> parameters = new LinkedHashMap<>(16);

    private String protocol;

    private String scheme;

    private String serverName;

    private int serverPort = -1;

    private String remoteAddr;

    private String remoteHost;

    private final LinkedList<Locale> locales = new LinkedList<>();

    private boolean secure = false;

    private int remotePort;

    private String localName;

    private String localAddr;

    private int localPort = -1;

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    private AsyncContext asyncContext;

    private DispatcherType dispatcherType;

    // ---------------------------------------------------------------------
    // HttpServletRequest properties
    // ---------------------------------------------------------------------

    private String authType;

    private Cookie[] cookies;

    private final Map<String, LinkedList<Object>> headers = new LinkedHashMap<>();

    private String method;

    private String pathInfo;

    private String contextPath;

    private String queryString;

    private String remoteUser;

    private final Set<String> userRoles = new LinkedHashSet<>();

    private Principal userPrincipal;

    private String requestedSessionId;

    private String requestURI;

    private String servletPath;

    private HttpSession session;

    private boolean requestedSessionIdValid = false;

    private boolean requestedSessionIdFromCookie = false;

    private boolean requestedSessionIdFromURL = false;

    private final Map<String, Part> parts = new LinkedHashMap<>();

    public MutableRequestWrapper(HttpServletRequest wrapped) {
        super(wrapped);
    }

    // ---------------------------------------------------------------------
    // ServletRequest interface
    // ---------------------------------------------------------------------

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding != null ? this.characterEncoding : super.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }

    private void updateContentTypeHeader() {
        String value = getContentType();
        if (hasLength(value)) {
            String encoding = getCharacterEncoding();
            if (hasLength(encoding) && !value.toLowerCase().contains(CHARSET_PREFIX)) {
                value += ';' + CHARSET_PREFIX + encoding;
            }
            doAddHeaderValue(CONTENT_TYPE, value, true);
        }
    }

    public void setContent(byte[] content) {
        Objects.requireNonNull(content, "Content must not be null");
        this.content = new ByteArrayOutputStream();
        this.content.write(content, 0, content.length);
        updateContentLengthHeader();
        this.inputStream = null;
        this.reader = null;
    }

    protected void updateContentLengthHeader() {
        String value = getHeader(CONTENT_LENGTH);
        if (hasLength(value) && this.content != null) {
            doAddHeaderValue(CONTENT_LENGTH, this.content.size(), true);
        }
    }

    public byte[] getContentAsByteArray() {
        return this.content != null ? this.content.toByteArray() : null;
    }

    public String getContentAsString() throws IllegalStateException, UnsupportedEncodingException {
        if (this.content == null) {
            return null;
        }
        String encoding = getCharacterEncoding() != null ? getCharacterEncoding() : "UTF-8";
        return new String(this.content.toByteArray(), encoding);
    }

    @Override
    public int getContentLength() {
        return (this.content != null ? this.content.size() : super.getContentLength());
    }

    @Override
    public long getContentLengthLong() {
        return (this.content != null ? this.content.size() : super.getContentLengthLong());
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            // Try to get charset value anyway
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public String getContentType() {
        return this.contentType != null ? this.contentType : super.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.inputStream != null) {
            return this.inputStream;
        } else if (this.reader != null) {
            throw new IllegalStateException(
                    "Cannot call getInputStream() after getReader() has already been called for the current request");
        }

        this.inputStream = (this.content != null ? new ContentServletInputStream(this.content.toByteArray())
                : super.getInputStream());
        return this.inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.reader != null) {
            return this.reader;
        } else if (this.inputStream != null) {
            throw new IllegalStateException(
                    "Cannot call getReader() after getInputStream() has already been called for the current request");
        }

        if (this.content != null) {
            ByteArrayInputStream sourceStream = new ByteArrayInputStream(this.content.toByteArray());
            InputStreamReader sourceReader = (this.characterEncoding != null)
                    ? new InputStreamReader(sourceStream, this.characterEncoding) : new InputStreamReader(sourceStream);
            this.reader = new BufferedReader(sourceReader);
        } else {
            this.reader = super.getReader();
        }
        return this.reader;
    }

    public void setParameter(String name, String... values) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        this.parameters.put(name, values);
    }

    public void setParameters(Map<String, ?> params) {
        Objects.requireNonNull(params, "Parameter map must not be null");
        params.forEach((key, value) -> {
            if (value instanceof String) {
                setParameter(key, (String) value);
            } else if (value instanceof String[]) {
                setParameter(key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type ["
                        + String.class.getName() + "]");
            }
        });
    }

    public void addParameter(String name, String... values) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        } else {
            this.parameters.put(name, values);
        }
    }

    public void addParameters(Map<String, ?> params) {
        Objects.requireNonNull(params, "Parameter map must not be null");
        params.forEach((key, value) -> {
            if (value instanceof String) {
                addParameter(key, (String) value);
            } else if (value instanceof String[]) {
                addParameter(key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type ["
                        + String.class.getName() + "]");
            }
        });
    }

    public void removeParameter(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        this.parameters.remove(name);
    }

    public void removeAllParameters() {
        this.parameters.clear();
    }

    @Override
    public String getParameter(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        String[] arr = this.parameters.get(name);
        return (arr != null && arr.length > 0 ? arr[0] : super.getParameter(name));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new LinkedHashMap<>();
        result.putAll(super.getParameterMap());
        result.putAll(parameters);
        return Collections.<String, String[]> unmodifiableMap(result);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> result = new LinkedHashSet<>(Collections.list(super.getParameterNames()));
        result.addAll(parameters.keySet());
        return Collections.enumeration(result);
    }

    @Override
    public String[] getParameterValues(String name) {
        if (parameters.get(name) != null) {
            return parameters.get(name);
        }
        return super.getParameterValues(name);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return this.protocol != null ? this.protocol : super.getProtocol();
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getScheme() {
        return this.scheme != null ? this.scheme : super.getScheme();
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getServerName() {
        return this.serverName != null ? this.serverName : super.getServerName();
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        return this.serverPort > 0 ? this.serverPort : super.getServerPort();
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr != null ? this.remoteAddr : super.getRemoteAddr();
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost != null ? this.remoteHost : super.getRemoteHost();
    }

    public void addLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null");
        this.locales.addFirst(locale);
        updateAcceptLanguageHeader();
    }

    public void setLocales(List<Locale> locales) {
        Objects.requireNonNull(locales, "Locale list must not be empty");
        this.locales.clear();
        this.locales.addAll(locales);
        updateAcceptLanguageHeader();
    }

    private void updateAcceptLanguageHeader() {
        setAcceptLanguageAsLocales(this.locales);
    }

    private void setAcceptLanguageAsLocales(List<Locale> locales) {
        setAcceptLanguage(locales.stream().map(locale -> new Locale.LanguageRange(locale.toLanguageTag()))
                .collect(Collectors.toList()));
    }

    private void setAcceptLanguage(List<Locale.LanguageRange> languages) {
        Objects.requireNonNull(languages, "LanguageRange List must not be null");
        DecimalFormat decimal = new DecimalFormat("0.0", DECIMAL_FORMAT_SYMBOLS);
        List<String> values = languages.stream().map(range -> range.getWeight() == Locale.LanguageRange.MAX_WEIGHT
                ? range.getRange() : range.getRange() + ";q=" + decimal.format(range.getWeight()))
                .collect(Collectors.toList());
        doAddHeaderValue(ACCEPT_LANGUAGE, toCommaDelimitedString(values), true);
    }

    @Override
    public Locale getLocale() {
        return (!this.locales.isEmpty()) ? this.locales.getFirst() : super.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return (!this.locales.isEmpty()) ? Collections.enumeration(this.locales) : super.getLocales();
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return (this.secure || super.isSecure());
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort > 0 ? this.remotePort : super.getRemotePort();
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return this.localName != null ? this.localName : super.getLocalName();
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr != null ? this.localAddr : super.getLocalAddr();
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public int getLocalPort() {
        return this.localPort > 0 ? this.localPort : super.getLocalPort();
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncStarted() {
        return (this.asyncStarted || super.isAsyncStarted());
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return (this.asyncSupported || super.isAsyncSupported());
    }

    public void setAsyncContext(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.asyncContext != null ? this.asyncContext : super.getAsyncContext();
    }

    public void setDispatcherType(DispatcherType dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType != null ? this.dispatcherType : super.getDispatcherType();
    }

    // ---------------------------------------------------------------------
    // HttpServletRequest interface
    // ---------------------------------------------------------------------

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public String getAuthType() {
        return this.authType != null ? this.authType : super.getAuthType();
    }

    public void setCookies(Cookie... cookies) {
        this.cookies = (cookies == null || cookies.length == 0 ? null : cookies);
        if (this.cookies == null) {
            removeHeader(COOKIE);
        } else {
            doAddHeaderValue(COOKIE, encodeCookies(this.cookies), true);
        }
    }

    private static String encodeCookies(Cookie... cookies) {
        return Arrays.stream(cookies).map(c -> c.getName() + '=' + (c.getValue() == null ? "" : c.getValue()))
                .collect(Collectors.joining("; "));
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies != null ? this.cookies : super.getCookies();
    }

    public void setHeader(String name, String value) {
        doAddHeaderValue(name, value, true);
    }

    public void addHeader(String name, Object value) {
        Objects.requireNonNull(value, "Header value must not be null");
        if (CONTENT_TYPE.equalsIgnoreCase(name) && !this.headers.containsKey(CONTENT_TYPE)) {
            setContentType(value.toString());
        } else if (ACCEPT_LANGUAGE.equalsIgnoreCase(name) && !this.headers.containsKey(ACCEPT_LANGUAGE)) {
            try {
                List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(value.toString());
                if (!ranges.isEmpty()) {
                    List<Locale> loc = ranges.stream().map(range -> Locale.forLanguageTag(range.getRange()))
                            .filter(locale -> hasText(locale.getDisplayName())).collect(Collectors.toList());
                    if (!loc.isEmpty()) {
                        this.locales.clear();
                        this.locales.addAll(loc);
                    }
                }
            } catch (IllegalArgumentException ex) {
                // Invalid Accept-Language format -> just store plain header
            }
            doAddHeaderValue(name, value, true);
        } else {
            doAddHeaderValue(name, value, false);
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        Objects.requireNonNull(value, "Header value must not be null");
        LinkedList<Object> header = this.headers.get(name);
        if (header == null || replace) {
            header = new LinkedList<>();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addAll((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            header.addAll(Arrays.asList((Object[]) value));
        } else {
            header.add(value);
        }
    }

    public void removeHeader(String name) {
        Objects.requireNonNull(name, "Header name must not be null");
        this.headers.remove(name);
    }

    @Override
    public long getDateHeader(String name) {
        LinkedList<Object> header = this.headers.get(name);
        Object value = (header != null ? header.getFirst() : null);
        if (value != null) {
            if (value instanceof Date) {
                return ((Date) value).getTime();
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return parseDateHeader(name, (String) value);
            } else {
                throw new IllegalArgumentException(
                        "Value for header '" + name + "' is not a Date, Number, or String: " + value);
            }
        }
        return super.getDateHeader(name);
    }

    private long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            } catch (ParseException ex) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    @Override
    public String getHeader(String name) {
        LinkedList<Object> header = this.headers.get(name);
        if (header != null && !header.isEmpty()) {
            return String.valueOf(header.getFirst());
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        LinkedList<Object> header = this.headers.get(name);
        if (header != null) {
            return Collections.enumeration(header.stream().map(Object::toString).collect(Collectors.toList()));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> result = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
        result.addAll(this.headers.keySet());
        return Collections.enumeration(result);
    }

    @Override
    public int getIntHeader(String name) {
        LinkedList<Object> header = this.headers.get(name);
        Object value = (header != null ? header.getFirst() : null);
        if (value != null) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else {
                throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
            }
        }
        return super.getIntHeader(name);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return this.method != null ? this.method : super.getMethod();
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathInfo() {
        return pathInfo != null ? pathInfo : super.getPathInfo();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : super.getPathTranslated());
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return contextPath != null ? contextPath : super.getContextPath();
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getQueryString() {
        return this.queryString != null ? this.queryString : super.getQueryString();
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser != null ? this.remoteUser : super.getRemoteUser();
    }

    public void addUserRole(String role) {
        this.userRoles.add(role);
    }

    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || super.isUserInRole(role));
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal != null ? this.userPrincipal : super.getUserPrincipal();
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getRequestedSessionId() {
        return this.requestedSessionId != null ? this.requestedSessionId : super.getRequestedSessionId();
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String getRequestURI() {
        return requestURI != null ? requestURI : super.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        String scheme = getScheme();
        String server = getServerName();
        int port = getServerPort();
        String uri = getRequestURI();

        StringBuffer url = new StringBuffer(scheme).append("://").append(server);
        if (port > 0
                && ((HTTP.equalsIgnoreCase(scheme) && port != 80) || (HTTPS.equalsIgnoreCase(scheme) && port != 443))) {
            url.append(':').append(port);
        }
        if (uri != null && !uri.isEmpty()) {
            url.append(uri);
        }
        return url;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getServletPath() {
        return servletPath != null ? servletPath : super.getServletPath();
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (this.session != null && !create) {
            return this.session;
        }
        return super.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return this.session != null ? this.session : super.getSession();
    }

    @Override
    public String changeSessionId() {
        return this.session != null ? this.session.getId() : super.changeSessionId();
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid || super.isRequestedSessionIdValid();
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie || super.isRequestedSessionIdFromCookie();
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL || super.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public void logout() throws ServletException {
        try {
            if (this.session != null) {
                this.session.invalidate();
                this.session = null;
            }
        } finally {
            this.userPrincipal = null;
            this.remoteUser = null;
            this.authType = null;
            super.logout();
        }
    }

    public void addPart(Part part) {
        Objects.requireNonNull(part, "Part value must not be null");
        this.parts.put(part.getName(), part);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        Objects.requireNonNull(name, "Part name must not be null");
        Part part = this.parts.get(name);
        return part != null ? part : super.getPart(name);
    }

    public void removePart(String name) {
        Objects.requireNonNull(name, "Part name must not be null");
        this.parts.remove(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        Set<Part> result = new LinkedHashSet<>(super.getParts());
        result.addAll(this.parts.values());
        return result;
    }

    // ---------------------------------------------------------------------
    // Utils methods
    // ---------------------------------------------------------------------

    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static String toCommaDelimitedString(List<String> headerValues) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String val : headerValues) {
            if (val != null) {
                joiner.add(val);
            }
        }
        return joiner.toString();
    }

}
