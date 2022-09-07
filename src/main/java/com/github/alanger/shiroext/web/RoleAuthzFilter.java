package com.github.alanger.shiroext.web;

import org.apache.shiro.authz.annotation.Logical;

public class RoleAuthzFilter extends RolesAuthzFilter {

    public RoleAuthzFilter() {
        logicalGlobal = Logical.OR;
    }
}
