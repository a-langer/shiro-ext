package com.github.alanger.shiroext.authz.permission;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;

public class AntPermissionResolver extends WildcardPermissionResolver {

    @Override
    public Permission resolvePermission(String permissionString) {
        return new AntPermission(permissionString, isCaseSensitive());
    }

}