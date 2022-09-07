package com.github.alanger.shiroext.realm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.shiro.util.StringUtils;

public class RealmUtils {

    private RealmUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> asList(String data) {
        if (StringUtils.hasLength(data))
            return Arrays.asList(data.replace(" ", "").split(","));
        return Collections.emptyList();
    }

}
