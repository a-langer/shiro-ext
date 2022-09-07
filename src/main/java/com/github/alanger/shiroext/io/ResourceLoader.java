package com.github.alanger.shiroext.io;

import com.github.alanger.shiroext.util.ResourceUtils;

// org.springframework.core.io.ResourceLoader
public interface ResourceLoader {

    /** Pseudo URL prefix for loading from the class path: "classpath:". */
    String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

    Resource getResource(String location);

    ClassLoader getClassLoader();

}
