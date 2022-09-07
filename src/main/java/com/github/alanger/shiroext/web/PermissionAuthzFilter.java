package com.github.alanger.shiroext.web;

import org.apache.shiro.authz.annotation.Logical;

public class PermissionAuthzFilter extends PermissionsAuthzFilter {

    public PermissionAuthzFilter() {
        logicalGlobal = Logical.OR;
    }

}
