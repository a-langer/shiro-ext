package com.github.alanger.shiroext.realm;

import java.util.Arrays;
import java.util.Collection;
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

    public static boolean isBlackOrWhite(String name, String whiteList, String blackList) {
        if (whiteList != null) {
            return name.matches(whiteList);
        }
        if (blackList != null) {
            return !name.matches(blackList);
        }
        return true;
    }

    public static void filterBlackOrWhite(Collection<String> names, String whiteList, String blackList) {
        names.removeIf(g -> !isBlackOrWhite(g, whiteList, blackList));
    }

}
