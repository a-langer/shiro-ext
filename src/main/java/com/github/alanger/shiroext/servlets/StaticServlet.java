package com.github.alanger.shiroext.servlets;

import static com.github.alanger.shiroext.web.Utils.normalizePath;
import static com.github.alanger.shiroext.web.Utils.getRequestURI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.alanger.shiroext.web.ISilent;

public class StaticServlet extends HttpServlet implements ISilent {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_DIR = "";
    public static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);
    public static final String ETAG_HEADER = "W/\"%s-%s\"";
    public static final String CONTENT_DISPOSITION_HEADER = "inline;filename=\"%1$s\"; filename*=UTF-8''%1$s";

    public static final long DEFAULT_EXPIRE_TIME_IN_MILLIS = TimeUnit.DAYS.toMillis(30);
    public static final int DEFAULT_STREAM_BUFFER_SIZE = 102400;

    private static final String CSS;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("h1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} ");
        sb.append("h2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} ");
        sb.append("h3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} ");
        sb.append("body {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} ");
        sb.append("b {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} ");
        sb.append("p {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;} ");
        sb.append("a {color:black;} a.name {color:black;} ");
        sb.append(".line {height:1px;background-color:#525D76;border:none;}");
        CSS = sb.toString();
    }

    public static final String DIR_KEY = "static-root";
    public static final String SHOW_DIR_KEY = "static-show-content";
    public static final String PATH_INFO_KEY = "static-path-info";
    public static final String SILENT_KEY = "static-silent";

    private String rootDir = DEFAULT_DIR;

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }
    private boolean showContent = false;

    public boolean isShowContent() {
        return showContent;
    }

    public void setShowContent(boolean showContent) {
        this.showContent = showContent;
    }

    private String pathInfo;

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    private boolean silent = false;

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public interface StaticResource {
        public String getFileName();

        public long getLastModified();

        public long getContentLength();

        public InputStream getInputStream() throws IOException;

        public String getContentType();
    }

    private String getPath(HttpServletRequest request) {
        String pathInfo = getPathInfo() != null ? getPathInfo() : request.getPathInfo();
        if (pathInfo == null)
            pathInfo = getRequestURI(request);
        return pathInfo;
    }

    @Override
    public void init() throws ServletException {
        rootDir = normalizePath(getInitParameter(DIR_KEY) != null ? getInitParameter(DIR_KEY) : getRootDir());
        if (DEFAULT_DIR.equals(rootDir))
            rootDir = getServletContext().getRealPath(DEFAULT_DIR);
        showContent = getInitParameter(SHOW_DIR_KEY) != null ? Boolean.valueOf(getInitParameter(SHOW_DIR_KEY))
                : isShowContent();
        pathInfo = getInitParameter(PATH_INFO_KEY) != null ? getInitParameter(PATH_INFO_KEY) : getPathInfo();
        silent = getInitParameter(SILENT_KEY) != null ? Boolean.valueOf(getInitParameter(SILENT_KEY))
                : isSilent();
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response, true);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response, false);
    }

    public void doStatic(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (response.isCommitted()) {
            return;
        }

        String verb = request.getMethod().toLowerCase();
        if (verb.equals("get")) {
            doGet(request, response);
        } else if (verb.equals("head")) {
            doHead(request, response);
        }
    }

    public void doRequest(HttpServletRequest request, HttpServletResponse response, boolean head) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        StaticResource resource;
        try {
            resource = getStaticResource(rootDir, request);
        } catch (IllegalArgumentException e) {
            if (!isSilent()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request, static resource "
                            + request.getMethod().toUpperCase() + ":" + getPath(request) + " , " + e);
                }
            }
            return;
        }

        doRequest(resource, request, response, head);
    }

    private void doRequest(StaticResource resource, HttpServletRequest request, HttpServletResponse response,
            boolean head) throws IOException {

        if (resource == null) {
            if (!isSilent()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found, static resource "
                            + request.getMethod().toUpperCase() + ":" + getPath(request) + " not exist");
                }
            }
            return;
        }

        String fileName = URLEncoder.encode(resource.getFileName(), StandardCharsets.UTF_8.name());
        boolean notModified = setCacheHeaders(request, response, fileName, resource.getLastModified());

        if (notModified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            if (!isSilent() && !response.isCommitted())
                response.flushBuffer();
            return;
        }

        setContentHeaders(response, resource);

        if (head) {
            if (!isSilent() && !response.isCommitted())
                response.flushBuffer();
            return;
        }

        writeContent(response, resource);
    }

    private StaticResource getStaticResource(String root, HttpServletRequest request)
            throws IllegalArgumentException {

        String pathInfo = getPath(request);
        
        if (pathInfo == null || pathInfo.isEmpty()) {
            throw new IllegalArgumentException("Path is null");
        }

        String name;
        try {
            name = URLDecoder.decode(pathInfo.substring(1), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

        final File file = new File(root, name);

        if (!file.exists()) {
            return null;
        }

        if (showContent && !file.isFile() && pathInfo.endsWith("/")) {
            return new HtmlResource(request, file);
        } else if (file.isFile()) {
            return new StaticResource() {
                @Override
                public long getLastModified() {
                    return file.lastModified();
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(file);
                }

                @Override
                public String getFileName() {
                    return file.getName();
                }

                @Override
                public long getContentLength() {
                    return file.length();
                }

                @Override
                public String getContentType() {
                    return getServletContext().getMimeType(file.getName());
                }
            };
        }
        return null;
    }

    private boolean setCacheHeaders(HttpServletRequest request, HttpServletResponse response, String fileName,
            long lastModified) {
        String eTag = String.format(ETAG_HEADER, fileName, lastModified);
        response.setHeader("ETag", eTag);
        response.setDateHeader("Last-Modified", lastModified);
        response.setDateHeader("Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_IN_MILLIS);
        return notModified(request, eTag, lastModified);
    }

    private boolean notModified(HttpServletRequest request, String eTag, long lastModified) {
        String ifNoneMatch = request.getHeader("If-None-Match");

        if (ifNoneMatch != null) {
            String[] matches = ifNoneMatch.split("\\s*,\\s*");
            Arrays.sort(matches);
            return (Arrays.binarySearch(matches, eTag) > -1 || Arrays.binarySearch(matches, "*") > -1);
        } else {
            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            // That second is because the header is in seconds, not millis
            return (ifModifiedSince + ONE_SECOND_IN_MILLIS > lastModified);
        }
    }

    private void setContentHeaders(HttpServletResponse response, StaticResource resource) {
        response.setHeader("Content-Type", resource.getContentType());
        response.setHeader("Content-Disposition", String.format(CONTENT_DISPOSITION_HEADER, resource.getFileName()));
        long contentLength = resource.getContentLength();
        if (contentLength != -1) {
            response.setHeader("Content-Length", String.valueOf(contentLength));
        }
    }

    private void writeContent(HttpServletResponse response, StaticResource resource) throws IOException {
        try (
                ReadableByteChannel inputChannel = Channels.newChannel(resource.getInputStream());
                WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream());) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_STREAM_BUFFER_SIZE);
            long size = 0;

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }

            if (resource.getContentLength() == -1 && !response.isCommitted()) {
                response.setHeader("Content-Length", String.valueOf(size));
            }
        }
    }

    public class HtmlResource implements StaticResource {
        private final File file;
        private final StringBuffer sb = new StringBuffer();

        private DateFormat shortDF = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

        public HtmlResource(HttpServletRequest request, File file) {
            this.file = file;
            final String contextPath = request.getContextPath();
            final String pathInfo = request.getPathInfo();
            final String requestURI = request.getRequestURI();

            sb.append("<html>");
            sb.append("<head>");
            sb.append("<title>Static</title>");
            sb.append("<link rel=\"SHORTCUT ICON\" href=\"" + contextPath + "/favicon.ico\">");
            sb.append("<style type=\"text/css\">");
            sb.append(getCSS());
            sb.append("</style>");
            sb.append("</head>");
            sb.append("<body>");
            sb.append("<h2>Content of folder: " + (pathInfo != null ? pathInfo : file.getName() + "/") + "</h2>");

            sb.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">\r\n");

            // Head of table
            sb.append("<tr>\r\n");
            sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
            sb.append("Name");
            sb.append("</strong></font></td>\r\n");
            sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
            sb.append("Size");
            sb.append("</strong></font></td>\r\n");
            sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
            sb.append("Type");
            sb.append("</strong></font></td>\r\n");
            sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
            sb.append("Modified");
            sb.append("</strong></font></td>\r\n");
            sb.append("</tr>");

            sb.append("<tr><td colspan=\"4\"><a href=\"../\"><tt>[Parent]</tt></a></td></tr>");

            String baseDir = requestURI.endsWith("/") ? requestURI : requestURI + "/";

            boolean isEven = false;
            for (File subFile : file.listFiles()) {
                isEven = !isEven;
                String name = subFile.isFile() ? subFile.getName() : subFile.getName() + "/";

                // Striped table
                sb.append("<tr " + (isEven ? "bgcolor=\"#eeeeee\"" : "") + "\">");

                // File name
                sb.append("<td>").append("<a href=\"");
                sb.append(baseDir + name);
                sb.append("\"><tt>");
                sb.append(name);
                sb.append("</tt></a></td>");

                // File size
                if (subFile.isDirectory()) {
                    sb.append("<td><tt>Folder</tt></td>");
                } else {
                    sb.append("<td><tt>").append(renderSize(subFile.length())).append("</tt></td>");
                }

                // MIME type
                if (subFile.isDirectory()) {
                    sb.append("<td><tt>-</tt></td>");
                } else {
                    sb.append("<td><tt>");
                    String mimeType = getServletContext().getMimeType(name);
                    mimeType = mimeType != null ? mimeType : "Unknown type";
                    sb.append(mimeType);
                    sb.append("</tt></td>");
                }

                // Modification date
                sb.append("<td><tt>");
                sb.append(shortDF.format(subFile.lastModified()));
                sb.append("</tt></td>");

                sb.append("</tr>");
            }

            sb.append("</table>");
            sb.append("</body></html>");
        }

        private String getCSS() {
            return CSS;
        }

        private String renderSize(long size) {
            long leftSide = size / 1024;
            long rightSide = (size % 1024) / 103; // Makes 1 digit
            if ((leftSide == 0) && (rightSide == 0) && (size > 0))
                rightSide = 1;
            return ("" + leftSide + "." + rightSide + " kb");
        }

        @Override
        public long getLastModified() {
            return file.lastModified();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(sb.toString().getBytes());
        }

        @Override
        public String getFileName() {
            return file.getName();
        }

        @Override
        public long getContentLength() {
            return sb.length();
        }

        @Override
        public String getContentType() {
            return "text/html";
        }
    }

}
