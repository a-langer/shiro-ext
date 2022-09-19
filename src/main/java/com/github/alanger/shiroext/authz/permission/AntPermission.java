package com.github.alanger.shiroext.authz.permission;

import java.util.List;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.util.AntPathMatcher;

public class AntPermission extends WildcardPermission {

    protected static final String ANT_TOKEN = "**";

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AntPermission(String wildcardString) {
        super(wildcardString, DEFAULT_CASE_SENSITIVE);
    }

    public AntPermission(String wildcardString, boolean caseSensitive) {
        super(wildcardString, caseSensitive);
    }

    @Override
    public boolean implies(Permission p) {

        // By default only supports comparisons with other WildcardPermissions
        if (!(p instanceof WildcardPermission)) {
            return false;
        }

        AntPermission wp = (AntPermission) p;

        List<Set<String>> otherParts = wp.getParts();

        boolean antMatch = false;
        boolean hasMoreParts = getParts().size() > otherParts.size();

        int i = 0;
        for (Set<String> otherPart : otherParts) {
            // If this permission has less parts than the other permission, everything after
            // the number of parts contained
            // in this permission is automatically implied, so return true
            if (getParts().size() - 1 < i) {
                return true;
            } else {
                Set<String> part = getParts().get(i);
                if (!part.contains(WILDCARD_TOKEN) && !part.containsAll(otherPart)
                        && !part.contains(ANT_TOKEN)) {
                    for (String path : otherPart) {
                        boolean pathMatch = false;
                        for (String pattern : part) {
                            pathMatch = antPathMatcher.matchStart(pattern, path);
                            if (pathMatch)
                                break;
                        }
                        if (!pathMatch)
                            return false;
                        antMatch = true;
                    }
                }
                i++;
            }
        }
        if (antMatch && !hasMoreParts)
            return true;

        // If this permission has more parts than the other parts, only imply it if all
        // of the other parts are wildcards
        for (; i < getParts().size(); i++) {
            Set<String> part = getParts().get(i);
            if (!part.contains(WILDCARD_TOKEN) && !part.contains(ANT_TOKEN)) {
                return false;
            }
        }

        return true;
    }

}