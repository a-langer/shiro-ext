package com.github.alanger.shiroext.io;

// org.springframework.core.io.FileSystemResourceLoader
public class FileSystemResourceLoader extends DefaultResourceLoader {

    /**
     * Resolve resource paths as file system paths.
     * <p>
     * Note: Even if a given path starts with a slash, it will get
     * interpreted as relative to the current VM working directory.
     * 
     * @param path
     *            the path to the resource
     * @return the corresponding Resource handle
     * @see FileSystemResource
     * @see org.springframework.web.context.support.ServletContextResourceLoader#
     *      getResourceByPath
     */
    @Override
    protected Resource getResourceByPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return new FileSystemContextResource(path);
    }

    /**
     * FileSystemResource that explicitly expresses a context-relative path
     * through implementing the ContextResource interface.
     */
    private static class FileSystemContextResource extends FileSystemResource {

        public FileSystemContextResource(String path) {
            super(path);
        }

        public String getPathWithinContext() {
            return getPath();
        }
    }

}
